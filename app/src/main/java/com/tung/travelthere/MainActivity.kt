package com.tung.travelthere

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.palette.graphics.Palette
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter.State.Empty.painter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.android.libraries.places.api.Places
import com.tung.travelthere.controller.*
import com.tung.travelthere.objects.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Places.initialize(applicationContext, "AIzaSyC6qQqaqfHQMN2FXMX301c6LQ2rfjKPg2E")

        AppController.placeViewModel = PlaceAutocompleteViewModel(applicationContext)

        //phần initialize cho city (initialize location)
        City.getSingleton().setName("Ho Chi Minh City")
        City.getSingleton().setCountry("Vietnam")
        val t1 = TouristPlace("Ben Thanh Market", Position(1f, 1f), "Ho Chi Minh City")
        t1.setDrawableName("benthanh")
        t1.categories.add(Category.ATTRACTION)
        t1.categories.add(Category.SHOPPING)
        t1.reviews.add(Review("viettung", "Good place", Date(), 10))

        val t2 = TouristPlace("The Cathedral", Position(1f, 1f), "Ho Chi Minh City")
        t2.setDrawableName("nhathoducba")
        t2.categories.add(Category.ATTRACTION)

        City.getSingleton().recommendationsRepository.recommendations.add(t1)
        City.getSingleton().recommendationsRepository.recommendations.add(t2)

        AppController.favoriteList.add(t2)


        setContent {
            Home(this)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Home(context: Context) {
    val tabTitles = listOf("Nearby", "Recommended", "Tourist attractions")
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        bottomBar = {
            BottomAppBar(cutoutShape = CircleShape, backgroundColor = colorBlue) {
                IconButton(onClick = { /* Handle click action */ }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        tint = Color.White,
                        contentDescription = "Home"
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(onClick = {
                    val intent = Intent(context, FavoritePage::class.java)
                    context.startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        tint = Color.White,
                        contentDescription = "Favorite"
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(onClick = {
                    val intent = Intent(context, SuggestPlace::class.java)
                    context.startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        tint = Color.White,
                        contentDescription = "Add"
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Handle FAB click */ },
                backgroundColor = Color(android.graphics.Color.parseColor("#b3821b"))
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        isFloatingActionButtonDocked = true,
        scaffoldState = scaffoldState
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.weight(0.5f)
            ) {
                CityIntroduction(context, City.getSingleton()) //phần thông tin thành phố hiện tại
            }

            Box(
                modifier = Modifier.weight(1.5f)
            ) {
                Column {
                    tabLayout(
                        pagerState = pagerState,
                        tabTitles = tabTitles,
                        coroutineScope = coroutineScope
                    )

                    HorizontalPager(state = pagerState, pageCount = tabTitles.size) { page ->

                        when (page) {
                            0 -> LocalRecommended(context, city = City.getSingleton())
                            1 -> LocalRecommended(context, city = City.getSingleton())
                        }
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun tabLayout(pagerState: PagerState, tabTitles: List<String>, coroutineScope: CoroutineScope) {
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = colorBlue,
        contentColor = Color.White,
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(50))
            .shadow(AppBarDefaults.TopAppBarElevation)
            .zIndex(10f),
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = (pagerState.currentPage == index), //current index có phải là index
                onClick = {
                    run {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                index
                            )
                        }
                    }
                },
                text = { Text(text = title) }
            )
        }
    }
}


@Composable
fun CityIntroduction(context: Context, city: City) {
    //phần cho city
    var imageUrl by remember { mutableStateOf<String?>(null) }

    //background color của text
    var textBgColor by remember { mutableStateOf(Color.Gray) }

    //bitmap hình nền
    var bitmap: Bitmap? = null

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(imageUrl) {
        coroutineScope.launch {
            imageUrl = city.fetchImageUrl()
        }

        bitmap = if (imageUrl == null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                BitmapFactory.decodeStream(URL(imageUrl).openConnection().getInputStream())
            }
        }

        if (bitmap != null) {
            textBgColor = colorFromImage(bitmap!!) //đặt màu nền phần box gần với màu cái ảnh
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ImageFromUrl(url = imageUrl ?: "", contentDescription = null)
    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,

        ) {
        Box(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                    bottom = 8.dp,
                )
                .background(textBgColor)
        ) {
            Text(
                text = "${city.getName()}",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

    }
}


//trang local recommended
@Composable
fun LocalRecommended(context: Context, city: City) {
    var listState by remember { mutableStateOf(ArrayList<Location>()) }

    listState.clear()
    listState.addAll(city.recommendationsRepository.recommendations)

    Column() {
        LazyRow(modifier = Modifier.padding(15.dp)) {
            itemsIndexed(Category.values()) { index, category -> //tương tự xuất ra location adapter
                categoryView(category, colorBlue, true)
            }
        }


        LazyRow {
            itemsIndexed(listState) { index, location -> //tương tự xuất ra location adapter
                SneakViewPlace(context, location)
            }
        }
    }

}


@Composable
fun NearbyPlaces(userPos: Position, city: City) { //đề xuất địa điểm gần với nơi đang đứng

}

@Composable
fun Weather(city: City) {

}


@Composable
fun Transportation(city: City) {

}

@Composable
fun InteractLocal(city: City) {

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Home(LocalContext.current)
}