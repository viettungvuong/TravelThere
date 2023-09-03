package com.tung.travelthere

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.tung.travelthere.controller.*

import com.tung.travelthere.objects.Category
import com.tung.travelthere.objects.City
import com.tung.travelthere.objects.PlaceLocation
import com.tung.travelthere.objects.Restaurant
import com.tung.travelthere.ui.theme.TravelThereTheme
import kotlinx.coroutines.launch



class SearchPlace : ComponentActivity() {
    lateinit var searchViewModel: SearchViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchViewModel = SearchViewModel()


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

    //tìm kiếm nhà hàng theo tên món ăn mà nhà hàng bán
    fun searchRestaurantByDish(searchViewModel: SearchViewModel, newString: String, available: Set<PlaceLocation>){
        searchViewModel.matchedQuery.clear()
        if (newString.isNotBlank()){
            searchViewModel.matchedQuery += available.filter {
                val hasDish = (
                        if (it is Restaurant){
                            //tìm trong các dish của nhà hàng có món nào có chứa cụm từ hiện tại hay kh
                            val dishes = it.getSpecializedDish().sortedBy {
                                    dish -> dish.name //sắp xếp các dish theo tên
                            }.filter { dish -> dish.name.contains(newString,ignoreCase = false) }

                            dishes.isNotEmpty()
                        }
                        else
                            false
                        )
                it.categories.contains(Category.RESTAURANT)&&
                        hasDish
            }
        }
        searchViewModel.originalMatchedQuery.clear()
        searchViewModel.originalMatchedQuery+=searchViewModel.matchedQuery //chuẩn bị cho cái categoryview
    }





    @Composable
    fun SearchPage(city: City, activity: Activity) {
        var listState by remember { mutableStateOf(setOf<PlaceLocation>()) }
        var chosenState = remember { mutableStateOf(mutableSetOf<Category>()) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(listState) {
            coroutineScope.launch {
                listState = city.locationsRepository.refreshLocations()
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

                Text(modifier = Modifier.fillMaxWidth().padding(vertical = 25.dp), text="Search", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)


                SearchBar(available = listState, searchViewModel = searchViewModel, modifier = Modifier.padding(10.dp))

                LazyRow(modifier = Modifier.padding(15.dp)) {
                    itemsIndexed(Category.values()) { index, category -> //tương tự xuất ra location adapter
                        categoryView(category, colorBlue, true, searchViewModel, chosenState)
                    }
                }

                LazyColumn() {
                    items(searchViewModel.matchedQuery) { location ->
                        SneakViewPlaceLong(context = activity, location = location, hasImage = false)
                    }
                }
            }
        }
    }

}

fun search(searchViewModel: SearchViewModel,newString: String,available: Set<PlaceLocation>){
    searchViewModel.matchedQuery.clear()
    if (newString.isNotBlank()){
        searchViewModel.matchedQuery += available.filter {
            it.getName().contains(newString, ignoreCase = true)
        }.sortedBy {
            val similarity = it.getName()
                .commonPrefixWith(newString).length.toDouble() / newString.length
            similarity //sắp xếp theo độ tương đồng so với từ đang nhập
        }
    }
    searchViewModel.originalMatchedQuery.clear()
    searchViewModel.originalMatchedQuery+=searchViewModel.matchedQuery //chuẩn bị cho cái categoryview

}



