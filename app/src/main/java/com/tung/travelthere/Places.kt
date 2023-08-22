package com.tung.travelthere

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse

class PlaceAutocompleteViewModel(private val context: Context): ViewModel() {
    private val placesClient = Places.createClient(context)

    var placeSuggestions: MutableState<List<AutocompletePrediction>> = mutableStateOf(emptyList())

    fun fetchPlaceSuggestions(query: String) {
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                Log.d("response",response.toString())
                placeSuggestions.value = response.autocompletePredictions
                Log.d("placesuggestions size",placeSuggestions.value.size.toString())
            }
            .addOnFailureListener { exception ->
                Log.d("Lỗi",exception.message.toString())
            }
    }
}