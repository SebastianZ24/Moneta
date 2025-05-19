package com.example.moneta.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.moneta.DatabaseHelper;
import com.example.moneta.R;
import com.example.moneta.adapter.TransactionAdapter;
import com.example.moneta.model.Transaction;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        TransactionAdapter.OnTransactionClickListener,
        TransactionAdapter.OnTransactionDeleteListener,
        NavigationView.OnNavigationItemSelectedListener {

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
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private static final int ADD_EDIT_TRANSACTION_REQUEST = 1;
    private AlertDialog monthYearDialog;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        balanceTextView = findViewById(R.id.balance_textview);
        incomeTextView = findViewById(R.id.income_textview);
        expenseTextView = findViewById(R.id.expense_textview);
        transactionsRecyclerView = findViewById(R.id.transactions_recyclerview);
        addTransactionFab = findViewById(R.id.add_transaction_fab);
        monthYearTextView = findViewById(R.id.month_year_textview);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        dbHelper = new DatabaseHelper(this);

        setSupportActionBar(toolbar);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(new ArrayList<>(), this, this);
        transactionsRecyclerView.setAdapter(transactionAdapter);

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        updateMonthYearTextView();
        loadTransactions();

        addTransactionFab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTransactionActivity.class);
            startActivityForResult(intent, ADD_EDIT_TRANSACTION_REQUEST);
        });

        monthYearTextView.setOnClickListener(v -> showMonthYearPickerDialog());

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_transactions);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (navigationView != null) {
            navigationView.setCheckedItem(R.id.nav_transactions);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_transactions) {

        } else if (id == R.id.nav_manage_categories) {
            Intent intent = new Intent(MainActivity.this, ManageCategoriesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_investments) {
            Intent intent = new Intent(MainActivity.this, InvestmentsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_stats) {
            Intent intent = new Intent(MainActivity.this, StatsActivity.class);
            startActivity(intent);
        }


        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void showMonthYearPickerDialog() {
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


        final int[] tempSelectedYear = {selectedYear};


        yearTextView.setText(String.valueOf(tempSelectedYear[0]));

        try {
            yearDecrementButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_arrow_back, 0, 0, 0);
            yearIncrementButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_forward, 0);
            yearDecrementButton.setText("");
            yearIncrementButton.setText("");
            yearDecrementButton.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
            yearIncrementButton.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));
        } catch (Exception e) {
            Toast.makeText(this, "Error setting button drawables: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }


        final String[] months = new String[]{"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
        monthGridLayout.removeAllViews();
        int columnCount = monthGridLayout.getColumnCount();

        for (int i = 0; i < months.length; i++) {
            Button monthButton = new Button(this);

            monthButton.setText(months[i]);

            int rowIndex = i / columnCount;
            int columnIndex = i % columnCount;

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(rowIndex, 1, 1f);
            params.columnSpec = GridLayout.spec(columnIndex, 1, 1f);
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setGravity(android.view.Gravity.FILL);
            monthButton.setLayoutParams(params);

            monthButton.setTag(i);
            monthButton.setOnClickListener(v -> {
                selectedMonth = (int) v.getTag();
                selectedYear = tempSelectedYear[0];
                updateMonthYearTextView();
                loadTransactions();
                if (monthYearDialog != null) {
                    monthYearDialog.dismiss();
                }
            });

            monthGridLayout.addView(monthButton, params);
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
            selectedYear = tempSelectedYear[0];
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
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();

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
                    dbHelper.deleteTransaction(transaction.getId());
                    loadTransactions();
                    Toast.makeText(MainActivity.this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}