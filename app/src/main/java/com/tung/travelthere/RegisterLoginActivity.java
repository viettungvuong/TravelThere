package com.tung.travelthere;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.widget.Toast;


public class RegisterLoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button registerButton, loginButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        auth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterLoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle login button click
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Sign in the user with the provided email and password
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterLoginActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Login success
                                Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_SHORT).show();

                                // You can navigate to a new activity here if needed
                                Intent intent = new Intent(RegisterLoginActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();

                            } else {
                                // Login failed
                                Toast.makeText(getApplicationContext(), "Login failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        System.exit(0);//thoát khỏi app luôn
    }
}
