package com.example.moneta;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import android.app.AlertDialog; // Import AlertDialog

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;


// --- Implement listener interfaces ---
public class InvestmentsActivity extends AppCompatActivity implements
        InvestmentHoldingAdapter.OnInvestmentClickListener,
        InvestmentHoldingAdapter.OnInvestmentLongClickListener {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private Button refreshButton;
    private FloatingActionButton addInvestmentFab;
    private DatabaseHelper dbHelper;
    private InvestmentHoldingAdapter adapter;
    // Use different request code for edit vs add if needed, or just check result
    private static final int ADD_EDIT_INVESTMENT_REQUEST = 2;
    private static final String TAG = "InvestmentsActivity";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final String API_KEY = "JIEML1MHVOKASSZV"; // Replace!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment);

        toolbar = findViewById(R.id.toolbar_investments);
        recyclerView = findViewById(R.id.investments_recyclerview);
        refreshButton = findViewById(R.id.refresh_investments_button);
        addInvestmentFab = findViewById(R.id.add_investment_fab);

        dbHelper = new DatabaseHelper(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Investments");
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InvestmentHoldingAdapter(new ArrayList<>());
        // --- Set listeners on the adapter ---
        adapter.setOnInvestmentClickListener(this);
        adapter.setOnInvestmentLongClickListener(this);
        // --- End set listeners ---
        recyclerView.setAdapter(adapter);

        loadInvestmentHoldings();

        refreshButton.setOnClickListener(v -> {
            if (API_KEY.equals("YOUR_ALPHA_VANTAGE_API_KEY") || API_KEY.isEmpty()) {
                Toast.makeText(this, "Please set your Alpha Vantage API_KEY in InvestmentsActivity.java", Toast.LENGTH_LONG).show();
                return;
            }
            refreshInvestmentPrices();
        });

        addInvestmentFab.setOnClickListener(v -> {
            // Start AddInvestmentActivity without passing an ID for adding
            Intent intent = new Intent(InvestmentsActivity.this, AddInvestmentActivity.class);
            startActivityForResult(intent, ADD_EDIT_INVESTMENT_REQUEST);
        });
    }

    // --- Implementation of OnInvestmentClickListener ---
    @Override
    public void onInvestmentClick(InvestmentHolding holding) {
        // Start AddInvestmentActivity in edit mode, passing the ID
        Intent intent = new Intent(InvestmentsActivity.this, AddInvestmentActivity.class);
        intent.putExtra("investment_id", holding.getId());
        startActivityForResult(intent, ADD_EDIT_INVESTMENT_REQUEST);
    }
    // --- End implementation ---

    // --- Implementation of OnInvestmentLongClickListener ---
    @Override
    public void onInvestmentLongClick(InvestmentHolding holding) {
        // Show delete confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Investment")
                .setMessage("Are you sure you want to delete this holding?\n(" + holding.getTickerSymbol() + " - " + holding.getQuantity() + " shares)")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteHolding(holding.getId()); // Call delete method
                })
                .setNegativeButton("No", null)
                .show();
    }
    // --- End implementation ---

    // --- NEW: Method to handle deletion ---
    private void deleteHolding(long id) {
        // Background thread recommended!
        executor.execute(() -> {
            dbHelper.deleteInvestmentHolding(id);
            // Post back to main thread to reload list and show toast
            mainHandler.post(() -> {
                Toast.makeText(InvestmentsActivity.this, "Investment deleted", Toast.LENGTH_SHORT).show();
                loadInvestmentHoldings(); // Refresh the list
            });
        });
    }
    // --- End delete method ---


    private void refreshInvestmentPrices() {
        refreshButton.setEnabled(false);
        Toast.makeText(this, "Refreshing prices...", Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            final List<String> failedSymbols = new ArrayList<>();
            final int[] successCount = {0};
            List<InvestmentHolding> currentHoldings = dbHelper.getAllInvestmentHoldings();
            final int totalCount = currentHoldings.size();

            if (totalCount == 0) {
                mainHandler.post(() -> {
                    refreshButton.setEnabled(true);
                    Toast.makeText(InvestmentsActivity.this, "No investments to refresh.", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            AlphaVantageService apiService = RetrofitClient.getClient().create(AlphaVantageService.class);

            for (InvestmentHolding holding : currentHoldings) {
                if (holding == null || holding.getTickerSymbol() == null || holding.getTickerSymbol().isEmpty()) {
                    Log.w(TAG, "Skipping holding with invalid data: ID " + (holding != null ? holding.getId() : "null"));
                    continue;
                }
                Call<QuoteResponse> call = apiService.getStockQuote("GLOBAL_QUOTE", holding.getTickerSymbol(), API_KEY);
                try {
                    Response<QuoteResponse> response = call.execute();
                    if (response.isSuccessful() && response.body() != null) {
                        GlobalQuote quote = response.body().getGlobalQuote();
                        if (quote != null && quote.getPrice() != null && !quote.getPrice().isEmpty()) {
                            double currentPrice = quote.getPriceAsDouble();
                            if (currentPrice > 0) {
                                int rowsAffected = dbHelper.updateInvestmentCurrentPrice(holding.getId(), currentPrice);
                                if (rowsAffected > 0) {
                                    successCount[0]++;
                                } else {
                                    Log.w(TAG, "DB update failed for: " + holding.getTickerSymbol());
                                    failedSymbols.add(holding.getTickerSymbol() + " (DB Error)");
                                }
                            } else {
                                Log.w(TAG, "Received invalid price value for symbol: " + holding.getTickerSymbol() + ", Price String: " + quote.getPrice());
                                failedSymbols.add(holding.getTickerSymbol() + " (Invalid Price Data)");
                            }
                        } else {
                            Log.w(TAG, "No valid quote data returned for symbol: " + holding.getTickerSymbol());
                            failedSymbols.add(holding.getTickerSymbol() + " (Invalid Symbol?)");
                        }
                    } else {
                        String errorBodyStr = "Code: " + response.code();
                        if (response.errorBody() != null) {
                            try { errorBodyStr = response.errorBody().string(); } catch (Exception e) { /* Ignore */ }
                        }
                        Log.e(TAG, "API call failed for " + holding.getTickerSymbol() + ": " + errorBodyStr);
                        failedSymbols.add(holding.getTickerSymbol() + " (API Error)");
                        if (errorBodyStr.contains("limit") || errorBodyStr.contains("premium") || errorBodyStr.contains("frequency")) {
                            Log.w(TAG, "Rate limit possibly hit for " + holding.getTickerSymbol());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception during API call for " + holding.getTickerSymbol(), e);
                    failedSymbols.add(holding.getTickerSymbol() + " (Network/Other Error)");
                }

                if (currentHoldings.indexOf(holding) < totalCount - 1) {
                    try {
                        Thread.sleep(13000); // Adjust sleep time based on API limits
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Log.e(TAG, "Thread sleep interrupted", e);
                        mainHandler.post(() -> Toast.makeText(InvestmentsActivity.this, "Refresh interrupted.", Toast.LENGTH_SHORT).show());
                        break;
                    }
                }
            } // End loop

            mainHandler.post(() -> {
                refreshButton.setEnabled(true);
                String summaryMsg;
                if (failedSymbols.isEmpty()) {
                    summaryMsg = "Refreshed prices successfully for " + successCount[0] + "/" + totalCount + " holdings.";
                } else {
                    summaryMsg = "Refreshed " + successCount[0] + "/" + totalCount + ". Failed: " + String.join(", ", failedSymbols);
                }
                Toast.makeText(InvestmentsActivity.this, summaryMsg, Toast.LENGTH_LONG).show();
                loadInvestmentHoldings(); // Reload data from DB
            });
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Reload list if item was added OR edited
        if (requestCode == ADD_EDIT_INVESTMENT_REQUEST && resultCode == RESULT_OK) {
            loadInvestmentHoldings();
        }
    }

    private void loadInvestmentHoldings() {
        executor.execute(() -> {
            List<InvestmentHolding> holdings = dbHelper.getAllInvestmentHoldings();
            mainHandler.post(() -> {
                if (adapter != null) {
                    adapter.setHoldings(holdings);
                }
            });
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
