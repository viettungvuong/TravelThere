package com.tung.travelthere.controller

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tung.travelthere.PlaceAutocompleteViewModel
import com.tung.travelthere.objects.City
import com.tung.travelthere.objects.PlaceLocation
import com.tung.travelthere.objects.Position
import com.tung.travelthere.objects.User
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

const val collectionCities = "cities"
const val collectionLocations = "locations"
const val cityNameField = "city-name"

const val locationNameField = "location-name"

val colorBlue = Color(android.graphics.Color.parseColor("#5980b3"))
val formatter = SimpleDateFormat("HH:mm:ss dd/MM/yyyy")

class AppController {
    class UserPlace{
        var cityName = ""
        var countryName = ""
        var currentLocation: Position?=null
    }

    class Favorites{
        private constructor()

        private val favoriteList = ArrayList<PlaceLocation>()

        companion object{
            private var singleton: Favorites?=null

            @JvmStatic
            fun getSingleton(): Favorites{
                if (singleton==null){
                    singleton= Favorites()
                }
                return singleton!!
            }
        }

        fun addFavorite(location: PlaceLocation){
            favoriteList.add(location)
        }

        fun removeFavorite(index: Int){
            favoriteList.removeAt(index)
        }

        fun getList(): ArrayList<PlaceLocation>{
            return favoriteList
        }
    }

    companion object{
        @JvmStatic

        val currentUser = User() //user hiện tại

        lateinit var placeViewModel: PlaceAutocompleteViewModel //view model cho place

        lateinit var currentPosition: UserPlace
    }
}

@Composable
fun ImageFromUrl(url: String, contentDescription: String?) {
    Image(
        painter = rememberImagePainter(url),
        contentDescription = contentDescription,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
}