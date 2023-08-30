package com.tung.travelthere.controller

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.tung.travelthere.PlaceView
import com.tung.travelthere.R
import com.tung.travelthere.objects.Category
import com.tung.travelthere.objects.PlaceLocation
import kotlinx.coroutines.launch

//phần hiện ra danh sách các categories
@Composable
fun categoryView(category: Category, color: Color, clickable: Boolean) {
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
        modifier = Modifier.padding(vertical = 10.dp)
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

    LaunchedEffect(imageUrl){
        coroutineScope.launch {
            imageUrl = location.fetchImageUrl()
        }
    }

    Card(
        modifier = Modifier
            .padding(
                horizontal = 40.dp,
                vertical = 20.dp
            )
            .clickable(onClick =
            {
                val intent = Intent(context, PlaceView::class.java)
                intent.putExtra("location", location)
                context.startActivity(intent)
            }),
        elevation = 10.dp
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
fun SneakViewPlaceLong(context: Context, location: PlaceLocation) {
    var imageUrl by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(imageUrl){
        coroutineScope.launch {
            imageUrl = location.fetchImageUrl()
        }
    }

    Card(
        modifier = Modifier
            .padding(
                horizontal = 5.dp,
                vertical = 20.dp
            )
            .clickable(onClick =
            {
                val intent = Intent(context, PlaceView::class.java)
                intent.putExtra("location", location)
                context.startActivity(intent)
            }),
        elevation = 10.dp
    ) {
        Row {
            ImageFromUrl(url = imageUrl ?: "", contentDescription = null, 150.0)


            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)) {
                Text(
                    text = location.getName(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
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
    var modifier: Modifier?=null
    modifier = if (size==0.0){
        Modifier.fillMaxSize()
    }
    else{
        Modifier.size(size.dp)
    }

    Image(
        painter = rememberImagePainter(url),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
}

//thanh tìm kiếm
@Composable
fun SearchBar(
    available: Set<PlaceLocation>,
    context: Context
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }

    Column {
        TextField(
            value = searchQuery,
            onValueChange = {newString -> searchQuery = newString
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

        if (searchQuery.text.isNotEmpty()) {
            SuggestionList(
                suggestions = available.filter { it.getName().contains(searchQuery.text, ignoreCase = true) }
                    .sortedBy { val similarity = it.getName().commonPrefixWith(searchQuery.text).length.toDouble() / searchQuery.text.length
                        similarity }, context
            )
        }
    }
}


@Composable
private fun SuggestionList(
    suggestions: List<PlaceLocation>,
    context: Context
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        items(suggestions.toTypedArray()) { suggestion ->
            SuggestionItem(
                suggestion = suggestion, context
            )
        }
    }
}

@Composable
private fun SuggestionItem(
    suggestion: PlaceLocation,
    context: Context
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, PlaceView::class.java)
                intent.putExtra("location", suggestion)
                context.startActivity(intent)
            }
    ) {
        Text(
            text = suggestion.getName(),
            modifier = Modifier
                .padding(16.dp)
        )
    }
}