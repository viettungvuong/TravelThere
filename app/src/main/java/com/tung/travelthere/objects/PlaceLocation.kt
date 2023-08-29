package com.tung.travelthere.objects

import android.content.Context
import com.tung.travelthere.Review
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
    var reviews: MutableSet<Review> = mutableSetOf() //danh sách các review

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