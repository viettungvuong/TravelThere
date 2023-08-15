package com.tung.travelthere.objects

class Position(var lat: Float, var long: Float)

class Dish(var name: String, var type: String)


//không cho phép tạo object từ class Location
open class Location protected constructor(private val name: String, private val pos: Position): java.io.Serializable{
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

class Restaurant(name: String, pos: Position, private val specializeIn: Dish): Location(name,pos){
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

class PlaceOfInterest(name: String, pos: Position): Location(name,pos)

class TouristPlace(name: String, pos: Position): Location(name,pos)