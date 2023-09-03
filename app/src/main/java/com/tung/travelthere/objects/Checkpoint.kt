package com.tung.travelthere.objects

import com.google.type.DateTime
import java.sql.Date
import java.sql.Time

class Checkpoint(private val placeLocation: PlaceLocation, val time: Time?=null): java.io.Serializable{
    fun distanceTo(other: Checkpoint): Float{
        return placeLocation.distanceTo(other.placeLocation)
    }

    fun getLocation(): PlaceLocation{
        return placeLocation
    }
}