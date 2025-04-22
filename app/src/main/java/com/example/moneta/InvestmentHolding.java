package com.example.moneta;

public class InvestmentHolding {
    private long id;
    private String tickerSymbol; // e.g., "AAPL", "GOOGL"
    private String companyName;  // Optional, can be fetched later or entered manually
    private double quantity;     // Number of shares/units owned
    private double purchasePrice; // Price per share/unit at purchase
    private long purchaseDate;   // Milliseconds since epoch
    private double currentPrice; // Last fetched price per share/unit (initially maybe 0 or purchasePrice)
    // You might add purchaseCommission, notes, etc. later

    // Constructor (consider one without ID for adding new)
    public InvestmentHolding(long id, String tickerSymbol, String companyName, double quantity, double purchasePrice, long purchaseDate, double currentPrice) {
        this.id = id;
        this.tickerSymbol = tickerSymbol;
        this.companyName = companyName;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
        this.currentPrice = currentPrice;
    }

    // --- Getters and Setters for all fields ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTickerSymbol() { return tickerSymbol; }
    public void setTickerSymbol(String tickerSymbol) { this.tickerSymbol = tickerSymbol; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }
    public long getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(long purchaseDate) { this.purchaseDate = purchaseDate; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    // --- Calculated Properties (Example) ---
    public double getTotalCost() {
        return quantity * purchasePrice;
        // Add commission later if needed
    }

    public double getCurrentValue() {
        return quantity * currentPrice;
    }

    public double getProfitLoss() {
        return getCurrentValue() - getTotalCost();
    }

    public double getProfitLossPercent() {
        double cost = getTotalCost();
        if (cost == 0) return 0; // Avoid division by zero
        return (getProfitLoss() / cost) * 100.0;
    }
}