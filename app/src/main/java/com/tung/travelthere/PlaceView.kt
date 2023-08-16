package com.tung.travelthere

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tung.travelthere.objects.Location

@Composable
fun PlaceView(location: Location){
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .shadow(
                elevation = 8.dp,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                clip = true
            )
            .background(Color.White)
    ){
        Column{
//            AsyncImage(model = location.getImageUrl(), contentDescription = null)
        }
    }
}