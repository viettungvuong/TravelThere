package com.tung.travelthere

import android.app.DatePickerDialog
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.material.datepicker.MaterialDatePicker
import com.tung.travelthere.controller.*
import com.tung.travelthere.objects.*
import com.tung.travelthere.ui.theme.TravelThereTheme
import java.util.*

const val checkpointField = "checkpoints"

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
                    FloatingActionButton(onClick = { finish() },
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
            index: Int,
            schedule: Schedule,
            showDialog: Boolean,
            searchViewModel: SearchViewModel,
            setShowDialog: (Boolean) -> Unit,
            setLocation: (PlaceLocation) -> Unit
        ) {
            var distance by remember { mutableStateOf(0f) }
            var indexState = remember { mutableStateOf(index) }
            var chosenState = remember { mutableStateOf(mutableSetOf<Category>()) }

            fun clear() {
                distance = 0f
                indexState.value = 0
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
                                                setLocation(location)

                                                //đặt ở index checkpoint
                                                val checkpoint = Checkpoint(location)
                                                schedule.setCheckpoint(
                                                    checkpoint = checkpoint,
                                                    index = indexState.value
                                                )

                                                clear()
                                                setShowDialog(false)

                                            }), elevation = 10.dp
                                    ) {
                                        Row {
                                            //hiện icon tương ứng category đầu tiên của địa điểm
                                            Icon(
                                                painter = when (location.categories.first()) {
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

                                            Box(modifier = Modifier.padding(horizontal = 15.dp)) {
                                                Text(
                                                    text = location.getName(), //hiện tên của địa điểm
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }


                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                //tính toán khoảng cách từ điểm này đến checkpoint trước
                                                distance = 0f
                                                if (index > 0 && schedule.getList()[index - 1] != null) {
                                                    distance =
                                                        schedule.getList()[index - 1]!!.getLocation()
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
            var location = remember { mutableStateOf<PlaceLocation?>(null) }


            Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                if (location.value!=null){
                    val intent = Intent(this, PlaceView::class.java)
                    intent.putExtra("location", location.value)
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

                        if (location.value==null){
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                tint = Color.Red,
                                contentDescription = null
                            )
                        }
                        else{
                            Icon(
                                painter = when (location.value!!.categories.first()) {
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

                        if (location.value != null) {
                            Text(text = location.value!!.getName())
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
                                    .clickable { showDialog.value = true },
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


                        SearchDialog(
                            index = index,
                            schedule = schedule,
                            showDialog = showDialog.value,
                            searchViewModel = searchViewModel,
                            setShowDialog = { showDialog.value = it },
                            setLocation = {
                                location.value = it
                            })

                    }


                }

                Row(modifier = Modifier.padding(vertical = 5.dp)) {
                    var distance = schedule.distances[index]

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
        }

        @Composable
        fun categoryCount(schedule: Schedule){

            Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center){
                for (category in schedule.countMap.keys){
                    if (schedule.countMap[category]!=0){
                        Row(verticalAlignment = Alignment.CenterVertically){
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

        val showDialog = remember { mutableStateOf(false) } //có hiện dialog không

        var schedule by remember { mutableStateOf(Schedule()) }
        var lazyListState = rememberLazyListState()

        fun updateSchedule(){ //update lên firebase
            val checkpointStr = mutableListOf<String>()
            for (checkpoint in schedule.getList()){
                checkpointStr.add(checkpoint.toString())
            }

            val scheduleData = hashMapOf(
                checkpointField to checkpointStr.toList()
            )

            AppController.db.collection("users").document(AppController.auth.currentUser!!.uid)
                .collection("schedules").add(scheduleData) //update lên firebase
                .addOnSuccessListener {
                    AppController.schedules.add(Schedule(schedule)) //thêm vào danh sách các schedule
                    schedule.clear()
                    Toast.makeText(this,"Updated schedule successfully",Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{
                    Toast.makeText(this,"Cannot update schedule",Toast.LENGTH_SHORT).show()
                }
        }


        val keyboardController = LocalSoftwareKeyboardController.current

        Column() {
            //hiện từng checkpoint
            LazyColumn(state = lazyListState) {
                itemsIndexed(schedule.getList()) { index, item ->
                    Checkpoint(index, schedule, showDialog)
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
                            schedule.addNullCheckpoint()
                            keyboardController?.hide()
                        })
            }
            
            categoryCount(schedule = schedule)

            Button(
                onClick = {
                    schedule.clear() //xoá hết lịch trình
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
        }

    }






    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun ViewSchedules() {
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
                LazyRow {
                    itemsIndexed(schedule.getList()){
                            index, checkpoint ->
                        if (checkpoint!=null){
                            Row(){
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "City",
                                    tint = Color.Red,
                                    modifier = Modifier.scale(0.8f)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = "${checkpoint.getLocation().getName()}"
                                )

                                if (index!=schedule.getList().lastIndex){
                                    Text(
                                        text = "→",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color.Red
                                    )
                                }
                            }
                        }

                    }
                }
            }
        }
        
        FlowColumn() {
            for (schedule in AppController.schedules){
                scheduleView(schedule = schedule)
            }
        }


    }

}

fun fetchSchedules(){
    AppController.db.collection("users").document(AppController.auth.currentUser!!.uid)
        .collection("schedules").get().addOnSuccessListener {
            documents ->
            for (document in documents){
                val posStrArr = document.get(checkpointField) as List<String>

                val currentSchedule = Schedule()
                for (str in posStrArr){
                    val getPlaceLocation = City.getSingleton().locationsRepository.locations[str]

                    if (getPlaceLocation!=null){
                        currentSchedule.getList().add(Checkpoint(getPlaceLocation))
                    }

                }
                AppController.schedules.add(currentSchedule) //thêm vào danh sách schedule
            }
        }
}

