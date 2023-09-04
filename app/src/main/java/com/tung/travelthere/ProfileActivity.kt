package com.tung.travelthere

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.tung.travelthere.controller.AppController
import com.tung.travelthere.controller.SearchViewModel
import com.tung.travelthere.controller.colorBlue


class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //User đang đăng nhập
        val user = AppController.auth.getCurrentUser() as FirebaseUser

        setContent {
            displayUserProfile(currentUser = user)
        }
    }

    override fun onResume() {
        super.onResume()

        //User đang đăng nhập
        val user = AppController.auth.getCurrentUser() as FirebaseUser

        setContent {
            displayUserProfile(currentUser = user)
        }
    }
    @Composable
    fun displayUserName(currentUser: FirebaseUser) {
        Row(
            modifier = Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth()
        ) {
            var text by remember { mutableStateOf(currentUser.displayName.toString()) }
            Column() {
                Text(text = "Name:",
                    fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text)
            }

            IconButton(onClick = {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@ProfileActivity)
                builder.setTitle("Enter your name")

                val input = EditText(this@ProfileActivity)
                input.inputType = InputType.TYPE_CLASS_TEXT
                builder.setView(input)

                builder.setPositiveButton("Save",
                    DialogInterface.OnClickListener { dialog, which ->
                        val newName = input.text.toString()
                        if (!newName.isEmpty()) {
                            text = newName
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(newName)
                                .build()
                            currentUser!!.updateProfile(profileUpdates)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d(TAG, "User profile updated.")
                                    }
                                }
                        } else {
                            Toast.makeText(
                                this@ProfileActivity,
                                "Please enter a valid name",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })

                builder.setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

                builder.show()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.edit),
                    tint = Color(0xff365875),
                    contentDescription = "Edit name"
                )
            }
        }
    }

    @Composable
    fun displayUserEmail(currentUser: FirebaseUser) {
        Row(
            modifier = Modifier
                .padding(vertical = 10.dp)
        ) {
            Column() {
                Text(text = "Email:",
                    fontWeight = FontWeight.Bold,
                fontSize = 20.sp)
                Text(text = currentUser.getEmail().toString())
            }
        }
    }


    fun logout(changePass: Boolean) {
        AppController.auth.signOut()
        val intent = Intent(this@ProfileActivity, RegisterLoginActivity::class.java)
        if(!changePass) intent.putExtra("LogOut", true)
        startActivity(intent)
        finish()
    }

    @Composable
    fun changePassword(currentUser: FirebaseUser) {
        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.blue)),
            modifier = Modifier.padding(vertical = 10.dp),
            onClick = {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@ProfileActivity)
            builder.setTitle("Enter your new password")

            val input = EditText(this@ProfileActivity)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            builder.setPositiveButton("Save",
                DialogInterface.OnClickListener { dialog, which ->
                    val newPass = input.text.toString()
                    if (!newPass.isEmpty()) {
                        currentUser!!.updatePassword(newPass)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(TAG, "User password updated.")
                                    Toast.makeText(
                                        this@ProfileActivity,
                                        "Password changed! Please log in with new password!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    logout(true)
                                }
                            }
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Please enter a valid password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

            builder.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

            builder.show()
        }) {
            Text(text = "Change password", color = Color.White)
        }
    }

    @Composable
    fun displayUserProfile(currentUser: FirebaseUser) {
        Column() {
            Box(
                modifier = Modifier.size(32.dp)
            ) {
                FloatingActionButton(onClick = { finish() },
                    backgroundColor = Color.White,
                    content = {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colorBlue
                        )
                    })
            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 25.dp),
                text = "Profile",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 25.dp, vertical = 25.dp)
            ) {
                displayUserName(currentUser = currentUser)
                displayUserEmail(currentUser = currentUser)
                changePassword(currentUser = currentUser)
                //log out
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.blue)),
                    modifier = Modifier.padding(vertical = 10.dp),
                    onClick = {
                    logout(false)
                }) {
                    Text(text = "Log out", color = Color.White)
                }
            }
        }
    }
}