package com.tung.travelthere;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

public class WeatherFragment extends Fragment {

    private TextView textCityName, textTemperature, textCondition;
    private TextInputEditText editTextCityName;
    private ImageView imgCondition, imgSearch;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;
    private View mView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("TAG", "Run here 1");

        mView = inflater.inflate(R.layout.fragment_weather, container, false);
        textCityName = mView.findViewById(R.id.text_weather_city_name);
        textTemperature = mView.findViewById(R.id.text_weather_temperature);
        textCondition = mView.findViewById(R.id.text_weather_condition);
        editTextCityName = mView.findViewById(R.id.edit_text_city_name);
        imgCondition = mView.findViewById(R.id.image_weather_condition);
        imgSearch = mView.findViewById(R.id.image_weather_search);
        Log.d("TAG", "Run here 2");

        locationManager = (LocationManager) this.getContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        cityName = getCityName(location.getLongitude(), location.getLatitude());
        getWeatherInfo(cityName);

        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = editTextCityName.getText().toString();
                if (city.isEmpty())
                {
                    Toast.makeText(getContext(), "Please enter the city name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    textCityName.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });

        return mView;
    }


    private String getCityName(double longitude, double latitude){
        String cityName = "Not found";
        Geocoder gcd = new Geocoder(getActivity().getBaseContext(), Locale.getDefault());
        Log.d("TAG city", Double.toString(longitude));
        Log.d("TAG city", Double.toString(latitude));
        try{
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);
            for (Address adr : addresses)
            {
                if (adr != null)
                {
                    String city = adr.getLocality();
                    if (city != null && !city.equals(""))
                    {
                        cityName = city;
                    }
                    else
                    {
                        Log.d("TAG", "City not found");
                        Toast.makeText(this.getContext(), "City not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return cityName;

    }
    private void getWeatherInfo(String cityName)
    {
        String url = "https://api.weatherapi.com/v1/forecast.json?key=50224d22b9804f92a1b94202230309&q="+cityName+"&days=1&aqi=yes&alerts=yes";
        textCityName.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(this.getContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    textTemperature.setText(temperature + "°C");
                    //int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionImg = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("https:".concat(conditionImg)).into(imgCondition);
                    textCondition.setText(condition);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //parseVolleyError(error);
                Log.d("TAG weather", "Something Error");
                Log.d("TAG weather", error.getMessage());
                Toast.makeText(getContext(), "Please enter a valid city name", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    public void parseVolleyError(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, "utf-8");
            JSONObject data = new JSONObject(responseBody);
            JSONArray errors = data.getJSONArray("errors");
            JSONObject jsonMessage = errors.getJSONObject(0);
            String message = jsonMessage.getString("message");
            Log.d("json error", message);
        } catch (JSONException e) {
        } catch (UnsupportedEncodingException errorr) {
        }
    }
}