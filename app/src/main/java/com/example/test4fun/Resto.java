package com.example.test4fun;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Resto extends AppCompatActivity implements OnMapReadyCallback {

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

        // Get the RecyclerView for the restaurant list
        recyclerView = findViewById(R.id.restaurantList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Request permission for location access if not granted already
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // Use the Google Places API to get a list of nearby restaurants
            findNearbyRestaurants();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Enable the zoom controls on the map
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Request permission for location access if not granted already
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // Enable the user's current location on the map
            googleMap.setMyLocationEnabled(true);
        }
    }

    private void findNearbyRestaurants() {
        // Define the fields to return for each PlaceLikelihood
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS);

        // Use the findCurrentPlace method to get a list of nearby restaurants
        @SuppressWarnings("MissingPermission")
        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(
                FindCurrentPlaceRequest.newInstance(placeFields));

        // Process the list of nearby restaurants
        placeResponse.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FindCurrentPlaceResponse response = task.getResult();
                List<PlaceLikelihood> likelihoods = response.getPlaceLikelihoods();

                // Create a list of restaurant names and addresses
                List<String> restaurantNames = new ArrayList<>();
                List<String> restaurantAddresses = new ArrayList<>();

                for (PlaceLikelihood likelihood : likelihoods) {
                    Place place = likelihood.getPlace();
                    restaurantNames.add(place.getName());
                    restaurantAddresses.add(place.getAddress());
                }

                // Set the list of restaurants in the RecyclerView
                recyclerView.setAdapter(new RestaurantAdapter(restaurantNames, restaurantAddresses));
            } else {
                Log.e("SecondActivity", "Error getting nearby restaurants", task.getException());
            }
        });
    }

    private static class RestaurantAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {
        private final List<String> restaurantNames;
        private final List<String> restaurantAddresses;

        public RestaurantAdapter(List<String> restaurantNames, List<String> restaurantAddresses) {
            this.restaurantNames = restaurantNames;
            this.restaurantAddresses = restaurantAddresses;
        }

        @NonNull
        @Override
        public RestaurantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.restaurant_item, parent, false);
            return new RestaurantViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RestaurantViewHolder holder, int position) {
            String name = restaurantNames.get(position);
            String address = restaurantAddresses.get(position);
            holder.nameTextView.setText(name);
            holder.addressTextView.setText(address);
        }

        @Override
        public int getItemCount() {
            return restaurantNames.size();
        }
    }

    private static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView addressTextView;

        public RestaurantViewHolder(View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.restaurant_name);
            addressTextView = itemView.findViewById(R.id.restaurant_address);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Use the Google Places API to get a list of nearby restaurants
                findNearbyRestaurants();
            }
        }
    }
}