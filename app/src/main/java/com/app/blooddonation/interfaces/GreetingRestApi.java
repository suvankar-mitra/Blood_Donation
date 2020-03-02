package com.app.blooddonation.interfaces;

import com.app.blooddonation.models.Greeting;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GreetingRestApi {
    String BASE_URL = "http://192.168.43.31:8080/api/";

    @GET("greeting")
    Call<Greeting> getGreeting(@Query("name") String name);
}




