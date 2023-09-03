package com.tung.travelthere;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    MenuItemAdapter mAdapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Test Activity", "Activity 3 is running");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        recyclerView = findViewById(R.id.rec_menu_item);

        ArrayList<MenuItem> menuItemList = new ArrayList<>();
        menuItemList.add(new MenuItem("Hello world", "Nhut", "123", "456"));
        menuItemList.add(new MenuItem("Good night", "Thi", "123", "456"));
        menuItemList.add(new MenuItem("Nothing fun", "Hong", "123", "456"));
        menuItemList.add(new MenuItem("Amazing place to visit", "Tung", "123", "456"));
        mAdapter = new MenuItemAdapter(menuItemList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);
    }
}