package com.example.moneta.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.moneta.DatabaseHelper;
import com.example.moneta.R;
import com.example.moneta.model.InvestmentHolding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddInvestmentActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText symbolEditText, quantityEditText, priceEditText, nameEditText;
    private Button dateButton, saveButton;

    private DatabaseHelper dbHelper;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final String TAG = "AddInvestmentActivity";

    private boolean isEditing = false;
    private long editingInvestmentId = -1;
    private InvestmentHolding existingHolding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_investment);

        toolbar = findViewById(R.id.toolbar_add_investment);
        symbolEditText = findViewById(R.id.add_inv_symbol_edittext);
        quantityEditText = findViewById(R.id.add_inv_quantity_edittext);
        priceEditText = findViewById(R.id.add_inv_purchase_price_edittext);
        nameEditText = findViewById(R.id.add_inv_company_name_edittext);
        dateButton = findViewById(R.id.add_inv_purchase_date_button);
        saveButton = findViewById(R.id.add_inv_save_button);

        dbHelper = new DatabaseHelper(this);

        if (getIntent().hasExtra("investment_id")) {
            editingInvestmentId = getIntent().getLongExtra("investment_id", -1);
            if (editingInvestmentId != -1) {
                isEditing = true;
            }
        }

        setupToolbar();

        if (isEditing) {
            loadHoldingData();
        } else {
            dateButton.setText(dateFormatter.format(calendar.getTime()));
        }

        dateButton.setOnClickListener(v -> showDatePickerDialog());
        saveButton.setOnClickListener(v -> saveOrUpdateInvestment());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(isEditing ? "Edit Investment" : "Add Investment");
        }
    }

    private void loadHoldingData() {
        Log.d(TAG, "Loading data for investment ID: " + editingInvestmentId);
        existingHolding = dbHelper.getInvestmentHolding(editingInvestmentId);
        if (existingHolding != null) {
            symbolEditText.setText(existingHolding.getTickerSymbol());
            quantityEditText.setText(String.format(Locale.US, "%.2f", existingHolding.getQuantity()));
            priceEditText.setText(String.format(Locale.US, "%.2f", existingHolding.getPurchasePrice()));
            nameEditText.setText(existingHolding.getCompanyName());
            calendar.setTimeInMillis(existingHolding.getPurchaseDate());
            dateButton.setText(dateFormatter.format(calendar.getTime()));
            saveButton.setText(R.string.update_investment);
        } else {
            Log.e(TAG, "Error loading investment data for ID: " + editingInvestmentId);
            Toast.makeText(this, "Error loading investment data.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            dateButton.setText(dateFormatter.format(calendar.getTime()));
        };

        new DatePickerDialog(this, dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void saveOrUpdateInvestment() {
        String symbol = symbolEditText.getText().toString().trim().toUpperCase();
        String quantityStr = quantityEditText.getText().toString().trim();
        String priceStr = priceEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        long purchaseDate = calendar.getTimeInMillis();

        boolean valid = true;
        if (symbol.isEmpty()) {
            symbolEditText.setError("Symbol is required");
            valid = false;
        }
        double quantity = 0;
        if (quantityStr.isEmpty()) {
            quantityEditText.setError("Quantity is required");
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(quantityStr);
                if (quantity <= 0) {
                    quantityEditText.setError("Quantity must be positive");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                quantityEditText.setError("Invalid number format");
                valid = false;
            }
        }
        double price = 0;
        if (priceStr.isEmpty()) {
            priceEditText.setError("Purchase Price is required");
            valid = false;
        } else {
            try {
                price = Double.parseDouble(priceStr);
                if (price < 0) {
                    priceEditText.setError("Price must be positive");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                priceEditText.setError("Invalid number format");
                valid = false;
            }
        }
        if (!valid) {
            Log.w(TAG, "Validation failed.");
            return;
        }

        try {
            boolean success = false;
            if (isEditing && existingHolding != null) {
                Log.d(TAG, "Attempting to update investment ID: " + existingHolding.getId());
                existingHolding.setTickerSymbol(symbol);
                existingHolding.setCompanyName(name);
                existingHolding.setQuantity(quantity);
                existingHolding.setPurchasePrice(price);
                existingHolding.setPurchaseDate(purchaseDate);

                int rowsAffected = dbHelper.updateInvestmentHolding(existingHolding);
                if (rowsAffected > 0) {
                    Log.i(TAG, "Investment updated successfully.");
                    Toast.makeText(this, "Investment updated!", Toast.LENGTH_SHORT).show();
                    success = true;
                } else {
                    Log.e(TAG, "Error updating investment in DB, rowsAffected=0.");
                    Toast.makeText(this, "Error updating investment.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(TAG, "Attempting to add new investment: " + symbol);
                InvestmentHolding newHolding = new InvestmentHolding(0, symbol, name, quantity, price, purchaseDate, price);
                long newId = dbHelper.addInvestmentHolding(newHolding);
                if (newId != -1) {
                    Log.i(TAG, "Investment saved successfully with ID: " + newId);
                    Toast.makeText(this, "Investment saved!", Toast.LENGTH_SHORT).show();
                    success = true;
                } else {
                    Log.e(TAG, "Error saving investment to DB, returned ID=-1.");
                    Toast.makeText(this, "Error saving investment.", Toast.LENGTH_SHORT).show();
                }
            }

            if (success) {
                setResult(RESULT_OK);
                finish();
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception during save/update operation", e);
            Toast.makeText(this, "An error occurred during save/update.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
