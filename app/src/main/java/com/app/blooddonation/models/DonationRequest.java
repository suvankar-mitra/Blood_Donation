package com.app.blooddonation.models;

public class DonationRequest {
    private int requestId;
    private String bloodType;
    private String dueDate;
    private int hospitalId;
    private String patientName;
    private int unitsOfBlood;
    private String purpose;
    private int requestedBy;
    private int acceptedBy;
    private String createDateTime;
    private String acceptedDateTime;
    private boolean donationCompleted;
    private String donationCompleteDateTime;
    private String requestedByName;
    private String acceptedByName;
    private  boolean cancelled;

    public DonationRequest() {
    }

    public DonationRequest(String bloodType, String dueDate, int hospitalId, String patientName, int unitsOfBlood, String purpose, int requestedBy, String createDateTime) {
        this.bloodType = bloodType;
        this.dueDate = dueDate;
        this.hospitalId = hospitalId;
        this.patientName = patientName;
        this.unitsOfBlood = unitsOfBlood;
        this.purpose = purpose;
        this.requestedBy = requestedBy;
        this.createDateTime = createDateTime;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public void setRequestedByName(String requestedByName) {
        this.requestedByName = requestedByName;
    }

    public String getAcceptedByName() {
        return acceptedByName;
    }

    public void setAcceptedByName(String acceptedByName) {
        this.acceptedByName = acceptedByName;
    }

    public boolean isDonationCompleted() {
        return donationCompleted;
    }

    public void setDonationCompleted(boolean donationCompleted) {
        this.donationCompleted = donationCompleted;
    }

    public String getDonationCompleteDateTime() {
        return donationCompleteDateTime;
    }

    public void setDonationCompleteDateTime(String donationCompleteDateTime) {
        this.donationCompleteDateTime = donationCompleteDateTime;
    }

    public int getRequestId() {
        return requestId;
    }

    public String getAcceptedDateTime() {
        return acceptedDateTime;
    }

    public void setAcceptedDateTime(String acceptedDateTime) {
        this.acceptedDateTime = acceptedDateTime;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public int getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(int hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public int getUnitsOfBlood() {
        return unitsOfBlood;
    }

    public void setUnitsOfBlood(int unitsOfBlood) {
        this.unitsOfBlood = unitsOfBlood;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public int getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(int requestedBy) {
        this.requestedBy = requestedBy;
    }

    public int getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(int acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public String getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(String createDateTime) {
        this.createDateTime = createDateTime;
    }

    @Override
    public String toString() {
        return "DonationRequest{" +
                "requestId=" + requestId +
                ", bloodType='" + bloodType + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", hospitalId=" + hospitalId +
                ", patientName='" + patientName + '\'' +
                ", unitsOfBlood=" + unitsOfBlood +
                ", purpose='" + purpose + '\'' +
                ", requestedBy=" + requestedBy +
                ", acceptedBy=" + acceptedBy +
                ", createDateTime='" + createDateTime + '\'' +
                ", acceptedDateTime='" + acceptedDateTime + '\'' +
                '}';
    }
}
