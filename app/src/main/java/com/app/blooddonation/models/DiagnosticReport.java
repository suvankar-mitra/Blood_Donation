package com.app.blooddonation.models;

public class DiagnosticReport {
    private int diagnosticId;

    private int requestId;
    private String labName;
    private String reportDate;
    private String patientName;
    private String doctorName;

    private float wbc;
    private float rbc;
    private float hgb;
    private float hct;
    private float mcv;
    private float mch;
    private float mchc;
    private float rdw;
    private float platelet;

    public int getDiagnosticId() {
        return diagnosticId;
    }

    public void setDiagnosticId(int diagnosticId) {
        this.diagnosticId = diagnosticId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public float getWbc() {
        return wbc;
    }

    public void setWbc(float wbc) {
        this.wbc = wbc;
    }

    public float getRbc() {
        return rbc;
    }

    public void setRbc(float rbc) {
        this.rbc = rbc;
    }

    public float getHgb() {
        return hgb;
    }

    public void setHgb(float hgb) {
        this.hgb = hgb;
    }

    public float getHct() {
        return hct;
    }

    public void setHct(float hct) {
        this.hct = hct;
    }

    public float getMcv() {
        return mcv;
    }

    public void setMcv(float mcv) {
        this.mcv = mcv;
    }

    public float getMch() {
        return mch;
    }

    public void setMch(float mch) {
        this.mch = mch;
    }

    public float getMchc() {
        return mchc;
    }

    public void setMchc(float mchc) {
        this.mchc = mchc;
    }

    public float getRdw() {
        return rdw;
    }

    public void setRdw(float rdw) {
        this.rdw = rdw;
    }

    public float getPlatelet() {
        return platelet;
    }

    public void setPlatelet(float platelet) {
        this.platelet = platelet;
    }
}
