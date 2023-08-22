package com.tung.travelthere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.tung.travelthere.controller.AppController
import com.tung.travelthere.objects.Location

class SuggestPlace : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            suggestPlace()
        }
    }


    @Composable
    fun suggestPlace(){
        var searchPlace by remember { mutableStateOf("") }
        val placeSuggestions by AppController.placeViewModel.placeSuggestions

        MaterialTheme{
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Text(
                    text = "Find your place"
                )

                Spacer(
                    Modifier.height(20.dp)
                )

                OutlinedTextField(
                    value = searchPlace,
                    onValueChange = {
                        searchPlace = it
                        AppController.placeViewModel.fetchPlaceSuggestions(searchPlace)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                LazyColumn {
                    items(placeSuggestions) { suggestion ->
                        Text(text = suggestion.getFullText(null).toString())
                    }
                }

            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        suggestPlace()
    }
}