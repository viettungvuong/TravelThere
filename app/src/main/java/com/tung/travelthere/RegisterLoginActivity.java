package com.tung.travelthere;

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
    private EditText emailEditText,passwordEditText;
    private Button registerButton, loginButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_register_activity);

        auth=FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                //create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterLoginActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Registration success
                                Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(getApplicationContext(), "Registration failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
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
