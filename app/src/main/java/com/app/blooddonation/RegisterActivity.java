package com.app.blooddonation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.app.blooddonation.interfaces.GreetingRestApi;
import com.app.blooddonation.interfaces.RestApi;
import com.app.blooddonation.models.Constants;
import com.app.blooddonation.models.User;
import com.app.blooddonation.util.LocationService;
import com.app.blooddonation.util.SharedPrefService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.net.HttpURLConnection.HTTP_OK;

public class RegisterActivity extends AppCompatActivity {

    private EditText mFirstName, mLastName, mEmail, mPassword, mPassword2, mPhoneNo;
    private Button mRegister, mLogin;
    private TextView mError;
    private Spinner mBloodType, mGender;
    private ProgressDialog mDialog;

    private String TAG = "RegisterActivity";

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String PHONE_PATTER = "^[0-9]{10}$";

    private Retrofit retrofit;

    public RegisterActivity() {

        retrofit = new  Retrofit.Builder()
                .baseUrl(GreetingRestApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mFirstName = findViewById(R.id.register_first_name);
        mLastName = findViewById(R.id.register_last_name);
        mEmail = findViewById(R.id.register_email);
        mPassword = findViewById(R.id.register_password);
        mPassword2 = findViewById(R.id.register_password_2);
        mPhoneNo = findViewById(R.id.register_phone);
        mError = findViewById(R.id.register_err);
        mRegister = findViewById(R.id.register_btn);
        mLogin = findViewById(R.id.register_login_btn);
        mBloodType = findViewById(R.id.register_blood_type);
        mGender = findViewById(R.id.register_gender);

        mDialog = new ProgressDialog(this);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage("Signing up. Please wait...");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mError.setText("");
                if(validate()) {
                    mDialog.show();
                    User user = new User();
                    user.setEmail(mEmail.getText().toString().trim());
                    user.setPassword(mPassword.getText().toString().trim());
                    user.setFirstName(mFirstName.getText().toString().trim());
                    user.setLastName(mLastName.getText().toString().trim());
                    user.setPhoneNo(mPhoneNo.getText().toString().trim());
                    user.setBloodType(mBloodType.getSelectedItem().toString().trim());
                    user.setRole(2); // default role
                    user.setGender(mGender.getSelectedItem().toString());

                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(RegisterActivity.this);
                    LatLng latLng = LocationService.loadCurrentLocation(RegisterActivity.this, fusedLocationClient);
                    Log.d(TAG, "onClick: LatLng::: " + latLng);
                    if(latLng != null)
                        user.setAreaCode(latLng.latitude+","+latLng.longitude);
                    else
                        user.setAreaCode("12.841586,77.649912");

                    RestApi restApi = retrofit.create(RestApi.class);
                    final Call<User> call = restApi.register(user);

                    Log.d(TAG, "onClick: Retrofit registering: " + user);

                    call.enqueue(new Callback<User>() {

                        @Override
                        public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                            mDialog.dismiss();
                            int responseCode = response.code();
                            if(responseCode == HttpURLConnection.HTTP_CREATED) {
                                User user = response.body();
                                Log.d(TAG, "onResponse: Response --" + response.toString());
                                SharedPrefService.saveToSharedPref(Constants.USER, user, getApplicationContext());
                                if(user!=null) {
                                    SharedPrefService.saveToSharedPref(Constants.USER_FULL_NAME, user.getFirstName() + " " + user.getLastName(), getApplicationContext());
                                    SharedPrefService.saveToSharedPref(Constants.USER_FIRST_NAME, user.getFirstName(), getApplicationContext());
                                    SharedPrefService.saveToSharedPref(Constants.USER_LAST_NAME, user.getFirstName(), getApplicationContext());
                                    SharedPrefService.saveToSharedPref(Constants.USER_ID, user.getUserId(), getApplicationContext());
                                    getAllUserList();
                                }
                                Toast.makeText(RegisterActivity.this, "Successfully registered.", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onResponse: Registered " + user);
                                gotoHomePage();
                            } else {
                                try {
                                    if (response.errorBody() != null) {
                                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                                        Log.d(TAG, "onResponse: "+jObjError.toString());
                                        mError.setText(jObjError.getString("message"));
                                    }
                                } catch (IOException | JSONException e) {
                                    e.printStackTrace();
                                    String msg = "Error Code: " + responseCode;
                                    mError.setText(msg);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            String msg = "Unexpected error occurred. Try again after some time.";
                            mError.setText(msg);
                            mDialog.dismiss();
                        }
                    });
                }
            }
        });


        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoLoginPage();
            }
        });

    }

    private void getAllUserList() {
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
                if(response.code() ==  HTTP_OK) {
                    List<User> users = response.body();
                    if(users != null) {
                        SharedPrefService.saveToSharedPref(Constants.USER_LIST, users, getApplicationContext());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {

            }
        });
    }

    private boolean validate() {
        if(mFirstName.getText().toString().isEmpty()) {
            mError.setText("First Name cannot be empty");
            return false;
        }

        if(mLastName.getText().toString().isEmpty()) {
            mError.setText("Last Name cannot be empty");
            return false;
        }

        String email = mEmail.getText().toString();
        if(email.isEmpty()) {
            mError.setText("Email cannot be empty");
            return false;
        }
        if(!email.matches(EMAIL_PATTERN)) {
            mError.setText("That is not a valid email address");
            return false;
        }

        if(mPassword.getText().toString().length()< 6) {
            mError.setText("Password must be at least 6 character long");
            return false;
        }

        if(!passwordsMatch()) {
            mError.setText("Passwords do not match! Try again");
            return false;
        }

        String phone = mPhoneNo.getText().toString();

        if(phone.length()< 10 || !phone.matches(PHONE_PATTER)) {
            mError.setText("Invalid phone number");
            return false;
        }


        return true;
    }

    private boolean passwordsMatch() {
        String pass1 = mPassword.getText().toString();
        String pass2 = mPassword2.getText().toString();
        return pass1.equals(pass2);
    }

    private void gotoHomePage() {
        finish();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void gotoLoginPage() {
        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
