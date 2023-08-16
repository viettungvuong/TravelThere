package com.tung.travelthere

import android.content.Context
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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.core.net.toUri
import androidx.palette.graphics.Palette
import androidx.viewpager2.widget.ViewPager2
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter.State.Empty.painter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.tung.travelthere.controller.getResourceIdFromName
import com.tung.travelthere.objects.City
import com.tung.travelthere.objects.Location
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.net.URL


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Home(this)
        }
    }
}

@Composable
fun Home(context: Context) {
    City.getSingleton().setName("Ho Chi Minh City")
    City.getSingleton().setCountry("Vietnam")
    City.getSingleton().setDescription("Ho Chi Minh City is the biggest city in Vietnam")
    City.getSingleton()
        .setImageUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/e/ed/DJI_0550-HDR-Pano.jpg/640px-DJI_0550-HDR-Pano.jpg")


    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.weight(0.5f)
            ) {
                CityIntroduction(context, City.getSingleton())
            }

            Box(
                modifier = Modifier.weight(1.5f)
            ) {
                DetailCity(City.getSingleton())
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CityIntroduction(context: Context, city: City) {
    //phần cho city
    var imageUrl by remember { mutableStateOf<String?>(city.getImageUrl()) }
    var description by remember { mutableStateOf<String?>(city.getDescription()) }

    //background color của text
    var textBgColor by remember { mutableStateOf(Color.Gray) }

    //bitmap hình nền
    var bitmap: Bitmap? = null

    val coroutineScope = rememberCoroutineScope()

    Log.d("image url", imageUrl.toString())

    LaunchedEffect(bitmap) {
//        coroutineScope.launch {
//            description = city.fetchDescription()
//            Log.d("description", description?:"")
//            imageUrl = city.fetchImageUrl()
//            Log.d("imageUrl", imageUrl?:"")
//        }

        bitmap = if (imageUrl == null) {
            null
        } else {
//                withContext(Dispatchers.IO){
//                    BitmapFactory.decodeStream(URL(imageUrl).openConnection().getInputStream())
//                }
            BitmapFactory.decodeResource(context.resources, R.drawable.hcmc)
        }


        if (bitmap != null) {
            val palette = Palette.Builder(bitmap!!).generate()
            val colorExtracted = palette.dominantSwatch?.let {
                Color(it.rgb)
            } ?: Color.Transparent
            textBgColor = colorExtracted //đặt màu nền phần box gần với màu cái ảnh
        }
    }

    SideEffect {

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.hcmc),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }


    // Text on top of the image
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,

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
            if (description != null) {
                Text(
                    text = "$description",
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(text = "Loading")
            }

        }
    }
}


@Composable
fun DetailCity(city: City) {
    Box(modifier = Modifier.fillMaxSize()) {

    }
}

@Composable
fun LocalRecommended(city: City) {
    var listState by remember { mutableStateOf(ArrayList<Location>()) }

    LaunchedEffect(true) {
        listState.addAll(city.recommendationsRepository.recommendations)
        //thêm toàn bộ list vào listState
    }

    LazyRow {
        itemsIndexed(listState) { index, location -> //tương tự xuất ra location adapter

        }
    }
}

@Composable
fun PlacesToGo(city: City) {

}

@Composable
fun Transportation(city: City) {

}

@Composable
fun Discussion(city: City) {

}

@Composable
fun InteractLocal(city: City) {

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Home(LocalContext.current)
}