package com.tung.travelthere.controller

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.tung.travelthere.objects.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

const val collectionCities = "cities"
const val collectionLocations = "locations"
const val cityNameField = "city-name"

const val locationNameField = "location-name"

val colorBlue = Color(android.graphics.Color.parseColor("#5980b3"))
val formatter = SimpleDateFormat("HH:mm:ss dd/MM/yyyy")
val formatterDateOnly = SimpleDateFormat("dd/MM/yyyy")
val formatterDateOnlyNoYear = SimpleDateFormat("dd/MM")
class AppController {
    class UserPlace{
        var cityName = ""
        var countryName = ""
        var currentLocation: Position?=null //nếu location này bằng null thì không có phần nearby
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
                val documentReference = Firebase.firestore.collection("users").document(auth.currentUser!!.uid)

                documentReference.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) { //có favorite trên firebase r
                            documentReference.update("favorites",
                                FieldValue.arrayUnion("${location.getPos()}"))
                        } else {
                            //chưa có thì phải tạo

                            val favoriteListStr = mutableListOf<String>()
                            favoriteListStr.add("${location.getPos()}")
                            val favoriteData = hashMapOf(
                                "favorites" to favoriteListStr
                            )
                            Firebase.firestore.collection("users").document(auth.currentUser!!.uid).set(favoriteData)
                        }
                    }
            }
            else{
                Firebase.firestore.collection("users").document(auth.currentUser!!.uid).update("favorites",
                    FieldValue.arrayRemove("${location.getPos()}"))
            }
        }

        suspend fun refreshFavorites(): Set<PlaceLocation> {
            if (favoriteList.isNotEmpty()){
                return favoriteList
            }

            val documentSnapshot = Firebase.firestore.collection("users").document(auth.currentUser!!.uid).get().await()

            if (documentSnapshot.exists()) {
                val favorites = documentSnapshot["favorites"]
                if (favorites != null) {
                    val list = favorites as List<String>

                    //từng str trong favorite
                    for (str in list){
                        val getPlaceLocation = City.getSingleton().locationsRepository.locations[str]
                        //lấy từng địa điểm trong schedule

                        if (getPlaceLocation!=null){
                            this.favoriteList.add(getPlaceLocation)
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
        val auth = FirebaseAuth.getInstance()

        @JvmField
        val storage = FirebaseStorage.getInstance()

        @JvmField
        val schedules = mutableListOf<Schedule>() //danh sách schedule

        @JvmField
        val countVisit = mutableMapOf<PlaceLocation,Int>() //đếm số lần đã đến các điểm (trong 1 tuần trở lại đây)

        @JvmField
        val currentSchedule = mutableStateOf(Schedule())
    }
}

class SearchViewModel : ViewModel() {
    var matchedQuery = mutableStateListOf<PlaceLocation>()
    var originalMatchedQuery = mutableStateListOf<PlaceLocation>()
}

