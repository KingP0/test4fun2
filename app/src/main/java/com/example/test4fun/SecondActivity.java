package com.example.test4fun;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

public class SecondActivity extends AppCompatActivity implements OnMapReadyCallback {

    private PlacesClient placesClient;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private final int LOCATION_REQUEST_CODE = 101;

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

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Enable the zoom controls on the map
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        mMap = googleMap;

        // Check for permission to access the location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }

        // If permission is granted, get the current location
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations, this can be null.
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    }
                });

        // Perform a place search for restaurants
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG))
                .build();
        placesClient.findCurrentPlace(request).addOnSuccessListener((response) -> {
            List<PlaceLikelihood> placeLikelihoods = response.getPlaceLikelihoods();

            for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
                Place place = placeLikelihood.getPlace();
                LatLng placeLatLng = place.getLatLng();
                String placeName = place.getName();

                if (placeLatLng != null && placeName != null) {
                    Marker marker = mMap.addMarker(new MarkerOptions().position(placeLatLng).title(placeName));
                    marker.setTag(place.getId());
                }
            }
        });


        // Add marker click listener for restaurants
        mMap.setOnMarkerClickListener(marker -> {
            // Add code to handle marker click event
            String placeId = (String) marker.getTag();
            if (placeId != null) {
                Toast.makeText(SecondActivity.this, "Marker Clicked: " + placeId, Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if permission is granted and call onMapReady() to get the last known location
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onMapReady(mMap);
        }
    }
}
