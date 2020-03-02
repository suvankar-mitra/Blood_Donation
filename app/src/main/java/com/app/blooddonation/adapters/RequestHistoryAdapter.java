package com.app.blooddonation.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.blooddonation.R;
import com.app.blooddonation.models.DonationRequest;
import com.app.blooddonation.util.MiscUtil;

import java.io.IOException;
import java.util.List;

public class RequestHistoryAdapter extends RecyclerView.Adapter<RequestHistoryAdapter.MyViewHolder> {
    private Activity mActivity;
    private List<DonationRequest> donationRequestList;

    public RequestHistoryAdapter(List<DonationRequest> donationRequestList, Activity mActivity) {
        this.mActivity = mActivity;
        this.donationRequestList = donationRequestList;
    }

    @NonNull
    @Override
    public RequestHistoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.request_history_list_view, parent, false);

        return new RequestHistoryAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestHistoryAdapter.MyViewHolder holder, int position) {
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
        holder.reqDate.setText("Created on: " +request.getCreateDateTime());

        boolean isCompleted = request.getAcceptedBy() > 0 && request.isDonationCompleted() && !request.isCancelled();
        if(isCompleted) {
            holder.completed.setText("COMPLETED");
            holder.completed.setTextColor(Color.parseColor("#4CAF50"));
            String msg = "Donated by " + request.getAcceptedByName() + "\non "+request.getDonationCompleteDateTime();
            holder.completedOn.setText(msg);
        }
        boolean isCancelled = !request.isDonationCompleted() && request.isCancelled();
        if(isCancelled) {
            holder.completed.setText("CANCELLED");
            holder.completed.setTextColor(Color.parseColor("#E91E63"));
            holder.completedOn.setText("Request was cancelled by you.");
        }
        boolean isPending = request.getAcceptedBy() <= 0 && !request.isDonationCompleted() && !request.isCancelled();
        if(isPending) {
            holder.completed.setText("PENDING");
            holder.completed.setTextColor(Color.parseColor("#FFC107"));
            holder.completedOn.setText("Your request is pending.");
        }
        boolean isAccepted = request.getAcceptedBy() > 0 && !request.isDonationCompleted() && !request.isCancelled();
        if(isAccepted) {
            holder.completed.setText("ACCEPTED");
            holder.completed.setTextColor(Color.parseColor("#03A9F4"));
            String msg = "Accepted by " + request.getAcceptedByName() + "\non "+request.getAcceptedDateTime();
            holder.completedOn.setText(msg);
        }
    }

    @Override
    public int getItemCount() {
        return donationRequestList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView due, hospital, blood, unit, name, purpose, completed, completedOn, reqDate;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            due = itemView.findViewById(R.id.req_hist_date);
            hospital = itemView.findViewById(R.id.req_hist_hospital);
            blood = itemView.findViewById(R.id.req_hist_blood_type);
            unit = itemView.findViewById(R.id.req_hist_unit);
            name = itemView.findViewById(R.id.req_hist_patient);
            purpose = itemView.findViewById(R.id.req_hist_purpose);
            completed = itemView.findViewById(R.id.req_hist_completed);
            completedOn = itemView.findViewById(R.id.req_hist_completed_on);
            reqDate = itemView.findViewById(R.id.req_hist_req_date);
        }
    }
}
