package com.appdev.jayesh.kiranastoremanager.Model;

import java.util.HashMap;
import java.util.List;

public class Items {
    private String Name;
    private double Price;
    private String ItemFor;
    private double Cost;
    private String key;

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

    public String getItemFor() {
        return ItemFor;
    }

    public void setItemFor(String itemFor) {
        this.ItemFor = itemFor;
    }

    public double getCost() {
        return Cost;
    }

    public void setCost(double cost) {
        this.Cost = cost;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
