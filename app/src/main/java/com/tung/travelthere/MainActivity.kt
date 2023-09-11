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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.google.android.libraries.places.api.Places
import com.tung.travelthere.controller.*
import com.tung.travelthere.objects.*
import kotlinx.coroutines.*
import java.net.URL
import java.util.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONException
import org.json.JSONObject

import java.io.Serializable
import java.io.UnsupportedEncodingException

val colorCold = Color(0xff447cbd)
val colorMedium = Color(0xffbd9544)
val colorHot = Color(0xffbd4444)



class MainActivity : ComponentActivity() {

    val optionStr = mutableListOf<String>()

    //bitmap hình nền thành phố
    var bitmap: Bitmap? = null

    //để biết chọn category nào
    lateinit var weatherViewModel: WeatherViewModel
    lateinit var selectedItemViewModel1: ChosenFilterViewModel
    lateinit var selectedItemViewModel2: ChosenFilterViewModel
    lateinit var selectedItemViewModel3: ChosenFilterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Places.initialize(this, apiKey)
        //bảo mật apiKey (không up file storage.kt lên git)

        for (filter in Filter.values()) {
            optionStr.add(filterToStr(filter))
        }
        if (AppController.currentPosition.currentLocation == null) {
            optionStr.remove("NEAREST FIRST") //nếu đang trong chế độ không có vị trí thì bỏ 2 filter này ra
            optionStr.remove("FARTHEST FIRST")
        }

        weatherViewModel = WeatherViewModel(this, City.getSingleton())

        selectedItemViewModel1 = ChosenFilterViewModel()
        selectedItemViewModel2 = ChosenFilterViewModel()
        selectedItemViewModel3 = ChosenFilterViewModel()

        setContent {
            Home(this@MainActivity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bitmap?.recycle() //giải phóng bitmap
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Home(context: Context) {
        @OptIn(ExperimentalFoundationApi::class)
        @Composable
        fun tabLayout(
            pagerState: PagerState,
            tabTitles: List<String>,
            coroutineScope: CoroutineScope
        ) {
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
                                coroutineScope.cancel()
                            }
                        },
                        text = { Text(text = title) }
                    )
                }
            }
        }

        val tabTitles = mutableListOf("Local recommends", "Tourist attractions")
        if (AppController.currentPosition.currentLocation!=null){
            tabTitles.add("Nearby") //không vào từ pick city thì mới có nearby
        }

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
                    Spacer(modifier = Modifier.width(3.dp))
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
                    Spacer(modifier = Modifier.width(3.dp))
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
                    Spacer(modifier = Modifier.width(3.dp))
                    IconButton(onClick = {
                        val intent = Intent(context, CreateScheduleActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Image(
                            painter = painterResource(id = R.drawable.luggage),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    IconButton(onClick = {
                        val intent = Intent(context, ProfileActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            tint = Color.White,
                            contentDescription = "User"
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        val intent = Intent(context, SearchPlace::class.java)
                        context.startActivity(intent)
                    },
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
                    CityIntroduction(City.getSingleton()) //phần thông tin thành phố hiện tại
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
                                0 ->
                                    LocalRecommended(
                                        modifier = Modifier.padding(padding),
                                        context,
                                        city = City.getSingleton()
                                    )
                                1 ->
                                    TouristAttractions(
                                        modifier = Modifier.padding(padding),
                                        context,
                                        city = City.getSingleton()
                                    )
                                2 ->
                                    NearbyPlaces(
                                        modifier = Modifier.padding(padding),
                                        context,
                                        city = City.getSingleton(),
                                    )
                            }
                        }
                    }
                }
            }

        }
    }


    @Composable
    fun CityIntroduction(city: City) {
        //phần cho city
        var imageUrl by remember { mutableStateOf<String?>(null) }

        //background color của text
        var textBgColor by remember { mutableStateOf(Color.Gray) }


        var temperature by remember { mutableStateOf(0f) }
        var conditionImgUrl by remember { mutableStateOf("") }

        var isLoading = remember { mutableStateOf(true) }

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

        LaunchedEffect(weatherViewModel.currentTemp, weatherViewModel.conditionImgUrl) {
            temperature = weatherViewModel.currentTemp
            conditionImgUrl = weatherViewModel.currentConditionImgUrl
        }


        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            ImageFromUrl(url = imageUrl ?: "", contentDescription = null, 0.0, isLoading) //hình ảnh
        }

        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Box(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        top = 8.dp,
                        bottom = 8.dp,
                    )
                    .background(textBgColor)
            ) {
                Text(
                    text = "${city.getName()}", //tên thành phố
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

            }

            Spacer(modifier = Modifier.weight(1f))

            //phần thời tiết
            Box(
                modifier = Modifier
                    .padding(
                        top = 8.dp,
                        end = 16.dp,
                        bottom = 8.dp,
                    )
                    .background(
                        if (temperature < 15.0) {
                            colorCold
                        } else if (temperature >= 15.0 && temperature < 30.0) {
                            colorMedium
                        } else {
                            colorHot
                        }
                    )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$temperature °C",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    AsyncImage(
                        model = conditionImgUrl,
                        contentDescription = "Condition",
                        modifier = Modifier.size(32.dp),
                        contentScale = ContentScale.Fit
                    )
                }

            }
        }


    }


    //trang local recommended
    @Composable
    fun LocalRecommended(modifier: Modifier, context: Context, city: City) {
        var listState = remember { mutableStateListOf<PlaceLocation>() }
        var chosenState = remember { mutableStateOf(mutableSetOf<Category>()) }
        var originalState = remember { mutableStateListOf<PlaceLocation>() }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(originalState) {
            coroutineScope.launch {
                originalState.addAll(city.locationsRepository.recommends)
                listState.addAll(originalState)
            }
        }


        Column(modifier = modifier) {
            DropDownMenu(
                modifier = Modifier
                    .padding(10.dp),
                options = optionStr, selectedItemViewModel = selectedItemViewModel2
            ) {
                filterSortList(
                    chosenFilterViewModel = selectedItemViewModel2,
                    listState = listState
                )
            }


            LazyRow(modifier = Modifier.padding(15.dp)) {
                itemsIndexed(Category.values()) { index, category -> //tương tự xuất ra location adapter
                    categoryView(category, colorBlue, true, listState, chosenState, originalState)
                }
            }

            if (listState.isNullOrEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "No places", fontStyle = FontStyle.Italic)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    content = {
                        items(listState) { location ->
                            if (!location.containCategory(Category.NECESSITY)) {
                                SneakViewPlace(context, location)
                            }
                        }
                    }
                )
            }
        }
    }


    @Composable
    fun TouristAttractions(modifier: Modifier, context: Context, city: City) {
        var listState = remember { mutableStateListOf<PlaceLocation>() }
        var chosenState = remember { mutableStateOf(mutableSetOf<Category>()) }
        var originalState = remember { mutableStateListOf<PlaceLocation>() }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(originalState) {
            coroutineScope.launch {
                originalState.addAll(city.locationsRepository.refreshLocations().map { it.value })
                listState.addAll(originalState)
            }
        }


        Column(modifier = modifier) {
            DropDownMenu(
                modifier = Modifier
                    .padding(10.dp),
                options = optionStr, selectedItemViewModel = selectedItemViewModel3
            ) {
                filterSortList(
                    chosenFilterViewModel = selectedItemViewModel3,
                    listState = listState
                )
            }


            LazyRow(modifier = Modifier.padding(15.dp)) {
                itemsIndexed(Category.values()) { index, category -> //tương tự xuất ra location adapter
                    categoryView(category, colorBlue, true, listState, chosenState, originalState)
                }
            }

            if (listState.isNullOrEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "No places", fontStyle = FontStyle.Italic)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    content = {
                        items(listState) { location ->
                            if (!location.containCategory(Category.NECESSITY)) {
                                SneakViewPlace(context, location)
                            }
                        }
                    }
                )
            }
        }

    }


    @Composable
    fun NearbyPlaces(
        modifier: Modifier,
        context: Context,
        city: City
    ) { //đề xuất địa điểm gần với nơi đang đứng
        var listState = remember { mutableStateListOf<PlaceLocation>() }
        var chosenState = remember { mutableStateOf(mutableSetOf<Category>()) }
        var originalState = remember { mutableStateListOf<PlaceLocation>() }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(originalState) {
            coroutineScope.launch {
                originalState.addAll(city.locationsRepository.nearbyLocations())
                listState.addAll(originalState)
            }
        }

        Column(modifier = modifier) {

            DropDownMenu(
                modifier = Modifier
                    .padding(10.dp),
                options = optionStr, selectedItemViewModel = selectedItemViewModel1
            ) {
                filterSortList(
                    chosenFilterViewModel = selectedItemViewModel1,
                    listState = listState
                )
            }


            LazyRow(modifier = Modifier.padding(15.dp)) {
                itemsIndexed(Category.values()) { index, category -> //tương tự xuất ra location adapter
                    categoryView(category, colorBlue, true, listState, chosenState, originalState)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Places that are 5 km from your current position",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                )
            }

            if (listState.isNullOrEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "No places", fontStyle = FontStyle.Italic)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    content = {
                        items(listState) { location ->
                            SneakViewPlace(context, location)
                        }
                    }
                )
            }

        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun DropDownMenu(
        modifier: Modifier,
        options: List<String>,
        selectedItemViewModel: ChosenFilterViewModel,
        onSelect: () -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        value = filterToStr(selectedItemViewModel.chosenFilter),
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
                                    selectedItemViewModel.chosenFilter = strToFilter(item)
                                    expanded = false

                                    onSelect()
                                }
                            ) {
                                Text(text = item)
                            }
                        }
                    }
                }
            }
        }
    }

    enum class Filter {
        NEAREST_FIRST,
        FARTHEST_FIRST,
        HIGHLY_REVIEWED,
        MOST_RECOMMENDS,
        DEFAULT
    }

    private fun filterToStr(filter: Filter): String {
        return (when (filter) {
            Filter.NEAREST_FIRST -> "Nearest first"
            Filter.FARTHEST_FIRST -> "Farthest first"
            Filter.HIGHLY_REVIEWED -> "Highly reviewed"
            Filter.MOST_RECOMMENDS -> "Most recommends"
            else -> "Default"
        })
    }

    private fun strToFilter(string: String): Filter {
        return (when (string.uppercase()) {
            "NEAREST FIRST" -> Filter.NEAREST_FIRST
            "FARTHEST FIRST" -> Filter.FARTHEST_FIRST
            "HIGHLY REVIEWED" -> Filter.HIGHLY_REVIEWED
            "MOST RECOMMENDS" -> Filter.MOST_RECOMMENDS
            else -> Filter.DEFAULT
        })
    }


    class ChosenFilterViewModel : ViewModel() {
        var chosenFilter by mutableStateOf(Filter.DEFAULT)
    }

    private fun filterSortList(
        chosenFilterViewModel: ChosenFilterViewModel, listState: SnapshotStateList<PlaceLocation>
    ) {
        val copyList = mutableListOf<PlaceLocation>()
        copyList.addAll(listState)
        listState.clear()

        when (chosenFilterViewModel.chosenFilter) {
            Filter.NEAREST_FIRST -> {
                listState.addAll(copyList.sortedBy {
                    it.getPos().distanceTo(AppController.currentPosition.currentLocation!!)
                })

            }
            Filter.FARTHEST_FIRST -> {
                listState.addAll(copyList.sortedByDescending {
                    it.getPos().distanceTo(AppController.currentPosition.currentLocation!!)
                })
            }
            Filter.HIGHLY_REVIEWED -> {
                listState.addAll(copyList.sortedByDescending {
                    it.reviewRepository.reviewScore
                })
            }
            Filter.MOST_RECOMMENDS -> {
                listState.addAll(copyList.sortedByDescending {
                    it.recommendsCount
                })
            }

            else -> {
                listState.addAll(copyList)
            }


        }
        Log.d("list 1 img url after", listState.first().imageUrl.toString())
    }

}


