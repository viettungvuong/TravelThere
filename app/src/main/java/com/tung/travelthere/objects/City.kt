package com.tung.travelthere.objects

import okhttp3.OkHttpClient
import okhttp3.Request
import android.app.DownloadManager
import android.media.Image
import android.os.Debug
import android.util.Log
import androidx.compose.runtime.mutableStateOf
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
            AppController.db.collection(collectionCities).whereEqualTo(cityNameField, name)
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

    val locationsRepository = LocationsRepository()

    inner class LocationsRepository : ViewModel() {

        //những nơi nên đi tới
        var locations = mutableSetOf<PlaceLocation>()
        var nearby = mutableSetOf<PlaceLocation>()
        var recommends = mutableSetOf<PlaceLocation>()

        suspend fun refreshLocations(refresh: Boolean = false): Set<PlaceLocation> {
            if (locations.isNotEmpty()&&!refresh) {
                return locations
            }

            locations.clear()
            val query =
                AppController.db.collection(collectionCities).document(name!!)
                    .get().await()

            val document = query.reference

            if (document != null) {

                val locationCollection =
                    document.collection(collectionLocations).get().await()

                val locations = locationCollection.documents
                for (location in locations) {

                    val placeName = location.getString(locationNameField) ?: ""
                    val cityName = this@City.name ?: ""
                    val lat = location.getDouble("lat") ?: 0.0
                    val long = location.getDouble("long") ?: 0.0

                    val categoriesStr = location.get("categories") as List<String>?
                    val categoriesArr: MutableList<Category> = mutableListOf()
                    var isRestaurant = false

                    if (categoriesStr!=null){
                        for (categoryStr in categoriesStr){
                            categoriesArr.add(convertStrToCategory(categoryStr)) //thêm category

                            if (categoryStr=="Restaurant"){
                                isRestaurant=true
                            }
                        }
                    }

                    val t = if (!isRestaurant){
                        TouristPlace(placeName, Position(lat, long), cityName)
                    }
                    else{
                        Restaurant(placeName, Position(lat, long), cityName, mutableListOf()) //specialize dish để thêm sau
                        //refresh restaurant
                    }

                    val recommendedNum = location.getLong("recommends") ?: 0
                    t.recommendsCount=recommendedNum.toInt()

                    if (recommendedNum>=5) //có nhiều hơn 5 lượt recommend
                    {
                        this.recommends.add(t) //thì thêm vào recommends luôn
                    }

                    this.locations.add(t)
                }
            }

            val currentPos = AppController.currentPosition.currentLocation

            if (currentPos!=null){
                locations.sortedBy {
                    it.getPos().distanceTo(currentPos!!) //sắp xếp các địa điểm theo khoảng cách tới vị trí hiện tại
                }
            }
            return locations
        }

        //lấy những địa điểm trong phạm vi 5000km
        fun nearbyLocations(refresh: Boolean=false): Set<PlaceLocation> {
            if (nearby.isNotEmpty()&&!refresh) {
                return nearby
            }

            nearby.clear()
            val currentPos = AppController.currentPosition.currentLocation ?: return emptySet()

            nearby = locations.filter {
                it.getPos().distanceTo(currentPos!!) <= 5000
            }.toMutableSet()

            nearby = (nearby + recommends.filter {
                it.getPos().distanceTo(currentPos!!) <= 5000
            }.toMutableSet()).toMutableSet() //gom recommends lại

            return nearby
        }


    }

}

