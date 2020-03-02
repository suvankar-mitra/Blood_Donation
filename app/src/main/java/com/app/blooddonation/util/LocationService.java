package com.app.blooddonation.util;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class LocationService {

    private static String TAG = "LocationService";

    public static LatLng loadCurrentLocation(AppCompatActivity activity, FusedLocationProviderClient fusedLocationClient) {
        AtomicReference<LatLng> latLngAtomic = new AtomicReference<>();
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(Objects.requireNonNull(activity), location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        Log.d(TAG, "LocationFailure: " + location.getLatitude() + ", " + location.getLongitude());
                        latLngAtomic.set(new LatLng(location.getLatitude(), location.getLongitude()));
                    } else {
                        Log.e(TAG, "loadCurrentLocation: Location coming as null" );
                    }
                    System.out.println("Location: " + location);
                })
                .addOnFailureListener(activity, e -> {
                    Log.d(TAG, "LocationFailure: Failure");
                    Log.e(TAG, "LocationFailure: ", e);
                });
        return latLngAtomic.get();
    }
}
