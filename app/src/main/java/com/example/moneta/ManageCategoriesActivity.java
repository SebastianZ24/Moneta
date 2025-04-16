package com.example.moneta;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ManageCategoriesActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryDeleteClickListener {

    private TextView manageCategoriesTitle;
    private RadioGroup categoriesTypeRadioGroup;
    private RadioButton incomeCategoriesRadioButton;
    private RadioButton expenseCategoriesRadioButton;
    private EditText newCategoryEditText;
    private Button addNewCategoryButton;
    private RecyclerView categoriesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private DatabaseHelper dbHelper;
    private Transaction.TransactionType currentCategoryType = Transaction.TransactionType.INCOME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        manageCategoriesTitle = findViewById(R.id.manage_categories_title);
        categoriesTypeRadioGroup = findViewById(R.id.categories_type_radio_group);
        incomeCategoriesRadioButton = findViewById(R.id.income_categories_radio_button);
        expenseCategoriesRadioButton = findViewById(R.id.expense_categories_radio_button);
        newCategoryEditText = findViewById(R.id.new_category_edittext);
        addNewCategoryButton = findViewById(R.id.add_new_category_button);
        categoriesRecyclerView = findViewById(R.id.categories_recyclerview);

        dbHelper = new DatabaseHelper(this);

        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
                dbHelper.addCategory(newCategoryName, currentCategoryType); // Pass the type
                newCategoryEditText.setText("");
                loadCategories();
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
    public void onDeleteClick(String categoryName) {
        // When deleting, we need to know the type to delete the correct category
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + categoryName + "' from " + currentCategoryType.toString().toLowerCase() + " categories? Transactions using this category will remain but the category will be removed.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // We need to delete based on both name and type
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    int rowsAffected = db.delete(
                            "categories",
                            "name=? AND type=?",
                            new String[]{categoryName, currentCategoryType.toString()}
                    );
                    db.close();
                    if (rowsAffected > 0) {
                        loadCategories();
                        Toast.makeText(ManageCategoriesActivity.this, "Category deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ManageCategoriesActivity.this, "Failed to delete category", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}