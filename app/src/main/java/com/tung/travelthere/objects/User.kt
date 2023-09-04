package com.tung.travelthere.objects

import java.io.Serializable

class User() {
    private var name: String?=null
    private var fromCity: City?=null

    constructor(name: String, fromCity: City) : this() {
        this.name = name
        this.fromCity = fromCity
    }

    fun getName(): String{
        return name?:""
    }

    fun getCityFrom(): City?{
        return fromCity
    }
}