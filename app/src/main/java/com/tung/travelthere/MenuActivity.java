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
        menuItemList.add(new MenuItem("Hello world", "Nhut", "https://cdn-icons-png.flaticon.com/128/2171/2171990.png", "https://lh6.googleusercontent.com/gJ9xrnpqP5_7AepeydfQ_XMGQpXzkfQHT0Du1OFo14pDswb18dSpm_QIAQXJ98zIYihOO4IIqJObqjdgUKeURlWIOYyn3A2WVZ1GDD5cJ3XSIXuqAMpTt8R9OdPlLAiIW1DjsyeuLF0Cc5pFpuNk"));
        menuItemList.add(new MenuItem("Good night", "Thi", "https://cdn-icons-png.flaticon.com/128/2171/2171990.png", "https://statics.vinpearl.com/du-lich-vinh-Ha-Long-hinh-anh1_1625911963.jpg"));
        menuItemList.add(new MenuItem("Nothing fun", "Hong", "https://cdn-icons-png.flaticon.com/128/2171/2171990.png", "https://images2.thanhnien.vn/Uploaded/phucndh/2022_04_14/a4-5211.jpg"));
        menuItemList.add(new MenuItem("Amazing place to visit", "Tung", "https://cdn-icons-png.flaticon.com/128/2171/2171990.png", "https://upload.wikimedia.org/wikipedia/commons/c/cc/Ngomon2.jpg"));
        mAdapter = new MenuItemAdapter(menuItemList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);
    }
}