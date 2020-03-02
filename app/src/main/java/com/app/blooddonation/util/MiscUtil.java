package com.app.blooddonation.util;

import android.content.Context;

import com.app.blooddonation.models.Hospital;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MiscUtil {
    public static String getTimeOfDay() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay < 12){
            return "Morning";
        }else if(timeOfDay < 16){
            return "Afternoon";
        }else if(timeOfDay < 21){
            return "Evening";
        }else {
            return "Night";
        }
    }

    public static String getCurrentTs() {
        return new SimpleDateFormat("dd/MM/yyyy HH.mm.ss").format(new Date());
    }

    public static int hospitalNameToId(String name, Context context) throws IOException, ClassNotFoundException {
        if(name.isEmpty())
            return -1;
        List<Hospital> hospitals = SharedPrefService.getSharedPrefHospitalList(context);
        int id = -1;
        for(Hospital hospital : hospitals) {
            if(hospital.getHospitalName().equalsIgnoreCase(name))
                return hospital.getHospitalId();
        }
        return id;
    }

    public static String hospitalIdToName(int id, Context context) throws IOException, ClassNotFoundException {
        if(id<=0)
            return "";
        List<Hospital> hospitals = SharedPrefService.getSharedPrefHospitalList(context);
        String name = "";
        for(Hospital hospital : hospitals) {
            if(hospital.getHospitalId() == id)
                return hospital.getHospitalName();
        }
        return name;
    }

    public static String hospitalIdToLocation(int id, Context context) throws ClassNotFoundException {
        if(id<=0)
            return "";
        List<Hospital> hospitals = SharedPrefService.getSharedPrefHospitalList(context);
        for(Hospital hospital : hospitals) {
            if(hospital.getHospitalId() == id)
                return hospital.getAreaCode();
        }
        return "";
    }
}
