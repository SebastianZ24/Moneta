package com.example.moneta.investapi;

import com.google.gson.annotations.SerializedName;
public class GlobalQuote {
    @SerializedName("01. symbol")
    private String symbol;

    @SerializedName("05. price")
    private String price;

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

    public double getPriceAsDouble() {
        try {
            return Double.parseDouble(price);
        } catch (NumberFormatException | NullPointerException e) {
            return 0.0;
        }
    }
}