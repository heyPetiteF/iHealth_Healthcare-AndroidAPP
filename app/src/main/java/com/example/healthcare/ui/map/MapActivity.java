package com.example.healthcare.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.app.NavUtils;

import com.example.healthcare.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.gms.common.api.Status;

import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Set up the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Map");
            actionBar.setDisplayHomeAsUpEnabled(true); // Show the back button
        }

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize the Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(this);

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize the AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // Handle the selected place
                Log.i("MapActivity", "Place: " + place.getName() + ", " + place.getId());
                if (place.getLatLng() != null) {
                    mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                // Handle the error
                Log.e("MapActivity", "An error occurred: " + status);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable My Location layer and control on the map
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                            findNearbyPharmacies(currentLocation);
                        }
                    });
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void findNearbyPharmacies(LatLng location) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        placesClient.findCurrentPlace(request).addOnSuccessListener(
                response -> {
                    mMap.clear(); // Clear existing markers
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        Place place = placeLikelihood.getPlace();
                        LatLng latLng = place.getLatLng();
                        if (latLng != null && place.getName().toLowerCase().contains("pharmacy")) {
                            mMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
                        }
                    }
                }
        ).addOnFailureListener(exception -> exception.printStackTrace());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, location -> {
                                if (location != null) {
                                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                                    findNearbyPharmacies(currentLocation);
                                }
                            });
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
