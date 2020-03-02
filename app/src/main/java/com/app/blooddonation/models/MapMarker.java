package com.app.blooddonation.models;

public class MapMarker {
    private String name;
    private double lat;
    private double lng;
    private String bloodType;
    private int iconId;

    public MapMarker(double lat, double lng, String bloodType, int iconId) {
        this.lat = lat;
        this.lng = lng;
        this.bloodType = bloodType;
        this.iconId = iconId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }
}
