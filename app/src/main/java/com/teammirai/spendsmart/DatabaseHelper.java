package com.teammirai.spendsmart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FinanceData.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_SAVED = "saved_money";
    private static final String TABLE_SPENT = "spent_money";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_AMOUNT = "amount";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSavedTable = "CREATE TABLE " + TABLE_SAVED + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_AMOUNT + " REAL)";

        String createSpentTable = "CREATE TABLE " + TABLE_SPENT + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_AMOUNT + " REAL)";

        db.execSQL(createSavedTable);
        db.execSQL(createSpentTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPENT);
        onCreate(db);
    }

    public void insertSavedMoney(String date, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_AMOUNT, amount);
        db.insert(TABLE_SAVED, null, values);
        db.close();
    }

    public boolean insertSpentMoney(String date, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_AMOUNT, amount);
        long result = db.insert(TABLE_SPENT, null, values);
        db.close();
        return result != -1;
    }

    public double getSpentMoney(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_AMOUNT + " FROM " + TABLE_SPENT + " WHERE " + COLUMN_DATE + " = ?", new String[]{date});
        double amount = 0.0;
        if (cursor.moveToFirst()) {
            amount = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return amount;
    }

    public boolean updateSpentMoney(String date, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        int rows = db.update(TABLE_SPENT, values, COLUMN_DATE + " = ?", new String[]{date});
        db.close();
        return rows > 0;
    }

    public Cursor getSavedMoney() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SAVED, null);
    }

    public Cursor getSpentMoney() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SPENT, null);
    }

    public void clearTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SAVED);
        db.execSQL("DELETE FROM " + TABLE_SPENT);
        db.close();
    }
}
