package com.tung.travelthere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.tung.travelthere.objects.Location

class SuggestPlace : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            suggestPlace()
        }
    }

    @Composable
    fun suggestPlace(){
        var searchPlace by remember { mutableStateOf("") }

        MaterialTheme {
            Column {
                Text(
                    text = "Find your place"
                )

                TextField(value = searchPlace, onValueChange = )
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        suggestPlace()
    }
}