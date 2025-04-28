package com.example.moneta;

import com.example.moneta.model.Transaction;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log; // Import Log

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "moneta.db";
    // Increment version if schema changes
    private static final int DATABASE_VERSION = 2; // <<< INCREMENTED VERSION
    private static final String TAG = "DatabaseHelper"; // For logging

    // Table name - Transactions
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String KEY_TRANS_ID = "id";
    private static final String KEY_TRANS_AMOUNT = "amount";
    private static final String KEY_TRANS_TYPE = "type";
    private static final String KEY_TRANS_CATEGORY = "category";
    private static final String KEY_TRANS_DATE = "date";
    private static final String KEY_TRANS_DESCRIPTION = "description";

    // Category Table
    private static final String TABLE_CATEGORIES = "categories";
    private static final String KEY_CAT_NAME = "name";
    private static final String KEY_CAT_TYPE = "type";

    // Investment Table Constants
    private static final String TABLE_INVESTMENTS = "investments";
    private static final String KEY_INV_ID = "id";
    private static final String KEY_INV_SYMBOL = "symbol";
    private static final String KEY_INV_COMPANY_NAME = "company_name";
    private static final String KEY_INV_QUANTITY = "quantity";
    private static final String KEY_INV_PURCHASE_PRICE = "purchase_price";
    private static final String KEY_INV_PURCHASE_DATE = "purchase_date";
    private static final String KEY_INV_CURRENT_PRICE = "current_price";

    // --- Create Statements ---
    private static final String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE "
            + TABLE_TRANSACTIONS + "(" + KEY_TRANS_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_TRANS_AMOUNT
            + " REAL NOT NULL," + KEY_TRANS_TYPE
            + " TEXT NOT NULL," + KEY_TRANS_CATEGORY
            + " TEXT NOT NULL," + KEY_TRANS_DATE
            + " INTEGER NOT NULL," + KEY_TRANS_DESCRIPTION + " TEXT);";

    private static final String CREATE_CATEGORIES_TABLE = "CREATE TABLE "
            + TABLE_CATEGORIES + "(" + KEY_CAT_NAME
            + " TEXT NOT NULL, " + KEY_CAT_TYPE
            + " TEXT NOT NULL, PRIMARY KEY (" + KEY_CAT_NAME + ", " + KEY_CAT_TYPE + "))";

    private static final String CREATE_INVESTMENTS_TABLE = "CREATE TABLE "
            + TABLE_INVESTMENTS + "(" + KEY_INV_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_INV_SYMBOL + " TEXT NOT NULL,"
            + KEY_INV_COMPANY_NAME + " TEXT,"
            + KEY_INV_QUANTITY + " REAL NOT NULL,"
            + KEY_INV_PURCHASE_PRICE + " REAL NOT NULL,"
            + KEY_INV_PURCHASE_DATE + " INTEGER NOT NULL,"
            + KEY_INV_CURRENT_PRICE + " REAL);";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Creating database tables...");
        db.execSQL(CREATE_TRANSACTIONS_TABLE);
        db.execSQL(CREATE_CATEGORIES_TABLE);
        db.execSQL(CREATE_INVESTMENTS_TABLE);
        insertDefaultCategories(db);
        Log.i(TAG, "Database tables created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        // Basic upgrade policy: drop old tables and recreate
        // For production apps, implement data migration logic here
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVESTMENTS);
        onCreate(db);
    }

    // ========================================================================
    // Transaction CRUD Operations (Keep existing methods)
    // ========================================================================
    public long addTransaction(Transaction transaction) {
        SQLiteDatabase db = null;
        long id = -1;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_TRANS_AMOUNT, transaction.getAmount());
            values.put(KEY_TRANS_TYPE, transaction.getType().toString());
            values.put(KEY_TRANS_CATEGORY, transaction.getCategory());
            values.put(KEY_TRANS_DATE, transaction.getDate());
            values.put(KEY_TRANS_DESCRIPTION, transaction.getDescription());
            id = db.insert(TABLE_TRANSACTIONS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error adding transaction", e);
        } finally {
            if (db != null) db.close();
        }
        return id;
    }

    public Transaction getTransaction(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Transaction transaction = null;
        try {
            cursor = db.query(TABLE_TRANSACTIONS, new String[]{KEY_TRANS_ID,
                            KEY_TRANS_AMOUNT, KEY_TRANS_TYPE, KEY_TRANS_CATEGORY, KEY_TRANS_DATE, KEY_TRANS_DESCRIPTION}, KEY_TRANS_ID + "=?",
                    new String[]{String.valueOf(id)}, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                transaction = new Transaction(
                        cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TRANS_ID)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_TRANS_AMOUNT)),
                        Transaction.TransactionType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TRANS_TYPE))),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_TRANS_CATEGORY)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TRANS_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_TRANS_DESCRIPTION))
                );
            } else {
                Log.w(TAG, "Transaction not found with id: " + id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting transaction with id: " + id, e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return transaction;
    }

    public List<Transaction> getTransactionsByMonthYear(long monthStartTime) {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(); // Use readable
        Cursor cursor = null;
        String selectQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS;

        if (monthStartTime != -1) {
            // Calculate the end of the selected month
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(monthStartTime); // Should be start of month
            calendar.add(Calendar.MONTH, 1); // Move to start of next month
            long endTimeExclusive = calendar.getTimeInMillis();

            // Use >= start and < end for accurate month range
            selectQuery += " WHERE " + KEY_TRANS_DATE + " >= " + monthStartTime +
                    " AND " + KEY_TRANS_DATE + " < " + endTimeExclusive;
        }
        selectQuery += " ORDER BY " + KEY_TRANS_DATE + " DESC";

        try {
            cursor = db.rawQuery(selectQuery, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Transaction transaction = new Transaction(
                            cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TRANS_ID)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_TRANS_AMOUNT)),
                            Transaction.TransactionType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TRANS_TYPE))),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_TRANS_CATEGORY)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TRANS_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_TRANS_DESCRIPTION))
                    );
                    transactionList.add(transaction);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting transactions by month", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return transactionList;
    }

    public int updateTransaction(Transaction transaction) {
        SQLiteDatabase db = null;
        int rowsAffected = 0;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_TRANS_AMOUNT, transaction.getAmount());
            values.put(KEY_TRANS_TYPE, transaction.getType().toString());
            values.put(KEY_TRANS_CATEGORY, transaction.getCategory());
            values.put(KEY_TRANS_DATE, transaction.getDate());
            values.put(KEY_TRANS_DESCRIPTION, transaction.getDescription());
            rowsAffected = db.update(TABLE_TRANSACTIONS, values, KEY_TRANS_ID + " = ?",
                    new String[]{String.valueOf(transaction.getId())});
        } catch (Exception e) {
            Log.e(TAG, "Error updating transaction with id: " + transaction.getId(), e);
        } finally {
            if (db != null) db.close();
        }
        return rowsAffected;
    }

    public void deleteTransaction(long id) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int rowsDeleted = db.delete(TABLE_TRANSACTIONS, KEY_TRANS_ID + " = ?",
                    new String[]{String.valueOf(id)});
            Log.i(TAG, "Deleted " + rowsDeleted + " transaction(s) with id: " + id);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting transaction with id: " + id, e);
        } finally {
            if (db != null) db.close();
        }
    }

    // ========================================================================
    // Category CRUD Operations (Keep existing methods)
    // ========================================================================
    public long addCategory(String categoryName, Transaction.TransactionType type) {
        SQLiteDatabase db = null;
        long id = -1;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_CAT_NAME, categoryName);
            values.put(KEY_CAT_TYPE, type.toString());
            // Use insertWithOnConflict to handle potential duplicate primary keys gracefully
            id = db.insertWithOnConflict(TABLE_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            if (id == -1) {
                Log.w(TAG, "Category already exists or error adding: " + categoryName + " (" + type + ")");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding category: " + categoryName, e);
        } finally {
            if (db != null) db.close();
        }
        return id;
    }

    public List<String> getAllCategories(Transaction.TransactionType type) {
        List<String> categoryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String selectQuery = "SELECT " + KEY_CAT_NAME + " FROM " + TABLE_CATEGORIES +
                " WHERE " + KEY_CAT_TYPE + " = ? ORDER BY " + KEY_CAT_NAME + " ASC";
        try {
            cursor = db.rawQuery(selectQuery, new String[]{type.toString()});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    categoryList.add(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CAT_NAME)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting categories for type: " + type, e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return categoryList;
    }

    public void deleteCategory(String categoryName, Transaction.TransactionType type) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int rowsDeleted = db.delete(TABLE_CATEGORIES, KEY_CAT_NAME + " = ? AND " + KEY_CAT_TYPE + " = ?",
                    new String[]{categoryName, type.toString()});
            Log.i(TAG, "Deleted " + rowsDeleted + " category(ies): " + categoryName + " (" + type + ")");
        } catch (Exception e) {
            Log.e(TAG, "Error deleting category: " + categoryName + " (" + type + ")", e);
        } finally {
            if (db != null) db.close();
        }
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        // Use a single ContentValues object
        ContentValues values = new ContentValues();

        // Define default categories
        String[] incomeCategories = {"Salary", "Other Income"};
        String[] expenseCategories = {"Rent", "Food", "Utilities", "Entertainment", "Transport"};

        // Insert Income categories
        values.put(KEY_CAT_TYPE, Transaction.TransactionType.INCOME.toString());
        for (String cat : incomeCategories) {
            values.put(KEY_CAT_NAME, cat);
            db.insertWithOnConflict(TABLE_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }

        // Insert Expense categories
        values.put(KEY_CAT_TYPE, Transaction.TransactionType.EXPENSE.toString());
        for (String cat : expenseCategories) {
            values.put(KEY_CAT_NAME, cat);
            db.insertWithOnConflict(TABLE_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        Log.i(TAG, "Inserted default categories.");
    }


    // ========================================================================
    // Investment CRUD Operations
    // ========================================================================

    public long addInvestmentHolding(InvestmentHolding holding) {
        SQLiteDatabase db = null;
        long id = -1;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_INV_SYMBOL, holding.getTickerSymbol());
            values.put(KEY_INV_COMPANY_NAME, holding.getCompanyName());
            values.put(KEY_INV_QUANTITY, holding.getQuantity());
            values.put(KEY_INV_PURCHASE_PRICE, holding.getPurchasePrice());
            values.put(KEY_INV_PURCHASE_DATE, holding.getPurchaseDate());
            // Store initial current price, often same as purchase price initially
            values.put(KEY_INV_CURRENT_PRICE, holding.getCurrentPrice());
            id = db.insert(TABLE_INVESTMENTS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error adding investment holding: " + holding.getTickerSymbol(), e);
        } finally {
            if (db != null) db.close();
        }
        return id;
    }

    public InvestmentHolding getInvestmentHolding(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        InvestmentHolding holding = null;
        try {
            cursor = db.query(TABLE_INVESTMENTS, null, // Select all columns
                    KEY_INV_ID + "=?", new String[]{String.valueOf(id)},
                    null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                holding = new InvestmentHolding(
                        cursor.getLong(cursor.getColumnIndexOrThrow(KEY_INV_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_INV_SYMBOL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_INV_COMPANY_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_INV_QUANTITY)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_INV_PURCHASE_PRICE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(KEY_INV_PURCHASE_DATE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_INV_CURRENT_PRICE))
                );
            } else {
                Log.w(TAG, "Investment holding not found with id: " + id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting investment holding with id: " + id, e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return holding;
    }

    public List<InvestmentHolding> getAllInvestmentHoldings() {
        List<InvestmentHolding> holdingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String selectQuery = "SELECT * FROM " + TABLE_INVESTMENTS + " ORDER BY " + KEY_INV_SYMBOL + " ASC";
        try {
            cursor = db.rawQuery(selectQuery, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    InvestmentHolding holding = new InvestmentHolding(
                            cursor.getLong(cursor.getColumnIndexOrThrow(KEY_INV_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_INV_SYMBOL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_INV_COMPANY_NAME)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_INV_QUANTITY)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_INV_PURCHASE_PRICE)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(KEY_INV_PURCHASE_DATE)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_INV_CURRENT_PRICE))
                    );
                    holdingList.add(holding);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all investment holdings", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return holdingList;
    }

    // --- NEW: Update Investment Holding ---
    public int updateInvestmentHolding(InvestmentHolding holding) {
        SQLiteDatabase db = null;
        int rowsAffected = 0;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            // Include symbol in the update now
            values.put(KEY_INV_SYMBOL, holding.getTickerSymbol()); // <<< ADDED
            values.put(KEY_INV_COMPANY_NAME, holding.getCompanyName());
            values.put(KEY_INV_QUANTITY, holding.getQuantity());
            values.put(KEY_INV_PURCHASE_PRICE, holding.getPurchasePrice());
            values.put(KEY_INV_PURCHASE_DATE, holding.getPurchaseDate());
            // Current price is updated separately

            rowsAffected = db.update(TABLE_INVESTMENTS, values, KEY_INV_ID + " = ?",
                    new String[]{String.valueOf(holding.getId())});
        } catch (Exception e) {
            Log.e(TAG, "Error updating investment holding with id: " + holding.getId(), e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
        return rowsAffected;
    }

    // --- NEW: Delete Investment Holding ---
    public void deleteInvestmentHolding(long id) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int rowsDeleted = db.delete(TABLE_INVESTMENTS, KEY_INV_ID + " = ?",
                    new String[]{String.valueOf(id)});
            Log.i(TAG, "Deleted " + rowsDeleted + " investment holding(s) with id: " + id);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting investment holding with id: " + id, e);
        } finally {
            if (db != null) db.close();
        }
    }


    public int updateInvestmentCurrentPrice(long id, double newPrice) {
        SQLiteDatabase db = null;
        int rowsAffected = 0;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_INV_CURRENT_PRICE, newPrice);
            rowsAffected = db.update(TABLE_INVESTMENTS, values, KEY_INV_ID + " = ?",
                    new String[]{String.valueOf(id)});
        } catch (Exception e) {
            Log.e(TAG, "Error updating current price for investment id: " + id, e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
        return rowsAffected;
    }
    // --- NEW: Method to get aggregated stats by category ---
    public List<CategoryStat> getCategoryStats(long startDate, long endDateExclusive, Transaction.TransactionType type) {
        List<CategoryStat> statsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        // SQL query to sum amounts by category within the date range and for the specific type
        String query = "SELECT " + KEY_TRANS_CATEGORY + ", SUM(" + KEY_TRANS_AMOUNT + ") as total " +
                "FROM " + TABLE_TRANSACTIONS + " " +
                "WHERE " + KEY_TRANS_TYPE + " = ? AND " +
                KEY_TRANS_DATE + " >= ? AND " +
                KEY_TRANS_DATE + " < ? " + // Use < for exclusive end date
                "GROUP BY " + KEY_TRANS_CATEGORY + " " +
                "HAVING total > 0 " + // Only include categories with actual amounts
                "ORDER BY total DESC"; // Order by amount descending

        // Arguments for the query placeholders
        String[] selectionArgs = {
                type.toString(),
                String.valueOf(startDate),
                String.valueOf(endDateExclusive)
        };

        Log.d(TAG, "Executing stats query: " + query + " with args: " + type + ", " + startDate + ", " + endDateExclusive);

        try {
            cursor = db.rawQuery(query, selectionArgs);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TRANS_CATEGORY));
                    double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                    statsList.add(new CategoryStat(categoryName, totalAmount));
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "No stats data found for the given criteria.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting category stats", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        Log.d(TAG, "Returning " + statsList.size() + " category stats.");
        return statsList;
    }
} // End of DatabaseHelper
