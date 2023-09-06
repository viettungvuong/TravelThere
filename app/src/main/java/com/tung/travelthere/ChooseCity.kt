package com.tung.travelthere

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tung.travelthere.controller.AppController
import com.tung.travelthere.controller.collectionCities
import com.tung.travelthere.controller.colorBlue
import com.tung.travelthere.objects.City
import com.tung.travelthere.objects.Position
import com.tung.travelthere.ui.theme.TravelThereTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ChooseCity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cities = mutableListOf<String>("Ho Chi Minh City")

        setContent {
            TravelThereTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ChooseCityView(cities = cities)
                }
            }
        }
    }

    private fun pickCity(city: String) {
        AppController.currentPosition= AppController.UserPlace()

        AppController.currentPosition.cityName = city
        AppController.currentPosition.countryName = "Vietnam"

        City.getSingleton().setName(city) //đặt tên cho thành phố hiện tại
        City.getSingleton().setCountry("Vietnam")

        AppController.currentPosition.currentLocation = Position(10.7628356,106.6801006)
        //đặt vị trí giả định

        Toast.makeText(this,"Please wait",Toast.LENGTH_LONG).show()

        runBlocking { //chạy trong main thread luôn load xong rồi vào
            City.getSingleton().locationsRepository.refreshLocations(true)
        }
        val intent = Intent(this@ChooseCity, MainActivity::class.java)
        startActivity(intent)
        finish()



        finish()
    }

    @Composable
    fun ChooseCityView(cities: List<String>) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorBlue)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Pick a city", color = Color.White, fontSize = 40.sp)

                for (city in cities) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 20.dp,
                                vertical = 10.dp
                            )
                            .clickable {
                                pickCity(city)
                            },
                        elevation = 5.dp
                    ) {
                        Row{
                            Image(
                                painter = painterResource(id = R.drawable.vietnam),
                                contentDescription = "Vietnam flag",
                                modifier = Modifier.size(32.dp),
                                contentScale = ContentScale.Fit
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(text = city, fontSize = 20.sp) //xuất ra danh sách các thành phố
                        }

                    }
                }
            }
        }
    }

}


