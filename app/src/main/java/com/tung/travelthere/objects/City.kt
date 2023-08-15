package com.tung.travelthere.objects

import okhttp3.OkHttpClient
import okhttp3.Request
import android.app.DownloadManager
import android.media.Image
import android.os.Debug
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.tung.travelthere.controller.AppController
import com.tung.travelthere.controller.cityNameField
import com.tung.travelthere.controller.collectionCities
import com.tung.travelthere.controller.locationsField
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

class City(val name: String, val country: String) {
    private var imageUrl: String?=null
    private var description: String?=null

    val recommendationsRepository= RecommendationsRepository()

    fun setDescription(description: String){
        this.description=description
    }


    fun getDescription(): String?{
        return description
    }

    fun getImageUrl(): String?{
        return imageUrl
    }

    fun fetchImageUrl(){
        AppController.db.collection(collectionCities).whereEqualTo(cityNameField,name).limit(1).get()
            .addOnSuccessListener {
                    documents ->
                for (document in documents){
                    val storage = Firebase.storage
                    val storageRef = storage.reference
                    val fileRef = storageRef.child(document.getString("file-name")!!)

                    fileRef.downloadUrl
                        .addOnSuccessListener { uri ->
                            val downloadUrl = uri.toString()
                            description = downloadUrl
                        }
                        .addOnFailureListener { exception ->
                            Log.d("Error","Error getting download URL: $exception")
                        }
                }
            }
    }


    fun fetchDescription(){

        AppController.db.collection(collectionCities).whereEqualTo(cityNameField,name).limit(1).get()
            .addOnSuccessListener {
                documents ->
                for (document in documents){
                    description = document.getString("description")
                }
            }


    }

    inner class RecommendationsRepository{

        var recommendations = ArrayList<Location>()

        suspend fun refreshRecommendations(){
            withContext(Dispatchers.IO){
                val ref = AppController.db.collection(collectionCities).whereEqualTo(cityNameField,name).whereEqualTo("country",country)
                    .limit(1).get()

                ref.addOnSuccessListener {
                    documents ->
                    for (document in documents){
                        document.reference.collection(locationsField).get().addOnSuccessListener {
                            //lấy từng địa điểm của thành phố hiện tại
                            documents ->
                            for (document in documents){
                                val name = document.getString("location-name")
                                //thêm toạ độ
                            }
                        }
                    }
                }
            }
        }
    }

}