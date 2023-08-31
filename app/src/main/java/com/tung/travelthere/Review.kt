package com.tung.travelthere

import com.tung.travelthere.controller.AppController
import com.tung.travelthere.controller.collectionCities
import com.tung.travelthere.controller.collectionLocations
import com.tung.travelthere.objects.PlaceLocation
import java.time.LocalDateTime
import java.util.*

class Review(val userId: String, val content: String, val time: Date, val score: Int): java.io.Serializable

