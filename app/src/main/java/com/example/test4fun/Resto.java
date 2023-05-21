package com.example.test4fun;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.MapsInitializer;

public class Resto extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private PlacesClient placesClient;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resto_activity);

        // Initialize the Places API with your API key
        String apiKey = "AIzaSyCJ0WliVHV7m8SKYnxBYrH4kLW9Tpr8no8";
        Places.initialize(getApplicationContext(), apiKey);

        // Create a PlacesClient object
        placesClient = Places.createClient(this);

        // Get the RecyclerView for the restaurant list
        recyclerView = findViewById(R.id.restaurantList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create an empty adapter and attach it to the RecyclerView
        RestaurantAdapter adapter = new RestaurantAdapter(new ArrayList<>(), new ArrayList<>());
        recyclerView.setAdapter(adapter);


        // Request permission for location access if not granted already
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            recyclerView.setAdapter(new RestaurantAdapter(new ArrayList<>(), new ArrayList<>()));
            // Use the Google Places API to get a list of nearby restaurants
            findNearbyRestaurants();
        }
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

    private void findNearbyRestaurants() {
        // Define the fields to return for each PlaceLikelihood
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.TYPES);

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

                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Location userLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                List<Place> nearbyRestaurants = new ArrayList<>();

                double validDistance = 1000; // Définir la distance de validité à 1000 mètres

                for (PlaceLikelihood likelihood : likelihoods) {
                    Place place = likelihood.getPlace();

                    if (place.getTypes().contains(Place.Type.RESTAURANT)) {
                        LatLng placeLatLng = place.getLatLng();

                        if (placeLatLng != null && userLocation != null) {
                            float[] distanceResults = new float[1];
                            Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(),
                                    placeLatLng.latitude, placeLatLng.longitude, distanceResults);

                            // Vérifier si la distance est inférieure à la distance de validité
                            if (distanceResults.length == 1 && distanceResults[0] <= validDistance) {
                                // Ajouter le restaurant à la liste s'il est dans le périmètre de validité
                                nearbyRestaurants.add(place);
                            }
                        }
                    }
                }



                // Set the adapter for the RecyclerView to display the list of restaurants
                RestaurantAdapter adapter = new RestaurantAdapter(restaurantNames, restaurantAddresses);
                recyclerView.setAdapter(adapter);
            } else {
                Exception exception = task.getException();
                if (exception != null) {
                    Log.e("Resto", "Exception: " + exception.getMessage());
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, use the Google Places API to get a list of nearby restaurants
                findNearbyRestaurants();
            } else {
                // Permission denied, show a message to the user and finish the activity
                Toast.makeText(this, "Location permission is required to use this feature.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}