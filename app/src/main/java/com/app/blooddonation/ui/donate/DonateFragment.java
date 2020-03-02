package com.app.blooddonation.ui.donate;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.blooddonation.R;
import com.app.blooddonation.adapters.DonationRequestAdapter;
import com.app.blooddonation.interfaces.GreetingRestApi;
import com.app.blooddonation.interfaces.RestApi;
import com.app.blooddonation.models.Constants;
import com.app.blooddonation.models.DonationRequest;
import com.app.blooddonation.models.User;
import com.app.blooddonation.util.SharedPrefService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class DonateFragment extends Fragment {

    private List<DonationRequest> donationRequestList = new ArrayList<>();
    private DonationRequestAdapter mAdapter;
    private final String TAG = "DonateFragment";

    public DonateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_donate, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.donate_request_recycler);
        mAdapter = new DonationRequestAdapter(donationRequestList, getContext());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        prepareDonationData();

        return root;
    }

    private void prepareDonationData() {
        donationRequestList.clear();
        int userId = (int) SharedPrefService.getSharedPref(Constants.USER_ID, Integer.class, getContext());
        Retrofit retrofit = new  Retrofit.Builder()
                .baseUrl(GreetingRestApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestApi restApi = retrofit.create(RestApi.class);
        final Call<List<DonationRequest>> call = restApi.getAllDonations();
        call.enqueue(new Callback<List<DonationRequest>>() {
            @Override
            public void onResponse(@NonNull Call<List<DonationRequest>> call, @NonNull Response<List<DonationRequest>> response) {
                int code = response.code();

                if(code ==  HTTP_OK) {
                    List<DonationRequest> donations = response.body();

                    if (donations != null) {
                        for(DonationRequest d : donations) {
                            if(d.getRequestedBy() != userId)
                                donationRequestList.add(d);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<DonationRequest>> call, Throwable t) {
                String msg = "Could not connect to server. Try again later or check your network connectivity.";
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                Log.e(TAG, "onFailure: ", t);
            }
        });

        mAdapter.notifyDataSetChanged();
    }

}
