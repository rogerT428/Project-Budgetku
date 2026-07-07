package com.mobile.uph24si3.model;

public class RecurringBill {
    public static final String FREQ_MONTHLY = "MONTHLY";
    public static final String FREQ_WEEKLY = "WEEKLY";
    public static final String FREQ_YEARLY = "YEARLY";

    private long id;
    private String name;
    private double amount;
    private String category;
    private int dueDay;        // day of month (1-31)
    private String frequency;  // MONTHLY, WEEKLY, YEARLY
    private boolean isActive;
    private String nextDueDate;
    private boolean isPaid;    // paid for current cycle

    public RecurringBill() {}

    public RecurringBill(String name, double amount, String category, int dueDay, String frequency) {
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.dueDay = dueDay;
        this.frequency = frequency;
        this.isActive = true;
        this.isPaid = false;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getDueDay() { return dueDay; }
    public void setDueDay(int dueDay) { this.dueDay = dueDay; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(String nextDueDate) { this.nextDueDate = nextDueDate; }
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }
}
