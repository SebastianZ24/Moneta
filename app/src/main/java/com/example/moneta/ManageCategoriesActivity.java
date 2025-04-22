package com.example.moneta;

import android.os.Bundle;
import android.view.MenuItem; // Import MenuItem
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull; // Import NonNull
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import Toolbar
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.moneta.model.Transaction;

import java.util.List;

public class ManageCategoriesActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryDeleteClickListener {

    // Keep existing variables, remove manageCategoriesTitle if not used
    // private TextView manageCategoriesTitle;
    private RadioGroup categoriesTypeRadioGroup;
    private RadioButton incomeCategoriesRadioButton;
    private RadioButton expenseCategoriesRadioButton;
    private EditText newCategoryEditText;
    private Button addNewCategoryButton;
    private RecyclerView categoriesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private DatabaseHelper dbHelper;
    private Transaction.TransactionType currentCategoryType = Transaction.TransactionType.INCOME; // Default
    private Toolbar toolbar; // Toolbar variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure this uses the layout file that includes the Toolbar
        setContentView(R.layout.activity_manage_categories);

        // --- Toolbar Setup ---
        toolbar = findViewById(R.id.toolbar_manage_categories); // Use the ID from your XML
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back arrow
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Manage Categories"); // Set title in Toolbar
        }
        // --- End Toolbar Setup ---

        // Initialize other views
        // manageCategoriesTitle = findViewById(R.id.manage_categories_title); // Remove if title is set in Toolbar
        categoriesTypeRadioGroup = findViewById(R.id.categories_type_radio_group);
        incomeCategoriesRadioButton = findViewById(R.id.income_categories_radio_button);
        expenseCategoriesRadioButton = findViewById(R.id.expense_categories_radio_button);
        newCategoryEditText = findViewById(R.id.new_category_edittext);
        addNewCategoryButton = findViewById(R.id.add_new_category_button);
        categoriesRecyclerView = findViewById(R.id.categories_recyclerview);

        dbHelper = new DatabaseHelper(this);

        // Setup RecyclerView
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Determine initial category type based on checked radio button
        currentCategoryType = incomeCategoriesRadioButton.isChecked() ? Transaction.TransactionType.INCOME : Transaction.TransactionType.EXPENSE;
        // Initialize adapter with current type's categories
        categoryAdapter = new CategoryAdapter(dbHelper.getAllCategories(currentCategoryType), this);
        categoriesRecyclerView.setAdapter(categoryAdapter);

        // RadioGroup Listener
        categoriesTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.income_categories_radio_button) {
                currentCategoryType = Transaction.TransactionType.INCOME;
            } else if (checkedId == R.id.expense_categories_radio_button) {
                currentCategoryType = Transaction.TransactionType.EXPENSE;
            }
            loadCategories(); // Reload categories when type changes
        });

        // Add Button Listener
        addNewCategoryButton.setOnClickListener(v -> {
            String newCategoryName = newCategoryEditText.getText().toString().trim();
            if (!newCategoryName.isEmpty()) {
                // Optional: Check if category already exists (case-insensitive)
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
                    // Attempt to add category - Background thread recommended
                    long result = dbHelper.addCategory(newCategoryName, currentCategoryType);
                    if (result != -1) { // Check if add was successful (assuming -1 for failure)
                        newCategoryEditText.setText("");
                        loadCategories(); // Refresh list
                        Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to add category (maybe duplicate?)", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Initial load is handled when adapter is initialized above
        // loadCategories();
    }

    // Method to reload categories into the adapter
    private void loadCategories() {
        // Background thread recommended
        List<String> categories = dbHelper.getAllCategories(currentCategoryType);
        categoryAdapter.setCategories(categories);
    }

    // Handle Toolbar item clicks (specifically the Up button)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle arrow click (android.R.id.home is the ID for the Up button)
        if (item.getItemId() == android.R.id.home) {
            // This mimics the back button press behavior, respecting the navigation stack
            // and the parent activity defined in the Manifest.
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // Handle delete button click from the adapter
    @Override
    public void onDeleteClick(String categoryName) {
        // Confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + categoryName + "'? Transactions using this category will remain.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Perform delete - Background thread recommended!
                    try {
                        dbHelper.deleteCategory(categoryName, currentCategoryType); // Use DatabaseHelper method
                        loadCategories(); // Refresh the list
                        Toast.makeText(ManageCategoriesActivity.this, "Category deleted", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        // Log error e.printStackTrace();
                        Toast.makeText(ManageCategoriesActivity.this, "Failed to delete category", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null) // Do nothing on "No"
                .show();
    }
}