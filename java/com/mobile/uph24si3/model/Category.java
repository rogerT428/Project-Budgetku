package com.mobile.uph24si3.model;

public class Category {
    private long id;
    private String name;
    private String icon;    // emoji
    private String color;   // hex color
    private String type;    // EXPENSE, INCOME, or BOTH
    private boolean isDefault;

    public Category() {}

    public Category(String name, String icon, String color, String type) {
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.type = type;
        this.isDefault = false;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    @Override
    public String toString() { return name; }
}
