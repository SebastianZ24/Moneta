package com.example.moneta.model;

public class InvestmentHolding {
    private long id;
    private String tickerSymbol;
    private String companyName;
    private double quantity;
    private double purchasePrice;
    private long purchaseDate;
    private double currentPrice;
    public InvestmentHolding(long id, String tickerSymbol, String companyName, double quantity, double purchasePrice, long purchaseDate, double currentPrice) {
        this.id = id;
        this.tickerSymbol = tickerSymbol;
        this.companyName = companyName;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
        this.currentPrice = currentPrice;
    }

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

    public double getTotalCost() {
        return quantity * purchasePrice;
    }

    public double getCurrentValue() {
        return quantity * currentPrice;
    }

    public double getProfitLoss() {
        return getCurrentValue() - getTotalCost();
    }

    public double getProfitLossPercent() {
        double cost = getTotalCost();
        if (cost == 0) return 0;
        return (getProfitLoss() / cost) * 100.0;
    }
}