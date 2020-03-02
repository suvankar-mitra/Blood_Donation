package com.app.blooddonation.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.app.blooddonation.models.Constants;
import com.app.blooddonation.models.Hospital;
import com.app.blooddonation.models.User;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SharedPrefService {

    public static void saveToSharedPref(String key, Object value, Context context) {
        if(key == null || key.isEmpty())
            return;

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(value);
        prefsEditor.putString(key, json);
        prefsEditor.apply();
    }

    public static Object getSharedPref(String key, Class target, Context context) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = mPrefs.getString(key, "");
        return gson.fromJson(json, target);
    }

    public static List<Hospital> getSharedPrefHospitalList(Context context) {
        List<Hospital> hospitalList;
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = mPrefs.getString(Constants.HOSPITALS, "");
        Hospital[] hospitals = gson.fromJson(json, Hospital[].class);
        hospitalList = Arrays.asList(hospitals);
        hospitalList = new ArrayList<>(hospitalList);
        return hospitalList;
    }

    public static List<User> getSharedPrefUserList(Context context) {
        List<User> userList;
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = mPrefs.getString(Constants.USER_LIST, "");
        User[] users = gson.fromJson(json, User[].class);
        userList = Arrays.asList(users);
        userList = new ArrayList<>(userList);
        return userList;
    }
}
