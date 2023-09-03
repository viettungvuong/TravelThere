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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tung.travelthere.PlaceAutocompleteViewModel
import com.tung.travelthere.Review
import com.tung.travelthere.objects.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
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
            updateFavoritesFirebase(location,add = true)
        }

        fun removeFavorite(location: PlaceLocation){
            favoriteList.remove(location)
            updateFavoritesFirebase(location,add = false)
        }

        fun getList(): Set<PlaceLocation> {
            return favoriteList
        }

        fun isFavorite(location: PlaceLocation): Boolean{
            return favoriteList.contains(location)
        }

        private fun updateFavoritesFirebase(location: PlaceLocation,add: Boolean){ //cập nhật trên firebase
            if (add){
                val documentReference = db.collection("users").document(auth.currentUser!!.uid)

                documentReference.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) { //có favorite trên firebase r
                            documentReference.update("favorites",
                                FieldValue.arrayUnion("${location.getPos()},${location.cityName}"))
                        } else {
                            //chưa có thì phải tạo

                            val favoriteListStr = mutableListOf<String>()
                            favoriteListStr.add("${location.getPos()},${location.cityName}")
                            val favoriteData = hashMapOf(
                                "favorites" to favoriteListStr
                            )
                            db.collection("users").document(auth.currentUser!!.uid).set(favoriteData)
                        }
                    }
            }
            else{
                db.collection("users").document(auth.currentUser!!.uid).update("favorites",
                    FieldValue.arrayRemove("${location.getPos()},${location.cityName}"))
            }
        }

        suspend fun refreshFavorites(): Set<PlaceLocation> {
            if (favoriteList.isNotEmpty()){
                return favoriteList
            }

            val documentSnapshot = db.collection("users").document(auth.currentUser!!.uid).get().await()

            if (documentSnapshot.exists()) {
                val favorites = documentSnapshot["favorites"]
                if (favorites != null) {
                    val list = favorites as List<String>

                    for (str in list){
                        val splitStr = str.split(',')
                        val cityName = splitStr[2]
                        val pos = splitStr[0]+","+splitStr[1]
                        db.collection(collectionCities).document(cityName).collection(
                            collectionLocations).document(pos).get().addOnSuccessListener {
                            location ->
                            val placeName = location.getString(locationNameField) ?: ""
                            val lat = location.getDouble("lat") ?: 0.0
                            val long = location.getDouble("long") ?: 0.0

                            val t = TouristPlace(placeName, Position(lat, long), cityName)

                            val categoriesStr = location.get("categories") as List<String>?

                            if (categoriesStr!=null){
                                for (categoryStr in categoriesStr){
                                    t.categories.add(convertStrToCategory(categoryStr)) //thêm category
                                }
                            }

                            this.favoriteList.add(t) //thêm vào danh sách favorite
                        }
                    }
                }
            }

            return favoriteList
        }
    }

    companion object{

        lateinit var currentPosition: UserPlace //địa điểm hiện tại

        @JvmField
        val currentUser = User() //user hiện tại

        @JvmField
        val db = Firebase.firestore

        @JvmField
        val auth = FirebaseAuth.getInstance()
    }
}

class SearchViewModel : ViewModel() {
    var matchedQuery = mutableStateListOf<PlaceLocation>()
    var originalMatchedQuery = mutableStateListOf<PlaceLocation>()
}
