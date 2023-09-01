package com.tung.travelthere.objects

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.tung.travelthere.Review
import com.tung.travelthere.controller.*
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

class Position(var lat: Double, var long: Double): java.io.Serializable{
    override fun toString(): String {
        return "$lat,$long"
    }
}

class Dish(var name: String, var type: String): java.io.Serializable

enum class Category{
    RESTAURANT,
    BAR,
    ATTRACTION,
    NECESSITY,
    NATURE,
    SHOPPING,
    OTHERS
}


//không cho phép tạo object từ class PlaceLocation
open class PlaceLocation protected constructor(private val name: String, private val pos: Position, val cityName: String): java.io.Serializable{

    private var drawableName: String?=null
    var categories: MutableSet<Category> = mutableSetOf() //các category của địa điểm này
    var imageUrl: String?=null

    fun setDrawableName(name: String){
        drawableName=name
    }

    fun getDrawableName(context: Context): Int?{
        if (drawableName==null){
            return null
        }
        val resourceId = context.resources.getIdentifier(drawableName,"drawable",context.packageName)
        return resourceId
    }

    fun getName(): String{
        return name
    }

    fun getPos(): Position{
        return pos
    }


    override fun toString(): String {
        return "$name,$cityName"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlaceLocation) return false

        return this.toString() == other.toString()
    }

    override fun hashCode(): Int {
        return name.hashCode() * cityName.hashCode() * 30
    }

    suspend fun fetchImageUrl(): String? {
        if (imageUrl != null) {
            return imageUrl!!
        }

        var res: String? = null

        val query = AppController.db.collection(collectionCities).whereEqualTo(cityNameField, cityName)
            .limit(1).get().await()
        val document = query.documents.firstOrNull()
        if (document != null) {
            val locationCollection = document.reference.collection(collectionLocations).whereEqualTo(
                locationNameField,name).limit(1).get().await()
            val document2 = locationCollection.documents.firstOrNull()
            if (document2!=null){
                res = document2.getString("file-name")
                val storageRef = Firebase.storage.reference
                val imageRef = storageRef.child(res!!)
                res = imageRef.downloadUrl.await().toString()
            }
        }
        imageUrl = res
        return res
    }

    val reviewRepository = ReviewRepository()

    inner class ReviewRepository : ViewModel(), java.io.Serializable {
        var reviews=mutableListOf<Review>()

        //đăng review lên firebase
        fun submitReview(review: Review, context: Context){
            val reviewData= hashMapOf(
                "sender" to review.userId,
                "content" to review.content,
                "score" to review.score,
                "time" to formatter.format(review.time)
            )
            AppController.db.collection(collectionCities).document(cityName)
                .collection(collectionLocations).document(pos.toString()).collection("reviews").add(reviewData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Added your review, thank you for your feedback",Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener{
                    Toast.makeText(context, "Cannot add your review, please try again",Toast.LENGTH_LONG).show()
                }
        }

        //lấy review về địa điểm
        suspend fun refreshReviews(refreshNow: Boolean=false): List<Review> {
            if (reviews.isNotEmpty()&&refreshNow){
                return reviews
            }

            reviews.clear()
            val query =
                AppController.db.collection(collectionCities).document(cityName)
                    .collection(collectionLocations).document(pos.toString())
                    .get().await()

            val document = query.reference

            if (document != null) {
                Log.d("document not null","not null")
                val reviewCollection =
                    document.collection("reviews").get().await()

                val fetchedReviews = reviewCollection.documents
                for (document in fetchedReviews) {

                    val userId = document.getString("sender")?:""
                    val content = document.getString("content")?:""
                    val time = formatter.parse(document.getString("time"))
                    val score = (document.getLong("score")?:0L).toInt()

                    val review = Review(userId,content,time,score)
                    Log.d("review",review.toString())
                    reviews.add(review)
                }
            }

            return reviews as List<Review>
        }
    }
}

class Restaurant(name: String, pos: Position, cityName: String, private val specializeIn: Dish): PlaceLocation(name,pos,cityName){
    init {
        this.categories.add(Category.RESTAURANT)
    }

    private var ratings = ArrayList<Rating>()
    private var ratingScore = 0f

    fun getSpecializedDish(): Dish{
        return specializeIn
    }

    fun rate(newRating: Rating){
        ratings.add(newRating)
        ratingScore = calculateNewRatingScore(newRating)
    }

    private fun calculateNewRatingScore(newRating: Rating): Float{ //tính điểm đánh giá (cái này dùng mỗi khi có rating mới)
        val n = ratings.size

        var total = ratingScore*(n-1)

        total += newRating.score

        return total / n
    }

    fun getRatingScore(): Float{ //lấy điểm đánh giá
        return ratingScore
    }


}

class PlaceOfInterest(name: String, pos: Position, cityName: String): PlaceLocation(name,pos,cityName){
    init {
        this.categories.add(Category.ATTRACTION)
    }
}

class TouristPlace(name: String, pos: Position, cityName: String): PlaceLocation(name,pos,cityName){
    init {
        this.categories.add(Category.ATTRACTION)
    }
}

class RecommendedPlace(name: String, pos: Position, cityName: String): PlaceLocation(name,pos,cityName)