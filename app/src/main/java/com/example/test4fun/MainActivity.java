package com.example.test4fun;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button hungryButton = findViewById(R.id.hungryButton);
        hungryButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            startActivity(intent);
        });

        Button restoButton = findViewById(R.id.restoButton);
        restoButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Resto.class);
            startActivity(intent);
        });
    }

}
