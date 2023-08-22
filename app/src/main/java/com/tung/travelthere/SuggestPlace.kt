package com.tung.travelthere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

        MaterialTheme{
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Find your place"
                )

                Spacer(
                    Modifier.height(20.dp)
                )

                Layouts.getSingleton().CustomTextFieldWithStroke(
                    value = searchPlace,
                    onValueChange = { searchPlace = it },
                    strokeColor = Color.Black,
                    textColor = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        suggestPlace()
    }
}