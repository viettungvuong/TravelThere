package com.tung.travelthere.objects

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.tung.travelthere.controller.AppController
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class WeatherViewModel(context: Context, city: City) : ViewModel() {
    var maxTemp = mutableStateListOf<Float>()
    var minTemp = mutableStateListOf<Float>()
    var conditionImgUrl = mutableStateListOf<String>()

    var currentTemp by mutableStateOf(0f)
    var currentConditionImgUrl by mutableStateOf("")


    init {
        val locStr = city.getName()
        val url =
            "https://api.weatherapi.com/v1/forecast.json?key=50224d22b9804f92a1b94202230309&q=$locStr&days=3&aqi=yes&alerts=yes"

        val requestQueue = Volley.newRequestQueue(context)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    currentTemp = response.getJSONObject("current").getString("temp_c").toFloat()
                    currentConditionImgUrl = "https:"+response.getJSONObject("current").getJSONObject("condition")
                        .getString("icon")

                    for (i in 0..2) {

                        minTemp.add(response.getJSONObject("forecast").getJSONArray("forecastday")
                            .getJSONObject(i).getJSONObject("day").getString("mintemp_c")
                            .toFloat()) //nhiệt độ hiện tại

                        maxTemp.add(response.getJSONObject("forecast").getJSONArray("forecastday")
                            .getJSONObject(i).getJSONObject("day").getString("maxtemp_c")
                            .toFloat()) //nhiệt độ hiện tại

                        conditionImgUrl.add("https:" + response.getJSONObject("forecast").getJSONArray("forecastday")
                                .getJSONObject(i).getJSONObject("day").getJSONObject("condition")
                                .getString("icon")) //hình đại diện cho điều kiện thời tiết
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }) { error ->
            parseVolleyError(error)
            Log.d("TAG weather", error.message!!)
        }

        requestQueue.add(jsonObjectRequest)
    }

    private fun parseVolleyError(error: VolleyError) {
        try {
            val responseBody = String(error.networkResponse.data)
            val data = JSONObject(responseBody)
            val errors = data.getJSONArray("errors")
            val jsonMessage = errors.getJSONObject(0)
            val message = jsonMessage.getString("message")
            Log.d("json error", message)
        } catch (e: JSONException) {
        } catch (error: UnsupportedEncodingException) {
        }
    }
}

//xem thời tiết cho địa điểm
class WeatherViewModelPlace(context: Context, placeLocation: PlaceLocation) : ViewModel() {
    var maxTemp = mutableStateListOf<Float>()
    var minTemp = mutableStateListOf<Float>()
    var conditionImgUrl = mutableStateListOf<String>()

    var currentTemp by mutableStateOf(0f)
    var currentConditionImgUrl by mutableStateOf("")

    init {
        val locStr = placeLocation.getPos().toString()
        val url =
            "https://api.weatherapi.com/v1/forecast.json?key=50224d22b9804f92a1b94202230309&q=$locStr&days=3&aqi=yes&alerts=yes"

        val requestQueue = Volley.newRequestQueue(context)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    currentTemp = response.getJSONObject("current").getString("temp_c").toFloat()
                    currentConditionImgUrl = "https:"+response.getJSONObject("current").getJSONObject("condition")
                        .getString("icon")

                    for (i in 0..2) {

                        minTemp.add(response.getJSONObject("forecast").getJSONArray("forecastday")
                            .getJSONObject(i).getJSONObject("day").getString("mintemp_c")
                            .toFloat()) //nhiệt độ hiện tại

                        maxTemp.add(response.getJSONObject("forecast").getJSONArray("forecastday")
                            .getJSONObject(i).getJSONObject("day").getString("maxtemp_c")
                            .toFloat()) //nhiệt độ hiện tại

                        conditionImgUrl.add("https:" + response.getJSONObject("forecast").getJSONArray("forecastday")
                            .getJSONObject(i).getJSONObject("day").getJSONObject("condition")
                            .getString("icon")) //hình đại diện cho điều kiện thời tiết
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }) { error ->
            parseVolleyError(error)
            Log.d("TAG weather", error.message!!)
        }

        requestQueue.add(jsonObjectRequest)
    }

    private fun parseVolleyError(error: VolleyError) {
        try {
            val responseBody = String(error.networkResponse.data)
            val data = JSONObject(responseBody)
            val errors = data.getJSONArray("errors")
            val jsonMessage = errors.getJSONObject(0)
            val message = jsonMessage.getString("message")
            Log.d("json error", message)
        } catch (e: JSONException) {
        } catch (error: UnsupportedEncodingException) {
        }
    }
}
