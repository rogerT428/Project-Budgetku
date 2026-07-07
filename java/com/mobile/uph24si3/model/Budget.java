package com.mobile.uph24si3.model;

public class Budget {
    private long id;
    private String category;
    private double monthlyLimit;
    private double usedAmount;
    private String month; // yyyy-MM format
    private String color; // for display

    public Budget() {}

    public Budget(String category, double monthlyLimit, String month) {
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.month = month;
        this.usedAmount = 0;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(double monthlyLimit) { this.monthlyLimit = monthlyLimit; }
    public double getUsedAmount() { return usedAmount; }
    public void setUsedAmount(double usedAmount) { this.usedAmount = usedAmount; }
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public double getRemainingAmount() {
        return monthlyLimit - usedAmount;
    }

    public float getProgressPercent() {
        if (monthlyLimit <= 0) return 0f;
        return (float) (usedAmount / monthlyLimit) * 100f;
    }

    public boolean isOverBudget() {
        return usedAmount > monthlyLimit;
    }

    public boolean isNearLimit() {
        return getProgressPercent() >= 80f && !isOverBudget();
    }
}
