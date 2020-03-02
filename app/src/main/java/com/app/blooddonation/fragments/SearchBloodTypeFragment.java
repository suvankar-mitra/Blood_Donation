package com.app.blooddonation.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.app.blooddonation.R;
import com.app.blooddonation.adapters.SearchAdapter;
import com.app.blooddonation.interfaces.GreetingRestApi;
import com.app.blooddonation.interfaces.RestApi;
import com.app.blooddonation.models.Constants;
import com.app.blooddonation.models.DemoUsers;
import com.app.blooddonation.models.User;
import com.app.blooddonation.util.SharedPrefService;

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
public class SearchBloodTypeFragment extends Fragment {

    private final String TAG = "SearchBloodTypeFragment";
    private List<User> userList = new ArrayList<>();
    private List<User> userListFiltered = new ArrayList<>();
    private SearchAdapter mAdapter;

    private boolean overlayFilterVisible = false;

    public SearchBloodTypeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_search_blood_type, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.search_recycler_view);
        mAdapter = new SearchAdapter(userListFiltered, getActivity());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        prepareAllUserData();

        ImageButton back = root.findViewById(R.id.search_back_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSelf();
            }
        });


        // To enable search functionality
        EditText mSearchBox = root.findViewById(R.id.search_edit_text);
        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable constraint) {
                String charString = constraint.toString();
                if (!charString.isEmpty()) {
                    List<User> filteredList = new ArrayList<>();
                    for (User row : userList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getFirstName().toLowerCase().contains(charString.toLowerCase())
                                || row.getLastName().toLowerCase().contains(charString.toLowerCase())
                                || row.getBloodType().toLowerCase().contains(charString.toLowerCase())
                                || row.getAreaCode().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    userListFiltered.clear();
                    userListFiltered.addAll(filteredList);
                }
                else {
                    userListFiltered.clear();
                    userListFiltered.addAll(userList);
                }
                mAdapter.notifyDataSetChanged();
            }
        });

        // To show filter functionality
        ImageButton mSearchFilter = root.findViewById(R.id.search_filter_btn);
        ConstraintLayout filterLayout = root.findViewById(R.id.search_filter_overlay);
        mSearchFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!overlayFilterVisible) {
                    filterLayout.setVisibility(View.VISIBLE);
                    overlayFilterVisible = true;
                    v.setAlpha(0.5f);
                } else {
                    filterLayout.setVisibility(View.GONE);
                    overlayFilterVisible = false;
                    v.setAlpha(1f);
                }
            }
        });

        // To apply filter
        Button mSearchFilterBtn = root.findViewById(R.id.search_filter_apply_btn);
        mSearchFilterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterLayout.setVisibility(View.GONE);
                overlayFilterVisible = false;
                v.setAlpha(1f);

                Spinner mFilterBloodType = root.findViewById(R.id.search_filter_blood_type);
                Spinner mFilterState = root.findViewById(R.id.search_filter_state);
                Spinner mFilterLocality = root.findViewById(R.id.search_filter_locality);

                String bt = mFilterBloodType.getSelectedItem().toString();
                String state = mFilterState.getSelectedItem().toString();
                String local = mFilterLocality.getSelectedItem().toString();

                List<User> filteredList = new ArrayList<>();
                for (User row : userList) {

                    // name match condition. this might differ depending on your requirement
                    // here we are looking for name or phone number match
                    if (row.getBloodType().equalsIgnoreCase(bt)) {
                        filteredList.add(row);
                    }
                }
                userListFiltered.clear();
                userListFiltered.addAll(filteredList);
                mAdapter.notifyDataSetChanged();
            }
        });

        // To clear filer
        Button mSearchFilterClearBtn = root.findViewById(R.id.search_filter_clear_btn);
        mSearchFilterClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterLayout.setVisibility(View.GONE);
                overlayFilterVisible = false;
                v.setAlpha(1f);
                
                userListFiltered.clear();
                userListFiltered.addAll(userList);
                mAdapter.notifyDataSetChanged();
            }
        });

        return root;
    }

    private void removeSelf() {
        getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right)
                .remove(this).commit();
        getChildFragmentManager().popBackStack();
        FragmentManager fm = getChildFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }

    private void prepareAllUserData() {
        ProgressDialog mDialog;
        mDialog = new ProgressDialog(getContext());
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage("Loading. Please wait...");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        User user = (User) SharedPrefService.getSharedPref(Constants.USER, User.class, getContext());
        int userId = user.getUserId();
        Log.d(TAG, "prepareAllUserData: User id = "+userId);
        Retrofit retrofit = new  Retrofit.Builder()
                .baseUrl(GreetingRestApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestApi restApi = retrofit.create(RestApi.class);
        final Call<List<User>> call = restApi.getAllUsersExceptMe(userId);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                mDialog.dismiss();
                int code = response.code();
                if(code ==  HTTP_OK) {
                    List<User> users = response.body();
                    userList.addAll(users);
                    userListFiltered.addAll(userList);
                    mAdapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "onResponse: Something is wrong: " + response);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                mDialog.dismiss();
                Toast.makeText(getContext(), "Could not get users from server. Try again later or check your network connectivity.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }



}
