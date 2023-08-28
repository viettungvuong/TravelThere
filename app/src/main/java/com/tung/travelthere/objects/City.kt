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

        val query = Firebase.firestore.collection(collectionCities).whereEqualTo(cityNameField, name)
            .whereEqualTo("country", country).limit(1).get().await()
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
        var recommendations = ArrayList<PlaceLocation>()

        suspend fun refreshRecommendations() {
//            withContext(Dispatchers.IO) {
//                val query =
//                    Firebase.firestore.collection(collectionCities).whereEqualTo(cityNameField, name)
//                        .whereEqualTo("country", country)
//                        .limit(1).get().await()
//
//                val documents = query.documents
//
//                for (document in documents) {
//                    document.reference.collection(collectionLocations).get().addOnSuccessListener {
//                        //lấy từng địa điểm của thành phố hiện tại
//                            documents ->
//                        for (document in documents) {
//                            val name = document.getString("location-name")
//                            //thêm toạ độ
//                        }
//                    }
//                }
//            }
        }

        suspend fun suggestPlace(location: PlaceLocation) {
            val cityDocRef =
                Firebase.firestore.collection(collectionCities)
                    .document(location.cityName)

            Firebase.firestore.runTransaction { transaction ->
                val cityDocument = transaction.get(cityDocRef)
                val locationCollectionRef = cityDocRef.collection(collectionLocations)

                val locationDocumentRef = locationCollectionRef.document(location.getPos().toString())

                if (cityDocument.exists()) {

                    val locationDocument = transaction.get(locationDocumentRef)

                    if (locationDocument.exists()) {
                        //có địa điểm này
                        val recommendedNum = locationDocument.getLong("recommends")?:0
                        transaction.update(locationDocumentRef, "recommends", recommendedNum+1)
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
    }
}

