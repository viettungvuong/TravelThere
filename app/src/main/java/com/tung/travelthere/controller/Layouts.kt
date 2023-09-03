package com.tung.travelthere.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.CalendarView
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat.startActivity
import coil.compose.rememberImagePainter
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.datepicker.MaterialDatePicker
import com.tung.travelthere.*
import com.tung.travelthere.R
import com.tung.travelthere.objects.Category
import com.tung.travelthere.objects.City
import com.tung.travelthere.objects.PlaceLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

//phần hiện ra danh sách các categories
@Composable
fun categoryView(
    category: Category,
    color: Color,
    clickable: Boolean,
    locationState: MutableState<MutableSet<PlaceLocation>>? = null,
    chosenState: MutableState<MutableSet<Category>>? = null, //danh sách các category được chọn
    originalState: MutableState<MutableSet<PlaceLocation>>? = null
) {
    var chosen by remember { mutableStateOf(false) }

    var painter: Painter? = null

    painter = when (category) {
        Category.RESTAURANT -> painterResource(R.drawable.restaurant)
        Category.BAR -> painterResource(R.drawable.bar)
        Category.ATTRACTION -> painterResource(R.drawable.attraction)
        Category.NATURE -> painterResource(R.drawable.nature)
        Category.NECESSITY -> painterResource(R.drawable.hospital)
        Category.OTHERS -> painterResource(R.drawable.other)
        Category.SHOPPING -> painterResource(R.drawable.shopping)
    }


    var categoryName: String? = null
    categoryName = when (category) {
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
        modifier = Modifier
            .padding(vertical = 10.dp)
            .border(
                width = if (chosen) 1.dp else 0.dp,
                color = if (chosen) Color(0xff365875) else Color.Transparent,
                shape = RoundedCornerShape(4.dp),
            )
            .then(if (clickable) Modifier.clickable {
                chosen = !chosen //chọn hay chưa
                if (chosen) {
                    chosenState!!.value.add(category)
                } else {
                    chosenState!!.value.remove(category)
                }
                if (chosenState!!.value.isNotEmpty()) { //nếu có chọn category rồi
                    locationState!!.value = originalState!!.value
                        .filter {
                            chosenState!!.value.all { category ->
                                it.categories.contains(category)
                            }
                        }
                        .toMutableSet()
                } else { //nếu không chọn category nào hết
                    locationState!!.value = originalState!!.value
                }
            } else Modifier)
    ) {
        Image(
            painter = painter!!,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(color = color)
        )

        Box(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(text = categoryName, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

//dung cho favoritepage
@Composable
fun categoryView(
    category: Category,
    color: Color,
    clickable: Boolean,
    locationState: SnapshotStateList<PlaceLocation>? = null,
    chosenState: MutableState<MutableSet<Category>>? = null, //danh sách các category được chọn
    originalState: SnapshotStateList<PlaceLocation>? = null
) {
    var chosen by remember { mutableStateOf(false) }

    var painter: Painter? = null

    painter = when (category) {
        Category.RESTAURANT -> painterResource(R.drawable.restaurant)
        Category.BAR -> painterResource(R.drawable.bar)
        Category.ATTRACTION -> painterResource(R.drawable.attraction)
        Category.NATURE -> painterResource(R.drawable.nature)
        Category.NECESSITY -> painterResource(R.drawable.hospital)
        Category.OTHERS -> painterResource(R.drawable.other)
        Category.SHOPPING -> painterResource(R.drawable.shopping)
    }


    var categoryName: String? = null
    categoryName = when (category) {
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
        modifier = Modifier
            .padding(vertical = 10.dp)
            .border(
                width = if (chosen) 1.dp else 0.dp,
                color = if (chosen) Color(0xff365875) else Color.Transparent,
                shape = RoundedCornerShape(4.dp),
            )
            .then(if (clickable) Modifier.clickable {
                chosen = !chosen //chọn hay chưa
                if (chosen) {
                    chosenState!!.value.add(category)
                } else {
                    chosenState!!.value.remove(category)
                }
                locationState!!.clear()
                if (chosenState!!.value.isNotEmpty()) { //nếu có chọn category rồi
                    locationState!!.addAll(originalState!!.filter {
                        chosenState!!.value.all { category ->
                            it.categories.contains(category)
                        }
                    })
                } else { //nếu không chọn category nào hết
                    locationState.addAll(originalState!!)
                }
            } else Modifier)
    ) {
        Image(
            painter = painter!!,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(color = color)
        )

        Box(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(text = categoryName, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

//category view dùng cho search place
@Composable
fun categoryView(
    category: Category,
    color: Color,
    clickable: Boolean,
    searchViewModel: SearchViewModel? = null,
    chosenState: MutableState<MutableSet<Category>>? = null
) {
    var chosen by remember { mutableStateOf(false) }

    var painter: Painter? = null

    painter = when (category) {
        Category.RESTAURANT -> painterResource(R.drawable.restaurant)
        Category.BAR -> painterResource(R.drawable.bar)
        Category.ATTRACTION -> painterResource(R.drawable.attraction)
        Category.NATURE -> painterResource(R.drawable.nature)
        Category.NECESSITY -> painterResource(R.drawable.hospital)
        Category.OTHERS -> painterResource(R.drawable.other)
        Category.SHOPPING -> painterResource(R.drawable.shopping)
    }


    var categoryName: String? = null
    categoryName = when (category) {
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
        modifier = Modifier
            .padding(vertical = 10.dp)
            .border(
                width = if (chosen) 1.dp else 0.dp,
                color = if (chosen) Color(0xff365875) else Color.Transparent,
                shape = RoundedCornerShape(4.dp),
            )
            .then(if (clickable) Modifier.clickable {
                chosen = !chosen //chọn hay chưa
                if (chosen) {
                    chosenState!!.value.add(category)
                } else {
                    chosenState!!.value.remove(category)
                }
                searchViewModel!!.matchedQuery.clear()
                if (chosenState!!.value.isNotEmpty()) { //nếu có chọn category rồi
                    searchViewModel!!.matchedQuery += (searchViewModel!!.originalMatchedQuery.filter {
                        chosenState!!.value.all { category ->
                            it.categories.contains(category)
                        }
                    })
                } else { //nếu không chọn category nào hết
                    searchViewModel!!.matchedQuery.addAll(searchViewModel!!.originalMatchedQuery)
                }
            } else Modifier)
    ) {
        Image(
            painter = painter!!,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(color = color)
        )

        Box(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(text = categoryName, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}


//xem trước địa điểm
@Composable
fun SneakViewPlace(context: Context, location: PlaceLocation) {
    var imageUrl by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(imageUrl) {
        coroutineScope.launch {
            imageUrl = location.fetchImageUrl()
        }
    }

    Card(
        modifier = Modifier
            .padding(
                horizontal = 40.dp, vertical = 20.dp
            )
            .clickable(onClick = {
                val intent = Intent(context, PlaceView::class.java)
                intent.putExtra("location", location)
                context.startActivity(intent)
            }), elevation = 10.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ImageFromUrl(url = imageUrl ?: "", contentDescription = null, 150.0)

            Text(
                text = location.getName()
            )

        }
    }
}

@Composable
fun SneakViewPlaceLong(context: Context, location: PlaceLocation, hasImage: Boolean = true) {
    var imageUrl by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(imageUrl) {
        coroutineScope.launch {
            imageUrl = location.fetchImageUrl()
        }
    }

    Card(
        modifier = Modifier
            .padding(
                vertical = 20.dp
            )
            .clickable(onClick = {
                val intent = Intent(context, PlaceView::class.java)
                intent.putExtra("location", location)
                context.startActivity(intent)
            }), elevation = 10.dp
    ) {
        Row {
            if (hasImage) {
                ImageFromUrl(url = imageUrl ?: "", contentDescription = null, 150.0)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = location.getName(), fontSize = 20.sp, fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "City",
                        tint = Color.Black
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Text(text = location.cityName)
                }

            }

        }
    }
}


@Composable
fun ImageFromUrl(url: String, contentDescription: String?, size: Double) {
    var modifier: Modifier? = null
    modifier = if (size == 0.0) {
        Modifier.fillMaxSize()
    } else {
        Modifier.size(size.dp)
    }

    Image(
        painter = rememberImagePainter(url),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
}

//thanh tìm kiếm (cho searchplace.kt)
@Composable
fun SearchBar(
    available: Set<PlaceLocation>, searchViewModel: SearchViewModel,modifier: Modifier
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }

    Column(modifier=modifier) {
        TextField(
            value = searchQuery,
            onValueChange = { newString ->
                searchQuery = newString

                search(searchViewModel, newString.text, available)
                //không dùng assignment operator vì nó là truyền reference
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun tabLayout(
    pagerState: PagerState,
    tabTitles: List<String>,
    coroutineScope: CoroutineScope,
    contentColor: Color
) {
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = Color.White,
        contentColor = contentColor,
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
fun dateTimePicker(modifier: Modifier, mDate: MutableState<String>, datePicker: () -> Unit) {
    var dateStr by remember { mutableStateOf("") }

    LaunchedEffect(mDate.value) {
        dateStr = mDate.value
    }

    Row(modifier = modifier) {
        TextField(
            value = dateStr,
            onValueChange = { newString ->
                dateStr = newString
            },
            textStyle = TextStyle(fontSize = 17.sp, color = Color.White),
            leadingIcon = {
                Icon(Icons.Filled.DateRange, null, tint = Color.White, modifier =
                Modifier.clickable {
                    datePicker()
                })
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 5.dp)
                .background(Color(0xFF445787), RoundedCornerShape(16.dp)),
            placeholder = { Text(text = "Date", color = Color.White) },
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                backgroundColor = Color.Transparent,
                cursorColor = Color.DarkGray
            ),
            enabled = false
        )

        Box(modifier = Modifier
            .size(24.dp)
            .clickable {

            }) {

        }
    }


}

