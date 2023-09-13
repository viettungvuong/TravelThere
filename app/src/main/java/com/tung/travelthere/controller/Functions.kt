package com.tung.travelthere.controller

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.location.LocationRequest
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat.startActivity
import androidx.palette.graphics.Palette
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
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

    fusedLocationClient.getCurrentLocation(
        LocationRequest.QUALITY_BALANCED_POWER_ACCURACY,
        object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                CancellationTokenSource().token

            override fun isCancellationRequested() = false
        })
        .addOnSuccessListener { location: Location? ->
            if (location == null) {
                Toast.makeText(context, "Cannot get location.", Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(context, ChooseCity::class.java)
                context.startActivity(intent)
            } else {
                AppController.currentPosition.currentLocation =
                    Position(location.latitude, location.longitude)

                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses =
                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    )
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


fun roundDecimal(value: Double, places: Int): Double {
    return (value * 100.0).roundToInt() / 10.0.pow(places.toDouble())
}

fun restartApp(activity: Activity) {
    val intent = Intent(
        activity,
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
    var res: Pair<Checkpoint, Checkpoint>? = null

    var checkpoints = mutableListOf<Checkpoint>()
    for (checkpoint in schedule.getList()) {
        if (checkpoint != null) {
            checkpoints.add(checkpoint)
        }
    }

    if (checkpoints.isEmpty() || checkpoints.size < 2) {
        return null
    }

    var maxDistance = Float.NEGATIVE_INFINITY

    for (i in checkpoints.indices) {
        val checkpoint = checkpoints[i]
        if (checkpoint != null) {
            for (j in checkpoints.indices) {
                if (i == j)
                    continue
                val checkpoint2 = checkpoints[j]
                if (checkpoint.distanceTo(checkpoint2) > maxDistance) {
                    maxDistance = checkpoint.distanceTo(checkpoint2)
                    res = Pair(Checkpoint(checkpoint), Checkpoint(checkpoint2))
                }
            }
        }
    }

    return res

}

fun shortestPathAlgo(schedule: Schedule): Pair<Float, Schedule>? {
    //tìm đường đi tối ưu
    val extremePoints = findExtremePoints(schedule = schedule) ?: return null

    var current = Checkpoint(extremePoints.first)
    var totalDistance = 0f
    var travel = LinkedList<Checkpoint>() //lưu lại đường đi


    var checkpoints = mutableListOf<Checkpoint>()
    for (checkpoint in schedule.getList()) {
        if (checkpoint != null) {
            checkpoints.add(checkpoint)
        }
    }

    var j = 0
    var n = 0

    var minDist = Float.POSITIVE_INFINITY
    var nextCheckpoint: Checkpoint? = null

    val visited = mutableMapOf<Checkpoint, Boolean>() //đã đi hay chưa
    visited[current] = true
    travel.add(current)

    var size = checkpoints.size

    //greedy algorithm
    while (n < size - 1 && j < size) {
        if (j in 0 until size) {
            if (checkpoints[j] != current && ((visited[checkpoints[j]] == null) || visited[checkpoints[j]] == false)) {
                val dist = current.distanceTo(checkpoints[j])
                if (dist < minDist) {
                    minDist = dist
                    nextCheckpoint = checkpoints[j]
                }
            }
        }

        j++

        if (j >= checkpoints.size) {
            j = 0

            if (nextCheckpoint != null) {
                current = Checkpoint(nextCheckpoint)
                visited[current] = true
                travel.add(Checkpoint(current))
                totalDistance += minDist
                minDist = Float.POSITIVE_INFINITY
                n++
                nextCheckpoint = null
            } else {
                break
            }
        }
    }

    val scheduleRes = Schedule()
    scheduleRes.getList().clear()
    scheduleRes.getList().addAll(travel)


    return Pair(totalDistance, scheduleRes)
}