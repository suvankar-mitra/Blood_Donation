package com.app.blooddonation.ui.request;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.app.blooddonation.R;
import com.app.blooddonation.interfaces.GreetingRestApi;
import com.app.blooddonation.interfaces.RestApi;
import com.app.blooddonation.models.Constants;
import com.app.blooddonation.models.DonationRequest;
import com.app.blooddonation.models.Hospital;
import com.app.blooddonation.models.User;
import com.app.blooddonation.util.MiscUtil;
import com.app.blooddonation.util.SharedPrefService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.app.blooddonation.util.MiscUtil.hospitalIdToName;
import static com.app.blooddonation.util.MiscUtil.hospitalNameToId;
import static java.net.HttpURLConnection.HTTP_OK;

public class RequestFragment extends Fragment {

    private final String TAG = "RequestFragment";

    private final Calendar myCalendar = Calendar.getInstance();

    private TextInputEditText mPatient, mUnitsBlood, mDueDate, mPurpose;
    private Spinner mBloodType, mHospital;
    private Button mRequestButton;
    private TextView mErr, mNoRequestTv;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_request, container, false);

        int userId = (int) SharedPrefService.getSharedPref(Constants.USER_ID, Integer.class, getContext());

        mPatient = root.findViewById(R.id.request_patient_name);
        mUnitsBlood = root.findViewById(R.id.request_units_blood);
        mDueDate = root.findViewById(R.id.request_due_date);
        mPurpose = root.findViewById(R.id.request_purpose);
        mBloodType = root.findViewById(R.id.request_blood_type);
        mHospital = root.findViewById(R.id.request_list_hospital);
        setupHospitals();
        mRequestButton = root.findViewById(R.id.request_btn);
        mErr = root.findViewById(R.id.request_error);
        mNoRequestTv = root.findViewById(R.id.request_no_request);

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        mDueDate.setTag(mDueDate.getKeyListener());
        mDueDate.setKeyListener(null);
        mDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(Objects.requireNonNull(getContext()), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        TextInputLayout dueDateLayout = root.findViewById(R.id.request_due_date_layout);
        dueDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(Objects.requireNonNull(getContext()), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mErr.setText("");
                String patientName = Objects.requireNonNull(mPatient.getText()).toString().trim();
                int units = Integer.parseInt(mUnitsBlood.getText().toString().trim().isEmpty() ? "0" : mUnitsBlood.getText().toString().trim());
                String due = Objects.requireNonNull(mDueDate.getText()).toString().trim();
                String purpose = Objects.requireNonNull(mPurpose.getText()).toString().trim();
                String bloodType = mBloodType.getSelectedItem().toString().trim();
                int hospitalId = 0;
                try {
                    hospitalId = hospitalNameToId(mHospital.getSelectedItem().toString().trim(), getContext());
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                DonationRequest donationRequest = new DonationRequest(bloodType, due, hospitalId, patientName, units, purpose, userId, MiscUtil.getCurrentTs());

                try {
                    if(valid())
                        saveRequest(donationRequest);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        //load own request if any
        loadMyRequest(root);

        return root;
    }

    private void loadMyRequest(View root) {
        User user = (User) SharedPrefService.getSharedPref(Constants.USER, User.class, getContext());
        int userId = user.getUserId();
        Log.d(TAG, "prepareAllUserData: User id = "+userId);
        Retrofit retrofit = new  Retrofit.Builder()
                .baseUrl(GreetingRestApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestApi restApi = retrofit.create(RestApi.class);
        final Call<DonationRequest> call = restApi.getDonationRequestByMeNotCompleted(userId);
        call.enqueue(new Callback<DonationRequest>() {
            @Override
            public void onResponse(@NonNull Call<DonationRequest> call, @NonNull Response<DonationRequest> response) {
                Log.d(TAG, "onResponse: " + response);
                int code = response.code();
                ConstraintLayout container = root.findViewById(R.id.request_my_request_container);
                ConstraintLayout disableL = root.findViewById(R.id.request_disabled);

                if(code ==  HTTP_OK) {
                    DonationRequest myRequest = response.body();

                    container.setVisibility(View.VISIBLE);
                    disableL.setVisibility(View.VISIBLE);
                    TextView hospital = root.findViewById(R.id.request_hospital);
                    TextView for_ = root.findViewById(R.id.request_for);
                    TextView date = root.findViewById(R.id.request_due);
                    TextView blood = root.findViewById(R.id.request_blood);
                    TextView unit = root.findViewById(R.id.request_units);
                    TextView accept = root.findViewById(R.id.request_accepted);
                    TextView purpose = root.findViewById(R.id.request_purpose_2);

                    //disable request button
                    mRequestButton.setEnabled(false);

                    try {
                        hospital.setText(hospitalIdToName(myRequest.getHospitalId(), getContext()));
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    for_.setText(myRequest.getPatientName());
                    date.setText(myRequest.getDueDate());
                    blood.setText(myRequest.getBloodType());
                    unit.setText(myRequest.getUnitsOfBlood() + " unit");
                    purpose.setText(myRequest.getPurpose());
                    String acceptMsg = "";
                    if(myRequest.getAcceptedBy() == 0)
                        acceptMsg = "Your request has not been accepted yet.";
                    else {
                        String acceptorName = getDonorName(myRequest.getAcceptedBy());
                        myRequest.setAcceptedByName(acceptorName);
                        acceptMsg = "Your request has been accepted by " + acceptorName + " on " + myRequest.getAcceptedDateTime();
                    }
                    accept.setText(acceptMsg);

                    // cancel button
                    ImageButton cancelBtn = root.findViewById(R.id.request_cancel_btn);
                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                                    .setTitle("Donation Cancelling")
                                    .setMessage("Are you sure you want to cancel?")
                                    .setPositiveButton("Yes",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    cancelRequest(myRequest);
                                                    container.setVisibility(View.GONE);
                                                }
                                            })
                                    .setNegativeButton("No", null).show();

                        }
                    });

                } else {
                    container.setVisibility(View.GONE);
                    disableL.setVisibility(View.GONE);
                    mRequestButton.setEnabled(true);
                    mNoRequestTv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<DonationRequest> call, Throwable t) {
                String msg = "Could not connect to server. Try again later or check your network connectivity.";
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                Log.e(TAG, "onFailure: ", t);

                ConstraintLayout container = root.findViewById(R.id.request_my_request_container);
                container.setVisibility(View.GONE);
                ConstraintLayout disableL = root.findViewById(R.id.request_disabled);
                disableL.setVisibility(View.GONE);
                mRequestButton.setEnabled(true);
            }
        });
    }

    private void cancelRequest(DonationRequest myRequest) {
        myRequest.setDonationCompleted(false);
        myRequest.setCancelled(true);
        Retrofit retrofit = new  Retrofit.Builder()
                .baseUrl(GreetingRestApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RestApi restApi = retrofit.create(RestApi.class);
        final Call<DonationRequest> call = restApi.updateDonation(myRequest);
        call.enqueue(new Callback<DonationRequest>() {
            @Override
            public void onResponse(Call<DonationRequest> call, Response<DonationRequest> response) {

            }

            @Override
            public void onFailure(Call<DonationRequest> call, Throwable t) {

            }
        });
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

        mDueDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void setupHospitals() {
        List<Hospital> hospitals = SharedPrefService.getSharedPrefHospitalList(getContext());
        List<String> hospitalNames = new ArrayList<>();
        for(Hospital hospital: hospitals) {
            hospitalNames.add(hospital.getHospitalName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, hospitalNames);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mHospital.setAdapter(adapter);
    }

    private boolean valid() throws IOException, ClassNotFoundException {
        String patientName = Objects.requireNonNull(mPatient.getText()).toString().trim();
        int units = Integer.parseInt(mUnitsBlood.getText().toString().trim().isEmpty() ? "0" : mUnitsBlood.getText().toString().trim());
        String due = Objects.requireNonNull(mDueDate.getText()).toString().trim();
        String purpose = Objects.requireNonNull(mPurpose.getText()).toString().trim();
        String bloodType = mBloodType.getSelectedItem().toString().trim();
        int hospitalId = hospitalNameToId(mHospital.getSelectedItem().toString().trim(), getContext());

        if(patientName.isEmpty() || units<=0 || due.isEmpty() || purpose.isEmpty() || bloodType.isEmpty() || hospitalId<=0){
            mErr.setText("All fields are mandatory. Please fill up all entries.");
            return false;
        }

        Date now = new Date();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        try {
            Date dueDate = simpleDateFormat.parse(due+" 11:59:59");
            if (dueDate != null && dueDate.before(now)) {
                mErr.setText("Date cannot be before current date.");
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void saveRequest(DonationRequest donationRequest) {
        ProgressDialog mDialog;
        mDialog = new ProgressDialog(getContext());
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setMessage("Saving request. Please wait...");
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
        final Call<DonationRequest> call = restApi.saveDonationRequest(donationRequest);
        call.enqueue(new Callback<DonationRequest>() {
            @Override
            public void onResponse(@NonNull Call<DonationRequest> call, @NonNull Response<DonationRequest> response) {
                mDialog.dismiss();
                int code = response.code();
                if(code ==  HTTP_OK) {
                    DonationRequest savedDonationRequest = response.body();
                    SharedPrefService.saveToSharedPref(Constants.MY_REQUEST, savedDonationRequest, getContext());
                    Log.d(TAG, "onResponse: Saved request " + savedDonationRequest);

                    //set all to blank
                    mPatient.setText("");
                    mUnitsBlood.setText("");
                    mDueDate.setText("");
                    mPurpose.setText("");
                    mDueDate.setText("");
                    mRequestButton.setEnabled(false);
                    //show success
                    if (savedDonationRequest != null) {
                        String message = "Yo have successfully created a Blood Donation request for " + savedDonationRequest.getPatientName() + ". Your request ID is " + savedDonationRequest.getRequestId()
                                + ".\n\nPlease note that you will not be able to make another donation request until this request is served or you cancel this request.";
                        new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                                .setTitle("Donation Request Created")
                                .setMessage(message)
                                .setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                Toast.makeText(getActivity(), "You've created a request with ID " + savedDonationRequest.getRequestId(), Toast.LENGTH_SHORT).show();
                                            }
                                        }).show();
                    }
                } else {
                    String msg = "Could not connect to server. Try again later or check your network connectivity.";
                    mErr.setText(msg);
                    Log.d(TAG, "onResponse: Something is wrong: " + response);
                }
            }

            @Override
            public void onFailure(Call<DonationRequest> call, Throwable t) {
                mDialog.dismiss();
                String msg = "Could not connect to server. Try again later or check your network connectivity.";
                mErr.setText(msg);
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    private String getDonorName(int acceptorId) {
        List<User> users = SharedPrefService.getSharedPrefUserList(getContext());
        if(users!=null) {
            for(User u : users) {
                if(u.getUserId() == acceptorId) {
                    return u.getFirstName() + " " + u.getLastName();
                }
            }
        }
        return "";
    }
}