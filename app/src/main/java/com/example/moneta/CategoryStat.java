package com.example.moneta;

// Simple class to hold aggregated category statistics
public class CategoryStat {
    private String categoryName;
    private double totalAmount;

    public CategoryStat(String categoryName, double totalAmount) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    // Optional: Override toString for debugging
    @Override
    public String toString() {
        return "CategoryStat{" +
                "categoryName='" + categoryName + '\'' +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
