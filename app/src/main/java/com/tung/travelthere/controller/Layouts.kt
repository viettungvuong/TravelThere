package com.tung.travelthere.controller

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tung.travelthere.PlaceView
import com.tung.travelthere.R
import com.tung.travelthere.objects.Category
import com.tung.travelthere.objects.Place

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
            modifier = Modifier.size(32.dp),
            colorFilter = ColorFilter.tint(color = color)
        )

        Box(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(text = categoryName, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

//xem trước địa điểm
@Composable
fun SneakViewPlace(context: Context, location: Place) {
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
            val id = location.getDrawableName(context)

            if (id != null) {
                Image(
                    painter = painterResource(id = id!!),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .border(BorderStroke(1.dp, Color.Black))
                )
            }


            Text(
                text = location.getName()
            )

        }
    }
}

@Composable
fun SneakViewPlaceLong(context: Context, location: Place) {
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
            val id = location.getDrawableName(context)

            if (id != null) {
                Image(
                    painter = painterResource(id = id!!),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .border(BorderStroke(1.dp, Color.Black))
                )
            }


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