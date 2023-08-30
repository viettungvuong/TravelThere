package com.tung.travelthere

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tung.travelthere.controller.AppController
import com.tung.travelthere.controller.SneakViewPlaceLong
import com.tung.travelthere.controller.categoryView
import com.tung.travelthere.controller.colorBlue
import com.tung.travelthere.objects.Category
import com.tung.travelthere.ui.theme.TravelThereTheme
//sẽ có thêm filter theo tên thành phố, theo loại category
class FavoritePage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)

        setContent {
            TravelThereTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    FavoriteList("Android",this)
                }
            }
        }
    }
}

@Composable
fun FavoriteList(name: String, activity: Activity) {
    val lazyListState = rememberLazyListState()

    MaterialTheme {

        Column {
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

            LazyRow(modifier = Modifier.padding(15.dp)) {
                itemsIndexed(Category.values()) { index, category -> //tương tự xuất ra location adapter
                    categoryView(category, colorBlue, true)
                }
            }

            LazyColumn(state = lazyListState, modifier = Modifier.padding(15.dp)){
                itemsIndexed(AppController.Favorites.getSingleton().getList().toTypedArray()) { index, location -> //tương tự xuất ra location adapter
                    SneakViewPlaceLong(context = LocalContext.current, location = location)
                }
            }
        }
    }


}

