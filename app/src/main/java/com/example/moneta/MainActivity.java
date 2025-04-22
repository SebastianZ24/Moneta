package com.example.moneta;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import androidx.core.view.ViewCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionClickListener {

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
    private AlertDialog dialog; // Declare dialog at the class level

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

        dbHelper = new DatabaseHelper(this);

        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(new ArrayList<>(), this);
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
    }

    private void showMonthYearPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_month_year_picker, null);
        builder.setView(dialogView);

        final TextView yearTextView = dialogView.findViewById(R.id.year_textview);
        final Button yearDecrementButton = dialogView.findViewById(R.id.year_decrement_button);
        final Button yearIncrementButton = dialogView.findViewById(R.id.year_increment_button);
        final GridLayout monthGridLayout = dialogView.findViewById(R.id.month_grid_layout);
        final Button okButton = dialogView.findViewById(R.id.ok_button);
        final Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        yearTextView.setText(String.valueOf(selectedYear));

        // Style for the year buttons
        int iconColor = getResources().getColor(R.color.icon_color, getTheme()); // Replace with your color
        yearDecrementButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_arrow_back, 0, 0, 0); // Use vector drawable
        yearIncrementButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_forward, 0);
        yearDecrementButton.setText(""); // Remove the text
        yearIncrementButton.setText("");
        yearDecrementButton.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme())); // Make background transparent
        yearIncrementButton.setBackgroundColor(getResources().getColor(android.R.color.transparent, getTheme()));

        final String[] months = new String[]{"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEPT", "OCT", "NOV", "DEC"};
        monthGridLayout.removeAllViews();
        for (int i = 0; i < months.length; i++) {
            Button monthButton = new Button(this);
            monthButton.setText(months[i]);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(i / 4, 1, 1f);
            params.columnSpec = GridLayout.spec(i % 4, 1, 1f);
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setGravity(android.view.Gravity.FILL);
            monthButton.setLayoutParams(params);
            monthButton.setTag(i);
            monthButton.setOnClickListener(v -> {
                selectedMonth = (int) v.getTag();
                updateMonthYearTextView();
                loadTransactions();
                dialog.dismiss();
            });
            monthGridLayout.addView(monthButton, params);
        }

        dialog = builder.create();
        dialog.show();

        yearDecrementButton.setOnClickListener(v -> {
            selectedYear--;
            yearTextView.setText(String.valueOf(selectedYear));
        });

        yearIncrementButton.setOnClickListener(v -> {
            selectedYear++;
            yearTextView.setText(String.valueOf(selectedYear));
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        okButton.setOnClickListener(v -> {
            dialog.dismiss();
            updateMonthYearTextView();
            loadTransactions();
        });
    }



    private void updateMonthYearTextView() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);
        long timeInMillis = calendar.getTimeInMillis();
        monthYearTextView.setText(monthYearFormat.format(timeInMillis));
    }

    private void loadTransactions() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);
        long timeInMillis = calendar.getTimeInMillis();

        List<Transaction> transactions = dbHelper.getTransactionsByMonthYear(timeInMillis);
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

        for (Transaction transaction : transactions) {
            if (transaction.getType() == Transaction.TransactionType.INCOME) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpense += transaction.getAmount();
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
}

