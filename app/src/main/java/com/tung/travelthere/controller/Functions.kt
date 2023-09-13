package com.tung.travelthere.controller

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat.startActivity
import androidx.palette.graphics.Palette
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.tung.travelthere.ChooseCity
import com.tung.travelthere.RegisterLoginActivity
import com.tung.travelthere.objects.Checkpoint
import com.tung.travelthere.objects.City
import com.tung.travelthere.objects.Position
import com.tung.travelthere.objects.Schedule
import kotlinx.coroutines.withTimeoutOrNull
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlinx.coroutines.*
import java.lang.Float.POSITIVE_INFINITY
import java.lang.Math.*
import java.text.SimpleDateFormat
import kotlin.system.measureTimeMillis


fun colorFromImage(bitmap: Bitmap): Color {
    val palette = Palette.Builder(bitmap!!).generate()
    val colorExtracted = palette.dominantSwatch?.let {
        Color(it.rgb)
    } ?: Color.Transparent
    return colorExtracted
}

//lấy vị trí hiện tại
@SuppressLint("MissingPermission")
fun getCurrentPosition(
    fusedLocationClient: FusedLocationProviderClient,
    context: Context,
    callback: () -> Unit
) {
    AppController.currentPosition = AppController.UserPlace() //initialize

    runBlocking {
        val job = launch {
            val result = withTimeoutOrNull(5000) {
                //chạy quá 20s thì thôi bỏ
                val executionTime = measureTimeMillis {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            AppController.currentPosition.currentLocation =
                                Position(location.latitude, location.longitude)

                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses =
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (addresses != null && addresses.isNotEmpty()) {
                                var cityName = if (addresses[0].locality == null) {
                                    addresses[0].adminArea //lấy tên thành phố theo tên tỉnh
                                } else {
                                    addresses[0].locality
                                }
                                if (cityName == "Thành phố Hồ Chí Minh")
                                    cityName = "Ho Chi Minh City"
                                val countryName = addresses[0].countryName

                                AppController.currentPosition.cityName = cityName
                                AppController.currentPosition.countryName = countryName

                                City.getSingleton()
                                    .setName(cityName) //đặt tên cho thành phố hiện tại
                                City.getSingleton().setCountry(countryName)

                                callback()
                            }
                        }
                    }
                }
                "Get location in $executionTime ms"
            }

            if (result == null) { //chạy quá 20s rồi
                Toast.makeText(
                    context,
                    "Location Service takes too long to get location",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(context, ChooseCity::class.java)
                context.startActivity(intent)
                //cho người dùng tự chọn thành phố
            }
        }

        job.join() //load địa điểm
    }


}


fun roundDecimal(value: Double, places: Int): Double {
    return (value * 100.0).roundToInt() / 10.0.pow(places.toDouble())
}

fun restartApp(activity: Activity) {
    val intent = Intent(
        getApplicationContext(),
        RegisterLoginActivity::class.java
    )
    activity.startActivity(intent)
    activity.finish()
}

fun dateAfterDays(date: Date, days: Int): String {
    val calendar = Calendar.getInstance()

    calendar.time = date

    calendar.add(Calendar.DAY_OF_MONTH, days)

    return formatterDateOnlyNoYear
        .format(calendar.time)
}

//tìm khoảng cách xa nhất theo latitude và longitude
private fun findExtremePoints(schedule: Schedule): Pair<Checkpoint, Checkpoint>? {
    var res: Pair<Checkpoint, Checkpoint>?=null

    var checkpoints = mutableListOf<Checkpoint>()
    for (checkpoint in schedule.getList()){
        if (checkpoint!=null){
            checkpoints.add(checkpoint)
        }
    }

    if (checkpoints.isEmpty() || checkpoints.size < 2) {
        return null
    }

    var maxDistance = Float.NEGATIVE_INFINITY

    for (i in checkpoints.indices) {
        val checkpoint = schedule.getList()[i]
        if (checkpoint != null) {
            for (j in schedule.getList().indices){
                if (i==j)
                    continue
                val checkpoint2 = schedule.getList()[j]
                if (checkpoint2!=null&&checkpoint.distanceTo(checkpoint2)>maxDistance){
                    maxDistance=checkpoint.distanceTo(checkpoint2)
                    res=Pair(Checkpoint(checkpoint),Checkpoint(checkpoint2))
                }
            }
        }
    }

    return res

}

fun shortestPathAlgo(schedule: Schedule): Pair<Float,Schedule>? {
    //tìm đường đi tối ưu
    val extremePoints = findExtremePoints(schedule = schedule) ?: return null

    var current = Checkpoint(extremePoints.first)
    Log.d("current",current.getLocation().getName())
    var totalDistance = 0f
    var travel = LinkedList<Checkpoint>() //lưu lại đường đi


    var checkpoints = mutableListOf<Checkpoint>()
    for (checkpoint in schedule.getList()){
        if (checkpoint!=null){
            checkpoints.add(checkpoint)
        }
    }

    var j = 0
    var n = 0

    var minDist = Float.POSITIVE_INFINITY
    var nextCheckpoint: Checkpoint?=null

    val visited = mutableMapOf<Checkpoint,Boolean>() //đã đi hay chưa
    visited[current]=true
    travel.add(current)

    //a* algorithm visit all nodes
    while (n<checkpoints.size-1&&j<checkpoints.size){
        if (j>0&&j<checkpoints.size){
            if (checkpoints[j]!=current&&((visited[checkpoints[j]]==null)||visited[checkpoints[j]]==false)){
                val dist = current.distanceTo(checkpoints[j])
                if (dist<minDist){
                    minDist = dist
                    nextCheckpoint = Checkpoint(checkpoints[j])
                }
            }
        }

        j++

        if (j>=checkpoints.size){
            j = 0


            if (nextCheckpoint!=null) {
                Log.d("next checkpoint",nextCheckpoint.getLocation().getName())
                current = Checkpoint(nextCheckpoint)
                visited[current] = true
                travel.add(Checkpoint(current))
                totalDistance+=minDist
                minDist = Float.POSITIVE_INFINITY
            }
            nextCheckpoint = null

            n++
        }
    }

    val scheduleRes = Schedule()
    scheduleRes.getList().clear()
    scheduleRes.getList().addAll(travel)


    return Pair(totalDistance,scheduleRes)
}