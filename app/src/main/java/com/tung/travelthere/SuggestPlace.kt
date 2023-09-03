package com.tung.travelthere

import android.content.Context
import android.content.Intent
import com.tung.travelthere.controller.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import com.tung.travelthere.controller.colorBlue
import com.tung.travelthere.objects.*
import kotlinx.coroutines.runBlocking
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

    lateinit var placeViewModel: PlaceAutocompleteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageViewModel = ImageViewModel()
        placeViewModel = PlaceAutocompleteViewModel(this)

        setContent {
            suggestPlace(placeViewModel)
        }
    }



    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun suggestPlace(placeViewModel: PlaceAutocompleteViewModel) {
        var searchPlace = remember { mutableStateOf("") }
        var chosenPlaceName by remember { mutableStateOf("") }
        var chosenPlaceCity by remember {mutableStateOf("") }
        var chosenPlacePos by remember { mutableStateOf(Position(0.0,0.0)) }

        var currentLocation: PlaceLocation?=null //location hiện tại (location người dùng muốn suggest)

        var listState = rememberLazyListState()
        var scaffoldState = rememberScaffoldState()

        val keyboardController = LocalSoftwareKeyboardController.current



        LaunchedEffect(placeViewModel.currentName,placeViewModel.currentCity) {
            chosenPlaceName = placeViewModel.currentName
            chosenPlaceCity = placeViewModel.currentCity
            chosenPlacePos = placeViewModel.currentPos

            currentLocation = RecommendedPlace(chosenPlaceName,chosenPlacePos,chosenPlaceCity) //đặt location đang được suggest
            Log.d("current location",currentLocation.toString())
        }



        Scaffold(
            bottomBar = {
                BottomAppBar(cutoutShape = CircleShape, backgroundColor = colorBlue) {
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (currentLocation!=null){
                            suggestPlace(this, currentLocation!!)
                            //đề xuất địa điểm

                            this.finish()
                        }
                        else{
                            Toast.makeText(this,"No location has been chosen",Toast.LENGTH_LONG).show()

                        }
                    }, //thêm vào đề xuất
                    backgroundColor =
                        Color(android.graphics.Color.parseColor("#b3821b"))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
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
                    .padding(10.dp)

                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                ) {
                    FloatingActionButton(
                        onClick = { finish() },
                        backgroundColor = Color.White,
                        content = {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = colorBlue
                            )
                        }
                    )
                }

                Text(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 25.dp), text="Suggest a place", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)


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
                            placeViewModel.fetchPlaceSuggestions(newString)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {keyboardController?.hide()})
                    )
                }

                placeSuggestionsAutocomplete(listState, searchPlace, placeViewModel)

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
    private fun placeSuggestionsAutocomplete(listState: LazyListState = rememberLazyListState(), searchPlace: MutableState<String>, placeViewModel: PlaceAutocompleteViewModel){
        val keyboardController = LocalSoftwareKeyboardController.current

        LazyColumn(state = listState) {
            items(placeViewModel.placeSuggestions) {
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable(onClick = {
                                chooseLocation(placeViewModel,it)

                                placeViewModel.placeSuggestions.clear()
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



    //chọn hình ảnh
    private fun chooseImage() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun chooseLocation(placeViewModel: PlaceAutocompleteViewModel, autocompleteResult: AutocompleteResult){
        //lấy các thông tin của địa điểm chọn
        placeViewModel.getName(autocompleteResult)
        placeViewModel.retrieveOtherInfo(autocompleteResult)


    }
}