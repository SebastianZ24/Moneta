package com.example.moneta;

public class Transaction {
    private long id;
    private double amount;
    private TransactionType type;
    private String category;
    private long date; // Store as milliseconds since epoch
    private String description;

    public enum TransactionType {
        INCOME, EXPENSE
    }

    public Transaction(long id, double amount, TransactionType type, String category, long date, String description) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.description = description;
    }

    // Getters and setters for all fields
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
