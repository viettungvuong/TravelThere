package com.tung.travelthere;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterLoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_register_activity);

        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        ImageButton registerImageButton = findViewById(R.id.registerImageButton);
        ImageButton loginImageButton = findViewById(R.id.loginImageButton);

        emailEditText.setText("123");

        registerImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle register button click
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                emailEditText.setText("registered");
                // Perform registration logic
                // You can call your registration function or navigate to the registration activity here
            }
        });

        loginImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle login button click
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                emailEditText.setText("logged in");
                // Perform login logic
                // You can call your login function or navigate to the login activity here
            }
        });
    }
}

