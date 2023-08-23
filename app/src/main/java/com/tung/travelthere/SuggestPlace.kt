package com.tung.travelthere

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tung.travelthere.controller.AppController
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

class SuggestPlace : ComponentActivity() {
    companion object {
        lateinit var imageViewModel: ImageViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageViewModel = ImageViewModel()

        setContent {
            suggestPlace(LocalContext.current)
        }
    }

    inner class ImageViewModel {
        var chosenImages = mutableStateListOf<Bitmap>()
    }

    @Composable
    fun suggestPlace(context: Context) {
        var searchPlace by remember { mutableStateOf("") }
        var chosenPlaceAddress by remember { mutableStateOf("") }
        var chosenPlaceName by remember { mutableStateOf("") }


        LaunchedEffect(AppController.placeViewModel.currentName) {
            chosenPlaceName = AppController.placeViewModel.currentName
        }

        MaterialTheme {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = "Find your place"
                )

                Spacer(
                    Modifier.height(20.dp)
                )


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    OutlinedTextField(
                        value = searchPlace,
                        onValueChange = { newString ->
                            searchPlace = newString
                            AppController.placeViewModel.fetchPlaceSuggestions(newString)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }

                LazyColumn {
                    items(AppController.placeViewModel.placeSuggestions) {
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clickable(onClick = {
                                        chosenPlaceAddress = it.address
                                        AppController.placeViewModel.getName(it)
                                        AppController.placeViewModel.placeSuggestions.clear()
                                    }

                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "Address",
                                    tint = Color.Black
                                )

                                Spacer(modifier = Modifier.width(20.dp))

                                Text(text = it.address)

                            }


                        }

                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Address: $chosenPlaceAddress",
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Name: $chosenPlaceName",
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Button(colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue),
                        onClick = {
                            chooseImage { uri ->
                                val bitmap =
                                    MediaStore.Images.Media.getBitmap(
                                        context.getContentResolver(),
                                        uri
                                    )
                                imageViewModel.chosenImages.add(bitmap)
                            }
                        }) {
                        Text(text = "Choose image about this place")
                    }
                }

                LazyRow{
                    items(imageViewModel.chosenImages){
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                        ){
                            Image(
                                bitmap =  it.asImageBitmap(),
                                contentDescription = null
                            )
                        }
                    }
                }


            }
        }
    }



    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        suggestPlace(LocalContext.current)
    }

    fun chooseImage(callback: (Uri) -> Unit) {
        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    callback(uri)
                } else {
                    Log.d("không chọn ảnh", "chưa chọn ảnh")
                }
            }
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}