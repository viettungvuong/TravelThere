package com.tung.travelthere.objects

import android.content.Context
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.database.annotations.NotNull
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import com.tung.travelthere.controller.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

class Position(var lat: Double, var long: Double): java.io.Serializable{
    override fun toString(): String {
        return "$lat,$long"
    }

    fun distanceTo(other: Position): Float{
        val locationA = Location("Current")
        locationA.latitude=lat
        locationA.longitude=long
        val locationB = Location("That")
        locationB.latitude=other.lat
        locationB.longitude=other.long

        return locationA.distanceTo(locationB)
    }
}

class Dish(var name: String): java.io.Serializable

enum class Category{
    RESTAURANT,
    BAR,
    ATTRACTION,
    NECESSITY,
    NATURE,
    SHOPPING,
    OTHERS
}

fun convertStrToCategory(string: String): Category{
    return (when (string.uppercase()){
        "RESTAURANT" -> Category.RESTAURANT
        "BAR" -> Category.BAR
        "ATTRACTION" -> Category.ATTRACTION
        "NECESSITY" -> Category.NECESSITY
        "NATURE" -> Category.NATURE
        "SHOPPING" -> Category.SHOPPING
        else -> Category.OTHERS
    })
}

fun convertCategoryToStr(category: Category): String{
        return when (category) {
            Category.RESTAURANT -> "RESTAURANT"
            Category.BAR -> "BAR"
            Category.ATTRACTION -> "ATTRACTION"
            Category.NECESSITY -> "NECESSITY"
            Category.NATURE -> "NATURE"
            Category.SHOPPING -> "SHOPPING"
            Category.OTHERS -> "OTHERS"
        }
}


//không cho phép tạo object từ class PlaceLocation nên dùng protected (inherit vẫn đc)
@Serializable
open class PlaceLocation protected constructor(private val name: String, private val pos: Position, val cityName: String): java.io.Serializable{
    var categories: MutableSet<Category> = mutableSetOf() //các category của địa điểm này
    var recommendsCount = 0

    var address: String?=null //địa chỉ

    @Transient //để bỏ qua imageUrlState khi serialize
    private var imageUrlState= mutableStateOf<String?>(null)
    var imageUrl: String?
        get() = imageUrlState?.value
        set(value) {
            imageUrlState?.value = value
        }
    //gán imageurl khi deserialize (do sau khi deserializae thì imageUrlState = null)
    fun afterDeserialization(imageUrl: String?) {
        this.imageUrlState =mutableStateOf(null)
        this.imageUrlState.value = imageUrl
    }

    var reviewRepository: ReviewRepository
    var imageViewModel: ImageViewModel

    init {
        imageViewModel=ImageViewModel()
        runBlocking {
            fetchImageUrl()
        }
        reviewRepository=ReviewRepository()
    }

    fun getName(): String{
        return name
    }

    fun getPos(): Position{
        return pos
    }

    override fun toString(): String {
        return "$name,$cityName,$imageUrl"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlaceLocation) return false

        return this.toString() == other.toString()
    }

    override fun hashCode(): Int {
        return name.hashCode() * cityName.hashCode() * 30
    }

    fun distanceTo(placeLocation: PlaceLocation): Float{
        return this.pos.distanceTo(placeLocation.getPos())
    }



    suspend fun fetchImageUrl() = withContext(Dispatchers.IO) {
        imageViewModel.fetchAllImageUrls(true) //lấy tất cả ảnh
        imageUrl = imageViewModel.urls.first() //đặt image url là ảnh đầu tiên
    }

    inner class ReviewRepository : ViewModel(), java.io.Serializable {
        var reviews=mutableListOf<Review>()
        var reviewScore = 0.0

        //lấy tổng điểm
        fun calculateReviewScore(): Double{
            if (reviews.isEmpty()){
                return 0.0
            }

            var res = 0.0
            for (review in reviews){
                res+=review.score
            }
            reviewScore = res/reviews.size
            return reviewScore
        }

        //đăng review lên firebase
        fun submitReview(review: Review, context: Context){
            reviews.add(review)
            reviewScore+=review.score

            val reviewData= hashMapOf(
                "sender" to review.userId,
                "sender-name" to review.name,
                "content" to review.content,
                "score" to review.score,
                "time" to formatter.format(review.time)
            )
            AppController.db.collection(collectionCities).document(cityName)
                .collection(collectionLocations).document(pos.toString()).collection("reviews").add(reviewData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Added your review, thank you for your feedback",Toast.LENGTH_LONG).show()
                    reviews.add(review)
                }
                .addOnFailureListener{
                    Toast.makeText(context, "Cannot add your review, please try again",Toast.LENGTH_LONG).show()
                }
        }

        //lấy review về địa điểm
        suspend fun refreshReviews(refreshNow: Boolean=false): List<Review> {
            if (reviews.isNotEmpty()&&!refreshNow){
                return reviews
            }

            reviews.clear()
            val query =
                AppController.db.collection(collectionCities).document(cityName)
                    .collection(collectionLocations).document(pos.toString())
                    .get().await()

            val document = query.reference

            if (document != null) {
                val reviewCollection =
                    document.collection("reviews").get().await()

                val fetchedReviews = reviewCollection.documents
                for (document in fetchedReviews) {

                    val userId = document.getString("sender")?:""
                    val name = document.getString("sender-name")?:""
                    val content = document.getString("content")?:""
                    val time = formatter.parse(document.getString("time"))
                    val score = (document.getLong("score")?:0L).toInt()

                    val review = Review(userId,name,content,time,score)
                    reviews.add(review)
                }
            }

            return reviews
        }
    }

    inner class ImageViewModel: ViewModel(), java.io.Serializable{
        var urls = mutableListOf<String>()

        suspend fun fetchAllImageUrls(refreshNow: Boolean=false): List<String>{
            if (urls.isNotEmpty()&&!refreshNow){
                return urls
            }

            urls.clear()
            val listResult = AppController.storage.reference.child("files/${pos}").listAll().await()

            if (listResult.items.isNotEmpty()) {
                for (item in listResult.items){
                    val uri = item.downloadUrl.await()
                    urls.add(uri.toString())
                }
            } else {
                println("No files found in the directory.")
            }

            return urls
        }
    }
}

class Restaurant(name: String, pos: Position, cityName: String, private val specializeIn: MutableList<Dish>): PlaceLocation(name,pos,cityName){
    init {
        this.categories.add(Category.RESTAURANT)
    }


    fun getSpecializedDish(): List<Dish>{
        return specializeIn
    }


}

class TouristPlace(name: String, pos: Position, cityName: String): PlaceLocation(name,pos,cityName){
}
