package com.tung.travelthere

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage
import com.tung.travelthere.controller.*
import com.tung.travelthere.objects.PlaceLocation
import com.tung.travelthere.objects.Review
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

val reviewFilters = listOf("0 - 4", "5 - 8", "9 - 10")
val colorFirst = Color.Red
val colorSecond = Color(0xffa88132)
val colorThird = Color(0xff326e14)

class PlaceView : ComponentActivity() {
    lateinit var location: PlaceLocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        location = intent.getSerializableExtra("location") as PlaceLocation

        val imageUrl = intent.getStringExtra("image url")
        location.afterDeserialization(imageUrl) //do imageurl không serializable nên ta đem nó qua riêng

        setContent {
            viewPlace(location = location)
        }
    }

    override fun onStart() {
        super.onStart()

        runBlocking {
            location.reviewRepository.refreshReviews(refreshNow = true) //lấy các review đánh giá
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
    @Composable
    fun viewPlace(location: PlaceLocation) {
        var imageUrl by remember { mutableStateOf<String?>(null) }

        val tabTitles = listOf("About", "Reviews", "Suggestions")
        val pagerState = rememberPagerState(initialPage = 0)
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(location.imageUrl) {
            imageUrl = location.imageUrl
            Log.d("location img url",location.imageUrl?:"")
        }

        Scaffold { padding ->
            Column {

                Box(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .fillMaxWidth()
                ) {
                    if (imageUrl!=null){
                        ImageFromUrl(url = imageUrl!!, contentDescription = null, 0.0)
                    }

                    FloatingActionButton(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(32.dp),
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


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                        )
                ) {
                    tabLayout(
                        pagerState = pagerState,
                        tabTitles = tabTitles,
                        coroutineScope = coroutineScope,
                        contentColor = Color.Red
                    )

                    HorizontalPager(state = pagerState, pageCount = tabTitles.size) { page ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .weight(1f)
                        ) {
                            when (page) {
                                0 -> aboutPlace(location)
                                1 -> reviewsPlace(location)
                                2 -> suggestionsPlace(location)
                            }
                        }
                    }
                }


            }
        }
    }


    @Composable
    private fun aboutPlace(location: PlaceLocation) {
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

        if (location.address != null) {
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

                Text(text = location.address!!)
            }
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
                val intent = Intent(this, FavoritePage::class.java)
                startActivity(intent)
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
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setPackage("com.google.android.apps.maps")
                intent.data =
                    Uri.parse("google.navigation:q=${location.getPos().lat},${location.getPos().long}")

                intent.putExtra("mode", "d") // Driving (default)
                startActivity(intent)

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

    //phần review
    private var chosenState by mutableStateOf(-1)
    private val reviewTotalScoreViewModel = ReviewTotalScoreViewModel()
    private val chosenScoreViewModel = ChosenScoreViewModel()

    class ReviewTotalScoreViewModel() : ViewModel() {
        var totalScore by mutableStateOf(0.0)
    }

    class ChosenScoreViewModel() : ViewModel() {
        var chosenScore by mutableStateOf(0)
    }

    //phần xem những đánh giá về địa điểm
    @Composable
    @OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
    private fun reviewsPlace(location: PlaceLocation) {
        //giao diện điểm số review
        @Composable
        fun reviewScoreText(modifier: Modifier, score: Double) {
            var color= when (score) {
                in 0.0..4.0 -> {
                    colorFirst
                }
                in 4.0..8.0 -> {
                    colorSecond
                }
                else -> {
                    colorThird
                }
            }
            Box(modifier = modifier) {
                Text(
                    text = score.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = color
                )
            }
        }

        @Composable
        fun reviewScoreText(modifier: Modifier, score: Int) {
            var color = if (score in 0..4) {
                colorFirst
            } else if (score in 5..8) {
                colorSecond
            } else {
                colorThird
            }
            Box(modifier = modifier) {
                Text(
                    text = score.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = color
                )
            }
        }

        //giao diện của mỗi review
        @Composable
        fun reviewLayout(review: Review) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 2.dp
                    ),
                elevation = 10.dp
            ) {
                Row() {
                    //sẽ có mục cho biết người dùng này là local hay foreigner
                    Column() {
                        Row(modifier = Modifier.padding(10.dp)) {

                            Text(text = review.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)

                            Box(modifier = Modifier.padding(horizontal = 10.dp)) {
                                Text(
                                    text = formatter.format(review.time),
                                    fontWeight = FontWeight.Light
                                )
                            }
                        }

                        Box(modifier = Modifier.padding(10.dp)) {
                            Text(text = review.content, fontSize = 15.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))

                    reviewScoreText(
                        score = review.score,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )

                }
            }
        }


        //phần lọc đánh giá
        @Composable
        fun filterReviewEach(
            index: Int, reviewState: SnapshotStateList<Review>,
            originalState: SnapshotStateList<Review>
        ) {
            var chosenIndex by remember { mutableStateOf(-1) }

            LaunchedEffect(chosenState) {
                chosenIndex = chosenState
            }

            Box(modifier = Modifier
                .padding(5.dp)
                .border(
                    width = if (chosenIndex == index) 1.dp else 0.dp,
                    color = if (chosenIndex == index) Color(0xff365875) else Color.Transparent,
                    shape = RoundedCornerShape(4.dp),
                )
                .padding(10.dp)
                .clickable {
                    for (i in reviewFilters.indices) {
                        if (i == index)
                            continue
                    }
                    reviewState.clear()
                    if (chosenState == index) {
                        chosenState = -1 //bỏ chọn
                        reviewState.addAll(originalState)
                    } else {
                        chosenState = index //chọn
                        reviewState.addAll(originalState.filter {
                            val filterBool = (
                                    when (index) {
                                        0 -> it.score in 0..4
                                        1 -> it.score in 5..8
                                        else -> it.score in 9..10
                                    })

                            filterBool
                        })

                    }


                }) {
                Row() {
                    Text(
                        text = reviewFilters[index],
                        color = if (index == 0) {
                            colorFirst
                        } else if (index == 1) {
                            colorSecond
                        } else {
                            colorThird
                        }
                    )
                }
            }
        }

        //dropdown menu chọn điểm số đang chọn
        @OptIn(ExperimentalMaterialApi::class)
        @Composable
        fun DropDownMenu(
            modifier: Modifier,
            options: List<String>,
            selectedItemViewModel: ChosenScoreViewModel
        ) {
            var expanded by remember { mutableStateOf(false) }

            Box(
                modifier = modifier
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = !expanded
                    }
                ) {
                    TextField(
                        value = selectedItemViewModel.chosenScore.toString(),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach { item ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedItemViewModel.chosenScore = item.toInt()
                                    expanded = false
                                }
                            ) {
                                Text(text = item)
                            }
                        }
                    }
                }
            }
        }


        //nếu đã up review rồi thì hiện review của người dùng và cho chỉnh sửa
        //nếu chưa thì cho phép tạo review
        @OptIn(ExperimentalComposeUiApi::class)
        @Composable
        fun yourReview(
            reviewTotalScoreViewModel: ReviewTotalScoreViewModel,
            modifier: Modifier,
            listState: SnapshotStateList<Review>
        ) {
            val textState = remember { mutableStateOf("") }

            val keyboardController = LocalSoftwareKeyboardController.current

            Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(
                    modifier = Modifier
                        .weight(0.55f)
                        .padding(5.dp)
                ) {
                    TextField(
                        value = textState.value,
                        onValueChange = { textState.value = it },
                        placeholder = { Text("Enter text") },
                        modifier = Modifier
                            .padding(vertical = 5.dp)
                            .background(color = Color(0xffd5ede6)),
                    )

                    val options = mutableListOf<String>()
                    for (i in 0..10) {
                        options.add(i.toString())
                    }

                    DropDownMenu(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        options = options,
                        selectedItemViewModel = chosenScoreViewModel
                    )
                }


                Button(
                    onClick = {
                        //ẩn bàn phím

                        keyboardController!!.hide()

                        val review =
                            Review(
                                AppController.auth.currentUser!!.uid,
                                AppController.auth.currentUser!!.displayName ?: "",
                                textState.value,
                                Date(),
                                chosenScoreViewModel.chosenScore
                            )
                        location.reviewRepository.submitReview(
                            review,
                            applicationContext
                        ) //đăng review lên

                        listState.add(review)

                        textState.value = ""
                    },
                    modifier = Modifier
                        .weight(0.45f)
                        .padding(15.dp),
                    colors = ButtonDefaults.buttonColors(Color(0xff56a88b))
                ) {
                    Row {
                        Box(modifier = Modifier.padding(horizontal = 10.dp)) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.White
                            )
                        }
                        Text("Submit", color = Color.White)
                    }

                }
            }
        }

        var listState = remember { mutableStateListOf<Review>() }
        var originalState = remember { mutableStateListOf<Review>() }
        var totalScore by remember { mutableStateOf(0.0) }

        val coroutineScope = rememberCoroutineScope()

        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(originalState) {
            coroutineScope.launch {
                originalState.addAll(location.reviewRepository.refreshReviews())
                listState.addAll(originalState)

                reviewTotalScoreViewModel.totalScore =
                    location.reviewRepository.calculateReviewScore()
                totalScore = reviewTotalScoreViewModel.totalScore
            }
        }

        LaunchedEffect(reviewTotalScoreViewModel.totalScore) {
            reviewTotalScoreViewModel.totalScore =
                location.reviewRepository.calculateReviewScore()
            totalScore = reviewTotalScoreViewModel.totalScore
        }

        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .padding(vertical = 5.dp, horizontal = 5.dp)
                    .fillMaxWidth()

            ) {
                reviewScoreText(
                    score = roundDecimal(totalScore, 2),
                    modifier = Modifier.padding(2.dp)
                )
            }


            Box(
                modifier = Modifier
                    .padding(vertical = 2.dp, horizontal = 2.dp)
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

            yourReview(
                reviewTotalScoreViewModel = reviewTotalScoreViewModel, modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White,
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.Black
                    ), originalState
            )

            if (chosenState == -1) { //không chọn filter gì hết
                LazyColumn(
                ) {
                    items(originalState) { review ->
                        reviewLayout(review = review)
                    }
                }
            } else {
                if (listState.isNotEmpty()) {
                    LazyColumn(
                    ) {
                        items(listState) { review ->
                            reviewLayout(review = review)
                        }
                    }
                } else {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 25.dp),
                        text = "No reviews",
                        textAlign = TextAlign.Center
                    )
                }
            }

        }
    }


    //phần suggestions (hiện điểm số và hình ảnh)
    @Composable
    private fun suggestionsPlace(location: PlaceLocation) {
        var imageUrls = remember { mutableStateListOf<String>() }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(imageUrls) {
            coroutineScope.launch {
                imageUrls.addAll(location.imageViewModel.fetchAllImageUrls())
            }
        }

        Column() {
            Text(
                text = "Recommendation score: ${location.recommendsCount}",
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = if (location.recommendsCount >= 10) {
                    colorThird
                } else {
                    Color.Black
                }
            )

            Button(
                onClick = {
                    //recommend địa điểm này
                    suggestPlace(this@PlaceView, location, null)
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xffa39662)),
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

                    Text(text = "Recommend this place", color = Color.White)
                }
            }

            Text(text = "Images")

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                content = {
                    items(imageUrls) { imageUrl ->
                        Box(modifier = Modifier.padding(horizontal = 5.dp)) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier.size(150.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                    }
                }
            )


        }
    }
}

