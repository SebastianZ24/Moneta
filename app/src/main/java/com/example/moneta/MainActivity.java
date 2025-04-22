package com.example.moneta;

import android.content.Intent;
// Removed unused ColorStateList import
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast; // Import Toast
import android.app.AlertDialog; // Import AlertDialog
import android.widget.GridLayout;
// Removed unused LinearLayout import
// Removed unused ViewCompat import

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// Implement both interfaces
public class MainActivity extends AppCompatActivity implements
        TransactionAdapter.OnTransactionClickListener,
        TransactionAdapter.OnTransactionDeleteListener { // <<< ADDED Delete Listener

    private TextView balanceTextView;
    private TextView incomeTextView;
    private TextView expenseTextView;
    private RecyclerView transactionsRecyclerView;
    private TransactionAdapter transactionAdapter;
    private DatabaseHelper dbHelper;
    private FloatingActionButton addTransactionFab;
    private TextView monthYearTextView;

    private int selectedYear;
    private int selectedMonth;
    // Corrected date format pattern to display month name and year
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private static final int ADD_EDIT_TRANSACTION_REQUEST = 1;
    private AlertDialog monthYearDialog; // Use a more specific name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Reference main activity layout

        // Initialize Views
        balanceTextView = findViewById(R.id.balance_textview);
        incomeTextView = findViewById(R.id.income_textview);
        expenseTextView = findViewById(R.id.expense_textview);
        transactionsRecyclerView = findViewById(R.id.transactions_recyclerview);
        addTransactionFab = findViewById(R.id.add_transaction_fab);
        monthYearTextView = findViewById(R.id.month_year_textview);

        // Initialize Database Helper
        dbHelper = new DatabaseHelper(this); //

        // Setup RecyclerView
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Pass 'this' as the implementation for BOTH listeners to the adapter's constructor
        transactionAdapter = new TransactionAdapter(new ArrayList<>(), this, this); // Adapter requires listeners
        transactionsRecyclerView.setAdapter(transactionAdapter);

        // Set initial month/year and load data
        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        updateMonthYearTextView(); // Display initial month/year
        loadTransactions(); // Load initial data

        // Setup Listeners
        addTransactionFab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTransactionActivity.class); // Navigate to Add/Edit screen
            startActivityForResult(intent, ADD_EDIT_TRANSACTION_REQUEST);
        });

        monthYearTextView.setOnClickListener(v -> showMonthYearPickerDialog());
    }

    private void showMonthYearPickerDialog() {
        // Prevent creating multiple dialogs if one is already showing
        if (monthYearDialog != null && monthYearDialog.isShowing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_month_year_picker, null); //
        builder.setView(dialogView);

        final TextView yearTextView = dialogView.findViewById(R.id.year_textview);
        final Button yearDecrementButton = dialogView.findViewById(R.id.year_decrement_button);
        final Button yearIncrementButton = dialogView.findViewById(R.id.year_increment_button);
        final GridLayout monthGridLayout = dialogView.findViewById(R.id.month_grid_layout);
        final Button okButton = dialogView.findViewById(R.id.ok_button);
        final Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        // Use a temporary variable for dialog interactions
        final int[] tempSelectedYear = {selectedYear};
        final int[] tempSelectedMonth = {selectedMonth}; // Store month temporarily if needed before OK


        yearTextView.setText(String.valueOf(tempSelectedYear[0]));

        // Style year buttons - Ensure you have ic_arrow_back and ic_arrow_forward drawables
        // And a color defined as R.color.icon_color or change as needed
        try {
            // int iconColor = getResources().getColor(R.color.icon_color, getTheme()); // If using tint
            yearDecrementButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_arrow_back, 0, 0, 0);
            yearIncrementButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_forward, 0);
            yearDecrementButton.setText("");
            yearIncrementButton.setText("");
            yearDecrementButton.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
            yearIncrementButton.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
            // Add tint if needed:
            // androidx.core.widget.ImageViewCompat.setImageTintList(yearDecrementButton, ColorStateList.valueOf(iconColor));
            // androidx.core.widget.ImageViewCompat.setImageTintList(yearIncrementButton, ColorStateList.valueOf(iconColor));

        } catch (Exception e) {
            Toast.makeText(this, "Error setting button drawables", Toast.LENGTH_SHORT).show();
            // Handle error - maybe drawables are missing or color is missing
        }

        final String[] months = new String[]{"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
        monthGridLayout.removeAllViews();
        int columnCount = monthGridLayout.getColumnCount(); // Get column count from XML (should be 4)

        for (int i = 0; i < months.length; i++) {
            // Using default Button constructor as per last attempt, assuming button style wasn't the issue.
            // If the Button style WAS also important, change this back to:
            // Button monthButton = new Button(this, null, android.R.attr.buttonBarButtonStyle);
            Button monthButton = new Button(this);

            monthButton.setText(months[i]);

            // Calculate row and column index explicitly
            int rowIndex = i / columnCount;
            int columnIndex = i % columnCount;

            // Create LayoutParams with explicit row and column specs
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(rowIndex, 1, 1f);
            params.columnSpec = GridLayout.spec(columnIndex, 1, 1f);
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;

            // *** REINSTATE these lines from your working version ***
            params.setGravity(android.view.Gravity.FILL);
            monthButton.setLayoutParams(params); // Apply params directly to button first

            // Set tag and listener
            monthButton.setTag(i);
            monthButton.setOnClickListener(v -> {
                selectedMonth = (int) v.getTag();
                // Use tempSelectedYear here if OK button is primary means of setting year
                selectedYear = tempSelectedYear[0];
                updateMonthYearTextView();
                loadTransactions();
                if (monthYearDialog != null) {
                    monthYearDialog.dismiss();
                }
            });

            // Add the button WITH the params object again (as in your code)
            monthGridLayout.addView(monthButton, params);
        }


        monthYearDialog = builder.create(); // Assign to class variable

        yearDecrementButton.setOnClickListener(v -> {
            tempSelectedYear[0]--;
            yearTextView.setText(String.valueOf(tempSelectedYear[0]));
        });

        yearIncrementButton.setOnClickListener(v -> {
            tempSelectedYear[0]++;
            yearTextView.setText(String.valueOf(tempSelectedYear[0]));
        });

        cancelButton.setOnClickListener(v -> monthYearDialog.dismiss());

        // OK button is less necessary if month click directly confirms, but kept for year changes
        okButton.setOnClickListener(v -> {
            selectedYear = tempSelectedYear[0];
            // selectedMonth would have been set by month click
            updateMonthYearTextView();
            loadTransactions();
            monthYearDialog.dismiss();
        });

        monthYearDialog.show();
    }

    private void updateMonthYearTextView() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);
        // No need to get timeInMillis just for formatting
        monthYearTextView.setText(monthYearFormat.format(calendar.getTime()));
    }

    private void loadTransactions() {
        // **Note:** Database operations should ideally be on a background thread!
        // This is a simplified example. Consider using AsyncTask, Coroutines, or Room.
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Start of the month
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();

        calendar.add(Calendar.MONTH, 1); // Move to the beginning of the next month
        long endTime = calendar.getTimeInMillis(); // End time is exclusive start of next month

        // Pass the correct start and end times to the database helper
        // You might need to adjust getTransactionsByMonthYear in DatabaseHelper
        // to accept start and end times instead of just one timestamp.
        // For now, we assume getTransactionsByMonthYear handles the range based on the start time passed.
        List<Transaction> transactions = dbHelper.getTransactionsByMonthYear(startTime); //
        transactionAdapter.setTransactions(transactions);
        updateBalances(transactions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Reload data if a transaction was added or edited successfully
        if (requestCode == ADD_EDIT_TRANSACTION_REQUEST && resultCode == RESULT_OK) {
            loadTransactions();
        }
    }

    private void updateBalances(List<Transaction> transactions) {
        double totalIncome = 0;
        double totalExpense = 0;

        if (transactions != null) { // Add null check
            for (Transaction transaction : transactions) {
                if (transaction.getType() == Transaction.TransactionType.INCOME) { //
                    totalIncome += transaction.getAmount(); //
                } else {
                    totalExpense += transaction.getAmount(); //
                }
            }
        }

        double balance = totalIncome - totalExpense;

        // Consider using NumberFormat.getCurrencyInstance() for better formatting
        balanceTextView.setText(String.format(Locale.getDefault(), "%.2f", balance));
        incomeTextView.setText(String.format(Locale.getDefault(), "%.2f", totalIncome));
        expenseTextView.setText(String.format(Locale.getDefault(), "%.2f", totalExpense));
    }

    // Implementation for editing (short click) - Handles navigation to AddEdit screen
    @Override
    public void onTransactionClick(Transaction transaction) {
        Intent intent = new Intent(MainActivity.this, AddEditTransactionActivity.class); //
        intent.putExtra("transaction_id", transaction.getId()); //
        startActivityForResult(intent, ADD_EDIT_TRANSACTION_REQUEST);
    }

    // Implementation for deleting (long click) <<< NEW METHOD
    @Override
    public void onTransactionDeleteLongClick(Transaction transaction) {
        // Show confirmation dialog, similar to ManageCategoriesActivity
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?\n(" + transaction.getCategory() + ": " + String.format(Locale.getDefault(), "%.2f", transaction.getAmount()) + ")") //
                .setPositiveButton("Yes", (dialog, which) -> {
                    // **Note:** Database operations should be on a background thread!
                    dbHelper.deleteTransaction(transaction.getId()); // Call DB delete method

                    // Refresh the list and balances
                    loadTransactions(); // Reload UI data

                    Toast.makeText(MainActivity.this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null) // No action on clicking "No"
                .show();
    }
}