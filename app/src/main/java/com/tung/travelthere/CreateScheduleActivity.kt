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
import com.tung.travelthere.objects.Checkpoint
import com.tung.travelthere.objects.City
import com.tung.travelthere.objects.PlaceLocation
import com.tung.travelthere.objects.Position
import com.tung.travelthere.ui.theme.TravelThereTheme
import java.util.*

class CreateScheduleActivity : ComponentActivity() {
    lateinit var searchViewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchViewModel = SearchViewModel()

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
        val mYear: Int
        val mMonth: Int
        val mDay: Int

        val mCalendar = Calendar.getInstance()

        mYear = mCalendar.get(Calendar.YEAR)
        mMonth = mCalendar.get(Calendar.MONTH)
        mDay = mCalendar.get(Calendar.DAY_OF_MONTH)

        mCalendar.time = Date()

        val mDate = remember { mutableStateOf("$mDay/${mMonth + 1}/$mYear") } //lưu ngày hiện tại

        val showDialog =  remember { mutableStateOf(false) } //có hiện dialog không

        var checkpointList =
            remember { mutableStateListOf<Checkpoint?>() } //danh sách các checkpoint

        val keyboardController = LocalSoftwareKeyboardController.current

        val mDatePickerDialog = DatePickerDialog(
            LocalContext.current,
            { datePicker: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, mYear)
                selectedDate.set(Calendar.MONTH, mMonth)
                selectedDate.set(Calendar.DAY_OF_MONTH, mDayOfMonth)

                val currentDate = Calendar.getInstance()

                if (selectedDate.timeInMillis >= currentDate.timeInMillis) {
                    mDate.value = "$mDayOfMonth/${mMonth + 1}/$mYear"
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Please select a date later than today",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            mYear,
            mMonth,
            mDay
        )

        Column() {
            dateTimePicker(
                modifier = Modifier.padding(20.dp), mDate
            ) { mDatePickerDialog.show() }

            LazyColumn {
                itemsIndexed(checkpointList) { index, item ->
                    Checkpoint(index, checkpointList, showDialog)
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
                            checkpointList.add(null)
                            keyboardController?.hide()
                        })
            }

            Button(
                onClick = {
                    /*TODO*/
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
                          /*TODO*/
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

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun Checkpoint(
        index: Int, checkpointList: SnapshotStateList<Checkpoint?>,
        showDialog: MutableState<Boolean>
    ) {
        var location = remember { mutableStateOf<PlaceLocation?>(null) }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier
                .padding(2.dp)
                .fillMaxSize()){
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

                    Icon(
                        imageVector = Icons.Default.LocationOn, tint = Color.Red, contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    if (location.value!=null){
                        Text(text= location.value!!.getName())
                    }
                    else{
                        Text(text= "No location", fontStyle = FontStyle.Italic)
                    }


                    Spacer(modifier = Modifier.weight(1f))

                    Column(modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xff185241), shape = RoundedCornerShape(4.dp))
                        .clickable { showDialog.value = true },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center){
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    SearchDialog(index = index, checkpointList = checkpointList, showDialog = showDialog.value, searchViewModel = searchViewModel, setShowDialog = {showDialog.value=it}, setLocation = {
                        location.value = it
                    })


                }


            }

            Row(modifier = Modifier.padding(vertical = 5.dp)){
                var distance = 0f
                if (index<checkpointList.size-1&&checkpointList[index]!=null&&checkpointList[index+1]!=null){
                    distance = checkpointList[index]!!.distanceTo(checkpointList[index+1]!!)/1000
                }
                Log.d("distance",distance.toString())

                if (distance>0f){
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "City",
                        tint = Color.Black,
                        modifier = Modifier.scale(0.8f)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(text = "$distance km", fontSize = 15.sp)
                }
            }
        }

    }

    @Composable
    private fun SearchDialog(index: Int, checkpointList: SnapshotStateList<Checkpoint?>, showDialog: Boolean, searchViewModel: SearchViewModel, setShowDialog: (Boolean) -> Unit, setLocation: (PlaceLocation)->Unit){
        if (showDialog){
            Dialog(onDismissRequest = { setShowDialog(false) }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxSize()
                ){
                    ConstraintLayout {
                        val (searchBar, autocomplete) = createRefs()

                        //join các địa điểm của thành phố lại
                        val joinList = City.getSingleton().locationsRepository.locations + City.getSingleton().locationsRepository.recommends
                        SearchBar(available = joinList, searchViewModel = searchViewModel, modifier = Modifier.constrainAs(searchBar){
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        })

                        LazyColumn( modifier = Modifier.constrainAs(autocomplete){
                            top.linkTo(searchBar.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            height = Dimension.wrapContent
                        } ) {
                            items(searchViewModel.matchedQuery) { location ->
                                Card(
                                    modifier = Modifier
                                        .padding(
                                            5.dp
                                        )
                                        .clickable(onClick = {
                                            setLocation(location)
                                            checkpointList[index] = Checkpoint(location)
                                            setShowDialog(false)
                                            searchViewModel.matchedQuery.clear()
                                        }), elevation = 10.dp
                                ) {
                                    Row {

                                        Box(modifier = Modifier.padding(horizontal = 15.dp)){
                                            Text(
                                                text = location.getName(), fontSize = 15.sp, fontWeight = FontWeight.Bold
                                            )
                                        }


                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        ) {


                                            var distance = 0f
                                            if (index>0&&checkpointList[index-1]!=null){
                                                distance = checkpointList[index-1]!!.getLocation().distanceTo(location)/1000

                                            }
                                            if (distance>0f){
                                                Icon(
                                                    imageVector = Icons.Default.Place,
                                                    contentDescription = "City",
                                                    tint = Color.Black,
                                                    modifier = Modifier.scale(0.8f)
                                                )

                                                Spacer(modifier = Modifier.width(5.dp))

                                                Text(text = "$distance km")
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
    

    @Composable
    private fun ViewSchedules() {

    }

}



