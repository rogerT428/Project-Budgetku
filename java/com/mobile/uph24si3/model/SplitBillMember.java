package com.mobile.uph24si3.model;

public class SplitBillMember {
    private long id;
    private long splitBillId;
    private String name;
    private double amountOwed;
    private boolean isPaid;
    private String paidDate;

    public SplitBillMember() {}

    public SplitBillMember(long splitBillId, String name, double amountOwed) {
        this.splitBillId = splitBillId;
        this.name = name;
        this.amountOwed = amountOwed;
        this.isPaid = false;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getSplitBillId() { return splitBillId; }
    public void setSplitBillId(long splitBillId) { this.splitBillId = splitBillId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getAmountOwed() { return amountOwed; }
    public void setAmountOwed(double amountOwed) { this.amountOwed = amountOwed; }
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }
    public String getPaidDate() { return paidDate; }
    public void setPaidDate(String paidDate) { this.paidDate = paidDate; }
}
