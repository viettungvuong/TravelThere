package com.tung.travelthere.objects

import okhttp3.OkHttpClient
import okhttp3.Request
import android.app.DownloadManager
import android.media.Image
import android.os.Debug
import android.util.Log
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.tung.travelthere.controller.*

class City private constructor() {
    private var name: String?=null
    private var country: String?=null
    private var imageUrl: String?=null

    fun setName(name: String){
        this.name = name
    }

    fun setCountry(country: String){
        this.country = country
    }

    fun getName(): String?{
        return name
    }

    fun getCountry(): String?{
        return country
    }


    fun setImageUrl(imageUrl: String){
        this.imageUrl = imageUrl
    }

    fun getImageUrl(): String?{
        return imageUrl
    }

    companion object{
        private var singleton: City?=null

        @JvmStatic
        fun getSingleton(): City{
            if (singleton==null){
                singleton=City()
            }
            return singleton!!
        }
    }

    val recommendationsRepository = RecommendationsRepository()

    //lấy từ trên firebase
    suspend fun fetchImageUrl(): String? {
        var res: String? = null

        val query = AppController.db.collection(collectionCities).whereEqualTo(cityNameField, name)
            .whereEqualTo("country", country).limit(1).get().await()
        Log.d("fetched", "true")
        val document = query.documents.firstOrNull()
        if (document != null) {
            res = document.getString("file-name")
            val storageRef = Firebase.storage.reference
            val imageRef = storageRef.child(res!!)
            res = imageRef.downloadUrl.await().toString()
        }
        Log.d("image url", res?:"")
        return res

    }


    inner class RecommendationsRepository {

        //những nơi nên đi tới
        var recommendations = ArrayList<Location>()

        suspend fun refreshRecommendations() {
            withContext(Dispatchers.IO) {
                val query =
                    AppController.db.collection(collectionCities).whereEqualTo(cityNameField, name)
                        .whereEqualTo("country", country)
                        .limit(1).get().await()

                val documents = query.documents

                for (document in documents) {
                    document.reference.collection(collectionLocations).get().addOnSuccessListener {
                        //lấy từng địa điểm của thành phố hiện tại
                            documents ->
                        for (document in documents) {
                            val name = document.getString("location-name")
                            //thêm toạ độ
                        }
                    }
                }
            }
        }
    }
}

