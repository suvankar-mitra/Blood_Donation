package com.app.blooddonation.adapters;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.app.blooddonation.R;
import com.app.blooddonation.models.User;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {

    private List<User> userList;

    private FragmentActivity mActivity;

    public SearchAdapter(List<User> userList, FragmentActivity activity) {
        this.userList = userList;
        mActivity = activity;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, phone, availability, locality;
        ImageView icon;
        ImageButton call;

        MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.search_list_name);
            phone = view.findViewById(R.id.search_list_phone);
            availability = view.findViewById(R.id.search_list_availability);
            icon = view.findViewById(R.id.search_list_icon);
            call = view.findViewById(R.id.search_list_call);
            locality = view.findViewById(R.id.search_list_locality);
        }
    }

    @NonNull
    @Override
    public SearchAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_list_view, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.MyViewHolder holder, int position) {
        User user = userList.get(position);

        holder.name.setText(user.getFirstName() + " " + user.getLastName());
        holder.phone.setText(user.getPhoneNo());
        holder.availability.setText("Available");

        Geocoder geocoder = new Geocoder(mActivity);
        try {
            String[] latlng = user.getAreaCode().split(",");
            if (latlng.length == 2) {
                double lat = Double.parseDouble(latlng[0]);
                double lng = Double.parseDouble(latlng[1]);
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String local = "";
                    if (address.getSubLocality() != null)
                        local += address.getSubLocality();
                    if (address.getLocality() != null)
                        local += ", " + address.getLocality();

                    if (!local.equals(""))
                        holder.locality.setText(local);
                    else
                        holder.locality.setText(user.getAreaCode());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(user.getBloodType().equalsIgnoreCase("B+"))
            holder.icon.setImageResource(R.drawable.ic_b_pos);
        else if(user.getBloodType().equalsIgnoreCase("B-"))
            holder.icon.setImageResource(R.drawable.ic_b_neg);
        else if(user.getBloodType().equalsIgnoreCase("AB+"))
            holder.icon.setImageResource(R.drawable.ic_ab_pos);
        else if(user.getBloodType().equalsIgnoreCase("AB-"))
            holder.icon.setImageResource(R.drawable.ic_ab_neg);
        else if(user.getBloodType().equalsIgnoreCase("A+"))
            holder.icon.setImageResource(R.drawable.ic_ab_pos);
        else if(user.getBloodType().equalsIgnoreCase("A-"))
            holder.icon.setImageResource(R.drawable.ic_ab_neg);
        else if(user.getBloodType().equalsIgnoreCase("O+"))
            holder.icon.setImageResource(R.drawable.ic_o_pos);
        else if(user.getBloodType().equalsIgnoreCase("O-"))
            holder.icon.setImageResource(R.drawable.ic_o_neg);

        holder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + user.getPhoneNo()));
                mActivity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
