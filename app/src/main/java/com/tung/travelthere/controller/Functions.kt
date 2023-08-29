package com.tung.travelthere.controller

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.location.Geocoder
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import com.google.android.gms.location.FusedLocationProviderClient
import com.tung.travelthere.R
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
fun getCurrentPosition(fusedLocationClient: FusedLocationProviderClient, context: Context) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            AppController.currentPosition.currentLocation = Position(location.latitude,location.longitude)

            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses!=null&&addresses.isNotEmpty()) {
                AppController.currentPosition.cityName = addresses[0].locality
            }
        }
    }
}

fun initialize(callback: ()->Unit){
}
