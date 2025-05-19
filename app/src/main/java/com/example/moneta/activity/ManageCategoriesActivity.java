package com.example.moneta.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.moneta.DatabaseHelper;
import com.example.moneta.R;
import com.example.moneta.adapter.CategoryAdapter;
import com.example.moneta.model.Transaction;

import java.util.List;

public class ManageCategoriesActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryDeleteClickListener {

    private RadioGroup categoriesTypeRadioGroup;
    private RadioButton incomeCategoriesRadioButton;
    private RadioButton expenseCategoriesRadioButton;
    private EditText newCategoryEditText;
    private Button addNewCategoryButton;
    private RecyclerView categoriesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private DatabaseHelper dbHelper;
    private Transaction.TransactionType currentCategoryType = Transaction.TransactionType.INCOME;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);
        toolbar = findViewById(R.id.toolbar_manage_categories);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Manage Categories");
        }

        categoriesTypeRadioGroup = findViewById(R.id.categories_type_radio_group);
        incomeCategoriesRadioButton = findViewById(R.id.income_categories_radio_button);
        expenseCategoriesRadioButton = findViewById(R.id.expense_categories_radio_button);
        newCategoryEditText = findViewById(R.id.new_category_edittext);
        addNewCategoryButton = findViewById(R.id.add_new_category_button);
        categoriesRecyclerView = findViewById(R.id.categories_recyclerview);

        dbHelper = new DatabaseHelper(this);

        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        currentCategoryType = incomeCategoriesRadioButton.isChecked() ? Transaction.TransactionType.INCOME : Transaction.TransactionType.EXPENSE;
        categoryAdapter = new CategoryAdapter(dbHelper.getAllCategories(currentCategoryType), this);
        categoriesRecyclerView.setAdapter(categoryAdapter);

        categoriesTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.income_categories_radio_button) {
                currentCategoryType = Transaction.TransactionType.INCOME;
            } else if (checkedId == R.id.expense_categories_radio_button) {
                currentCategoryType = Transaction.TransactionType.EXPENSE;
            }
            loadCategories();
        });

        addNewCategoryButton.setOnClickListener(v -> {
            String newCategoryName = newCategoryEditText.getText().toString().trim();
            if (!newCategoryName.isEmpty()) {
                List<String> existingCategories = dbHelper.getAllCategories(currentCategoryType);
                boolean exists = false;
                for(String existing : existingCategories) {
                    if(existing.equalsIgnoreCase(newCategoryName)) {
                        exists = true;
                        break;
                    }
                }

                if (exists) {
                    Toast.makeText(this, "'" + newCategoryName + "' already exists for " + currentCategoryType.toString().toLowerCase() + "s", Toast.LENGTH_SHORT).show();
                } else {
                    long result = dbHelper.addCategory(newCategoryName, currentCategoryType);
                    if (result != -1) {
                        newCategoryEditText.setText("");
                        loadCategories();
                        Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to add category (maybe duplicate?)", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        List<String> categories = dbHelper.getAllCategories(currentCategoryType);
        categoryAdapter.setCategories(categories);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeleteClick(String categoryName) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + categoryName + "'? Transactions using this category will remain.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    try {
                        dbHelper.deleteCategory(categoryName, currentCategoryType);
                        loadCategories();
                        Toast.makeText(ManageCategoriesActivity.this, "Category deleted", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(ManageCategoriesActivity.this, "Failed to delete category", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}