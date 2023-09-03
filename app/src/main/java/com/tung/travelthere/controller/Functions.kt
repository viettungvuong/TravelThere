package com.tung.travelthere.controller

import android.annotation.SuppressLint
import android.content.Context
import android.content.SearchRecentSuggestionsProvider
import android.graphics.Bitmap
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.palette.graphics.Palette
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.libraries.places.api.Places
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tung.travelthere.PlaceAutocompleteViewModel
import com.tung.travelthere.R
import com.tung.travelthere.objects.City
import com.tung.travelthere.objects.PlaceLocation
import com.tung.travelthere.objects.Position
import kotlinx.coroutines.runBlocking
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

//tìm kiếm và hiện đề xuất tìm kiếm
fun search(searchQuery: String){

}



//tạo lịch trình đi chơi
fun createSchedule(){

}

fun roundDecimal(value: Double, places: Int): Double{
    return (value * 100.0).roundToInt() / 10.0.pow(places.toDouble())
}