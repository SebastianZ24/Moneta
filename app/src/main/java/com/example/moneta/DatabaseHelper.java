package com.example.moneta;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "moneta.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_TRANSACTIONS = "transactions";

    // Table Columns
    private static final String KEY_ID = "id";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_TYPE = "type";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_DATE = "date";
    private static final String KEY_DESCRIPTION = "description";


    // Category Table
    private static final String TABLE_CATEGORIES = "categories";
    private static final String KEY_CATEGORY_NAME = "name";
    private static final String KEY_CATEGORY_TYPE = "type"; // Add this column

    private static final String CREATE_CATEGORIES_TABLE = "CREATE TABLE "
            + TABLE_CATEGORIES + "(" + KEY_CATEGORY_NAME
            + " TEXT NOT NULL, " + KEY_CATEGORY_TYPE
            + " TEXT NOT NULL, PRIMARY KEY (" + KEY_CATEGORY_NAME + ", " + KEY_CATEGORY_TYPE + "))";

    // Database creation sql statement
    private static final String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE "
            + TABLE_TRANSACTIONS + "(" + KEY_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_AMOUNT
            + " REAL NOT NULL," + KEY_TYPE
            + " TEXT NOT NULL," + KEY_CATEGORY
            + " TEXT NOT NULL," + KEY_DATE
            + " INTEGER NOT NULL," + KEY_DESCRIPTION + " TEXT);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TRANSACTIONS_TABLE);
        db.execSQL(CREATE_CATEGORIES_TABLE);
        // Initialize with some default categories if you want
        insertDefaultCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        // Create tables again
        onCreate(db);
    }

    // Transaction CRUD Operations

    public long addTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_AMOUNT, transaction.getAmount());
        values.put(KEY_TYPE, transaction.getType().toString());
        values.put(KEY_CATEGORY, transaction.getCategory());
        values.put(KEY_DATE, transaction.getDate());
        values.put(KEY_DESCRIPTION, transaction.getDescription());

        // Inserting Row
        long id = db.insert(TABLE_TRANSACTIONS, null, values);
        db.close(); // Closing database connection
        return id;
    }

    public Transaction getTransaction(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRANSACTIONS, new String[]{KEY_ID,
                        KEY_AMOUNT, KEY_TYPE, KEY_CATEGORY, KEY_DATE, KEY_DESCRIPTION}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Transaction transaction = new Transaction(Integer.parseInt(cursor.getString(0)),
                cursor.getDouble(1),
                Transaction.TransactionType.valueOf(cursor.getString(2)),
                cursor.getString(3),
                cursor.getLong(4),
                cursor.getString(5));
        cursor.close();
        db.close();
        return transaction;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactionList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + " ORDER BY " + KEY_DATE + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction(Integer.parseInt(cursor.getString(0)),
                        cursor.getDouble(1),
                        Transaction.TransactionType.valueOf(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getLong(4),
                        cursor.getString(5));
                // Adding transaction to list
                transactionList.add(transaction);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return transactionList;
    }

    public int updateTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_AMOUNT, transaction.getAmount());
        values.put(KEY_TYPE, transaction.getType().toString());
        values.put(KEY_CATEGORY, transaction.getCategory());
        values.put(KEY_DATE, transaction.getDate());
        values.put(KEY_DESCRIPTION, transaction.getDescription());

        // updating row
        return db.update(TABLE_TRANSACTIONS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(transaction.getId())});
    }

    public void deleteTransaction(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    // Category CRUD Operations

    public long addCategory(String categoryName, Transaction.TransactionType type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY_NAME, categoryName);
        values.put(KEY_CATEGORY_TYPE, type.toString());
        long id = db.insert(TABLE_CATEGORIES, null, values);
        db.close();
        return id;
    }
    public List<String> getAllCategories(Transaction.TransactionType type) {
        List<String> categoryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " + KEY_CATEGORY_NAME + " FROM " + TABLE_CATEGORIES +
                " WHERE " + KEY_CATEGORY_TYPE + " = '" + type.toString() + "'" +
                " ORDER BY " + KEY_CATEGORY_NAME + " ASC";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                categoryList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categoryList;
    }

    public void deleteCategory(String categoryName) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Also, consider updating transactions that use this category to a default or null
        db.delete(TABLE_CATEGORIES, KEY_CATEGORY_NAME + " = ?",
                new String[]{categoryName});
        db.close();
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        // Income categories
        values.put(KEY_CATEGORY_NAME, "Salary");
        values.put(KEY_CATEGORY_TYPE, Transaction.TransactionType.INCOME.toString());
        db.insert(TABLE_CATEGORIES, null, values);

        values.put(KEY_CATEGORY_NAME, "Other Income");
        values.put(KEY_CATEGORY_TYPE, Transaction.TransactionType.INCOME.toString());
        db.insert(TABLE_CATEGORIES, null, values);

        // Expense categories
        values.put(KEY_CATEGORY_NAME, "Rent");
        values.put(KEY_CATEGORY_TYPE, Transaction.TransactionType.EXPENSE.toString());
        db.insert(TABLE_CATEGORIES, null, values);

        values.put(KEY_CATEGORY_NAME, "Food");
        values.put(KEY_CATEGORY_TYPE, Transaction.TransactionType.EXPENSE.toString());
        db.insert(TABLE_CATEGORIES, null, values);

        values.put(KEY_CATEGORY_NAME, "Utilities");
        values.put(KEY_CATEGORY_TYPE, Transaction.TransactionType.EXPENSE.toString());
        db.insert(TABLE_CATEGORIES, null, values);

        values.put(KEY_CATEGORY_NAME, "Entertainment");
        values.put(KEY_CATEGORY_TYPE, Transaction.TransactionType.EXPENSE.toString());
        db.insert(TABLE_CATEGORIES, null, values);
    }
}