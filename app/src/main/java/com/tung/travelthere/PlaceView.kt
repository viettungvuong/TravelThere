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
import androidx.lifecycle.ViewModel
import com.tung.travelthere.controller.*
import com.tung.travelthere.objects.Category
import com.tung.travelthere.objects.PlaceLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

val reviewFilters = listOf("0 - 4", "5 - 8", "9 - 10")
val colorFirst = Color.Red
val colorSecond = Color(0xffa88132)
val colorThird = Color(0xff326e14)

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

        runBlocking {
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

        LaunchedEffect(imageUrl) {
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
        var indexFav by remember {
            mutableStateOf(
                AppController.Favorites.getSingleton().isFavorite(location)
            )
        }
        var iconVector by remember { mutableStateOf(Icons.Default.Favorite) }

        LaunchedEffect(indexFav) {
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
                else {
                    AppController.Favorites.getSingleton().removeFavorite(location)
                    Toast.makeText(this, "Remove from favorites", Toast.LENGTH_SHORT).show()
                    false
                }
            },
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
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = colorThird),
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

    var chosenState by mutableStateOf(-1)

    //phần xem những đánh giá về địa điểm
    @Composable
    fun reviewsPlace(location: PlaceLocation) {
        var listState = remember { mutableStateOf(mutableListOf<Review>()) }
        var originalState = remember { mutableStateOf(mutableListOf<Review>()) }

        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(originalState) {
            coroutineScope.launch {
                originalState.value =
                    location.reviewRepository.refreshReviews() as MutableList<Review>
                listState.value = originalState.value
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in reviewFilters.indices) {
                        filterReviewEach(i, listState, originalState)
                    }
                }
            }

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
                    color = colorFirst
                } else if (review.score in 5..8) {
                    color = colorSecond
                } else {
                    color = colorThird
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
    private fun filterReviewEach(
        index: Int, reviewState: MutableState<MutableList<Review>>,
        originalState: MutableState<MutableList<Review>>
    ) {
        var chosenIndex by remember { mutableStateOf(-1) }

        LaunchedEffect(chosenState){
            chosenIndex = chosenState
        }

        Box(modifier = Modifier
            .padding(5.dp)
            .border(
                width = if (chosenIndex==index) 1.dp else 0.dp,
                color = if (chosenIndex==index) Color(0xff365875) else Color.Transparent,
                shape = RoundedCornerShape(4.dp),
            )
            .padding(10.dp)
            .clickable {
                for (i in reviewFilters.indices) {
                    if (i == index)
                        continue
                }
                if (chosenState==index){
                    chosenState = -1 //bỏ chọn
                    reviewState!!.value = originalState!!.value
                } else{
                    chosenState = index //chọn
                    reviewState!!.value = originalState!!.value.filter {
                        val filterBool = (
                                when (index) {
                                    0 -> it.score in 0..4
                                    1 -> it.score in 5..8
                                    else -> it.score in 9..10
                                })

                        filterBool
                    } as MutableList<Review>

                }


            }) {
            Row() {
                Text(
                    text = reviewFilters[index],
                    color = if (index==0) {
                        colorFirst
                    } else if (index==1) {
                        colorSecond
                    } else {
                        colorThird
                    }
                )
            }
        }
    }

    //nếu đã up review rồi thì hiện review của người dùng và cho chỉnh sửa
    //nếu chưa thì cho phép tạo review
    @Composable
    private fun yourReview() {

    }


    @Composable
    private fun discussionsPlace(location: PlaceLocation) {

    }

}