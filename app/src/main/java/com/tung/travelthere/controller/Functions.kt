package com.tung.travelthere.controller

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.location.Geocoder
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.libraries.places.api.Places
import com.tung.travelthere.PlaceAutocompleteViewModel
import com.tung.travelthere.R
import com.tung.travelthere.objects.City
import com.tung.travelthere.objects.PlaceLocation
import com.tung.travelthere.objects.Position
import java.util.*

fun getDrawableNameFromName(resourceName: String): Int {
    try {
        val field = R.drawable::class.java.getField(resourceName)
        return field.getInt(null)
    } catch (e: Exception) {
        return 0
    }
}

fun colorFromImage(bitmap: Bitmap): Color {
    val palette = Palette.Builder(bitmap!!).generate()
    val colorExtracted = palette.dominantSwatch?.let {
        Color(it.rgb)
    } ?: Color.Transparent
    return colorExtracted
}

@SuppressLint("MissingPermission")
fun getCurrentPosition(fusedLocationClient: FusedLocationProviderClient, context: Context, callback: () -> Unit) {
    AppController.currentPosition= AppController.UserPlace()

    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            AppController.currentPosition.currentLocation = Position(location.latitude,location.longitude)

            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses!=null&&addresses.isNotEmpty()) {
                val cityName = if (addresses[0].locality==null){
                    addresses[0].adminArea //lấy tên thành phố theo tên tỉnh
                } else{
                    addresses[0].locality
                }
                Log.d("addresses[0]",addresses[0].toString())
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

fun initialize(context: Context, callback: ()->Unit){


}
