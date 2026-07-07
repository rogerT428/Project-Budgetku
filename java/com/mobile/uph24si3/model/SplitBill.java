package com.mobile.uph24si3.model;

import java.util.ArrayList;
import java.util.List;

public class SplitBill {
    private long id;
    private String title;
    private double totalAmount;
    private String date;
    private String notes;
    private List<SplitBillMember> members;
    private boolean isSettled;

    public SplitBill() {
        members = new ArrayList<>();
    }

    public SplitBill(String title, double totalAmount, String date) {
        this.title = title;
        this.totalAmount = totalAmount;
        this.date = date;
        this.members = new ArrayList<>();
        this.isSettled = false;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<SplitBillMember> getMembers() { return members; }
    public void setMembers(List<SplitBillMember> members) { this.members = members; }
    public boolean isSettled() { return isSettled; }
    public void setSettled(boolean settled) { isSettled = settled; }

    public double getAmountPerPerson() {
        if (members == null || members.isEmpty()) return totalAmount;
        return totalAmount / members.size();
    }

    public int getMemberCount() {
        return members != null ? members.size() : 0;
    }

    public int getPaidCount() {
        if (members == null) return 0;
        int count = 0;
        for (SplitBillMember m : members) {
            if (m.isPaid()) count++;
        }
        return count;
    }

    public double getTotalPaid() {
        if (members == null) return 0;
        double total = 0;
        for (SplitBillMember m : members) {
            if (m.isPaid()) total += m.getAmountOwed();
        }
        return total;
    }
}
