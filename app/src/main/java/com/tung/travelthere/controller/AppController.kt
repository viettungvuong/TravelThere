package com.tung.travelthere.controller

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tung.travelthere.PlaceAutocompleteViewModel
import com.tung.travelthere.objects.*
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

        private val favoriteList = mutableSetOf<PlaceLocation>()
        //dùng set để tránh trùng lặp

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
            Log.d("favorite",favoriteList.toString())
        }

        fun removeFavorite(location: PlaceLocation){
            favoriteList.remove(location)
        }

        fun getList(): Set<PlaceLocation> {
            return favoriteList
        }

        fun isFavorite(location: PlaceLocation): Boolean{
            return favoriteList.contains(location)
        }
    }

    companion object{
        @JvmStatic

        val currentUser = User() //user hiện tại

        lateinit var currentPosition: UserPlace //địa điểm hiện tại

        val db = Firebase.firestore
    }
}

