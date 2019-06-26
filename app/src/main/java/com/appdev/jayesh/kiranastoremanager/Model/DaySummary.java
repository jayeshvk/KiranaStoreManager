package com.appdev.jayesh.kiranastoremanager.Model;


import com.google.firebase.Timestamp;

public class DaySummary {
    private double CASHSALES;
    private double CREDITSALES;
    private double CASHPURCHASES;
    private double CREDITPURCHASES;
    private double CUSTOMERPAYMENTS;
    private double VENDORPAYMENTS;
    private Timestamp timestamp;
    private long timeInMilli;
    private double SUM;
    private double EXPENSES;
    private double LOAN;
    private double LOANPAYMENT;
    private String date;

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }


    public long getTimeInMilli() {
        return timeInMilli;
    }

    public void setTimeInMilli(long timeInMilli) {
        this.timeInMilli = timeInMilli;
    }


    public double getSUM() {
        return SUM;
    }

    public void setSUM(double SUM) {
        this.SUM = SUM;
    }


    public double getCASHSALES() {
        return CASHSALES;
    }

    public void setCASHSALES(double CASHSALES) {
        this.CASHSALES = CASHSALES;
    }

    public double getCREDITSALES() {
        return CREDITSALES;
    }

    public void setCREDITSALES(double CREDITSALES) {
        this.CREDITSALES = CREDITSALES;
    }

    public double getCASHPURCHASES() {
        return CASHPURCHASES;
    }

    public void setCASHPURCHASES(double CASHPURCHASES) {
        this.CASHPURCHASES = CASHPURCHASES;
    }

    public double getCREDITPURCHASES() {
        return CREDITPURCHASES;
    }

    public void setCREDITPURCHASES(double CREDITPURCHASES) {
        this.CREDITPURCHASES = CREDITPURCHASES;
    }

    public double getCUSTOMERPAYMENTS() {
        return CUSTOMERPAYMENTS;
    }

    public void setCUSTOMERPAYMENTS(double CUSTOMERPAYMENTS) {
        this.CUSTOMERPAYMENTS = CUSTOMERPAYMENTS;
    }

    public double getVENDORPAYMENTS() {
        return VENDORPAYMENTS;
    }

    public void setVENDORPAYMENTS(double VENDORPAYMENTS) {
        this.VENDORPAYMENTS = VENDORPAYMENTS;
    }

    public double getEXPENSES() {
        return EXPENSES;
    }

    public void setEXPENSES(double EXPENSES) {
        this.EXPENSES = EXPENSES;
    }

    public double getLOAN() {
        return LOAN;
    }

    public void setLOAN(double LOAN) {
        this.LOAN = LOAN;
    }

    public double getLOANPAYMENT() {
        return LOANPAYMENT;
    }

    public void setLOANPAYMENT(double LOANPAYMENT) {
        this.LOANPAYMENT = LOANPAYMENT;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public DaySummary() {
    }


}
