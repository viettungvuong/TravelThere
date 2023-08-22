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
    @Composable
    fun CustomTextFieldWithStroke(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        shape: Shape = RectangleShape,
        strokeWidth: Int = 1,
        strokeColor: Color = Color.Black,
        textColor: Color = LocalContentColor.current.copy(alpha = ContentAlpha.high)
    ) {
        Box(
            modifier = modifier
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = LocalTextStyle.current.copy(color = textColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                decorationBox = { innerTextField ->
                    Column {
                        innerTextField()
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(strokeWidth.dp)
                                .background(strokeColor)
                        )
                    }
                }
            )
        }
    }
}