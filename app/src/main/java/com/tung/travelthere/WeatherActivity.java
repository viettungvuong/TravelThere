package com.tung.travelthere;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class WeatherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new WeatherFragment()).commit();
    }
}