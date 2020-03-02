package com.app.blooddonation.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.blooddonation.R;
import com.app.blooddonation.fragments.DiagnosticReportFragment;
import com.app.blooddonation.fragments.RequestHistoryFragment;
import com.app.blooddonation.models.Constants;
import com.app.blooddonation.models.DonationRequest;
import com.app.blooddonation.util.MiscUtil;

import java.io.IOException;
import java.util.List;

public class DonationHistoryAdapter extends RecyclerView.Adapter<DonationHistoryAdapter.MyViewHolder> {

    private Activity mActivity;
    private List<DonationRequest> donationRequestList;

    public DonationHistoryAdapter(List<DonationRequest> donationRequestList, Activity mActivity) {
        this.mActivity = mActivity;
        this.donationRequestList = donationRequestList;
    }

    @NonNull
    @Override
    public DonationHistoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.donate_history_list_view, parent, false);

        return new DonationHistoryAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationHistoryAdapter.MyViewHolder holder, int position) {
        DonationRequest request = donationRequestList.get(position);
        holder.due.setText(request.getDueDate());
        try {
            holder.hospital.setText(MiscUtil.hospitalIdToName(request.getHospitalId(), mActivity.getApplicationContext()));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        holder.blood.setText(request.getBloodType());
        holder.unit.setText(request.getUnitsOfBlood()+" Unit");
        holder.name.setText(request.getPatientName());
        holder.purpose.setText(request.getPurpose());
        String msg = "Requested by " +request.getRequestedByName() + "\non "+ request.getCreateDateTime();
        holder.requestedBy.setText(msg);

        boolean isCancelled = request.isCancelled();
        if(isCancelled) {
            holder.completed.setText("CANCELLED BY USER");
            holder.completed.setTextColor(Color.parseColor("#E91E63"));
            holder.completedOn.setText("Request was cancelled by user");
            holder.report.setEnabled(false);
        }
        boolean isCompleted = request.isDonationCompleted() && !request.isCancelled();
        if(isCompleted) {
            holder.completed.setText("COMPLETED");
            holder.completed.setTextColor(Color.parseColor("#4CAF50"));
            msg = "Donated on "+request.getDonationCompleteDateTime();
            holder.completedOn.setText(msg);
            holder.report.setEnabled(true);
        }
        boolean isPending = !request.isDonationCompleted() && !request.isCancelled();
        if(isPending) {
            holder.completed.setText("PENDING");
            holder.completed.setTextColor(Color.parseColor("#FFC107"));
            holder.completedOn.setText("This request is pending with you.");
            holder.report.setEnabled(false);
        }

        holder.report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new DiagnosticReportFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.REQUEST_ID, request.getRequestId());
                fragment.setArguments(bundle);
                FragmentManager fm = ((AppCompatActivity)mActivity).getSupportFragmentManager();
                fm.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.don_hist_container, fragment, "diag_report").addToBackStack("null").commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return donationRequestList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView due, hospital, blood, unit, name, purpose, requestedBy, completedOn, completed;
        Button report;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            due = itemView.findViewById(R.id.don_hist_date);
            hospital = itemView.findViewById(R.id.don_hist_hospital);
            blood = itemView.findViewById(R.id.don_hist_blood_type);
            unit = itemView.findViewById(R.id.don_hist_unit);
            name = itemView.findViewById(R.id.don_hist_patient);
            purpose = itemView.findViewById(R.id.don_hist_purpose);
            completed = itemView.findViewById(R.id.don_hist_completed);
            completedOn = itemView.findViewById(R.id.don_hist_completed_on);
            requestedBy = itemView.findViewById(R.id.don_hist_requested_by);
            report = itemView.findViewById(R.id.don_hist_report_btn);
        }
    }
}
