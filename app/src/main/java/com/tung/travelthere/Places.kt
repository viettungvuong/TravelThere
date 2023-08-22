package com.tung.travelthere

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest

class PlaceAutocompleteViewModel(private val context: Context): ViewModel() {
    private val placesClient = Places.createClient(context)

    val placeSuggestions: MutableState<List<AutocompletePrediction>> = mutableStateOf(emptyList())

    fun fetchPlaceSuggestions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                Log.d("Lấy thành công","Lấy thành công")
                placeSuggestions.value = response.autocompletePredictions
            }
            .addOnFailureListener { exception ->
                Log.d("Lỗi","Lỗi khi lấy map")
            }
    }
}