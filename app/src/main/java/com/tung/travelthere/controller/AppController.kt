package com.tung.travelthere.controller

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tung.travelthere.objects.User

const val collectionCities = "cities"

const val locationsField = "locations"

class AppController {
    companion object{
        @JvmStatic
        val db = Firebase.firestore

        val currentUser = User()
    }
}