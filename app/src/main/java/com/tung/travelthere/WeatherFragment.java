package com.tung.travelthere;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

public class WeatherFragment extends Fragment {

    private TextView textCityName, textTemperature, textCondition;
    private TextInputEditText editTextCityName;
    private ImageView imgCondition, imgSearch;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        textCityName = getView().findViewById(R.id.text_weather_city_name);
        textTemperature = getView().findViewById(R.id.text_weather_temperature);
        textCondition = getView().findViewById(R.id.text_weather_condition);
        editTextCityName = getView().findViewById(R.id.edit_text_city_name);
        imgCondition = getView().findViewById(R.id.image_weather_condition);
        imgSearch = getView().findViewById(R.id.image_weather_search);


        return inflater.inflate(R.layout.fragment_weather, container, false);
    }
}