package com.tung.travelthere

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tung.travelthere.controller.*
import com.tung.travelthere.objects.Category
import com.tung.travelthere.objects.PlaceLocation
import com.tung.travelthere.ui.theme.TravelThereTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt


//sẽ có thêm filter theo tên thành phố, theo loại category
class FavoritePage : ComponentActivity() {
    lateinit var searchViewModel: SearchViewModel
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)

        searchViewModel= SearchViewModel()

        setContent {
            FavoriteList(this)
        }
    }

    override fun onStart() {
        super.onStart()
        runBlocking {
            AppController.Favorites.getSingleton().refreshFavorites()
        }
    }

    @OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
    @Composable
    fun FavoriteList(activity: Activity) {
        var listState = remember { mutableStateListOf<PlaceLocation>() }
        var originalState = remember { mutableStateListOf<PlaceLocation>() }
        var chosenState = remember { mutableStateOf(mutableSetOf<Category>()) }

        var lazyListState = rememberLazyListState()
        var coroutineScope = rememberCoroutineScope()

        LaunchedEffect(originalState){
            coroutineScope.launch {
                originalState.clear()
                originalState.addAll(AppController.Favorites.getSingleton().refreshFavorites())
                listState.clear()
                listState.addAll(originalState)
            }
        }

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

                Text(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 25.dp), text="Favorites", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)


                LazyRow(modifier = Modifier.padding(15.dp)) {
                    itemsIndexed(Category.values()) { index, category -> //tương tự xuất ra location adapter
                        categoryView(category, colorBlue, true, listState, chosenState, originalState)
                    }
                }

                if (listState.isNotEmpty()){
                    LazyColumn(
                        state= lazyListState,
                        modifier = Modifier.padding(15.dp)
                    ) {
                        itemsIndexed(listState) { index, location ->
                            val currentItem by rememberUpdatedState(newValue = location)
                            val dismissState = rememberDismissState(
                                confirmStateChange = {
                                    if (it==DismissValue.DismissedToEnd||it==DismissValue.DismissedToStart) {
                                        listState.remove(currentItem)
                                        AppController.Favorites.getSingleton().removeFavorite(currentItem)
                                        //xoá khỏi favorite
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismiss(state = dismissState, background = {SwipeBackground(dismissState)}
                                , dismissThresholds = {
                                    FractionalThreshold(0.4f)
                                }
                                , modifier = Modifier
                                    .animateItemPlacement()
                                , dismissContent = {SneakViewPlaceLong(context = LocalContext.current, location = location)})

                        }
                    }
                }
                else{
                    Text(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 25.dp), text="List is empty", textAlign = TextAlign.Center)
                }

            }
        }
    }


}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun SwipeBackground(dismissState: DismissState) {

    val color by animateColorAsState(
        when (dismissState.targetValue) {
            DismissValue.Default -> Color.Transparent
            DismissValue.DismissedToEnd -> Color.Red
            DismissValue.DismissedToStart -> Color.Red

        }
    )
    val alignment = Alignment.CenterEnd

    val icon = Icons.Default.Delete

    val scale by animateFloatAsState(
        if (dismissState.targetValue == DismissValue.Default) 0.5f else 1f
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = alignment
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier
                .scale(scale)
                .size(50.dp),
            tint =  when (dismissState.targetValue) {
                DismissValue.Default -> Color.Black
                DismissValue.DismissedToEnd -> Color.White
                DismissValue.DismissedToStart -> Color.White
            }
        )
    }
}


