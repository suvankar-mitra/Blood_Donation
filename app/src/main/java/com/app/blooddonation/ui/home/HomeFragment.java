package com.app.blooddonation.ui.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.app.blooddonation.R;
import com.app.blooddonation.fragments.SearchBloodTypeFragment;
import com.app.blooddonation.interfaces.GreetingRestApi;
import com.app.blooddonation.interfaces.RestApi;
import com.app.blooddonation.models.Constants;
import com.app.blooddonation.models.DonationRequest;
import com.app.blooddonation.models.Hospital;
import com.app.blooddonation.models.MapMarker;
import com.app.blooddonation.models.User;
import com.app.blooddonation.util.MiscUtil;
import com.app.blooddonation.util.ObjectSerializer;
import com.app.blooddonation.util.SharedPrefService;
import com.app.blooddonation.util.VectorToBitmap;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.net.HttpURLConnection.HTTP_OK;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private final String TAG = "HomeFragment";

    private MapView mapView;
    private GoogleMap googleMap;
    private TextView mGreeting;

    private FusedLocationProviderClient fusedLocationClient;

    private HomeViewModel homeViewModel;

    private static final String MAP_VIEW_BUNDLE_KEY = "AIzaSyC3ln7T2xYXHnnPAKZ4qGYlviGiaQumZoM";

    private Retrofit retrofit;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        String userName = (String) SharedPrefService.getSharedPref(Constants.USER_FIRST_NAME, String.class, getContext());
        String greeting = "Good " + MiscUtil.getTimeOfDay() + ", " + userName;
        mGreeting = root.findViewById(R.id.home_user_greeting);
        mGreeting.setText(greeting);

        /*
         * Everything related to MapView - START
         */

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
        // Map view
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = root.findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        ImageButton mMyLocation = root.findViewById(R.id.map_my_location);
        mMyLocation.setOnClickListener(v -> {
            loadCurrentLocation();
        });

        /*
         * Everything related to MapView - END
         */

        LinearLayout home_search_bar = root.findViewById(R.id.home_search_bar);
        home_search_bar.setOnClickListener(v -> {
            root.findViewById(R.id.home_loading_panel).setVisibility(View.VISIBLE);

            Fragment fragment = new SearchBloodTypeFragment();

            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.home_search_frag_container, fragment, "Search").addToBackStack("null").commit();

            root.findViewById(R.id.home_loading_panel).setVisibility(View.GONE);
        });


        retrofit = new  Retrofit.Builder()
                .baseUrl(GreetingRestApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        updateUpcomingAcceptedRequest(root);

        return root;
    }

    private void updateUpcomingAcceptedRequest(View root) {
        ConstraintLayout container = root.findViewById(R.id.home_upcoming_req);
        TextView hospital = root.findViewById(R.id.home_upcoming_req_hospital);
        TextView patient = root.findViewById(R.id.home_upcoming_req_patient);
        TextView due = root.findViewById(R.id.home_upcoming_req_date);
        TextView blood = root.findViewById(R.id.home_upcoming_req_blood_type);
        TextView unit = root.findViewById(R.id.home_upcoming_req_blood_unit);
        ImageButton nav = root.findViewById(R.id.home_upcoming_req_navigate);
        ImageButton done = root.findViewById(R.id.home_upcoming_req_done);
        ImageButton cancel = root.findViewById(R.id.home_upcoming_req_cancel);

        User user = (User) SharedPrefService.getSharedPref(Constants.USER, User.class, getContext());
        int userId = user.getUserId();

        Retrofit retrofit = new  Retrofit.Builder()
                .baseUrl(GreetingRestApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestApi restApi = retrofit.create(RestApi.class);
        final Call<DonationRequest> call = restApi.getDonationRequestForAcceptorId(userId);
        call.enqueue(new Callback<DonationRequest>() {
            @Override
            public void onResponse(@NonNull Call<DonationRequest> call, @NonNull Response<DonationRequest> response) {
                Log.d(TAG, "onResponse: " + response);
                int code = response.code();
                if(code ==  HTTP_OK) {
                    DonationRequest donationRequest = response.body();
                    SharedPrefService.saveToSharedPref(Constants.ACCEPTED_REQUEST, donationRequest, getContext());
                    if (donationRequest != null) {
                        SharedPrefService.saveToSharedPref(Constants.ACCEPTED_REQUEST_ID, donationRequest.getRequestId(), getContext());
                    }
                    container.setVisibility(View.VISIBLE);

                    try {
                        if (donationRequest != null) {
                            hospital.setText(MiscUtil.hospitalIdToName(donationRequest.getHospitalId(), getContext()));
                            patient.setText(donationRequest.getPatientName());
                            due.setText(donationRequest.getDueDate());
                            blood.setText(donationRequest.getBloodType());
                            unit.setText(donationRequest.getUnitsOfBlood()+" Unit");

                            nav.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        String hospitalArea = MiscUtil.hospitalIdToLocation(donationRequest.getHospitalId(), getContext());
                                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                                Uri.parse("http://maps.google.com/maps?daddr="+hospitalArea));
                                        Objects.requireNonNull(getContext()).startActivity(intent);
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            done.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                                            .setTitle("Donation Completed")
                                            .setMessage("Is your donation complete for "
                                                    + donationRequest.getUnitsOfBlood() + " units of "
                                                    + donationRequest.getBloodType() + " blood?")
                                            .setPositiveButton("Yes",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                            updateDonationMarkDoneOrCancel(donationRequest, "DONE");
                                                            container.setVisibility(View.GONE);
                                                        }
                                                    })
                                            .setNegativeButton("No", null).show();
                                }
                            });

                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                                            .setTitle("Donation Cancelling")
                                            .setMessage("Are you  sure you want to cancel this request for "
                                                    + donationRequest.getUnitsOfBlood() + " units of "
                                                    + donationRequest.getBloodType() + " blood?")
                                            .setPositiveButton("Yes",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                            updateDonationMarkDoneOrCancel(donationRequest, "CANCEL");
                                                            container.setVisibility(View.GONE);
                                                        }
                                                    })
                                            .setNegativeButton("No", null).show();
                                }
                            });
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "onResponse: Something is wrong: " + response);
                    container.setVisibility(View.GONE);
                    SharedPrefService.saveToSharedPref(Constants.ACCEPTED_REQUEST, null, getContext());
                    SharedPrefService.saveToSharedPref(Constants.ACCEPTED_REQUEST_ID, 0, getContext());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DonationRequest> call, Throwable t) {
                Toast.makeText(getContext(), "Could not get hospital list from server. Try again later or check your network connectivity.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onFailure: ", t);
                container.setVisibility(View.GONE);
                SharedPrefService.saveToSharedPref(Constants.ACCEPTED_REQUEST, null, getContext());
                SharedPrefService.saveToSharedPref(Constants.ACCEPTED_REQUEST_ID, 0, getContext());
            }
        });

    }

    // update = DONE, CANCEL
    private void updateDonationMarkDoneOrCancel(DonationRequest donationRequest, String update) {
        if(update.equalsIgnoreCase("done")) {
            donationRequest.setDonationCompleted(true);
            donationRequest.setDonationCompleteDateTime(MiscUtil.getCurrentTs());
        }
        if(update.equalsIgnoreCase("cancel")) {
            donationRequest.setDonationCompleted(false);
            donationRequest.setAcceptedBy(0);
        }

        Retrofit retrofit = new  Retrofit.Builder()
                .baseUrl(GreetingRestApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestApi restApi = retrofit.create(RestApi.class);
        final Call<DonationRequest> call = restApi.updateDonation(donationRequest);
        call.enqueue(new Callback<DonationRequest>() {
            @Override
            public void onResponse(Call<DonationRequest> call, Response<DonationRequest> response) {
                if(response.code() == HTTP_OK) {
                    SharedPrefService.saveToSharedPref(Constants.ACCEPTED_REQUEST, null, getContext());
                    SharedPrefService.saveToSharedPref(Constants.ACCEPTED_REQUEST_ID, 0, getContext());
                }
            }

            @Override
            public void onFailure(Call<DonationRequest> call, Throwable t) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            Objects.requireNonNull(getContext()), R.raw.gmap_style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        //googleMap.setMinZoomPreference(8);
        loadCurrentLocation();

        loadOtherUsersLocations();

        loadHospitalLocations();
    }

    private void loadHospitalLocations() {
        Retrofit retrofit = new  Retrofit.Builder()
                .baseUrl(GreetingRestApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestApi restApi = retrofit.create(RestApi.class);
        final Call<List<Hospital>> call = restApi.getAllHospitals();
        call.enqueue(new Callback<List<Hospital>>() {
            @Override
            public void onResponse(@NonNull Call<List<Hospital>> call, @NonNull Response<List<Hospital>> response) {
                int code = response.code();
                if(code ==  HTTP_OK) {
                    List<Hospital> hospitals = response.body();
                    if (hospitals != null) {
                        setMapMarkersForHospitals(hospitals);

                        //save the list of hospitals for future use
                        SharedPrefService.saveToSharedPref(Constants.HOSPITALS, hospitals, getContext());
                    }
                } else {
                    Log.d(TAG, "onResponse: Something is wrong: " + response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Hospital>> call, Throwable t) {
                Toast.makeText(getContext(), "Could not get hospital list from server. Try again later or check your network connectivity.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    private void setMapMarkersForHospitals(List<Hospital> hospitals) {
        List<MapMarker> markers = new ArrayList<>();

        for(Hospital hospital : hospitals) {
            String[] latlng = Objects.requireNonNull(hospital.getAreaCode()).split(",");
            if (latlng.length == 2) {
                double lat = Double.parseDouble(latlng[0]);
                double lng = Double.parseDouble(latlng[1]);
                MapMarker mm = new MapMarker(lat, lng, hospital.getHospitalName(), R.drawable.ic_local_hospital_black_24dp);
                mm.setName(hospital.getHospitalName());
                markers.add(mm);
            }
        }

        for (MapMarker mapMarker : markers) {
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(mapMarker.getLat(), mapMarker.getLng()))
                    .title(mapMarker.getName())
                    .icon(VectorToBitmap.bitmapDescriptorFromVector(getActivity(), mapMarker.getIconId())));
        }
    }

    private void loadOtherUsersLocations() {
        int userId = 0;
        try {
            userId = (int) SharedPrefService.getSharedPref(Constants.USER_ID, Integer.class, getContext());
        } catch (Exception e){e.printStackTrace();}
        Retrofit retrofit = new  Retrofit.Builder()
                .baseUrl(GreetingRestApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestApi restApi = retrofit.create(RestApi.class);
        final Call<List<User>> call = restApi.getAllUsersExceptMe(userId);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                int code = response.code();
                if(code ==  HTTP_OK) {
                    List<User> users = response.body();
                    if (users != null) {
                        setMapMarkersForUsers(users);
                    }
                } else {
                    Log.d(TAG, "onResponse: Something is wrong: " + response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, Throwable t) {
                Toast.makeText(getContext(), "Could not get users from server. Try again later or check your network connectivity.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    private void setMapMarkersForUsers(List<User> users) {
        List<MapMarker> markers = new ArrayList<>();

        for(User user : users) {
            String[] latlng = Objects.requireNonNull(user.getAreaCode()).split(",");
            if (latlng.length == 2) {
                double lat = Double.parseDouble(latlng[0]);
                double lng = Double.parseDouble(latlng[1]);
                MapMarker mm = new MapMarker(lat, lng, user.getBloodType(), R.drawable.ic_unknown_blood);
                mm.setName(user.getFirstName() + " " + user.getLastName());
                if (user.getBloodType().equalsIgnoreCase("B+"))
                    mm.setIconId(R.drawable.ic_b_pos);
                else if (user.getBloodType().equalsIgnoreCase("B-"))
                    mm.setIconId(R.drawable.ic_b_neg);
                else if (user.getBloodType().equalsIgnoreCase("AB+"))
                    mm.setIconId(R.drawable.ic_ab_pos);
                else if (user.getBloodType().equalsIgnoreCase("AB-"))
                    mm.setIconId(R.drawable.ic_ab_neg);
                else if (user.getBloodType().equalsIgnoreCase("A+"))
                    mm.setIconId(R.drawable.ic_ab_pos);
                else if (user.getBloodType().equalsIgnoreCase("A-"))
                    mm.setIconId(R.drawable.ic_ab_neg);
                else if (user.getBloodType().equalsIgnoreCase("O+"))
                    mm.setIconId(R.drawable.ic_o_pos);
                else if (user.getBloodType().equalsIgnoreCase("O-"))
                    mm.setIconId(R.drawable.ic_o_neg);
                markers.add(mm);
            }
        }

        for (MapMarker mapMarker : markers) {
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(mapMarker.getLat(), mapMarker.getLng()))
                    .title(mapMarker.getName())
                    .icon(VectorToBitmap.bitmapDescriptorFromVector(getActivity(), mapMarker.getIconId())));
        }
    }

    private void loadCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(Objects.requireNonNull(getActivity()), location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        Log.d(TAG, "onMapReady: " + location.getLatitude() + ", " + location.getLongitude());
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                        // Update in the map view my own location
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title("You");
                        markerOptions.icon(VectorToBitmap.bitmapDescriptorFromVector(getActivity(), R.drawable.ic_location_on_black_24dp));
                        googleMap.addMarker(markerOptions);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

                        // Save my updated location
                        User user = (User) SharedPrefService.getSharedPref(Constants.USER, User.class, getContext());
                        if(!user.getAreaCode().equals(latLng.latitude+","+latLng.longitude)) {
                            updateCurrentLocation(user, latLng);
                        }

                    } else {
                        Log.d(TAG, "onMapReady: null location");
                        // setting default location
                        LatLng latLng = new LatLng(12.841984, 77.645856);

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title("You");
                        //markerOptions.icon(VectorToBitmap.bitmapDescriptorFromVector(getActivity(), R.drawable.ic_b_pos));

                        googleMap.addMarker(markerOptions);

                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                    }
                })
                .addOnFailureListener(getActivity(), e -> {
                    Log.d(TAG, "onMapReady: Failure");
                    Log.e(TAG, "onFailure: ", e);
                });
    }

    private void updateCurrentLocation(User user, LatLng latLng) {
        user.setAreaCode(latLng.latitude+","+latLng.longitude);
        RestApi restApi = retrofit.create(RestApi.class);
        final Call<User> call = restApi.update(user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                int responseCode = response.code();
                if(responseCode == 200) {
                    User user = response.body();
                    SharedPrefService.saveToSharedPref(Constants.USER, user, getContext());
                    //Toast.makeText(getActivity(), "Successfully Updated your current location.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onResponse: updated " + user);
                } else {
                    String msg = "Unexpected error occurred. Try again after some time. Return Code: " + responseCode;
                    Log.e(TAG, "onResponse: " + msg);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                String msg = "Unexpected error occurred. Try again after some time.";
                Log.e(TAG, "onResponse: " + msg, t);
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}