package com.tung.travelthere

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

class Layouts {
    private constructor()

    companion object{
        private var singleton: Layouts?=null

        @JvmStatic
        fun getSingleton(): Layouts{
            if (this.singleton==null)
            {
                this.singleton= Layouts()
            }
            return singleton!!
        }
    }

}