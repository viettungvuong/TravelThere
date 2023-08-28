package com.tung.travelthere

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.tung.travelthere.objects.Place
import com.tung.travelthere.objects.Position
import java.util.*


data class AutocompleteResult(
    val address: String,
    val placeId: String,
)

class PlaceAutocompleteViewModel(private val context: Context): ViewModel() {
    private val placesClient = Places.createClient(context)

    var placeSuggestions= mutableStateListOf<AutocompleteResult>() //chứa autocomplete

    var currentName by mutableStateOf("")
    var currentCity by mutableStateOf("")
    var currentPos by mutableStateOf(Position(0f,0f))


    fun fetchPlaceSuggestions(query: String) {
        placeSuggestions.clear()

        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setTypesFilter(listOf(PlaceTypes.ESTABLISHMENT))
            .setSessionToken(token)
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                placeSuggestions += response.autocompletePredictions.map {
                    AutocompleteResult(
                        it.getFullText(null).toString(),
                        it.placeId,
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Lỗi",exception.message.toString())
            }
    }


    fun getName(result: AutocompleteResult) {
        val placeFields = listOf(Place.Field.NAME)
        val request = FetchPlaceRequest.newInstance(result.placeId, placeFields)
        placesClient.fetchPlace(request)
            .addOnSuccessListener {
                if (it != null) {
                    currentName = it.place.name?:""
                }
            }
            .addOnFailureListener {
                exception -> Log.d("Lỗi",exception.message.toString())
            }
    }

    fun retrieveOtherInfo(result: AutocompleteResult){
        val placeFields = listOf(Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(result.placeId, placeFields)
        placesClient.fetchPlace(request)
            .addOnSuccessListener {
                if (it != null) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val latLng = it.place.latLng
                    currentPos = Position(latLng.latitude.toFloat(),latLng.longitude.toFloat())
                    val addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1)
                    if (addresses != null) {
                        currentCity = addresses[0].locality //lấy tên thành phố
                    }
                }
            }
            .addOnFailureListener {
                    exception -> Log.d("Lỗi",exception.message.toString())
            }
    }

}