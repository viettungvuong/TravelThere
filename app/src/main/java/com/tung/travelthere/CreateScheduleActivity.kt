package com.tung.travelthere

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.datepicker.MaterialDatePicker
import com.tung.travelthere.controller.colorBlue
import com.tung.travelthere.controller.dateTimePicker
import com.tung.travelthere.controller.formatter
import com.tung.travelthere.controller.tabLayout
import com.tung.travelthere.objects.Checkpoint
import com.tung.travelthere.ui.theme.TravelThereTheme
import java.util.*

class CreateScheduleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    modifier = Modifier
                        .size(32.dp)
                ) {
                    FloatingActionButton(
                        onClick = { finish() },
                        backgroundColor = Color.White,
                        content = {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = colorBlue
                            )
                        }
                    )
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

        val mDate = remember { mutableStateOf("$mDay/${mMonth+1}/$mYear") } //lưu ngày hiện tại


        var checkpointList = remember { mutableStateListOf<Checkpoint>() } //danh sách các checkpoint

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
                    Toast.makeText(applicationContext, "Please select a date later than today", Toast.LENGTH_SHORT).show()
                }
            },
            mYear,
            mMonth,
            mDay
        )

        Column() {
            dateTimePicker(
                modifier = Modifier.padding(20.dp),mDate
            ) {mDatePickerDialog.show()}

            LazyColumn(){
                itemsIndexed(checkpointList){
                    index, item -> Checkpoint(item,index,checkpointList)
                }
            }

        }

    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun Checkpoint(
        checkpoint: Checkpoint,
        index: Int,
        checkpointList: SnapshotStateList<Checkpoint>
    ) {
        var text by remember { mutableStateOf(checkpointText) }
        val keyboardController = LocalSoftwareKeyboardController.current

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
                imageVector = Icons.Default.LocationOn,
                tint = Color.Red,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(10.dp))

            BasicTextField(
                value = text,
                onValueChange = {
                    text = it
                    onTextChanged(it)
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                textStyle = MaterialTheme.typography.body1.copy(fontSize = 16.sp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Row{
                Icon(
                    imageVector = Icons.Default.Delete,
                    tint = Color.Red,
                    contentDescription = null,
                    modifier = Modifier
                        .height(40.dp)
                        .clickable {
                            onAddClick()
                            keyboardController?.hide()
                        }
                )


                if (index>=checkpointList.size-1){
                    Spacer(modifier = Modifier.width(5.dp))

                    Icon(
                        imageVector = Icons.Default.Add,
                        tint = Color(0xff146325),
                        contentDescription = null,
                        modifier = Modifier
                            .height(40.dp)
                            .clickable {
                                onAddClick()
                                keyboardController?.hide()
                            }
                    )
                }
            }



        }
    }


    @Composable
    private fun ViewSchedules() {

    }

}

