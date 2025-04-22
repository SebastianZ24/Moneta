package com.example.moneta;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AlphaVantageService {

    // Define the endpoint for Alpha Vantage's GLOBAL_QUOTE function
    // Example URL: https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=IBM&apikey=YOUR_API_KEY
    @GET("query")
    Call<QuoteResponse> getStockQuote(
            @Query("function") String function, // Should be "GLOBAL_QUOTE"
            @Query("symbol") String symbol,
            @Query("apikey") String apiKey
    );
}