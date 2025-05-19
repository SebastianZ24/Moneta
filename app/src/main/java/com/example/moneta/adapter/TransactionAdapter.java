package com.example.moneta.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneta.R;
import com.example.moneta.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private OnTransactionClickListener clickListener;
    private OnTransactionDeleteListener deleteListener;

    public TransactionAdapter(List<Transaction> transactionList, OnTransactionClickListener clickListener, OnTransactionDeleteListener deleteListener) {
        this.transactionList = transactionList;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
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
        holder.bind(transaction, clickListener, deleteListener);
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
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

        public void bind(final Transaction transaction, final OnTransactionClickListener clickListener, final OnTransactionDeleteListener deleteListener) {
            categoryTextView.setText(transaction.getCategory());
            descriptionTextView.setText(transaction.getDescription());
            amountTextView.setText(String.format(Locale.getDefault(), "%.2f", transaction.getAmount()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            dateTextView.setText(sdf.format(new Date(transaction.getDate())));

            if (transaction.getType() == Transaction.TransactionType.INCOME) {
                amountTextView.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                amountTextView.setText("+" + amountTextView.getText());
            } else {
                amountTextView.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                amountTextView.setText("-" + amountTextView.getText());
            }

            itemView.setOnClickListener(v -> {
                if (clickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    clickListener.onTransactionClick(transaction);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (deleteListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    deleteListener.onTransactionDeleteLongClick(transaction);
                    return true;
                }
                return false;
            });
        }
    }

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    public interface OnTransactionDeleteListener {
        void onTransactionDeleteLongClick(Transaction transaction);
    }
}