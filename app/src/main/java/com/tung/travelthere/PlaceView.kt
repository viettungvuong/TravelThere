package com.tung.travelthere

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.tung.travelthere.controller.colorBlue
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
                    tabLayout(
                        pagerState = pagerState,
                        tabTitles = tabTitles,
                        coroutineScope = coroutineScope
                    )
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

        Button(
            onClick = { /*TODO*/ },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
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

}