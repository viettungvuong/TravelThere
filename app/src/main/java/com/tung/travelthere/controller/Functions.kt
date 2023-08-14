package com.tung.travelthere.controller

import com.tung.travelthere.R
import com.tung.travelthere.objects.City

fun getResourceIdFromName(resourceName: String): Int {
    try {
        val field = R.drawable::class.java.getField(resourceName)
        return field.getInt(null)
    } catch (e: Exception) {
        return 0
    }
}
