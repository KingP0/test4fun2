package com.example.test4fun;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;


public class SecondActivity extends AppCompatActivity implements OnMapReadyCallback {

    private PlacesClient placesClient;
    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // Initialize the Places API with your API key
        String apiKey = "AIzaSyCJ0WliVHV7m8SKYnxBYrH4kLW9Tpr8no8";
        Places.initialize(getApplicationContext(), apiKey);

        // Initialize the Google Maps API
        MapsInitializer.initialize(getApplicationContext());

        // Create a PlacesClient object
        placesClient = Places.createClient(this);

        // Get the MapFragment and initialize the Google Map object
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Enable the zoom controls on the map
        googleMap.getUiSettings().setZoomControlsEnabled(true);

    }
}