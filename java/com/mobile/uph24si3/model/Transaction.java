package com.mobile.uph24si3.model;

public class Transaction {
    public static final String TYPE_INCOME = "INCOME";
    public static final String TYPE_EXPENSE = "EXPENSE";

    private long id;
    private String type;       // INCOME or EXPENSE
    private double amount;
    private String category;
    private String description;
    private String date;       // yyyy-MM-dd
    private String time;       // HH:mm:ss
    private long bankAccountId; // 0 = manual/cash

    public Transaction() {}

    public Transaction(String type, double amount, String category, String description, String date, long bankAccountId) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.bankAccountId = bankAccountId;
    }

    public Transaction(String type, double amount, String category, String description, String date, String time, long bankAccountId) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.time = time;
        this.bankAccountId = bankAccountId;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public long getBankAccountId() { return bankAccountId; }
    public void setBankAccountId(long bankAccountId) { this.bankAccountId = bankAccountId; }

    public boolean isIncome() { return TYPE_INCOME.equals(type); }
    public boolean isExpense() { return TYPE_EXPENSE.equals(type); }
}
