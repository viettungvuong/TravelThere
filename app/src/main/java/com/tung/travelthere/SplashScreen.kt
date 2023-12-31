package com.tung.travelthere

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.tung.travelthere.controller.*
import com.tung.travelthere.objects.City
import com.tung.travelthere.ui.theme.TravelThereTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

const val LOCATION_ENABLE_REQUEST_CODE = 123

class SplashScreen : ComponentActivity() {
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) { //không có permission
                finishAffinity()
                System.exit(0) //thoát khỏi app luôn //thoát khỏi app
            }
        }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Places.initialize(applicationContext, apiKey)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            TravelThereTheme() {
                Greeting()
            }
        }

    }

    override fun onResume() {
        super.onResume()

        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            if (hasLocationPermission()) {
                if (!isLocationEnabled(this@SplashScreen)) {
                    //chưa bật location services thì yêu cầu người dùng bật
                    requestLocationEnable(this@SplashScreen)
                } else {
                    runOnUiThread{
                        Toast.makeText(this@SplashScreen,"Please wait",Toast.LENGTH_LONG).show()
                    }

                    getCurrentPosition(fusedLocationClient, this@SplashScreen) {

                        CoroutineScope(Dispatchers.Main).launch { //chạy trong main thread luôn load xong rồi vào
                            City.getSingleton().locationsRepository.refreshLocations(true)

                            val intent = Intent(this@SplashScreen, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }

            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    //kiểm tra bật location service chưa
    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = ContextCompat.getSystemService(
            context,
            LocationManager::class.java
        ) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    //hộp thoại yêu cầu bật location services
    private fun requestLocationEnable(activity: Activity) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Location Services Required")
        builder.setMessage("Please enable Location Services to use this feature.")
        builder.setPositiveButton("Go to Settings") { dialog, _ ->
            //mở location settings
            val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            activity.startActivityForResult(intent, LOCATION_ENABLE_REQUEST_CODE)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()

            val intent = Intent(this, ChooseCity::class.java)
            startActivity(intent)
            finish() //cho người dùng tự chọn thành phố
        }
        runOnUiThread {
            builder.show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOCATION_ENABLE_REQUEST_CODE) {
            //nếu đã bật location services
            if (isLocationEnabled(this)) {
                Toast.makeText(
                    this,
                    "Application will be restarted to reinitialize",
                    Toast.LENGTH_LONG
                ).show()
                restartApp(this) //restart vì khi bật location phải mất tgian để android để lấy địa điểm
            } else {
                Toast.makeText(
                    this,
                    "Location service is required to use this app",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(this, ChooseCity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}

@Composable
fun Greeting() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff3266a8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.travel),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(32.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
            
            LoadingAnimation(color = Color.White)

        }
    }
}


