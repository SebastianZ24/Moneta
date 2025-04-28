package com.example.moneta;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem; // <<< NEW Import
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // <<< NEW Import
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // <<< NEW Import

import com.example.moneta.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddEditTransactionActivity extends AppCompatActivity {

    private TextView titleTextView;
    private RadioGroup typeRadioGroup;
    private RadioButton incomeRadioButton;
    private RadioButton expenseRadioButton;
    private Spinner categorySpinner;
    private EditText amountEditText;
    private EditText descriptionEditText;
    private Button dateButton;
    private Button saveButton;

    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private DatabaseHelper dbHelper;
    private long transactionId = -1; // To track if we are editing
    private Transaction.TransactionType currentTransactionType = Transaction.TransactionType.INCOME; // Default
    private Toolbar toolbar; // <<< NEW Toolbar variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use the new layout with Toolbar and ScrollView
        setContentView(R.layout.activity_add_edit_transaction);

        // --- Toolbar Setup --- <<< NEW Block
        toolbar = findViewById(R.id.toolbar_add_edit); // Use the ID from your XML
        setSupportActionBar(toolbar);
        // --- End Toolbar Setup ---

        // Initialize other views
        titleTextView = findViewById(R.id.title_textview);
        typeRadioGroup = findViewById(R.id.type_radio_group);
        incomeRadioButton = findViewById(R.id.income_radio_button);
        expenseRadioButton = findViewById(R.id.expense_radio_button);
        categorySpinner = findViewById(R.id.category_spinner);
        amountEditText = findViewById(R.id.amount_edittext);
        descriptionEditText = findViewById(R.id.description_edittext);
        dateButton = findViewById(R.id.date_button);
        saveButton = findViewById(R.id.save_button);
        // manageCategoriesButton = findViewById(R.id.manage_categories_button); // <<< REMOVE this line

        dbHelper = new DatabaseHelper(this);

        // Check if we are editing an existing transaction
        transactionId = getIntent().getLongExtra("transaction_id", -1);

        // --- Setup Toolbar Title and Up Button --- <<< NEW/MODIFIED Block
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back arrow
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (transactionId != -1) {
                getSupportActionBar().setTitle("Edit Transaction"); // Set title in Toolbar
                titleTextView.setVisibility(View.GONE); // Hide the old TextView title
                loadTransactionDetails(transactionId);
            } else {
                getSupportActionBar().setTitle("Add New Transaction"); // Set title in Toolbar
                titleTextView.setVisibility(View.GONE); // Hide the old TextView title
                incomeRadioButton.setChecked(true);
                currentTransactionType = Transaction.TransactionType.INCOME;
                updateCategorySpinner(currentTransactionType);
                dateButton.setText(dateFormatter.format(calendar.getTime()));
            }
        }
        // --- End Toolbar Title Setup ---


        // Set listener for type change
        typeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.income_radio_button) {
                currentTransactionType = Transaction.TransactionType.INCOME;
            } else if (checkedId == R.id.expense_radio_button) {
                currentTransactionType = Transaction.TransactionType.EXPENSE;
            }
            updateCategorySpinner(currentTransactionType); // Update categories on type change
        });


        dateButton.setOnClickListener(v -> showDatePickerDialog());

        saveButton.setOnClickListener(v -> saveTransaction());

        // manageCategoriesButton.setOnClickListener(v -> { // <<< REMOVE this whole listener block
        //     Intent intent = new Intent(AddEditTransactionActivity.this, ManageCategoriesActivity.class);
        //     startActivity(intent);
        // });
    }

    // Handle Up button click <<< NEW Method
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Mimic back press behavior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadTransactionDetails(long id) {
        // Background thread recommended
        Transaction transaction = dbHelper.getTransaction(id);
        if (transaction != null) {
            amountEditText.setText(String.format(Locale.US, "%.2f", transaction.getAmount())); // Use locale-safe format
            descriptionEditText.setText(transaction.getDescription());
            calendar.setTimeInMillis(transaction.getDate());
            dateButton.setText(dateFormatter.format(calendar.getTime()));

            // Set the correct radio button and update the spinner
            if (transaction.getType() == Transaction.TransactionType.INCOME) {
                incomeRadioButton.setChecked(true);
                currentTransactionType = Transaction.TransactionType.INCOME;
            } else {
                expenseRadioButton.setChecked(true);
                currentTransactionType = Transaction.TransactionType.EXPENSE;
            }
            // Update spinner *after* setting the type
            updateCategorySpinner(currentTransactionType);

            // Select the correct category in the spinner *after* adapter is set
            String categoryToSelect = transaction.getCategory();
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) categorySpinner.getAdapter();
            if (adapter != null) {
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (adapter.getItem(i).equals(categoryToSelect)) {
                        categorySpinner.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            Toast.makeText(this, "Error loading transaction details.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if transaction doesn't exist
        }
    }

    private void updateCategorySpinner(Transaction.TransactionType type) {
        // Background thread recommended
        List<String> categories = dbHelper.getAllCategories(type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories); // Use standard item layout
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // Standard dropdown layout
        categorySpinner.setAdapter(adapter);
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            dateButton.setText(dateFormatter.format(calendar.getTime()));
        };

        new DatePickerDialog(AddEditTransactionActivity.this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void saveTransaction() {
        String amountStr = amountEditText.getText().toString().trim(); // Trim whitespace
        String description = descriptionEditText.getText().toString().trim(); // Trim whitespace
        Object selectedItem = categorySpinner.getSelectedItem(); // Check if null
        String category = (selectedItem != null) ? selectedItem.toString() : null;
        long date = calendar.getTimeInMillis(); // Consider setting time to start/end of day?

        // Validation
        if (amountStr.isEmpty()) {
            amountEditText.setError("Amount is required");
            // Toast.makeText(this, "Amount is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (category == null || category.isEmpty()) {
            Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show();
            // Optionally set error on spinner? Tricky.
            return;
        }


        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) { // Basic validation for amount > 0
                amountEditText.setError("Amount must be positive");
                return;
            }

            Transaction transaction;
            boolean success = false;

            // Background thread HIGHLY recommended for DB operations
            if (transactionId != -1) {
                // Update existing transaction
                transaction = new Transaction(transactionId, amount, currentTransactionType, category, date, description);
                int rowsAffected = dbHelper.updateTransaction(transaction);
                if (rowsAffected > 0) {
                    Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show();
                    success = true;
                } else {
                    Toast.makeText(this, "Failed to update transaction", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Add new transaction
                transaction = new Transaction(0, amount, currentTransactionType, category, date, description);
                long newId = dbHelper.addTransaction(transaction);
                if (newId != -1) {
                    Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show();
                    success = true;
                } else {
                    Toast.makeText(this, "Failed to add transaction", Toast.LENGTH_SHORT).show();
                }
            }

            // Finish activity only if save/update was successful
            if (success) {
                setResult(RESULT_OK); // Signal success to MainActivity
                finish(); // Close this activity
            }

        } catch (NumberFormatException e) {
            amountEditText.setError("Invalid amount format");
            // Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Log general errors: Log.e("SaveTransaction", "Error saving transaction", e);
            Toast.makeText(this, "An error occurred while saving.", Toast.LENGTH_SHORT).show();
        }
    }
}