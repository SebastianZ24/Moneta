package com.example.moneta;

import android.content.Intent;
// Removed unused ColorStateList import
import android.os.Bundle;
import android.view.MenuItem; // <<< NEW Import
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast; // Import Toast
import android.app.AlertDialog; // Import AlertDialog
import android.widget.GridLayout;
// Removed unused LinearLayout import
// Removed unused ViewCompat import

// Imports for Drawer
import androidx.annotation.NonNull; // <<< NEW Import
import androidx.appcompat.app.ActionBarDrawerToggle; // <<< NEW Import
import androidx.appcompat.widget.Toolbar; // <<< NEW Import
import androidx.core.view.GravityCompat; // <<< NEW Import
import androidx.drawerlayout.widget.DrawerLayout; // <<< NEW Import

import com.example.moneta.model.Transaction;
import com.google.android.material.navigation.NavigationView; // <<< NEW Import

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// Implement Navigation listener
public class MainActivity extends AppCompatActivity implements
        TransactionAdapter.OnTransactionClickListener,
        TransactionAdapter.OnTransactionDeleteListener,
        NavigationView.OnNavigationItemSelectedListener { // <<< ADDED Navigation Listener

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
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()); // Corrected pattern
    private static final int ADD_EDIT_TRANSACTION_REQUEST = 1;
    private AlertDialog monthYearDialog; // Use a more specific name

    // Drawer variables <<< NEW
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the new layout containing the DrawerLayout
        setContentView(R.layout.activity_main);

        // Initialize Views
        balanceTextView = findViewById(R.id.balance_textview);
        incomeTextView = findViewById(R.id.income_textview);
        expenseTextView = findViewById(R.id.expense_textview);
        transactionsRecyclerView = findViewById(R.id.transactions_recyclerview);
        addTransactionFab = findViewById(R.id.add_transaction_fab);
        monthYearTextView = findViewById(R.id.month_year_textview);

        // Initialize Drawer Views <<< NEW
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        // Initialize Database Helper
        dbHelper = new DatabaseHelper(this);

        // Setup Toolbar <<< NEW
        setSupportActionBar(toolbar);

        // Setup Drawer Toggle <<< NEW
        // Make sure you have these strings in res/values/strings.xml
        // (e.g., R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar, // Use toolbar instead of R.string resource ids
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Setup Navigation View Listener <<< NEW
        navigationView.setNavigationItemSelectedListener(this);

        // Setup RecyclerView
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(new ArrayList<>(), this, this);
        transactionsRecyclerView.setAdapter(transactionAdapter);

        // Set initial month/year and load data
        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        updateMonthYearTextView();
        loadTransactions();

        // Setup Listeners for FAB and Month/Year TextView
        addTransactionFab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTransactionActivity.class);
            startActivityForResult(intent, ADD_EDIT_TRANSACTION_REQUEST);
        });

        monthYearTextView.setOnClickListener(v -> showMonthYearPickerDialog());

        // Select Transactions item by default in drawer <<< NEW
        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_transactions);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Ensure the "Transactions" item is checked when the activity resumes
        // This handles returning from other activities like ManageCategoriesActivity
        if (navigationView != null) {
            navigationView.setCheckedItem(R.id.nav_transactions);
        }
    }

    // Handle Drawer Item Clicks <<< NEW METHOD
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_transactions) {
            // Already on the transactions screen, do nothing or reload if needed
            // You might replace this with Fragment transactions later
            // Toast.makeText(this, "Transactions selected", Toast.LENGTH_SHORT).show(); // Optional feedback
        } else if (id == R.id.nav_manage_categories) {
            // Start ManageCategoriesActivity
            Intent intent = new Intent(MainActivity.this, ManageCategoriesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_investments) { // <<< ADD THIS CASE
            Intent intent = new Intent(MainActivity.this, InvestmentsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_stats) { // <<< ADD THIS CASE
            Intent intent = new Intent(MainActivity.this, StatsActivity.class);
            startActivity(intent);
        }
        // Add more else if blocks here for future items (Reports, Settings etc.)

        // Close the drawer after selection
        drawerLayout.closeDrawer(GravityCompat.START);
        return true; // Indicate item selection was handled
    }

    // Handle back press to close drawer if open <<< NEW METHOD
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    private void showMonthYearPickerDialog() {
        // Prevent creating multiple dialogs if one is already showing
        if (monthYearDialog != null && monthYearDialog.isShowing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_month_year_picker, null);
        builder.setView(dialogView);

        final TextView yearTextView = dialogView.findViewById(R.id.year_textview);
        final Button yearDecrementButton = dialogView.findViewById(R.id.year_decrement_button);
        final Button yearIncrementButton = dialogView.findViewById(R.id.year_increment_button);
        final GridLayout monthGridLayout = dialogView.findViewById(R.id.month_grid_layout);
        final Button okButton = dialogView.findViewById(R.id.ok_button);
        final Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        // Use a temporary variable for dialog interactions
        final int[] tempSelectedYear = {selectedYear};


        yearTextView.setText(String.valueOf(tempSelectedYear[0]));

        // Style year buttons - Ensure you have ic_arrow_back and ic_arrow_forward drawables
        try {
            yearDecrementButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_arrow_back, 0, 0, 0);
            yearIncrementButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_forward, 0);
            yearDecrementButton.setText("");
            yearIncrementButton.setText("");
            yearDecrementButton.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
            yearIncrementButton.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        } catch (Exception e) {
            Toast.makeText(this, "Error setting button drawables: " + e.getMessage(), Toast.LENGTH_LONG).show(); // Show detailed error
        }


        final String[] months = new String[]{"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
        monthGridLayout.removeAllViews();
        int columnCount = monthGridLayout.getColumnCount(); // Should be 4 based on XML

        for (int i = 0; i < months.length; i++) {
            Button monthButton = new Button(this); // Use default Button constructor

            monthButton.setText(months[i]);

            int rowIndex = i / columnCount;
            int columnIndex = i % columnCount;

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            // Explicit row/column spec
            params.rowSpec = GridLayout.spec(rowIndex, 1, 1f);
            params.columnSpec = GridLayout.spec(columnIndex, 1, 1f);
            params.width = 0; // Use 0 width with weight
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            // Set gravity and apply params to button itself (combination from user's "working" version)
            params.setGravity(android.view.Gravity.FILL);
            monthButton.setLayoutParams(params);

            monthButton.setTag(i); // Month index 0-11
            monthButton.setOnClickListener(v -> {
                selectedMonth = (int) v.getTag();
                selectedYear = tempSelectedYear[0]; // Use the year currently displayed in dialog
                updateMonthYearTextView();
                loadTransactions();
                if (monthYearDialog != null) {
                    monthYearDialog.dismiss();
                }
            });

            monthGridLayout.addView(monthButton, params); // Add with params
        }


        monthYearDialog = builder.create();

        yearDecrementButton.setOnClickListener(v -> {
            tempSelectedYear[0]--;
            yearTextView.setText(String.valueOf(tempSelectedYear[0]));
        });

        yearIncrementButton.setOnClickListener(v -> {
            tempSelectedYear[0]++;
            yearTextView.setText(String.valueOf(tempSelectedYear[0]));
        });

        cancelButton.setOnClickListener(v -> monthYearDialog.dismiss());

        okButton.setOnClickListener(v -> {
            selectedYear = tempSelectedYear[0]; // Update year if OK is pressed
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
        monthYearTextView.setText(monthYearFormat.format(calendar.getTime()));
    }

    private void loadTransactions() {
        // **Note:** Database operations should ideally be on a background thread!
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();

        // Assuming getTransactionsByMonthYear uses the startTime to determine the month/year
        // Consider modifying DatabaseHelper to take start and end time for clarity
        List<Transaction> transactions = dbHelper.getTransactionsByMonthYear(startTime);
        transactionAdapter.setTransactions(transactions);
        updateBalances(transactions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_EDIT_TRANSACTION_REQUEST && resultCode == RESULT_OK) {
            loadTransactions();
        }
    }

    private void updateBalances(List<Transaction> transactions) {
        double totalIncome = 0;
        double totalExpense = 0;

        if (transactions != null) {
            for (Transaction transaction : transactions) {
                if (transaction.getType() == Transaction.TransactionType.INCOME) {
                    totalIncome += transaction.getAmount();
                } else {
                    totalExpense += transaction.getAmount();
                }
            }
        }

        double balance = totalIncome - totalExpense;

        // Consider using NumberFormat.getCurrencyInstance() for locale-aware formatting
        balanceTextView.setText(String.format(Locale.getDefault(), "%.2f", balance));
        incomeTextView.setText(String.format(Locale.getDefault(), "%.2f", totalIncome));
        expenseTextView.setText(String.format(Locale.getDefault(), "%.2f", totalExpense));
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        Intent intent = new Intent(MainActivity.this, AddEditTransactionActivity.class);
        intent.putExtra("transaction_id", transaction.getId());
        startActivityForResult(intent, ADD_EDIT_TRANSACTION_REQUEST);
    }

    @Override
    public void onTransactionDeleteLongClick(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?\n(" + transaction.getCategory() + ": " + String.format(Locale.getDefault(), "%.2f", transaction.getAmount()) + ")")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // **Note:** Database operations should be on a background thread!
                    dbHelper.deleteTransaction(transaction.getId());
                    loadTransactions(); // Reload UI data
                    Toast.makeText(MainActivity.this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}