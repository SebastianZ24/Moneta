package com.example.moneta.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.moneta.DatabaseHelper;
import com.example.moneta.R;
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
    private long transactionId = -1;
    private Transaction.TransactionType currentTransactionType = Transaction.TransactionType.INCOME;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_transaction);

        toolbar = findViewById(R.id.toolbar_add_edit);
        setSupportActionBar(toolbar);

        titleTextView = findViewById(R.id.title_textview);
        typeRadioGroup = findViewById(R.id.type_radio_group);
        incomeRadioButton = findViewById(R.id.income_radio_button);
        expenseRadioButton = findViewById(R.id.expense_radio_button);
        categorySpinner = findViewById(R.id.category_spinner);
        amountEditText = findViewById(R.id.amount_edittext);
        descriptionEditText = findViewById(R.id.description_edittext);
        dateButton = findViewById(R.id.date_button);
        saveButton = findViewById(R.id.save_button);

        dbHelper = new DatabaseHelper(this);

        transactionId = getIntent().getLongExtra("transaction_id", -1);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (transactionId != -1) {
                getSupportActionBar().setTitle("Edit Transaction");
                titleTextView.setVisibility(View.GONE);
                loadTransactionDetails(transactionId);
            } else {
                getSupportActionBar().setTitle("Add New Transaction");
                titleTextView.setVisibility(View.GONE);
                incomeRadioButton.setChecked(true);
                currentTransactionType = Transaction.TransactionType.INCOME;
                updateCategorySpinner(currentTransactionType);
                dateButton.setText(dateFormatter.format(calendar.getTime()));
            }
        }

        typeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.income_radio_button) {
                currentTransactionType = Transaction.TransactionType.INCOME;
            } else if (checkedId == R.id.expense_radio_button) {
                currentTransactionType = Transaction.TransactionType.EXPENSE;
            }
            updateCategorySpinner(currentTransactionType);
        });

        dateButton.setOnClickListener(v -> showDatePickerDialog());
        saveButton.setOnClickListener(v -> saveTransaction());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadTransactionDetails(long id) {
        Transaction transaction = dbHelper.getTransaction(id);
        if (transaction != null) {
            amountEditText.setText(String.format(Locale.US, "%.2f", transaction.getAmount()));
            descriptionEditText.setText(transaction.getDescription());
            calendar.setTimeInMillis(transaction.getDate());
            dateButton.setText(dateFormatter.format(calendar.getTime()));

            if (transaction.getType() == Transaction.TransactionType.INCOME) {
                incomeRadioButton.setChecked(true);
                currentTransactionType = Transaction.TransactionType.INCOME;
            } else {
                expenseRadioButton.setChecked(true);
                currentTransactionType = Transaction.TransactionType.EXPENSE;
            }

            updateCategorySpinner(currentTransactionType);

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
            finish();
        }
    }

    private void updateCategorySpinner(Transaction.TransactionType type) {
        List<String> categories = dbHelper.getAllCategories(type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
        String amountStr = amountEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        Object selectedItem = categorySpinner.getSelectedItem();
        String category = (selectedItem != null) ? selectedItem.toString() : null;
        long date = calendar.getTimeInMillis();

        if (amountStr.isEmpty()) {
            amountEditText.setError("Amount is required");
            return;
        }
        if (category == null || category.isEmpty()) {
            Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show();
            return;
        }


        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                amountEditText.setError("Amount must be positive");
                return;
            }

            Transaction transaction;
            boolean success = false;

            if (transactionId != -1) {
                transaction = new Transaction(transactionId, amount, currentTransactionType, category, date, description);
                int rowsAffected = dbHelper.updateTransaction(transaction);
                if (rowsAffected > 0) {
                    Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show();
                    success = true;
                } else {
                    Toast.makeText(this, "Failed to update transaction", Toast.LENGTH_SHORT).show();
                }
            } else {
                transaction = new Transaction(0, amount, currentTransactionType, category, date, description);
                long newId = dbHelper.addTransaction(transaction);
                if (newId != -1) {
                    Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show();
                    success = true;
                } else {
                    Toast.makeText(this, "Failed to add transaction", Toast.LENGTH_SHORT).show();
                }
            }

            if (success) {
                setResult(RESULT_OK);
                finish();
            }

        } catch (NumberFormatException e) {
            amountEditText.setError("Invalid amount format");
        } catch (Exception e) {
            Toast.makeText(this, "An error occurred while saving.", Toast.LENGTH_SHORT).show();
        }
    }
}