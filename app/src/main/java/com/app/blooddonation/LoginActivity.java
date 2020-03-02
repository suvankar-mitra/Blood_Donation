package com.app.blooddonation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.app.blooddonation.interfaces.GreetingRestApi;
import com.app.blooddonation.interfaces.RestApi;
import com.app.blooddonation.models.Constants;
import com.app.blooddonation.models.User;
import com.app.blooddonation.util.SharedPrefService;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "LoginActivity";

    private Button mRegisterBtn;
    private Button mLoginBtn;
    private EditText mEmail;
    private EditText mPassword;
    private TextView mError;

    private SharedPreferences mPrefs;

    private Retrofit retrofit;

    public LoginActivity() {
        retrofit = new  Retrofit.Builder()
                .baseUrl(GreetingRestApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);

        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
        mRegisterBtn = findViewById(R.id.login_register_btn);
        mLoginBtn = findViewById(R.id.login_btn);
        mError = findViewById(R.id.login_error_msg);

        mRegisterBtn.setOnClickListener(v -> gotoRegisterPage());

        mLoginBtn.setOnClickListener(v -> {
            final String email = mEmail.getText().toString();
            final String password = mPassword.getText().toString();

            mError.setText("");

            if(email.isEmpty() || password.isEmpty()) {
                String msg = "Username / Password cannot be empty.";
                mError.setText(msg);
                return;
            }

            RestApi restApi = retrofit.create(RestApi.class);
            final Call<User> call = restApi.login(email, password);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    int responseCode = response.code();
                    Log.d(TAG, "onResponse: response -- " + response);
                    if(responseCode == HttpURLConnection.HTTP_OK) {
                        User user = response.body();
                        Log.d(TAG, "onResponse: " + user);

                        // save the token to verify user logged in or not
                        SharedPrefService.saveToSharedPref(Constants.USER, user, getApplicationContext());
                        SharedPrefService.saveToSharedPref(Constants.USER_FULL_NAME, user.getFirstName() + " " + user.getLastName(), getApplicationContext());
                        SharedPrefService.saveToSharedPref(Constants.USER_FIRST_NAME, user.getFirstName(), getApplicationContext());
                        SharedPrefService.saveToSharedPref(Constants.USER_LAST_NAME, user.getFirstName(), getApplicationContext());
                        SharedPrefService.saveToSharedPref(Constants.USER_ID, user.getUserId(), getApplicationContext());
                        /*SharedPreferences.Editor prefsEditor = mPrefs.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(user);
                        prefsEditor.putString(Constants.USER, json);
                        prefsEditor.apply();*/

                        Log.d(TAG, "onResponse: Shared pref created: " + user);

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
                            Log.e(TAG, "onResponse: ", e);
                        }
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    String msg = "Unexpected failure occurred. Try again after some time.";
                    mError.setText(msg);
                    Log.e(TAG, "onFailure: ", t);
                }
            });
        });
    }

    private void gotoHomePage() {
        finish();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void gotoRegisterPage() {
        finish();
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
