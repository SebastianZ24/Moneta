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
    private OnTransactionClickListener listener;

    public TransactionAdapter(List<Transaction> transactionList, OnTransactionClickListener listener) {
        this.transactionList = transactionList;
        this.listener = listener;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactionList = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.categoryTextView.setText(transaction.getCategory());
        holder.descriptionTextView.setText(transaction.getDescription());
        holder.amountTextView.setText(String.format(Locale.getDefault(), "%.2f", transaction.getAmount()));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        holder.dateTextView.setText(sdf.format(new Date(transaction.getDate())));

        if (transaction.getType() == Transaction.TransactionType.INCOME) {
            holder.amountTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            holder.amountTextView.setText("+" + holder.amountTextView.getText());
        } else {
            holder.amountTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            holder.amountTextView.setText("-" + holder.amountTextView.getText());
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onTransactionClick(transactionList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

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
    }

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }
}
