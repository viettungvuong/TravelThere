package com.tung.travelthere.objects

import android.content.Context
import java.net.URL

class Position(var lat: Float, var long: Float): java.io.Serializable

class Dish(var name: String, var type: String): java.io.Serializable


//không cho phép tạo object từ class Location
open class Location protected constructor(private val name: String, private val pos: Position, val city: City): java.io.Serializable{
//    private var imageUrl : URL?=null
//fun setImageUrl(url: URL) {
//    imageUrl = url
//}
//
//    fun getImageUrl(): URL? {
//        return imageUrl
//    }

    private var drawableName: String?=null
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
        return name + "," + pos.lat.toString() + "," + pos.long.toString()
    }
}

class Restaurant(name: String, pos: Position, city: City, private val specializeIn: Dish): Location(name,pos,city){
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

class PlaceOfInterest(name: String, pos: Position, city: City): Location(name,pos,city)

class TouristPlace(name: String, pos: Position, city: City): Location(name,pos,city)