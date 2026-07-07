package com.mobile.uph24si3.model;

public class BankAccount {
    private long id;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private double balance;
    private String bankType; // BCA, GoPay, OVO, Dana, Mandiri, BNI, BRI, etc.
    private String color;   // hex color for card display

    public BankAccount() {}

    public BankAccount(String bankName, String accountNumber, String accountHolder, double balance, String bankType) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = balance;
        this.bankType = bankType;
        this.color = getDefaultColorForBank(bankType);
    }

    public static String getDefaultColorForBank(String bankType) {
        if (bankType == null) return "#1890FF";
        switch (bankType.toUpperCase()) {
            case "BCA":      return "#0066AE";
            case "MANDIRI":  return "#003D72";
            case "BNI":      return "#FF6600";
            case "BRI":      return "#003087";
            case "GOPAY":    return "#00AED6";
            case "OVO":      return "#4C3494";
            case "DANA":     return "#118EEA";
            case "SHOPEEPAY": return "#EE4D2D";
            default:         return "#1890FF";
        }
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getAccountHolder() { return accountHolder; }
    public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public String getBankType() { return bankType; }
    public void setBankType(String bankType) { this.bankType = bankType; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getMaskedAccountNumber() {
        if (accountNumber == null || accountNumber.length() < 4) return accountNumber;
        return "**** **** " + accountNumber.substring(accountNumber.length() - 4);
    }
}
