package com.tung.travelthere.objects

class Rating(val score: Int, private val content: String, private val user: User) {
    fun getContent(): String{
        return content
    }

    fun getUser(): User{
        return user
    }
}