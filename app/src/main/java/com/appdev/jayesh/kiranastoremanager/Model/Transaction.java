package com.appdev.jayesh.kiranastoremanager.Model;

public class Transaction implements Comparable<Transaction> {

    private String accountName;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    private String accountId;
    private String transactionType;
    private Long timeInMilli;
    private String itemName;
    private String itemId;
    private double quantity;
    private double price;
    private double amount;
    private String notes;
    private String Id;
    private long timestamp;
    private Boolean isInventory;
    private Boolean isProcessed;
    private Boolean isBatchItem;

    public String getRawMaterial() {
        return rawMaterial;
    }

    public void setRawMaterial(String rawMaterial) {
        this.rawMaterial = rawMaterial;
    }

    private String rawMaterial;

    public Boolean getInventory() {
        return isInventory;
    }

    public void setInventory(Boolean inventory) {
        isInventory = inventory;
    }

    public Boolean getProcessed() {
        return isProcessed;
    }

    public void setProcessed(Boolean processed) {
        isProcessed = processed;
    }

    public Boolean getBatchItem() {
        return isBatchItem;
    }

    public void setBatchItem(Boolean batchItem) {
        isBatchItem = batchItem;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    private String uom;

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    private String transaction;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public Transaction() {
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Long getTimeInMilli() {
        return timeInMilli;
    }

    public void setTimeInMilli(Long timeInMilli) {
        this.timeInMilli = timeInMilli;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public int compareTo(Transaction o) {
        return Long.compare(timeInMilli, o.timeInMilli);

    }

}
