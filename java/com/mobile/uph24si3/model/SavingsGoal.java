package com.mobile.uph24si3.model;

public class SavingsGoal {
    private long id;
    private String name;
    private double targetAmount;
    private double currentAmount;
    private String targetDate; // yyyy-MM-dd
    private String icon;       // emoji or icon name
    private boolean isCompleted;

    public SavingsGoal() {}

    public SavingsGoal(String name, double targetAmount, double currentAmount, String targetDate) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
        this.isCompleted = false;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }
    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }
    public String getTargetDate() { return targetDate; }
    public void setTargetDate(String targetDate) { this.targetDate = targetDate; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public float getProgressPercent() {
        if (targetAmount <= 0) return 0f;
        float pct = (float) (currentAmount / targetAmount) * 100f;
        return Math.min(pct, 100f);
    }

    public double getRemainingAmount() {
        return Math.max(0, targetAmount - currentAmount);
    }
}
