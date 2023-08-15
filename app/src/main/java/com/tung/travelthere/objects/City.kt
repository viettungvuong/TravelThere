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

    val recommendationsRepository= RecommendationsRepository()

    suspend fun fetchImageUrl(): String?{
        var res: String?=null

        val query = AppController.db.collection(collectionCities).whereEqualTo(cityNameField,name).limit(1).get().await()

        val document = query.documents.firstOrNull()
        if (document!=null){
            res = document.getString("file-name")
            val storageRef = Firebase.storage.reference
            val imageRef = storageRef.child(res!!)
            res = imageRef.downloadUrl.await().toString()
        }

        return res

    }


    suspend fun fetchDescription(): String?{
        var res: String?=null

        val query = AppController.db.collection(collectionCities).whereEqualTo(cityNameField,name).limit(1).get().await()

        val document = query.documents.firstOrNull()
        if (document!=null){
            res = document.getString("description")
        }

        return res

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