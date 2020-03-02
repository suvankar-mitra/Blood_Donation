package com.app.blooddonation.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.app.blooddonation.R;
import com.app.blooddonation.interfaces.GreetingRestApi;
import com.app.blooddonation.interfaces.RestApi;
import com.app.blooddonation.models.Constants;
import com.app.blooddonation.models.DiagnosticReport;
import com.app.blooddonation.models.User;
import com.app.blooddonation.util.SharedPrefService;

import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class DiagnosticReportFragment extends Fragment {


    public DiagnosticReportFragment() {
        // Required empty public constructor
    }

    private int requestId = -1;
    private String TAG = "DiagnosticReportFragment";

    private TextView mId, mLab, mPatient, mDoctor, mDate, mWbc, mRbc, mHgb, mHct, mMcv, mMchc, mRdw, mMch, mPlatelet;
    private ImageButton back;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_diagnostic_report, container, false);


        if (getArguments() != null) {
            requestId = getArguments().getInt(Constants.REQUEST_ID);
        }

        mId = root.findViewById(R.id.diag_report_id);
        mLab = root.findViewById(R.id.diag_report_lab);
        mPatient = root.findViewById(R.id.diag_report_patient);
        mDoctor = root.findViewById(R.id.diag_report_doctor);
        mDate = root.findViewById(R.id.diag_report_date);

        mWbc = root.findViewById(R.id.diag_report_wbc);
        mRbc = root.findViewById(R.id.diag_report_rbc);
        mHgb = root.findViewById(R.id.diag_report_hgb);
        mHct = root.findViewById(R.id.diag_report_hct);
        mMcv = root.findViewById(R.id.diag_report_mcv);
        mMchc = root.findViewById(R.id.diag_report_mchc);
        mRdw = root.findViewById(R.id.diag_report_rdw);
        mMch = root.findViewById(R.id.diag_report_mch);
        mPlatelet = root.findViewById(R.id.diag_report_platelet);

        back = root.findViewById(R.id.diag_report_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSelf();
            }
        });

        ProgressDialog mDialog;
        mDialog = new ProgressDialog(getContext());
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage("Loading. Please wait...");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        Log.d(TAG, "onCreateView: request = " + requestId);
        Retrofit retrofit = new  Retrofit.Builder()
                .baseUrl(GreetingRestApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestApi restApi = retrofit.create(RestApi.class);
        Call<DiagnosticReport> call = restApi.getDiagnosticReport(requestId);

        call.enqueue(new Callback<DiagnosticReport>() {
            @Override
            public void onResponse(Call<DiagnosticReport> call, Response<DiagnosticReport> response) {
                mDialog.dismiss();
                if(response.code() == HttpURLConnection.HTTP_OK) {
                    DiagnosticReport report = response.body();
                    if(report!=null) {
                        mId.setText(report.getDiagnosticId()+"");
                        mLab.setText(report.getLabName());
                        mPatient.setText(report.getPatientName());
                        mDoctor.setText(report.getDoctorName());
                        mDate.setText(report.getReportDate());
                        mWbc.setText(String.format("%.2f", report.getWbc()));
                        mRbc.setText(String.format("%.2f", report.getRbc()));
                        mHgb.setText(String.format("%.2f", report.getHgb()));
                        mHct.setText(String.format("%.2f", report.getWbc()));
                        mMcv.setText(String.format("%.2f", report.getMcv()));
                        mMchc.setText(String.format("%.2f", report.getMchc()));
                        mRdw.setText(String.format("%.2f", report.getRdw()));
                        mMch.setText(String.format("%.2f", report.getMch()));
                        mPlatelet.setText(String.format("%.2f", report.getPlatelet()));
                    }
                }
            }

            @Override
            public void onFailure(Call<DiagnosticReport> call, Throwable t) {
                mDialog.dismiss();
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

}
