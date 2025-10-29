package com.smartrecipes.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents an ingredient in the pantry with name, quantity, and expiry date
 */
public class Ingredient {
    private String name;
    private double quantity;
    private String unit;
    private LocalDate expiryDate;
    private String category;
    
    // Default constructor for JSON deserialization
    public Ingredient() {}
    
    public Ingredient(String name, double quantity, String unit, LocalDate expiryDate, String category) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.category = category;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    /**
     * Check if ingredient is expiring within specified days
     */
    public boolean isExpiringSoon(int days) {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now().plusDays(days));
    }
    
    /**
     * Check if ingredient has expired
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }
    
    /**
     * Get formatted expiry date string
     */
    public String getFormattedExpiryDate() {
        if (expiryDate == null) return "No expiry";
        return expiryDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }
    
    /**
     * Get days until expiry
     */
    public long getDaysUntilExpiry() {
        if (expiryDate == null) return Long.MAX_VALUE;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(name.toLowerCase(), that.name.toLowerCase());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }
    
    @Override
    public String toString() {
        return String.format("%.1f %s %s", quantity, unit, name);
    }
}
