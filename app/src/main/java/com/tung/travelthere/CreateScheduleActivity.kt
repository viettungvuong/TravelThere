package com.tung.travelthere

import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.*
import com.tung.travelthere.controller.*
import com.tung.travelthere.objects.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

const val checkpointField = "checkpoints"
const val dateField = "date"

class CreateScheduleActivity : ComponentActivity() {
    lateinit var searchViewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchViewModel = SearchViewModel()

        fetchSchedules() //lấy danh sách schedule

        setContent {
            CreateScheduleView()
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun CreateScheduleView() {
        val tabTitles = listOf("Create a schedule", "View schedules")
        val pagerState = rememberPagerState(initialPage = 0)
        val coroutineScope = rememberCoroutineScope()

        MaterialTheme() {
            Column {
                Box(
                    modifier = Modifier.size(32.dp)
                ) {
                    FloatingActionButton(onClick = {
                        val intent = Intent(this@CreateScheduleActivity, MainActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        this@CreateScheduleActivity.startActivity(intent)},
                        backgroundColor = Color.White,
                        content = {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = colorBlue
                            )
                        })
                }

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 25.dp),
                    text = "Schedules",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                tabLayout(
                    pagerState = pagerState,
                    tabTitles = tabTitles,
                    coroutineScope = coroutineScope,
                    contentColor = Color.Magenta
                )

                HorizontalPager(state = pagerState, pageCount = tabTitles.size) { page ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .weight(1f)
                    ) {
                        when (page) {
                            0 -> CreateSchedule()
                            1 -> ViewSchedules()
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun CreateSchedule() {
        @Composable
        fun SearchDialog(
            schedule: Schedule,
            showDialog: Boolean,
            searchViewModel: SearchViewModel,
            setShowDialog: (Boolean) -> Unit,
        ) {
            var distance by remember { mutableStateOf(0f) }
            var chosenState = remember { mutableStateOf(mutableSetOf<Category>()) }

            fun clear() {
                distance = 0f
                searchViewModel.matchedQuery.clear()
            }

            if (showDialog) {
                Dialog(onDismissRequest = {
                    clear()
                    setShowDialog(false)
                }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ConstraintLayout {
                            val (categories, searchBar, autocomplete) = createRefs()

                            //join các địa điểm của thành phố lại
                            val joinList =
                                City.getSingleton().locationsRepository.locations.map { it.value } + City.getSingleton().locationsRepository.recommends
                            SearchBar(
                                available = joinList.toSet(),
                                searchViewModel = searchViewModel,
                                modifier = Modifier.constrainAs(searchBar) {
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                })

                            LazyRow(modifier = Modifier
                                .padding(15.dp)
                                .constrainAs(categories) {
                                    top.linkTo(searchBar.bottom)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }) {
                                itemsIndexed(Category.values()) { index, category ->
                                    categoryView(category, colorBlue, true, searchViewModel, chosenState)
                                }
                            } //filter category

                            //autocomplete result
                            LazyColumn(modifier = Modifier.constrainAs(autocomplete) {
                                top.linkTo(categories.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                height = Dimension.wrapContent
                            }) {
                                items(searchViewModel.matchedQuery) { location ->
                                    Card(
                                        modifier = Modifier
                                            .padding(
                                                5.dp
                                            )
                                            .clickable(onClick = {
                                                val checkpoint = Checkpoint(location)

                                                if (schedule
                                                        .getList()
                                                        .isNotEmpty()
                                                ) {
                                                    val prevCheckpoint = schedule
                                                        .getList()
                                                        .last()

                                                    //thêm distance
                                                    schedule.distances.add(
                                                        checkpoint.distanceTo(
                                                            prevCheckpoint
                                                        ) / 1000
                                                    )
                                                }
                                                schedule
                                                    .getList()
                                                    .add(checkpoint)

                                                //thêm category
                                                for (category in location.getCategoriesList()) {
                                                    schedule.countMap[category] =
                                                        (schedule.countMap[category] ?: 0) + 1
                                                }

                                                clear()
                                                setShowDialog(false) //đóng hộp thoại lại

                                            }), elevation = 10.dp
                                    ) {
                                        Row {
                                            //hiện icon tương ứng category đầu tiên của địa điểm
                                            Icon(
                                                painter = when (location.getRepCategory()) {
                                                    Category.RESTAURANT -> painterResource(R.drawable.restaurant)
                                                    Category.BAR -> painterResource(R.drawable.bar)
                                                    Category.ATTRACTION -> painterResource(R.drawable.attraction)
                                                    Category.NATURE -> painterResource(R.drawable.nature)
                                                    Category.NECESSITY -> painterResource(R.drawable.hospital)
                                                    Category.OTHERS -> painterResource(R.drawable.other)
                                                    Category.SHOPPING -> painterResource(R.drawable.shopping)
                                                },
                                                tint = colorBlue,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .padding(vertical = 2.dp, horizontal = 2.dp)
                                            )

                                            //tên địa điểm
                                            Column() {
                                                Box(modifier = Modifier.padding(horizontal = 15.dp, vertical = 2.dp)) {
                                                    Text(
                                                        text = location.getName(), //hiện tên của địa điểm
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                //hiện khoảng cách đến chỗ hiện tại
                                                Row(
                                                    modifier = Modifier.padding(10.dp)
                                                ) {
                                                    //tính toán khoảng cách từ điểm này đến checkpoint trước
                                                    distance = 0f
                                                    if (schedule.getList().size > 0 && schedule.getList().last() != null) {
                                                        distance =
                                                            schedule.getList().last().getLocation()
                                                                .distanceTo(location) / 1000
                                                    }

                                                    //nếu có distance
                                                    if (distance > 0f) {
                                                        Icon(
                                                            imageVector = Icons.Default.Place,
                                                            contentDescription = "City",
                                                            tint = Color.Black,
                                                            modifier = Modifier.scale(0.8f)
                                                        )

                                                        Spacer(modifier = Modifier.width(5.dp))

                                                        Text(
                                                            text = "${
                                                                roundDecimal(
                                                                    distance.toDouble(),
                                                                    2
                                                                )
                                                            } km"
                                                        )
                                                    }
                                                }
                                            }


                                            Spacer(modifier = Modifier.weight(0.5f))

                                            Box(modifier = Modifier
                                                .padding(10.dp)
                                                .background(
                                                    color = colorBlue
                                                )
                                                .clickable {
                                                    val intent = Intent(
                                                        this@CreateScheduleActivity,
                                                        PlaceView::class.java
                                                    )
                                                    intent.putExtra("location", location)
                                                    intent.putExtra("image url", location?.imageUrl)
                                                    startActivity(intent)
                                                }){
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        imageVector = Icons.Default.Search,
                                                        contentDescription = null,
                                                        tint = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        @OptIn(ExperimentalComposeUiApi::class)
        @Composable
        fun Checkpoint(
            index: Int, schedule: Schedule,
            showDialog: MutableState<Boolean>
        ) {
            var location by remember { mutableStateOf<PlaceLocation?>(null) }
            var timesInAWeek = remember { mutableStateOf(0) }

            LaunchedEffect(schedule.getList()[index]?.placeLocationState){
                location = schedule.getList()[index]?.getLocation()
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                if (location!=null){
                    val intent = Intent(this, PlaceView::class.java)
                    intent.putExtra("location", location)
                    intent.putExtra("image url", location?.imageUrl)
                    startActivity(intent)
                }

            }) {
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(1.dp, 40.dp)
                                .background(Color.Gray)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        if (location==null){
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                tint = Color.Red,
                                contentDescription = null
                            )
                        }
                        else{
                            Icon(
                                painter = when (location!!.getRepCategory()) {
                                    Category.RESTAURANT -> painterResource(R.drawable.restaurant)
                                    Category.BAR -> painterResource(R.drawable.bar)
                                    Category.ATTRACTION -> painterResource(R.drawable.attraction)
                                    Category.NATURE -> painterResource(R.drawable.nature)
                                    Category.NECESSITY -> painterResource(R.drawable.hospital)
                                    Category.OTHERS -> painterResource(R.drawable.other)
                                    Category.SHOPPING -> painterResource(R.drawable.shopping)
                                },
                                tint = Color.Red,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }


                        Spacer(modifier = Modifier.width(10.dp))

                        if (location != null) {
                            Text(text = location!!.getName())
                        } else {
                            Text(text = "No location", fontStyle = FontStyle.Italic)
                        }


                        Spacer(modifier = Modifier.weight(1f))

                        //nút tìm kiếm
                        if (index == schedule.getList().lastIndex){
                            Column(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xff185241), shape = RoundedCornerShape(4.dp))
                                    .clickable { showDialog.value = true }, //mở hộp thoại tìm kiếm
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }


                }

                if (index<schedule.distances.size){
                    Row(modifier = Modifier.padding(vertical = 5.dp)) {
                        var distance = schedule.distances[index]
                        //khoảng cách

                        if (distance > 0f) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "City",
                                tint = Color.Black,
                                modifier = Modifier.scale(0.8f)
                            )

                            Spacer(modifier = Modifier.width(5.dp))

                            //phần distance
                            Text(text = "${roundDecimal(distance.toDouble(), 2)} km", fontSize = 15.sp)
                        }
                    }
                }


                if (timesInAWeek.value > 0) {
                    val text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("${timesInAWeek.value}")
                        }
                        append(" times within a week")
                    }

                    Text(
                        text = text,
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }


        }

        @Composable
        fun categoryCount(schedule: Schedule){
            Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center){
                for (category in schedule.countMap.keys){
                    if (schedule.countMap[category]!=0){
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 5.dp)){
                            Icon(
                                painter = when (category) {
                                    Category.RESTAURANT -> painterResource(R.drawable.restaurant)
                                    Category.BAR -> painterResource(R.drawable.bar)
                                    Category.ATTRACTION -> painterResource(R.drawable.attraction)
                                    Category.NATURE -> painterResource(R.drawable.nature)
                                    Category.NECESSITY -> painterResource(R.drawable.hospital)
                                    Category.OTHERS -> painterResource(R.drawable.other)
                                    Category.SHOPPING -> painterResource(R.drawable.shopping)
                                },
                                tint = colorBlue,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(5.dp))

                            Text(text = schedule.countMap[category].toString(), fontSize = 15.sp)
                        }
                    }

                }
            }
        }

        fun updateSchedule(){ //update lên firebase
            AppController.schedules.add(Schedule(AppController.currentSchedule.value))
            //dùng copy constructor để tách biệt ra không reference

            val checkpointStr = mutableListOf<String>()
            for (checkpoint in AppController.currentSchedule.value.getList()){
                checkpointStr.add(checkpoint.toString())
            }

            val scheduleData = hashMapOf(
                checkpointField to checkpointStr.toList(),
                dateField to formatterDateOnly.format(AppController.currentSchedule.value.date)
            )

            Firebase.firestore.collection("users").document(AppController.auth.currentUser!!.uid)
                .collection("schedules").add(scheduleData) //update lên firebase
                .addOnSuccessListener {
                    AppController.currentSchedule.value.clear()
                    Toast.makeText(this,"Updated schedule successfully",Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{
                    Toast.makeText(this,"Cannot update schedule",Toast.LENGTH_SHORT).show()
                }
        }

        val showDialog = remember { mutableStateOf(false) } //có hiện dialog không
        val keyboardController = LocalSoftwareKeyboardController.current
        var optimalSchedule = remember { mutableStateOf<Pair<Float,Schedule>?>(null) }

        val startPos = if (AppController.currentPosition!!.currentLocation!=null){
            AppController.currentPosition!!.currentLocation!!.convertToLatLng()
        }
        else{
            LatLng(10.7628409,106.6799075)
        } //nơi xuất phát nếu không dùng Location services

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(startPos, 8f)
        }

        LaunchedEffect(AppController.currentSchedule.value.getList().size){
            if (optimalSchedule.value!=null){
                optimalSchedule.value!!.second.clear()
            }

            optimalSchedule.value = shortestPathAlgo(AppController.currentSchedule.value)
        }

        Column() {
            val checkpoints = AppController.currentSchedule.value.getList()
            if (checkpoints.isNotEmpty()&&checkpoints.first()!=null){
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    cameraPositionState = cameraPositionState
                ) {
                    val checkpoints = AppController.currentSchedule.value.getList()
                    for (i in checkpoints.indices){
                        if (checkpoints[i]!=null){
                            cameraPositionState.position = CameraPosition
                                .fromLatLngZoom(checkpoints[i]!!.getLocation().getPos().convertToLatLng(), 15f)

                            Marker(
                                state = MarkerState(position = checkpoints[i]!!.getLocation().getPos().convertToLatLng()),
                                title = "Checkpoint $i",
                                snippet = "Checkpoint $i",
                            )
                        }
                    }
                }
            }

            if (optimalSchedule.value!=null){
                Box(modifier = Modifier
                    .padding(vertical = 5.dp)){
                    Column {
                        Text("Optimal schedule", fontWeight = FontWeight.Bold)

                        Box(modifier = Modifier.padding(vertical = 2.dp)){
                            Text("Distance ${roundDecimal((optimalSchedule.value!!.first/1000).toDouble(),2)} km")
                        }

                        scheduleViewHorizontal(schedule = optimalSchedule.value!!.second, onClick = {
                            val builder: AlertDialog.Builder = AlertDialog.Builder(this@CreateScheduleActivity)
                            builder.setTitle("Do you want to save the optimal schedule")

                            builder.setPositiveButton("Yes",
                                DialogInterface.OnClickListener { dialog, which ->
                                    AppController.currentSchedule.value = optimalSchedule.value!!.second
                                    updateSchedule()
                                    AppController.currentSchedule.value.clear()
                                })

                            builder.setNegativeButton("Cancel",
                                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

                            builder.show()
                        })
                    }
                }
            }


            //để căn giữa nút cộng
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.Add,
                    tint = Color(0xff468a55),
                    contentDescription = null,
                    modifier = Modifier
                        .height(40.dp)
                        .clickable {
                            showDialog.value = true
                            keyboardController?.hide()
                        })
            }

            SearchDialog(
                schedule = AppController.currentSchedule.value,
                showDialog = showDialog.value,
                searchViewModel = searchViewModel,
                setShowDialog = { showDialog.value = it })

            categoryCount(schedule = AppController.currentSchedule.value)

            Button(
                onClick = {
                    AppController.currentSchedule.value.clear() //xoá hết lịch trình
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorFirst),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Row(
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Text(text = "Clear", color = Color.White)
                }
            }

            Button(
                onClick = {
                    updateSchedule()

                    AppController.currentSchedule.value.clear()
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorThird),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Row(
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Text(text = "Save", color = Color.White)
                }
            }

            //hiện từng checkpoint
            LazyColumn() {
                itemsIndexed(AppController.currentSchedule.value.getList()) { index, item ->
                    Checkpoint(index, AppController.currentSchedule.value, showDialog)
                }
            }
        }

    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun ViewSchedules() {
        //adapter schedule

        LazyColumn() {
            items(AppController.schedules){
                schedule ->  scheduleView(schedule = schedule)
            }
        }


    }

}

fun fetchSchedules(){
    fun diffDate(date1: Date, date2: Date): Long{
        val diffInMillies = abs(date1.time - date2.getTime())
        val diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)
        return diff
    }

    AppController.schedules.clear()

    Firebase.firestore.collection("users").document(AppController.auth.currentUser!!.uid)
        .collection("schedules").get().addOnSuccessListener {
            documents ->
            for (document in documents){
                val dateStr = document.getString(dateField)
                val date = formatterDateOnly.parse(dateStr)

                val posStrArr = document.get(checkpointField) as List<String>

                val currentSchedule = Schedule(date)

                for (str in posStrArr){
                    val getPlaceLocation = City.getSingleton().locationsRepository.locations[str]
                    //lấy từng địa điểm trong schedule

                    if (getPlaceLocation!=null){
                        currentSchedule.getList().add(Checkpoint(getPlaceLocation))

                        //đếm số lần đến địa điểm này trong 1 tuần
                        if (diffDate(date,Date())<=7L){
                            val inc = (AppController.countVisit[getPlaceLocation]?:0)+1
                            AppController.countVisit[getPlaceLocation!!] = inc
                        }
                    }
                }

                AppController.schedules.add(currentSchedule)
            }

            AppController.schedules.sortBy { it.date }
        }


}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun scheduleView(schedule: Schedule){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 10.dp,
                horizontal = 10.dp
            )
            .clickable(onClick = {
            }), elevation = 10.dp
    ) {
        Column {
            Text(modifier = Modifier.padding(5.dp), text = formatterDateOnly.format(schedule.date), fontWeight = FontWeight.Bold)
            FlowColumn{
                for (i in schedule.getList().indices){
                    val checkpoint = schedule.getList()[i]
                    if (checkpoint!=null){
                        Row(){
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.scale(0.8f)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "${checkpoint.getLocation().getName()}"
                            )
                        }
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun scheduleViewHorizontal(schedule: Schedule, onClick:()->Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 10.dp,
                horizontal = 10.dp
            )
            .clickable(onClick = {
                onClick()
            }), elevation = 10.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp)) {
            Text(modifier = Modifier.padding(5.dp), text = formatterDateOnly.format(schedule.date), fontWeight = FontWeight.Bold)
            LazyRow{
                items(schedule.getList()){
                    val checkpoint = it
                    if (checkpoint!=null){
                        Row(modifier = Modifier.padding(horizontal = 5.dp)){
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.scale(0.8f)
                            )

                            Spacer(modifier = Modifier.width(2.dp))

                            Text(
                                text = "${checkpoint.getLocation().getName()}"
                            )
                        }
                    }
                }
            }
        }

    }
}
