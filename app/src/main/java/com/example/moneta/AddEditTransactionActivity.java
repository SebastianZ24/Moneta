package com.example.moneta;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    private Button manageCategoriesButton;

    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private DatabaseHelper dbHelper;
    private long transactionId = -1; // To track if we are editing
    private Transaction.TransactionType currentTransactionType = Transaction.TransactionType.INCOME; // Default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_transaction);

        titleTextView = findViewById(R.id.title_textview);
        typeRadioGroup = findViewById(R.id.type_radio_group);
        incomeRadioButton = findViewById(R.id.income_radio_button);
        expenseRadioButton = findViewById(R.id.expense_radio_button);
        categorySpinner = findViewById(R.id.category_spinner);
        amountEditText = findViewById(R.id.amount_edittext);
        descriptionEditText = findViewById(R.id.description_edittext);
        dateButton = findViewById(R.id.date_button);
        saveButton = findViewById(R.id.save_button);
        manageCategoriesButton = findViewById(R.id.manage_categories_button);

        dbHelper = new DatabaseHelper(this);

        // Check if we are editing an existing transaction
        transactionId = getIntent().getLongExtra("transaction_id", -1);
        if (transactionId != -1) {
            titleTextView.setText("Edit Transaction");
            loadTransactionDetails(transactionId);
        } else {
            titleTextView.setText("Add New Transaction"); // Ensure the title is correct for adding
            incomeRadioButton.setChecked(true); // Select the income radio button by default
            currentTransactionType = Transaction.TransactionType.INCOME; // Set the initial type
            updateCategorySpinner(currentTransactionType); // Load income categories initially
            dateButton.setText(dateFormatter.format(calendar.getTime()));
        }

        typeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.income_radio_button) {
                currentTransactionType = Transaction.TransactionType.INCOME;
                updateCategorySpinner(currentTransactionType);
            } else if (checkedId == R.id.expense_radio_button) {
                currentTransactionType = Transaction.TransactionType.EXPENSE;
                updateCategorySpinner(currentTransactionType);
            }
        });


        dateButton.setOnClickListener(v -> showDatePickerDialog());

        saveButton.setOnClickListener(v -> saveTransaction());

        manageCategoriesButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddEditTransactionActivity.this, ManageCategoriesActivity.class);
            startActivity(intent);
        });
    }

    private void loadTransactionDetails(long id) {
        Transaction transaction = dbHelper.getTransaction(id);
        if (transaction != null) {
            amountEditText.setText(String.valueOf(transaction.getAmount()));
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

            // Select the correct category in the spinner
            String categoryToSelect = transaction.getCategory();
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) categorySpinner.getAdapter();
            if (adapter != null) {
                int position = adapter.getPosition(categoryToSelect);
                if (position != -1) {
                    categorySpinner.setSelection(position);
                }
            }
        }
    }

    private void updateCategorySpinner(Transaction.TransactionType type) {
        List<String> categories = dbHelper.getAllCategories(type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        categorySpinner.setAdapter(adapter);
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dateButton.setText(dateFormatter.format(calendar.getTime()));
            }
        };

        new DatePickerDialog(AddEditTransactionActivity.this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void saveTransaction() {
        String amountStr = amountEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String category = (String) categorySpinner.getSelectedItem();
        long date = calendar.getTimeInMillis();

        if (amountStr.isEmpty() || category == null) {
            Toast.makeText(this, "Amount and category are required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            Transaction transaction;

            if (transactionId != -1) {
                transaction = new Transaction(transactionId, amount, currentTransactionType, category, date, description);
                dbHelper.updateTransaction(transaction);
                Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show();
            } else {
                transaction = new Transaction(0, amount, currentTransactionType, category, date, description);
                long newId = dbHelper.addTransaction(transaction);
                if (newId != -1) {
                    Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to add transaction", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            setResult(RESULT_OK);
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
        }
    }
}