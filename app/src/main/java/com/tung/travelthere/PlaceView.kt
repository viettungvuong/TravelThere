package com.tung.travelthere

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.tung.travelthere.controller.*
import com.tung.travelthere.objects.Category
import com.tung.travelthere.objects.PlaceLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PlaceView : ComponentActivity() {
    lateinit var location: PlaceLocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        location = intent.getSerializableExtra("location") as PlaceLocation

        setContent {
            viewPlace(location = location)
        }
    }

    override fun onResume() {
        super.onResume()

        runBlocking{
            location.reviewRepository.refreshReviews() //lấy các review đánh giá
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun viewPlace(location: PlaceLocation) {
        var imageUrl by remember { mutableStateOf<String?>(null) }
        val tabTitles = listOf("About", "Reviews", "Discussions")
        val pagerState = rememberPagerState(initialPage = 0)
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(imageUrl){
            coroutineScope.launch {
                imageUrl = location.fetchImageUrl()
            }
        }


        MaterialTheme {
            ConstraintLayout {
                val (image, button, detail) = createRefs()

                Box(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .fillMaxWidth()
                        .constrainAs(image) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        }
                ) {
                    ImageFromUrl(url = imageUrl ?: "", contentDescription = null, 0.0)
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .constrainAs(button) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                        }
                ) {
                    FloatingActionButton(
                        onClick = { finish() },
                        backgroundColor = Color.White,
                        content = {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Red
                            )
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                        )
                        .constrainAs(detail) {
                            top.linkTo(image.bottom, margin = 200.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        }
                ) {
                    Column {
                        tabLayout(
                            pagerState = pagerState,
                            tabTitles = tabTitles,
                            coroutineScope = coroutineScope
                        )

                        HorizontalPager(state = pagerState, pageCount = tabTitles.size) { page ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                when (page) {
                                    0 -> aboutPlace(location)
                                    1 -> reviewsPlace(location)
                                }
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
            backgroundColor = Color.White,
            contentColor = Color.Red,
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
    fun aboutPlace(location: PlaceLocation) {
        var indexFav by remember { mutableStateOf(AppController.Favorites.getSingleton().isFavorite(location)) }
        var iconVector by remember { mutableStateOf(Icons.Default.Favorite) }

        LaunchedEffect(indexFav){
            iconVector = if (!indexFav) {
                Icons.Default.Favorite
            } else {
                Icons.Default.Delete
            }
        }

        Text(
            text = location.getName(),
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "Address",
                tint = Color.Black
            )

            Spacer(modifier = Modifier.width(20.dp))

            Text(text = "18 đường số 7")
        }



        Column(
            modifier = Modifier.padding(vertical = 20.dp)
        ) {
            Text(
                text = "Categories",
                fontWeight = FontWeight.Bold
            )

            LazyRow {
                itemsIndexed(location.categories.toTypedArray()) { index, category -> //tương tự xuất ra location adapter
                    categoryView(category, Color.Red, false)
                }
            }
        }

        Button(
            onClick = {
                indexFav = if (!indexFav) {
                    AppController.Favorites.getSingleton().addFavorite(location)
                    Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
                    true
                }//thêm địa điểm vào favorite
                else{
                    AppController.Favorites.getSingleton().removeFavorite(location)
                    Toast.makeText(this, "Remove from favorites", Toast.LENGTH_SHORT).show()
                    false
                }
            }
            ,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF36D72)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
            ) {
                Icon(
                    imageVector = iconVector!!,
                    contentDescription = "Favorite",
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(20.dp))

                if (!indexFav) {
                    Text(text = "Add to favorites", color = Color.White)
                } else {
                    Text(text = "Remove from favorites", color = Color.White)
                }
            }
        }

        Button(
            onClick = {
                      //mở google maps chỉ đường
            }
            ,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Favorite",
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(20.dp))

                Text(text = "Navigate", color = Color.White)
            }
        }

    }

    //phần xem những đánh giá về địa điểm
    @Composable
    fun reviewsPlace(location: PlaceLocation) {
        var listState = remember { mutableStateOf(listOf<Review>()) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(listState) {
            coroutineScope.launch {
                listState.value =
                    location.reviewRepository.refreshReviews()
                Log.d("list state value",listState.value.toString())
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn() {
                itemsIndexed(listState.value.toTypedArray()) { index, review ->
                    reviewLayout(review = review)
                }
            }
        }
    }

    @Composable
    fun reviewLayout(review: Review) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 20.dp
                ),
            elevation = 10.dp
        ) {
            Column() {
                //sẽ có mục cho biết người dùng này là local hay foreigner
                Row(modifier = Modifier.padding(10.dp)) {

                    Text(text = review.userId, fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    Box(modifier = Modifier.padding(horizontal = 10.dp)) {
                        Text(text = formatter.format(review.time), fontWeight = FontWeight.Light)
                    }
                }

                Box(modifier = Modifier.padding(10.dp)) {
                    Text(text = review.content, fontSize = 15.sp)
                }

                var color: Color? = null
                if (review.score in 0..4) {
                    color = Color.Red
                } else if (review.score in 5..8) {
                    color = Color(0xffa88132)
                } else {
                    color = Color(0xff326e14)
                }
                Box(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = review.score.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = color
                    )
                }

            }
        }
    }

    @Composable
    fun discussionsPlace(location: PlaceLocation) {

    }

}