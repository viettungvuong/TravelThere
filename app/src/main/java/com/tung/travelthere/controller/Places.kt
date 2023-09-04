package com.tung.travelthere

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.tung.travelthere.objects.Category
import com.tung.travelthere.objects.Position
import java.util.*


data class AutocompleteResult(
    val address: String,
    val placeId: String,
)

class PlaceAutocompleteViewModel(private val context: Context): ViewModel() {
    val placesClient = Places.createClient(context)

    var placeSuggestions= mutableStateListOf<AutocompleteResult>() //chứa autocomplete

    var currentName by mutableStateOf("")
    var currentAddress by mutableStateOf("")
    var currentCity by mutableStateOf("")
    var currentPos by mutableStateOf(Position(0.0,0.0))
    var currentCategories = mutableStateListOf<Category>()

    fun fetchPlaceSuggestions(query: String) {
        placeSuggestions.clear()

        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setTypesFilter(listOf(PlaceTypes.ESTABLISHMENT))
            .setSessionToken(token)
            .setCountry("VN") //app dùng cho Việt Nam ở thời điểm hiện tại
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
        retrieveCategories(result)

        val placeFields = listOf(Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(result.placeId, placeFields)
        placesClient.fetchPlace(request)
            .addOnSuccessListener {
                if (it != null) {
                    val geocoder = Geocoder(context, Locale.getDefault())

                    val latLng = it.place.latLng //lấy toạ độ
                    currentPos = Position(latLng.latitude,latLng.longitude)

                    //lấy địa chỉ
                    val addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1)
                    if (addresses!=null){
                        currentAddress = addresses[0].getAddressLine(0)
                    }

                    //lấy tên thành phố, tỉnh
                    if (addresses != null) {
                        currentCity = if (addresses[0].locality==null){
                            addresses[0].adminArea //lấy tên thành phố theo tên tỉnh
                        } else{
                            addresses[0].locality
                        }
                        if (currentCity=="Thành phố Hồ Chí Minh"){
                            currentCity="Ho Chi Minh City"
                        }
                    }

                }
            }
            .addOnFailureListener {
                    exception -> Log.d("Lỗi",exception.message.toString())
            }

    }

    private fun retrieveCategories(result: AutocompleteResult){
        val placeFields = listOf(Place.Field.TYPES)
        val request = FetchPlaceRequest.newInstance(result.placeId, placeFields)
        placesClient.fetchPlace(request)
            .addOnSuccessListener {
                val placeTypes = it.place.types
                Log.d("place types",(placeTypes==null).toString())
                // categories là set nên là cũng sẽ tự loại bỏ trùng lặp
                if (placeTypes != null) {
                    if (placeTypes.contains(Place.Type.RESTAURANT)||placeTypes.contains(Place.Type.BAKERY)||placeTypes.contains(Place.Type.FOOD)) {
                        currentCategories.add(Category.RESTAURANT)
                    }
                    if (placeTypes.contains(Place.Type.TOURIST_ATTRACTION)||placeTypes.contains(Place.Type.MUSEUM)||placeTypes.any { it.toString().contains("PARK", ignoreCase = true)}) {
                        currentCategories.add(Category.ATTRACTION)
                    }
                    if (placeTypes.contains(Place.Type.NATURAL_FEATURE)||placeTypes.contains(Place.Type.PARK)) {
                        currentCategories.add(Category.NATURE)
                    }
                    if (placeTypes.any { it.toString().contains("STORE", ignoreCase = true) || it.toString().contains("SHOP", ignoreCase = true) }) {
                        currentCategories.add(Category.SHOPPING)
                    }
                    if (placeTypes.contains(Place.Type.BAR)) {
                        currentCategories.add(Category.BAR)
                    }
                    if (placeTypes.contains(Place.Type.HOSPITAL)||placeTypes.contains(Place.Type.BANK)||placeTypes.contains(Place.Type.PHARMACY)||placeTypes.contains(Place.Type.EMBASSY)){
                        currentCategories.add(Category.NECESSITY)
                    }
                }

                //chưa thêm category nào hết
                if (currentCategories.isEmpty()){
                    currentCategories.add(Category.OTHERS)
                }
            }.addOnFailureListener {
                    exception -> Log.d("Lỗi",exception.message.toString())
            }
    }

    override fun onCleared() {
        currentName = ""
        currentCity = ""
        currentPos = Position(0.0,0.0)
        currentAddress = ""
        currentCategories.clear()

        super.onCleared()
    }

}