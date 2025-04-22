package com.example.moneta;

import com.google.gson.annotations.SerializedName;
// Object containing the actual quote details
public class GlobalQuote {
    @SerializedName("01. symbol")
    private String symbol;

    @SerializedName("05. price")
    private String price; // Price comes as String, need to parse to double

    // Add other fields if needed (e.g., "02. open", "03. high", etc.)

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    // Helper method to get price as double safely
    public double getPriceAsDouble() {
        try {
            return Double.parseDouble(price);
        } catch (NumberFormatException | NullPointerException e) {
            return 0.0; // Return 0 or throw custom exception on parsing error
        }
    }
}