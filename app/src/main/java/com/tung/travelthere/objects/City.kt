package com.tung.travelthere.objects

import okhttp3.OkHttpClient
import okhttp3.Request
import android.app.DownloadManager
import android.media.Image
import android.os.Debug
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.tung.travelthere.controller.*
import kotlinx.coroutines.*

class City private constructor() {
    private var name: String? = null
    private var country: String? = null
    private var imageUrl: String? = null

    companion object {
        private var singleton: City? = null

        @JvmStatic
        fun getSingleton(): City {
            if (singleton == null) {
                singleton = City()
            }
            return singleton!!
        }
    }

    fun setName(name: String) {
        this.name = name
    }

    fun setCountry(country: String) {
        this.country = country
    }

    fun getName(): String? {
        return name
    }

    fun getCountry(): String? {
        return country
    }


    //lấy từ trên firebase
    suspend fun fetchImageUrl(): String? {
        if (imageUrl != null) {
            return imageUrl!!
        }

        var res: String? = null

        val query =
            Firebase.firestore.collection(collectionCities).whereEqualTo(cityNameField, name)
                .limit(1).get().await()
        val document = query.documents.firstOrNull()
        if (document != null) {
            res = document.getString("file-name")
            val storageRef = Firebase.storage.reference
            val imageRef = storageRef.child(res!!)
            res = imageRef.downloadUrl.await().toString()
        }
        imageUrl = res
        return res
    }

    val recommendationsRepository = RecommendationsRepository()

    inner class RecommendationsRepository : ViewModel() {

        //những nơi nên đi tới
        var recommendations = mutableSetOf<PlaceLocation>()

        suspend fun refreshRecommendations(): Set<PlaceLocation> {
            if (recommendations.isNotEmpty()) {
                return recommendations
            }

            val query =
                Firebase.firestore.collection(collectionCities).whereEqualTo(cityNameField, name)
                    .limit(1).get().await()

            val document = query.documents.firstOrNull()

            if (document != null) {
                val locationCollection =
                    document.reference.collection(collectionLocations).get().await()

                val locations = locationCollection.documents
                for (location in locations) {

                    val placeName = location.getString(locationNameField) ?: ""
                    val cityName = this@City.name ?: ""
                    val lat = location.getDouble("lat") ?: 0.0
                    val long = location.getDouble("long") ?: 0.0

                    val t = TouristPlace(placeName, Position(lat, long), cityName)

                    recommendations.add(t)
                }
            }

            return recommendations
        }


    }

}

