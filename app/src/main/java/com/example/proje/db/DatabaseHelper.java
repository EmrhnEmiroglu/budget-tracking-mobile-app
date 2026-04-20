package com.example.proje.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.proje.model.Goal;
import com.example.proje.model.Transaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "butce.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, amount REAL, category TEXT, type TEXT, note TEXT, date TEXT)");
        db.execSQL("CREATE TABLE goals (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, target_amount REAL, current_amount REAL, note TEXT)");
        db.execSQL("CREATE TABLE budget_limits (id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT UNIQUE, monthly_limit REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE budget_limits (id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT UNIQUE, monthly_limit REAL)");
        }
    }

    public long addTransaction(String title, double amount, String category, String type, String note, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("amount", amount);
        values.put("category", category);
        values.put("type", type);
        values.put("note", note);
        values.put("date", date);
        return db.insert("transactions", null, values);
    }

    public void updateTransaction(int id, String title, double amount, String category, String type, String note, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("amount", amount);
        values.put("category", category);
        values.put("type", type);
        values.put("note", note);
        values.put("date", date);
        db.update("transactions", values, "id=?", new String[]{String.valueOf(id)});
    }

    public Transaction getTransaction(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("transactions", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Transaction t = new Transaction(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2),
                    cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6));
            cursor.close();
            return t;
        }
        return null;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM transactions ORDER BY date DESC, id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Transaction(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2),
                        cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("transactions", "id=?", new String[]{String.valueOf(id)});
    }

    public double getTotalIncome() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE type='gelir'", null);
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    public double getTotalExpense() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE type='gider'", null);
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    public Map<String, Double> getCategoryExpenses() {
        Map<String, Double> map = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT category, SUM(amount) FROM transactions WHERE type='gider' GROUP BY category", null);
        if (cursor.moveToFirst()) {
            do {
                map.put(cursor.getString(0), cursor.getDouble(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return map;
    }

    public double getMonthlyExpenseByCategory(String category, String yearMonth) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE category=? AND type='gider' AND date LIKE ?", new String[]{category, yearMonth + "%"});
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    public Map<String, Map<String, Double>> getMonthlySummary() {
        Map<String, Map<String, Double>> summary = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT strftime('%m', date) as month, type, SUM(amount) FROM transactions GROUP BY month, type", null);
        if (cursor.moveToFirst()) {
            do {
                String month = cursor.getString(0);
                String type = cursor.getString(1);
                double amount = cursor.getDouble(2);
                if (!summary.containsKey(month)) summary.put(month, new HashMap<>());
                summary.get(month).put(type, amount);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return summary;
    }

    public long addGoal(String title, double target, double current, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("target_amount", target);
        values.put("current_amount", current);
        values.put("note", note);
        return db.insert("goals", null, values);
    }

    public List<Goal> getAllGoals() {
        List<Goal> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM goals", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Goal(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void updateGoalAmount(int id, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE goals SET current_amount = current_amount + ? WHERE id = ?", new Object[]{amount, id});
    }

    public void deleteGoal(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("goals", "id=?", new String[]{String.valueOf(id)});
    }

    public void setBudgetLimit(String category, double limit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("category", category);
        values.put("monthly_limit", limit);
        db.insertWithOnConflict("budget_limits", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public double getBudgetLimit(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("budget_limits", new String[]{"monthly_limit"}, "category=?", new String[]{category}, null, null, null);
        double limit = 0;
        if (cursor != null && cursor.moveToFirst()) {
            limit = cursor.getDouble(0);
            cursor.close();
        }
        return limit;
    }

    public Map<String, Double> getAllBudgetLimits() {
        Map<String, Double> map = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT category, monthly_limit FROM budget_limits", null);
        if (cursor.moveToFirst()) {
            do {
                map.put(cursor.getString(0), cursor.getDouble(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return map;
    }

    public List<Transaction> getFilteredTransactions(String category, String type, String startDate, String endDate) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder query = new StringBuilder("SELECT * FROM transactions WHERE 1=1");
        List<String> args = new ArrayList<>();
        if (category != null && !category.equals("Tümü")) { query.append(" AND category=?"); args.add(category); }
        if (type != null && !type.equals("Tümü")) { query.append(" AND type=?"); args.add(type.toLowerCase()); }
        if (startDate != null && endDate != null) { query.append(" AND date BETWEEN ? AND ?"); args.add(startDate); args.add(endDate); }
        query.append(" ORDER BY date DESC, id DESC");
        Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]));
        if (cursor.moveToFirst()) {
            do {
                list.add(new Transaction(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2),
                        cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int getDistinctCategoryCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT category) FROM transactions", null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public void deleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("transactions", null, null);
        db.delete("goals", null, null);
        db.delete("budget_limits", null, null);
    }
}