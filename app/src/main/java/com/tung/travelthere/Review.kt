package com.tung.travelthere

import java.time.LocalDateTime
import java.util.*

class Review(val userId: String, val content: String, val time: Date, val score: Int): java.io.Serializable