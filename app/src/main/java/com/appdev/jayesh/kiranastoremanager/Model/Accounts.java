package com.appdev.jayesh.kiranastoremanager.Model;

import android.support.annotation.NonNull;

public class Accounts {
    private String name;
    private String mobile;
    private boolean customer;
    private String id;
    private boolean vendor;

    public boolean isLender() {
        return lender;
    }

    public void setLender(boolean lender) {
        this.lender = lender;
    }

    private boolean lender;

    public Accounts() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public boolean isCustomer() {
        return customer;
    }

    public void setCustomer(boolean customer) {
        this.customer = customer;
    }

    public boolean isVendor() {
        return vendor;
    }

    public void setVendor(boolean vendor) {
        this.vendor = vendor;
    }

    //to display object as a string in spinner
    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Accounts) {
            Accounts c = (Accounts) obj;
            if (c.getName().equals(name) && c.getId() == id) return true;
        }

        return false;
    }

}
