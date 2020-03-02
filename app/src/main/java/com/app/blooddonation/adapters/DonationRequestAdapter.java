package com.app.blooddonation.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.app.blooddonation.R;
import com.app.blooddonation.interfaces.GreetingRestApi;
import com.app.blooddonation.interfaces.RestApi;
import com.app.blooddonation.models.Constants;
import com.app.blooddonation.models.DonationRequest;
import com.app.blooddonation.models.User;
import com.app.blooddonation.models.UserName;
import com.app.blooddonation.util.MiscUtil;
import com.app.blooddonation.util.SharedPrefService;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.net.HttpURLConnection.HTTP_OK;

public class DonationRequestAdapter extends RecyclerView.Adapter<DonationRequestAdapter.MyViewHolder> {

    private final String TAG = "DonationRequestAdapter";
    private List<DonationRequest> donationRequestList;
    private Context mContext;

    public DonationRequestAdapter(List<DonationRequest> donationRequestList, Context context) {
        this.donationRequestList = donationRequestList;
        mContext = context;
    }

    @NonNull
    @Override
    public DonationRequestAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.donate_notification_list_view, parent, false);

        return new DonationRequestAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationRequestAdapter.MyViewHolder holder, int position) {
        DonationRequest donationRequest = donationRequestList.get(position);
        holder.bloodType.setText(donationRequest.getBloodType());
        holder.date.setText(donationRequest.getDueDate());
        holder.patient.setText(donationRequest.getPatientName());
        try {
            holder.hospital.setText(MiscUtil.hospitalIdToName(donationRequest.getHospitalId(), mContext));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        holder.units.setText(donationRequest.getUnitsOfBlood() + " Units");
        holder.purpose.setText(donationRequest.getPurpose());
        holder.requestedBy.setText(donationRequest.getRequestedByName());
        setUserNameForId(position);
        holder.time.setText(donationRequest.getCreateDateTime());

        int acceptedId = 0;
        try {
            acceptedId =(int) SharedPrefService.getSharedPref(Constants.ACCEPTED_REQUEST_ID, Integer.class, mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(acceptedId > 0) {
            // I dont want to see any other donate button if I accepted one
            holder.donate.setEnabled(false);
        } else {
            holder.donate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(mContext)
                            .setTitle("Donation Request")
                            .setMessage("Do you want to accept this request by "
                                    + donationRequest.getRequestedByName() + " for "
                                    + donationRequest.getUnitsOfBlood() + " units of "
                                    + donationRequest.getBloodType() + " blood?")
                            .setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            // update backend && save locally
                                            int userId = (int) SharedPrefService.getSharedPref(Constants.USER_ID, Integer.class, mContext);
                                            ;
                                            donationRequest.setAcceptedBy(userId);
                                            donationRequest.setAcceptedDateTime(MiscUtil.getCurrentTs());
                                            updateDonation(donationRequest);

                                            holder.donate.setEnabled(false);
                                            holder.donate.setText("ACCEPTED");
                                            Toast.makeText(mContext, "You've accepted this request", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                                    Uri.parse("http://maps.google.com/maps?daddr=12.854625,77.662423"));
                                            mContext.startActivity(intent);
                                        }
                                    })
                            .setNegativeButton("No", null).show();
                }
            });
        }
    }

    private void setUserNameForId(int pos) {
        DonationRequest donationRequest = donationRequestList.get(pos);
        if(donationRequest.getRequestedByName() != null)
            return;
        int userId = donationRequest.getRequestedBy();
        Retrofit retrofit = new  Retrofit.Builder()
                .baseUrl(GreetingRestApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestApi restApi = retrofit.create(RestApi.class);
        final Call<UserName> call = restApi.getUserNameForUserId(userId);

        call.enqueue(new Callback<UserName>() {
            @Override
            public void onResponse(@NonNull Call<UserName> call, @NonNull Response<UserName> response) {
                int code = response.code();
                Log.d(TAG, "onResponse: " + response);
                if(code ==  HTTP_OK) {
                    if (response.body() != null) {
                        donationRequest.setRequestedByName(response.body().getUserName());
                    }
                    notifyDataSetChanged();
                    Log.d(TAG, "onResponse: user name " + donationRequest.getRequestedByName());
                }
            }

            @Override
            public void onFailure(Call<UserName> call, Throwable t) {
                donationRequest.setRequestedByName("");
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    private void updateDonation(DonationRequest donationRequest) {
        Retrofit retrofit = new  Retrofit.Builder()
                .baseUrl(GreetingRestApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestApi restApi = retrofit.create(RestApi.class);
        final Call<DonationRequest> call = restApi.updateDonation(donationRequest);

        call.enqueue(new Callback<DonationRequest>() {
            @Override
            public void onResponse(@NonNull Call<DonationRequest> call, @NonNull Response<DonationRequest> response) {
                int code = response.code();
                Log.d(TAG, "onResponse: " + response);
                if(code ==  HTTP_OK) {
                    DonationRequest udated = response.body();
                    SharedPrefService.saveToSharedPref(Constants.ACCEPTED_REQUEST, udated, mContext);
                    SharedPrefService.saveToSharedPref(Constants.ACCEPTED_REQUEST_ID, udated.getRequestId(), mContext);
                }
            }

            @Override
            public void onFailure(Call<DonationRequest> call, Throwable t) {
                donationRequest.setRequestedByName("");
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    @Override
    public int getItemCount() {
        return donationRequestList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView bloodType, date, patient, hospital, units, purpose, requestedBy, time;
        Button donate;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            bloodType = itemView.findViewById(R.id.donate_notif_blood_type);
            date = itemView.findViewById(R.id.donate_notif_date);
            patient = itemView.findViewById(R.id.donate_notif_patient);
            hospital = itemView.findViewById(R.id.donate_notif_hospital);
            units = itemView.findViewById(R.id.donate_notif_unit);
            purpose = itemView.findViewById(R.id.donate_notif_purpose);
            requestedBy = itemView.findViewById(R.id.donate_notif_requested_by);
            time = itemView.findViewById(R.id.donate_notif_time);
            donate = itemView.findViewById(R.id.donate_notif_donate_btn);
        }
    }
}
