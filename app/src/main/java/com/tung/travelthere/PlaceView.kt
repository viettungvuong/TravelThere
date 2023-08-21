package com.tung.travelthere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tung.travelthere.objects.Location

class PlaceView: ComponentActivity(){
    lateinit var location: Location
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        location = intent.getSerializableExtra("location") as Location

        setContent { 
            PlaceView(location = location)
        }
    }

    @Composable
    fun PlaceView(location: Location){
        val id = location.getDrawableName(LocalContext.current)

        MaterialTheme{
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
//            AsyncImage(model = location.getImageUrl(), contentDescription = null)
                Image(painter = painterResource(id = id!!), contentDescription = null)

            }
        }


    }
}