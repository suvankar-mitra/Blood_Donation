package com.app.blooddonation.interfaces;

import com.app.blooddonation.models.DiagnosticReport;
import com.app.blooddonation.models.DonationRequest;
import com.app.blooddonation.models.Hospital;
import com.app.blooddonation.models.LoginToken;
import com.app.blooddonation.models.User;
import com.app.blooddonation.models.UserName;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RestApi {

    @GET("login")
    Call<User> login(@Query("email") String email, @Query("password") String password);

    @POST("register")
    Call<User> register(@Body User user);

    @POST("update")
    Call<User> update(@Body User user);

    @GET("all-users-except")
    Call<List<User>> getAllUsersExceptMe(@Query("userId") int userId);

    @GET("all-hospitals")
    Call<List<Hospital>> getAllHospitals();

    @POST("save-donation")
    Call<DonationRequest> saveDonationRequest(@Body DonationRequest donationRequest);

    @GET("donation-for-requester-id-not-completed")
    Call<DonationRequest> getDonationRequestByMeNotCompleted(@Query("userId") int userId);

    @GET("donation-for-requester-id-all")
    Call<List<DonationRequest>> getDonationRequestByMeAll(@Query("userId") int userId);

    @GET("donation-for-acceptor-id-not-completed")
    Call<DonationRequest> getDonationRequestForAcceptorId(@Query("userId") int userId);

    @GET("donation-for-acceptor-id-all")
    Call<List<DonationRequest>> getDonationRequestForAcceptorIdAll(@Query("userId") int userId);

    @GET("all-donations-not-accepted")
    Call<List<DonationRequest>> getAllDonations();

    @GET("user-name")
    Call<UserName> getUserNameForUserId(@Query("userId") int userId);

    @POST("update-donation")
    Call<DonationRequest> updateDonation(@Body DonationRequest donationRequest);

    @GET("diagnostic-report-for-id")
    Call<DiagnosticReport> getDiagnosticReport(@Query("requestId") int requestId);
}
