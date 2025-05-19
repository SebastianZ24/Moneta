package com.example.moneta.investapi;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AlphaVantageService {
    @GET("query")
    Call<QuoteResponse> getStockQuote(
            @Query("function") String function,
            @Query("symbol") String symbol,
            @Query("apikey") String apiKey
    );
}