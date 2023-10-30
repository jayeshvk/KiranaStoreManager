package com.appdev.jayesh.kiranastoremanager.Model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.HashMap;

public class Items implements Serializable {
    private String Name;
    private double Price;
    private double Cost;
    private String id;
    private double rawStock;
    private Boolean isInventory;
    private Boolean isProcessed;
    private Boolean isBatchItem;

    public Boolean getIsInventory() {
        return isInventory;
    }

    public void setIsInventory(Boolean inventory) {
        isInventory = inventory;
    }

    public Boolean getIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(Boolean processed) {
        isProcessed = processed;
    }

    public Boolean getIsBatchItem() {
        return isBatchItem;
    }

    public void setIsBatchItem(Boolean batch) {
        isBatchItem = batch;
    }

    public double getRawStock() {
        return rawStock;
    }

    public void setRawStock(double rawStock) {
        this.rawStock = rawStock;
    }

    public String getRawMaterial() {
        return rawMaterial;
    }

    public void setRawMaterial(String rawMaterial) {
        this.rawMaterial = rawMaterial;
    }

    private String rawMaterial;

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
