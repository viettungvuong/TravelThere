package com.tung.travelthere

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tung.travelthere.controller.SearchBar
import com.tung.travelthere.controller.categoryView
import com.tung.travelthere.controller.colorBlue
import com.tung.travelthere.objects.Category
import com.tung.travelthere.objects.City
import com.tung.travelthere.objects.PlaceLocation
import com.tung.travelthere.ui.theme.TravelThereTheme
import kotlinx.coroutines.launch

class SearchPlace : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelThereTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SearchPage(City.getSingleton(), this)
                }
            }
        }
    }
}

@Composable
fun SearchPage(city: City, activity: Activity) {
    var listState by remember { mutableStateOf(setOf<PlaceLocation>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(listState) {
        coroutineScope.launch {
            listState = city.recommendationsRepository.refreshRecommendations()
            Log.d("list state add", listState.size.toString())
        }
    }

    MaterialTheme() {
        Column() {
            Box(
                modifier = Modifier
                    .size(32.dp)
            ) {
                FloatingActionButton(
                    onClick = { activity.finish() },
                    backgroundColor = Color.White,
                    content = {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colorBlue
                        )
                    }
                )
            }

            SearchBar(available = listState, context = activity)

            LazyRow(modifier = Modifier.padding(15.dp)) {
                itemsIndexed(Category.values()) { index, category -> //tương tự xuất ra location adapter
                    categoryView(category, colorBlue, true)
                }
            }
        }
    }
}
