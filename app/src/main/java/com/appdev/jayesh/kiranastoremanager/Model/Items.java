package com.appdev.jayesh.kiranastoremanager.Model;

import android.support.annotation.NonNull;

import java.util.HashMap;

public class Items {
    private String Name;
    private double Price;
    private double Cost;
    private String id;

    public HashMap<String, Boolean> getUsedFor() {
        return UsedFor;
    }

    public void setUsedFor(HashMap<String, Boolean> usedFor) {
        UsedFor = usedFor;
    }

    private HashMap<String, Boolean> UsedFor;

    public Items() {
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        this.Price = price;
    }

    public double getCost() {
        return Cost;
    }

    public void setCost(double cost) {
        this.Cost = cost;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    //to display object as a string in spinner
    @NonNull
    @Override
    public String toString() {
        return Name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Items) {
            Items c = (Items) obj;
            if (c.getName().equals(Name) && c.getId() == id) return true;
        }

        return false;
    }
}
