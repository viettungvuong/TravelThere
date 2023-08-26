package com.tung.travelthere

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import com.tung.travelthere.controller.colorBlue
import com.tung.travelthere.objects.Location
import com.tung.travelthere.objects.PlaceOfInterest
import com.tung.travelthere.objects.RecommendedPlace
import java.io.File

class SuggestPlace : ComponentActivity() {

    lateinit var currentLocation: Location //location hiện tại (location người dùng muốn suggest)

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
            suggestPlace()
        }
    }



    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun suggestPlace() {
        var searchPlace = remember { mutableStateOf("") }
        var chosenPlaceName by remember { mutableStateOf("") }
        var chosenPlaceCity by remember { mutableStateOf("") }

        var listState = rememberLazyListState()
        var scaffoldState = rememberScaffoldState()

        val keyboardController = LocalSoftwareKeyboardController.current


        LaunchedEffect(AppController.placeViewModel.currentName,AppController.placeViewModel.currentCity) {
            chosenPlaceName = AppController.placeViewModel.currentName
            chosenPlaceCity = AppController.placeViewModel.currentCity
        }

        Scaffold(
            bottomBar = {
                BottomAppBar(cutoutShape = CircleShape, backgroundColor = colorBlue) {
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* Handle FAB click */ },
                    backgroundColor = Color(android.graphics.Color.parseColor("#b3821b"))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            isFloatingActionButtonDocked = true,
            scaffoldState = scaffoldState
        ){
            padding ->
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
                        value = searchPlace.value,
                        onValueChange = { newString ->
                            searchPlace.value = newString
                            AppController.placeViewModel.fetchPlaceSuggestions(newString)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {keyboardController?.hide()})
                    )
                }

                placeSuggestionsAutocomplete(listState, searchPlace, keyboardController)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Column{
                        Text(
                            text = "Name: $chosenPlaceName",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "City: $chosenPlaceCity",
                        )
                    }

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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)

                ) {
                    LazyRow {
                        items(imageViewModel.chosenImages) {
                            Box(
                                modifier = Modifier
                                    .padding(10.dp)
                            ) {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    alignment = Alignment.Center
                                )
                            }
                        }
                    }
                }


            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun placeSuggestionsAutocomplete(listState: LazyListState = rememberLazyListState(), searchPlace: MutableState<String>, keyboardController: SoftwareKeyboardController?){
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
                                chooseLocation(it)

                                AppController.placeViewModel.placeSuggestions.clear()
                                searchPlace.value = ""
                                keyboardController?.hide()
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
        suggestPlace()
    }

    fun chooseImage() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun chooseLocation(autocompleteResult: AutocompleteResult){
        //lấy các thông tin của địa điểm chọn
        AppController.placeViewModel.getName(autocompleteResult)
        AppController.placeViewModel.retrieveOtherInfo(autocompleteResult)

        val currentName = AppController.placeViewModel.currentName
        val currentCity = AppController.placeViewModel.currentCity
        val currentPosition = AppController.placeViewModel.currentPos

        currentLocation = RecommendedPlace(currentName,currentPosition,currentCity) //đặt location đang được suggest
    }
}