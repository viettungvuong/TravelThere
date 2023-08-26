package com.tung.travelthere.controller

import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tung.travelthere.PlaceAutocompleteViewModel
import com.tung.travelthere.objects.City
import com.tung.travelthere.objects.User

const val collectionCities = "cities"
const val collectionLocations = "locations"
const val cityNameField = "city-name"

const val locationNameField = "location-name"



class AppController {
    companion object{
        @JvmStatic
        val db = Firebase.firestore

        val currentUser = User()

        lateinit var placeViewModel: PlaceAutocompleteViewModel
    }
}