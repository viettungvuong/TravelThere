package com.tung.travelthere.objects

import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.tung.travelthere.controller.*
import kotlinx.coroutines.tasks.await

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


//không cho phép tạo object từ class PlaceLocation
open class PlaceLocation protected constructor(private val name: String, private val pos: Position, val cityName: String): java.io.Serializable{
    var categories: MutableSet<Category> = mutableSetOf() //các category của địa điểm này
    var imageUrl: String?=null



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

    fun distanceTo(placeLocation: PlaceLocation): Float{
        return this.pos.distanceTo(placeLocation.getPos())
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
                if (res!=null&&res.isNotBlank()){
                    val imageRef = storageRef.child(res)
                    res = imageRef.downloadUrl.await().toString()
                }

            }
        }
        imageUrl = res
        return res
    }

    val reviewRepository = ReviewRepository()

    inner class ReviewRepository : ViewModel(), java.io.Serializable {
        var reviews=mutableListOf<Review>()

        //lấy tổng điểm
        fun calculateReviewScore(): Double{
            var res = 0.0
            for (review in reviews){
                res+=review.score
            }
            return res/reviews.size
        }

        //đăng review lên firebase
        fun submitReview(review: Review, context: Context){
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
    init {
        this.categories.add(Category.ATTRACTION)
    }
}
