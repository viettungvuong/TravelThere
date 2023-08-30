package com.tung.travelthere

import android.app.Activity
import android.content.Context
import android.os.Bundle
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.tung.travelthere.controller.CategoryChosenViewModel
import com.tung.travelthere.controller.SneakViewPlaceLong
import com.tung.travelthere.controller.categoryView
import com.tung.travelthere.controller.colorBlue
import com.tung.travelthere.objects.Category
import com.tung.travelthere.objects.City
import com.tung.travelthere.objects.PlaceLocation
import com.tung.travelthere.ui.theme.TravelThereTheme
import kotlinx.coroutines.launch

lateinit var searchViewModel: SearchViewModel
lateinit var chosenViewModel2: CategoryChosenViewModel
class SearchPlace : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchViewModel = SearchViewModel()
        chosenViewModel2 = CategoryChosenViewModel()

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

class SearchViewModel: ViewModel(){
    var matchedQuery = mutableStateListOf<PlaceLocation>()
}

//thanh tìm kiếm
@Composable
fun SearchBar(
    available: Set<PlaceLocation>, searchViewModel: SearchViewModel, context: Context
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }

    Column {
        TextField(value = searchQuery,
            onValueChange = { newString ->
                searchQuery = newString

                searchViewModel.matchedQuery.removeAll(searchViewModel.matchedQuery)

                if (newString.text.isNotBlank()) {
                    searchViewModel.matchedQuery.addAll(available.filter {
                        it.getName().contains(newString.text, ignoreCase = true)
                    }.sortedBy {
                        val similarity = it.getName()
                            .commonPrefixWith(newString.text).length.toDouble() / newString.text.length
                        //các từ nào tương đồng nhất sẽ được xếp trên đầu
                        similarity
                    })
                }

            },
            textStyle = TextStyle(fontSize = 17.sp),
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 5.dp)
                .background(Color(0xFFE7F1F1), RoundedCornerShape(16.dp)),
            placeholder = { Text(text = "Search") },
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                backgroundColor = Color.Transparent,
                cursorColor = Color.DarkGray
            )
        )
    }
}

@Composable
fun SearchPage(city: City, activity: Activity) {
    var listState by remember { mutableStateOf(setOf<PlaceLocation>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(listState) {
        coroutineScope.launch {
            listState = city.recommendationsRepository.refreshRecommendations()
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

            SearchBar(available = listState, context = activity, searchViewModel = searchViewModel)

            LazyRow(modifier = Modifier.padding(15.dp)) {
                itemsIndexed(Category.values()) { index, category -> //tương tự xuất ra location adapter
                    categoryView(category, colorBlue, true)
                }
            }

            LazyColumn(){
                items(searchViewModel.matchedQuery){
                    location ->
                    SneakViewPlaceLong(context = activity, location = location, hasImage = false)
                }
            }
        }
    }
}
