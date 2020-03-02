package com.app.blooddonation;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.blooddonation.interfaces.GreetingRestApi;
import com.app.blooddonation.interfaces.RestApi;
import com.app.blooddonation.models.Constants;
import com.app.blooddonation.models.DonationRequest;
import com.app.blooddonation.models.Greeting;
import com.app.blooddonation.models.User;
import com.app.blooddonation.util.SharedPrefService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.net.HttpURLConnection.HTTP_OK;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private SharedPreferences mPrefs;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);

        mDialog = new ProgressDialog(this);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage("Connecting. Please wait...");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);

        Button getStartedBtn = findViewById(R.id.get_started_btn);

        getStartedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.show();
                checkConnectionHealthyGotoLoginPage();
            }
        });

        checkPermission();

        // check if I am logged in or not
        checkUserLoginState();

    }

    private void checkUserLoginState() {
        Log.d(TAG, "checkUserLoginState: ");
        User user = (User) SharedPrefService.getSharedPref(Constants.USER, User.class, getApplicationContext());
        if(user!=null) {
            //I am logged in
            Log.d(TAG, "onCreate: You are already logged in");
            SharedPrefService.saveToSharedPref(Constants.USER_FULL_NAME, user.getFirstName() + " " + user.getLastName(), getApplicationContext());
            SharedPrefService.saveToSharedPref(Constants.USER_FIRST_NAME, user.getFirstName(), getApplicationContext());
            SharedPrefService.saveToSharedPref(Constants.USER_LAST_NAME, user.getFirstName(), getApplicationContext());
            SharedPrefService.saveToSharedPref(Constants.USER_ID, user.getUserId(), getApplicationContext());
            //getAllUserList();
            gotoHomePage();
        } else {
            Log.d(TAG, "onCreate: You are not logged in");
        }
    }

    /*private void getAllUserList() {
        Log.d(TAG, "getAllUserList: ");
        User user = (User) SharedPrefService.getSharedPref(Constants.USER, User.class, getApplicationContext());
        int userId = user.getUserId();
        Log.d(TAG, "getAllUserList: User id = "+userId);
        Retrofit retrofit = new  Retrofit.Builder()
                .baseUrl(GreetingRestApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestApi restApi = retrofit.create(RestApi.class);
        Call<List<User>> call = restApi.getAllUsersExceptMe(userId);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                Log.d(TAG, "onResponse: " + response);
                if(response.code() ==  HTTP_OK) {
                    List<User> users = response.body();
                    if(users != null) {
                        SharedPrefService.saveToSharedPref(Constants.USER_LIST, users, getApplicationContext());
                        Log.d(TAG, "onResponse: " + users);
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {

            }
        });
    }*/

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
    }

    private void checkConnectionHealthyGotoLoginPage() {
        Retrofit retrofit = new  Retrofit.Builder()
                .baseUrl(GreetingRestApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GreetingRestApi api = retrofit.create(GreetingRestApi.class);

        final Call<Greeting> call = api.getGreeting(getString(R.string.app_name));

        call.enqueue(new Callback<Greeting>() {
            @Override
            public void onResponse(Call<Greeting> call, Response<Greeting> response) {
                Greeting greeting = response.body();
                Log.d(TAG, "onResponse: " + greeting);
                // since server connection is healthy, we can go to login page
                gotoLoginPage();
            }

            @Override
            public void onFailure(Call<Greeting> call, Throwable t) {
                mDialog.dismiss();
                Log.e(TAG, "onFailure: ", t);
                TextView tv = findViewById(R.id.welcome_screen_msg_text);
                tv.setText("Could not connect with server. Check your internet connection!");
            }
        });
    }

    private void gotoLoginPage() {
        mDialog.dismiss();
        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void gotoHomePage() {
        finish();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}
