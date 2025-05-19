package com.example.moneta.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneta.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<String> categoryList;
    private OnCategoryDeleteClickListener deleteListener;
    public CategoryAdapter(List<String> categoryList, OnCategoryDeleteClickListener deleteListener) {
        this.categoryList = categoryList;
        this.deleteListener = deleteListener;
    }

    public void setCategories(List<String> categories) {
        this.categoryList = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categoryList.get(position);
        holder.categoryNameTextView.setText(category);
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null && position != RecyclerView.NO_POSITION) {
                deleteListener.onDeleteClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryNameTextView;
        public ImageButton deleteButton;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.category_name_textview);
            deleteButton = itemView.findViewById(R.id.delete_category_button);
        }
    }

    public interface OnCategoryDeleteClickListener {
        void onDeleteClick(String categoryName);
    }
}