package com.tung.travelthere.controller

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Geocoder
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.tung.travelthere.RegisterLoginActivity
import com.tung.travelthere.objects.City
import com.tung.travelthere.objects.Position
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt


fun colorFromImage(bitmap: Bitmap): Color {
    val palette = Palette.Builder(bitmap!!).generate()
    val colorExtracted = palette.dominantSwatch?.let {
        Color(it.rgb)
    } ?: Color.Transparent
    return colorExtracted
}

//lấy vị trí hiện tại
@SuppressLint("MissingPermission")
fun getCurrentPosition(fusedLocationClient: FusedLocationProviderClient, context: Context, callback: () -> Unit) {
    AppController.currentPosition= AppController.UserPlace()

    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            AppController.currentPosition.currentLocation = Position(location.latitude,location.longitude)

            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses!=null&&addresses.isNotEmpty()) {
                var cityName = if (addresses[0].locality==null){
                    addresses[0].adminArea //lấy tên thành phố theo tên tỉnh
                } else{
                    addresses[0].locality
                }
                if (cityName == "Thành phố Hồ Chí Minh")
                    cityName="Ho Chi Minh City"
                val countryName = addresses[0].countryName

                AppController.currentPosition.cityName = cityName
                AppController.currentPosition.countryName = countryName

                City.getSingleton().setName(cityName) //đặt tên cho thành phố hiện tại
                City.getSingleton().setCountry(countryName)

                callback()
            }
        }
    }
}


fun roundDecimal(value: Double, places: Int): Double{
    return (value * 100.0).roundToInt() / 10.0.pow(places.toDouble())
}

fun restartApp(activity: Activity){
    val intent = Intent(
        getApplicationContext(),
        RegisterLoginActivity::class.java
    )
    activity.startActivity(intent)
    activity.finish()
}
