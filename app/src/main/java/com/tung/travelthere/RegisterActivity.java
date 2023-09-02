package com.tung.travelthere;

import static com.tung.travelthere.controller.AppController.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.tung.travelthere.controller.AppController;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText, nameEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);


        nameEditText = findViewById(R.id.nameEditText);
        ImageButton back = findViewById(R.id.back_btn);
        back.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        Button registerBtn = findViewById(R.id.registerButton);
        registerBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email = emailEditText.getText().toString();
                        String password = passwordEditText.getText().toString();
                        String name = nameEditText.getText().toString();

                        if (TextUtils.isEmpty(email)) {
                            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (TextUtils.isEmpty(password)) {
                            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (TextUtils.isEmpty(name)) {
                            Toast.makeText(getApplicationContext(), "Enter name!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //create user
                        AppController.auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(RegisterActivity.this, task -> {
                                    if (task.isSuccessful()) {
                                        // Registration success
                                        FirebaseUser user = auth.getCurrentUser();
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name) //đặt tên người dùng
                                                .build();

                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), "Registration successful, but failed to set display name. " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Registration failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                }
        );


    }
}