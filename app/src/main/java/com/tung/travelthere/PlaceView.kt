package com.tung.travelthere

import android.annotation.SuppressLint
import android.os.Bundle
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.tung.travelthere.controller.colorBlue
import com.tung.travelthere.objects.Category
import com.tung.travelthere.objects.City
import com.tung.travelthere.objects.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PlaceView : ComponentActivity() {
    lateinit var location: Location
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        location = intent.getSerializableExtra("location") as Location

        setContent {
            viewPlace(location = location)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun viewPlace(location: Location) {
        val id = location.getDrawableName(this)

        val tabTitles = listOf("About", "Reviews", "Discussions")
        val pagerState = rememberPagerState(initialPage = 0)
        val coroutineScope = rememberCoroutineScope()


        MaterialTheme {
            ConstraintLayout {
                val (image, detail) = createRefs()

//                Box(
//                    modifier = Modifier
//                        .constrainAs(image) {
//                            top.linkTo(parent.top)
//                            start.linkTo(parent.start)
//                        }
//                ){
//                    FloatingActionButton(
//                        onClick = { finish() },
//                        backgroundColor = Color.White,
//                        content = {
//                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Red)
//                        }
//                    )
//                }


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
                    Image( //hình ảnh
                        painter = painterResource(id = id!!),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
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
                    Column{
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
    fun aboutPlace(location: Location) {
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
        ){
            Text(
                text="Categories",
                fontWeight = FontWeight.Bold
            )

            LazyRow {
                itemsIndexed(location.categories.toTypedArray()) { index, category -> //tương tự xuất ra location adapter
                    categoryView(category = category)
                }
            }
        }

        Button(
            onClick = { /*TODO*/ },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF36D72)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(20.dp))

                Text(text = "Add to favorites", color = Color.White)
            }
        }

    }

    @Composable
    fun categoryView(category: Category){
        var painter: Painter?=null

        painter = when (category){
            Category.RESTAURANT -> painterResource(R.drawable.restaurant)
            Category.BAR -> painterResource(R.drawable.bar)
            Category.ATTRACTION -> painterResource(R.drawable.attraction)
            Category.NATURE -> painterResource(R.drawable.nature)
            Category.NECESSITY -> painterResource(R.drawable.hospital)
            Category.OTHERS -> painterResource(R.drawable.other)
            Category.SHOPPING -> painterResource(R.drawable.shopping)
        }

        var categoryName: String?=null
        categoryName = when (category){
            Category.RESTAURANT -> "Restaurant"
            Category.BAR -> "Bar"
            Category.ATTRACTION -> "Attraction"
            Category.NATURE -> "Nature"
            Category.NECESSITY -> "Necessity"
            Category.OTHERS -> "Others"
            Category.SHOPPING -> "Shopping"
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Image(
                painter = painter!!,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(32.dp)
            )

            Box(
                modifier = Modifier.padding(20.dp)
            ){
                Text(text = categoryName, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
    }

    @Composable
    fun reviewsPlace(location: Location){

    }

    @Composable
    fun discussionsPlace(location: Location){

    }

}