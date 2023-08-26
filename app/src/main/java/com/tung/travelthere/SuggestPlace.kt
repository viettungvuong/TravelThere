package com.tung.travelthere

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

class SuggestPlace : ComponentActivity() {

    inner class ImageViewModel {
        var currentChosenImage by mutableStateOf<String>("")
        var chosenImages = mutableStateListOf<Bitmap>()
    }

    lateinit var imageViewModel: ImageViewModel

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imageViewModel.currentChosenImage = uri.toString()
            val bitmap =
                MediaStore.Images.Media.getBitmap(
                    this.getContentResolver(),
                    Uri.parse(imageViewModel.currentChosenImage)
                )
            imageViewModel.currentChosenImage=""
            imageViewModel.chosenImages.add(bitmap)
        } else {
            Log.d("không chọn ảnh", "chưa chọn ảnh")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageViewModel = ImageViewModel()

        setContent {
            suggestPlace(LocalContext.current)
        }
    }



    @Composable
    fun suggestPlace(context: Context) {
        var searchPlace by remember { mutableStateOf("") }
        var chosenPlaceAddress = remember { mutableStateOf("") }
        var chosenPlaceName by remember { mutableStateOf("") }

        var listState = rememberLazyListState()


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

                placeSuggestionsAutocomplete(chosenPlaceAddress = chosenPlaceAddress, listState)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Name: $chosenPlaceName",
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Button(colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue),
                        onClick = {
                            chooseImage()
                        }) {
                        Text(text = "Choose image about this place", color = Color.White)
                    }
                }

                LazyRow {
                    items(imageViewModel.chosenImages) {
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                        ) {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null
                            )
                        }
                    }
                }


            }
        }
    }

    @Composable
    fun placeSuggestionsAutocomplete(chosenPlaceAddress: MutableState<String>, listState: LazyListState = rememberLazyListState()){
        LazyColumn(state = listState) {
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
                                chosenPlaceAddress.value = it.address
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
    }


    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        suggestPlace(LocalContext.current)
    }

    fun chooseImage() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}