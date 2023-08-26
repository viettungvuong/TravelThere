package com.tung.travelthere.controller

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
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

@Composable
fun ImageFromUrl(url: String, contentDescription: String?) {
    Image(
        painter = rememberImagePainter(url),
        contentDescription = contentDescription,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
}