package com.tung.travelthere

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter.State.Empty.painter
import com.tung.travelthere.controller.getResourceIdFromName
import com.tung.travelthere.objects.City
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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
    //scroll state
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    MaterialTheme{
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Box(
                modifier = Modifier.weight(1f)
            ) {
                CityIntroduction(context, City("Ho Chi Minh City"))
            }

            Box(
                modifier = Modifier.weight(1f)
            ) {
                Text("Hello")
            }
        }
    }
}

@Composable
fun CityIntroduction(context: Context, city: City) {
    //phần cho city
    var imageUrl by remember{ mutableStateOf(getResourceIdFromName("hcmc")) }
    var description by remember{ mutableStateOf("") }

    //background color của text
    var textBgColor by remember{ mutableStateOf(Color.Gray) }

    //bitmap hình nền
    var bitmap: Bitmap?


    LaunchedEffect(true){
//        CoroutineScope(Dispatchers.IO)
//            .launch {
//            imageUrl = city.getImageUrl()
//            bitmap = BitmapFactory.decodeStream(URL(imageUrl).openConnection().getInputStream())
//            description = city.getDescription()
//        }

        bitmap = BitmapFactory.decodeResource(context.resources, imageUrl)
        //đặt bitmap từ resources
        description="Ho Chi Minh City is the biggest city in Vietnam"

        if (bitmap!=null){
            val palette = Palette.Builder(bitmap!!).generate()
            val colorExtracted = palette.dominantSwatch?.let {
                Color(it.rgb)
            } ?: Color.Transparent
            textBgColor = colorExtracted //đặt màu nền phần box gần với màu cái ảnh
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Background Image
//        AsyncImage(model = imageUrl
//            , contentDescription = null)

        Image(
            painter = painterResource(id = imageUrl),
            contentDescription = "Image from Drawable",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Text on top of the image
        Column(
            modifier = Modifier
                .fillMaxSize() ,
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
                text = "${city.name}",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis)
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
                Text(
                    text = "$description",
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Home(LocalContext.current)
}