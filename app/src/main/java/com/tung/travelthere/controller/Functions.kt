package com.tung.travelthere.controller

import android.annotation.SuppressLint
import android.content.Context
import android.content.SearchRecentSuggestionsProvider
import android.graphics.Bitmap
import android.location.Geocoder
import android.util.Log
import androidx.compose.ui.graphics.Color
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


//tìm kiếm và hiện đề xuất tìm kiếm
fun search(searchQuery: String){

}

//cho phép người dùng thêm địa điểm
fun suggestPlace(location: PlaceLocation) {
    val cityDocRef =
        Firebase.firestore.collection(collectionCities)
            .document(location.cityName)

    Firebase.firestore.runTransaction { transaction ->
        val cityDocument = transaction.get(cityDocRef)
        val locationCollectionRef = cityDocRef.collection(collectionLocations)

        val locationDocumentRef =
            locationCollectionRef.document(location.getPos().toString())

        if (cityDocument.exists()) {

            val locationDocument = transaction.get(locationDocumentRef)

            if (locationDocument.exists()) {
                //có địa điểm này
                val recommendedNum = locationDocument.getLong("recommends") ?: 0 //số lượng được recommends
                transaction.update(locationDocumentRef, "recommends", recommendedNum + 1)
            } else {
                //chưa có địa điểm này
                val locationData = hashMapOf(
                    "name" to location.getName(),
                    "pos" to location.getPos().toString(),
                )
                transaction.set(locationDocumentRef, locationData) //tạo document mới
            }
        }
    }
}