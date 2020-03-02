package com.app.blooddonation.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.app.blooddonation.LoginActivity;
import com.app.blooddonation.R;
import com.app.blooddonation.fragments.DonationHistoryFragment;
import com.app.blooddonation.fragments.RequestHistoryFragment;
import com.app.blooddonation.models.Constants;
import com.app.blooddonation.models.User;
import com.app.blooddonation.util.SharedPrefService;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private TextView mName, mBloodType, mPhone, mEmail, mGender;
    private Button mLogout, mReqHistory, mDonHistory;
    private SharedPreferences mPrefs;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext() /* Activity context */);

        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        mLogout = root.findViewById(R.id.profile_logout_btn);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPrefService.saveToSharedPref(Constants.USER, null, getContext());
                Toast.makeText(getContext(), "You have been logged out", Toast.LENGTH_SHORT).show();
                gotoLoginPage();
            }
        });

        User user = (User) SharedPrefService.getSharedPref(Constants.USER, User.class, getContext());

        mName = root.findViewById(R.id.profile_name);
        mName.setText(user.getFirstName()+" " +user.getLastName());
        mBloodType = root.findViewById(R.id.profile_blood_type);
        mBloodType.setText(user.getBloodType());
        mPhone = root.findViewById(R.id.profile_phone);
        mPhone.setText(user.getPhoneNo());
        mEmail = root.findViewById(R.id.profile_email);
        mEmail.setText(user.getEmail());
        mGender = root.findViewById(R.id.profile_gender);
        mGender.setText(user.getGender());

        mReqHistory = root.findViewById(R.id.profile_req_history);
        mReqHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new RequestHistoryFragment();
                FragmentManager fm = getChildFragmentManager();
                fm.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.profile_frag_container, fragment, "req_history").addToBackStack("null").commit();
            }
        });

        mDonHistory = root.findViewById(R.id.profile_don_history);
        mDonHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new DonationHistoryFragment();
                FragmentManager fm = getChildFragmentManager();
                fm.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.profile_frag_container, fragment, "don_history").addToBackStack("null").commit();
            }
        });

        return root;
    }

    private void gotoLoginPage() {
        Objects.requireNonNull(getActivity()).finish();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }
}