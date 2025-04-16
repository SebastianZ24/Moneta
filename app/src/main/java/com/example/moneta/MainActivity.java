package com.example.moneta;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    private static final int ADD_EDIT_TRANSACTION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        balanceTextView = findViewById(R.id.balance_textview);
        incomeTextView = findViewById(R.id.income_textview);
        expenseTextView = findViewById(R.id.expense_textview);
        transactionsRecyclerView = findViewById(R.id.transactions_recyclerview);
        addTransactionFab = findViewById(R.id.add_transaction_fab);

        dbHelper = new DatabaseHelper(this);
        List<Transaction> transactions = dbHelper.getAllTransactions();

        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(transactions, this);
        transactionsRecyclerView.setAdapter(transactionAdapter);

        updateBalances(transactions);

        addTransactionFab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTransactionActivity.class);
            startActivityForResult(intent, ADD_EDIT_TRANSACTION_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_EDIT_TRANSACTION_REQUEST && resultCode == RESULT_OK) {
            // Reload transactions from the database
            List<Transaction> updatedTransactions = dbHelper.getAllTransactions();
            transactionAdapter.setTransactions(updatedTransactions);
            updateBalances(updatedTransactions);
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
        // Handle the click on a transaction item to open the edit screen
        Intent intent = new Intent(MainActivity.this, AddEditTransactionActivity.class);
        intent.putExtra("transaction_id", transaction.getId());
        startActivityForResult(intent, ADD_EDIT_TRANSACTION_REQUEST);
    }
}