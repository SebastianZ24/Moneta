package com.example.moneta;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    // Keep the existing listener for edits (short clicks)
    private OnTransactionClickListener clickListener;
    // Add a new listener specifically for delete requests (long clicks)
    private OnTransactionDeleteListener deleteListener; // <<< NEW

    // Modify constructor to accept the new delete listener
    public TransactionAdapter(List<Transaction> transactionList, OnTransactionClickListener clickListener, OnTransactionDeleteListener deleteListener) { // <<< MODIFIED
        this.transactionList = transactionList;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener; // <<< NEW
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactionList = transactions;
        notifyDataSetChanged(); // Consider using DiffUtil for better performance later
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false); // Ensure this matches your layout file name
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.bind(transaction, clickListener, deleteListener); // Pass listeners to bind method
    }

    @Override
    public int getItemCount() {
        // Handle potential null list
        return transactionList != null ? transactionList.size() : 0;
    }

    // ViewHolder remains mostly the same, but add bind method for clarity
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryTextView;
        public TextView descriptionTextView;
        public TextView amountTextView;
        public TextView dateTextView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.transaction_category_textview);
            descriptionTextView = itemView.findViewById(R.id.transaction_description_textview);
            amountTextView = itemView.findViewById(R.id.transaction_amount_textview);
            dateTextView = itemView.findViewById(R.id.transaction_date_textview);
        }

        // Bind data and set listeners here
        public void bind(final Transaction transaction, final OnTransactionClickListener clickListener, final OnTransactionDeleteListener deleteListener) {
            categoryTextView.setText(transaction.getCategory()); //
            descriptionTextView.setText(transaction.getDescription()); //
            amountTextView.setText(String.format(Locale.getDefault(), "%.2f", transaction.getAmount())); //

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            dateTextView.setText(sdf.format(new Date(transaction.getDate()))); //

            // Apply text color and +/- sign based on transaction type
            if (transaction.getType() == Transaction.TransactionType.INCOME) { //
                // Consider defining colors in colors.xml instead of hardcoding
                amountTextView.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                amountTextView.setText("+" + amountTextView.getText());
            } else {
                // Consider defining colors in colors.xml instead of hardcoding
                amountTextView.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                amountTextView.setText("-" + amountTextView.getText());
            }

            // Short click listener for editing
            itemView.setOnClickListener(v -> {
                // Check listener is not null and position is valid
                if (clickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    clickListener.onTransactionClick(transaction);
                }
            });

            // Long click listener for deleting <<< NEW
            itemView.setOnLongClickListener(v -> {
                // Check listener is not null and position is valid
                if (deleteListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    deleteListener.onTransactionDeleteLongClick(transaction);
                    return true; // Indicate the click was handled
                }
                return false;
            });
        }
    }

    // Existing interface for editing
    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    // New interface for deletion <<< NEW
    public interface OnTransactionDeleteListener {
        void onTransactionDeleteLongClick(Transaction transaction);
    }
}