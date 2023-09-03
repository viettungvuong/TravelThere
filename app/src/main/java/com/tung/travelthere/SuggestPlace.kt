package com.tung.travelthere

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.tung.travelthere.controller.*
import com.tung.travelthere.objects.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class SuggestPlace : ComponentActivity() {

    inner class ImageViewModel {
        var chosenImagesUri = mutableStateListOf<Uri>()
        var chosenImages = mutableStateListOf<Bitmap>()
    }

    lateinit var imageViewModel: ImageViewModel

    val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            if (uris != null) {
                for (uri in uris) {
                    imageViewModel.chosenImagesUri.add(uri)
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(
                            this.getContentResolver(),
                            Uri.parse(uri.toString())
                        )
                    imageViewModel.chosenImages.add(bitmap)
                }

            } else {
                Toast.makeText(this, "No images have been selected", Toast.LENGTH_LONG).show()
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
        var chosenPlaceCity by remember { mutableStateOf("") }
        var chosenPlacePos by remember { mutableStateOf(Position(0.0, 0.0)) }

        var currentLocation: PlaceLocation? =
            null //location hiện tại (location người dùng muốn suggest)

        var listState = rememberLazyListState()
        var scaffoldState = rememberScaffoldState()

        val keyboardController = LocalSoftwareKeyboardController.current



        LaunchedEffect(placeViewModel.currentName, placeViewModel.currentCity) {
            chosenPlaceName = placeViewModel.currentName
            chosenPlaceCity = placeViewModel.currentCity
            chosenPlacePos = placeViewModel.currentPos

            if (chosenPlaceName.isNotBlank()) {
                currentLocation = TouristPlace(
                    chosenPlaceName,
                    chosenPlacePos,
                    chosenPlaceCity
                ) //đặt location đang được suggest
            } else {
                currentLocation = null //trong trường hợp xoá mất địa điểm
            }
        }



        Scaffold(
            bottomBar = {
                BottomAppBar(cutoutShape = CircleShape, backgroundColor = colorBlue) {
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (currentLocation != null) {
                            suggestPlace(this, currentLocation!!, imageViewModel)
                            //đề xuất địa điểm

                            this.finish()
                        } else {
                            Toast.makeText(this, "No location has been chosen", Toast.LENGTH_LONG)
                                .show()

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
        ) { padding ->
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

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 25.dp),
                    text = "Suggest a place",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
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
                            placeViewModel.fetchPlaceSuggestions(newString)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() })
                    )
                }

                placeSuggestionsAutocomplete(listState, searchPlace, placeViewModel)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Column {
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
                        itemsIndexed(imageViewModel.chosenImages) { index, image ->
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = image.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(150.dp),
                                    contentScale = ContentScale.Fit
                                )

                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .size(25.dp)
                                        .background(Color.Red, shape = RoundedCornerShape(4.dp))
                                        .clickable {
                                            imageViewModel.chosenImages.removeAt(index)
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }


            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun placeSuggestionsAutocomplete(
        listState: LazyListState = rememberLazyListState(),
        searchPlace: MutableState<String>,
        placeViewModel: PlaceAutocompleteViewModel
    ) {
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
                                chooseLocation(placeViewModel, it)

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

    private fun chooseLocation(
        placeViewModel: PlaceAutocompleteViewModel,
        autocompleteResult: AutocompleteResult
    ) {
        //lấy các thông tin của địa điểm chọn
        placeViewModel.getName(autocompleteResult)
        placeViewModel.retrieveOtherInfo(autocompleteResult)
    }
}

//cho phép người dùng thêm địa điểm
fun suggestPlace(
    context: Context,
    location: PlaceLocation,
    imageViewModel: SuggestPlace.ImageViewModel?
) {
    val cityRef = AppController.db.collection(collectionCities).document(location.cityName)
    cityRef.get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val documentSnapshot = task.result
            val cityExists = documentSnapshot?.exists() ?: false

            if (!cityExists) {
                //chưa có thành phố
                val cityData = hashMapOf(
                    "city-name" to location.cityName,
                )
                cityRef.set(cityData)
            }

            val locationRef = cityRef.collection(
                collectionLocations
            ).document(location.getPos().toString())
            locationRef.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documentSnapshot = task.result
                        val documentExists = documentSnapshot?.exists() ?: false

                        if (imageViewModel!=null){
                            CoroutineScope(Dispatchers.Main).launch {
                                uploadImages(context, imageViewModel, location)
                                Log.d("upload image", "true")
                            }
                        } //nếu có hình ảnh


                        if (documentExists) {
                            //có tồn tại
                            var recommendIds = documentSnapshot.get("recommend-ids") as List<String>
                            if (recommendIds==null){
                                recommendIds= emptyList()
                            }
                            val index = recommendIds.binarySearch(AppController.auth.currentUser!!.uid)
                            if (index>=0){ //người dùng đã recommend rồi, không cho recommend
                                Toast.makeText(
                                    context,
                                    "You have already recommended this place",
                                    Toast.LENGTH_LONG
                                ).show()

                                return@addOnCompleteListener
                            }

                            val recommendedNum = documentSnapshot.getLong("recommends")
                                ?: 0 //số lượng được recommends
                            val updatedField = mapOf("recommends" to recommendedNum + 1)

                            locationRef.update(updatedField)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Thank you for your suggestion!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    // Handle the update failure
                                    Toast.makeText(
                                        context,
                                        "There is an error when adding your suggestion, please try again",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        } else {
                            //không tồn tại
                            val locationData = hashMapOf(
                                "location-name" to location.getName(),
                                "lat" to location.getPos().lat,
                                "long" to location.getPos().long,
                                "categories" to listOf("Recommend"),
                                "recommends" to 1
                            )
                            locationRef.set(locationData) // Create a new document with locationData
                                .addOnSuccessListener {

                                    Toast.makeText(
                                        context,
                                        "Thank you for your suggestion!",
                                        Toast.LENGTH_LONG
                                    ).show()

                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "There is an error when adding your suggestion, please try again",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    } else {
                        Log.d("error", "fetching location unsuccessful")
                    }
                }
        } else {
            Log.d("error", "fetching city unsuccessful")
        }
    }
}

private suspend fun uploadImages(
    context: Context,
    imageViewModel: SuggestPlace.ImageViewModel,
    location: PlaceLocation,
) {
    var imageCount = 0
    val listRef = AppController.storage.reference.child("files/${location.getPos()}")

    listRef.listAll()
        .addOnSuccessListener { (items, prefixes) ->
            imageCount = items.size //tìm số hình ảnh
            Log.d("image count", imageCount.toString())

            runBlocking {
                withContext(Dispatchers.IO) {
                    for (uri in imageViewModel.chosenImagesUri) {
                        Log.d("uri", uri.toString())
                        val fileExtension = getFileExtension(context.contentResolver, uri)

                        val uploadTask = listRef.child("$imageCount$fileExtension").putFile(uri)

                        try {
                            uploadTask.await()
                            imageCount++

                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "There was an error when adding your image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        val handler = Handler(Looper.getMainLooper())

                        handler.post {
                            Toast.makeText(
                                context,
                                "Your images have been added",
                                Toast.LENGTH_SHORT
                            )
                                .show() //chạy trên ui thread
                        }


                    }
                }

            }
        }
        .addOnFailureListener { exception ->
            Log.d("error upload image", exception.message.toString())
        }
}

private fun getFileExtension(contentResolver: ContentResolver, uri: Uri): String? {
    val mimeType = contentResolver.getType(uri)
    return if (mimeType != null) {
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        if (extension != null && extension.isNotEmpty()) {
            ".$extension"
        } else {
            null
        }
    } else {
        null
    }
}