package com.example.barcodescanner_zxing;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;


public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Barcodes.db";
    public static final String TABLE_NAME = "barcode_records";
    public static final String TABLE_NAME2 = "shoppingList_records";
    public static final String TABLE_NAME3 = "historyList_records";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "BARCODE";
    public static final String COL_3 = "PRICE";
    public static final String COL_4 = "STORE";
    public static final String COL_5 = "PRODUCT_NAME";
    public static final String COL_6 = "PRODUCT_ID";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 7);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ( "
                + "ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "BARCODE VARCHAR(16) UNIQUE, "
                + "PRICE INTEGER, "
                + "STORE VARCHAR(16), "
                + "PRODUCT_NAME TEXT )");

        db.execSQL("create table " + TABLE_NAME2 + " ( "
                + "ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "PRODUCT_NAME VARCHAR(16), "
                + "PRODUCT_ID INTEGER, "
                + "AMOUNT INTEGER, "
                + "TAKEN INTEGER DEFAULT 0 )");

        db.execSQL("create table " + TABLE_NAME3 + " ( "
                + "ID INTEGER, "
                + "PRODUCT_NAME VARCHAR(16), "
                + "PRODUCT_ID INTEGER, "
                + "AMOUNT INTEGER, "
                + "TAKEN INTEGER DEFAULT 0, "
                + "LIST_ID INTEGER DEFAULT 0, "
                + "DATE DATETIME )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //IF DATABASE TABLE  UPDATED
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME2);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME3);
        onCreate(db);
    }

    public boolean insertData(String barcode, Integer price, String store, String productName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, barcode);
        contentValues.put(COL_3, price);
        contentValues.put(COL_4, store);
        contentValues.put(COL_5, productName);

        //if result is -1 we got an error
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean updateData(String barcode, Integer price, String store, String productName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //contentValues.put(COL_2, barcode);
        contentValues.put(COL_3, price);
        contentValues.put(COL_4, store);
        contentValues.put(COL_5, productName);

        //if result is -1 we got an error
        long result = db.update(TABLE_NAME, contentValues, "barcode=?", new String[]{barcode});
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Cursor getBarcodeInfo(String barcode) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("select * from " + TABLE_NAME + " where barcode =?", new String[]{barcode});
        return result;
    }

    public String[] getGroceries() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("select * from " + TABLE_NAME, null);
        String[] groceries = new String[result.getCount()];
        int counter = 0;
        while (result.moveToNext()) {
            groceries[counter] = result.getString(4);
            counter++;
        }
        return groceries;
    }

    public boolean insertToGroceryList(String productName) {
        System.out.println("yolo 3");
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("select id from barcode_records where product_name =?", new String[]{productName});
        ContentValues contentValues = new ContentValues();
        //contentValues.put(COL_2, barcode);
        result.moveToFirst();
        int productID = Integer.parseInt(result.getString(0));
        contentValues.put(COL_5, productName);
        contentValues.put(COL_6, productID);
        contentValues.put("AMOUNT", 1);
        contentValues.put("TAKEN", 0);
        // contentValues.put("AMOUNT", 1);
        Cursor productExist = db.rawQuery("select * from shoppingList_records where product_name =?", new String[]{productName});
        if (productExist.getCount() > 0) {
            //   long resultInsert = db.insert(TABLE_NAME2, null , contentValues);
            return false;
        } else {
            long resultInsert = db.insert(TABLE_NAME2, null, contentValues);
            if (resultInsert == -1) {
                return false;
            } else {
                return true;
            }
        }
    }

    public boolean changeAmount(String productId, int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        Cursor taken = db.rawQuery("select taken, amount from " + TABLE_NAME2 + " where product_id =? and taken=0", new String[]{productId});


        //if result is -1 we got an error
        if (taken.getCount() == 1) {
            taken.moveToFirst();
            int newAmount = Integer.parseInt(taken.getString(1)) + value;
            contentValues.put("AMOUNT", newAmount);
            long result = db.update(TABLE_NAME2, contentValues, "product_id=?", new String[]{productId});
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    public int getAmount(String productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        Cursor taken = db.rawQuery("select amount from " + TABLE_NAME2 + " where product_id =?", new String[]{productId});
        taken.moveToFirst();
        return Integer.parseInt(taken.getString(0));
    }

    public Cursor getShoppingList() {
        SQLiteDatabase db = this.getWritableDatabase();
        //Cursor result = db.rawQuery("select "+ TABLE_NAME2 + ".PRODUCT_NAME, "+ TABLE_NAME +".ID, "+TABLE_NAME +".PRICE, "+TABLE_NAME +".STORE FROM " + TABLE_NAME2 + " INNER JOIN "+ TABLE_NAME + " ON " + TABLE_NAME2 +".PRODUCT_ID = "+TABLE_NAME+".ID", null);
        Cursor result = db.rawQuery("select " + TABLE_NAME2 + ".PRODUCT_NAME, " + TABLE_NAME + ".ID, " + TABLE_NAME + ".PRICE, " + TABLE_NAME + ".STORE, " + TABLE_NAME2 + ".AMOUNT, " + TABLE_NAME2 + ".TAKEN FROM " + TABLE_NAME2 + " INNER JOIN " + TABLE_NAME + " ON " + TABLE_NAME2 + ".PRODUCT_ID = " + TABLE_NAME + ".ID", null);

        return result;
    }

    public boolean takeGrocery(String productId, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("TAKEN", status);

        //if result is -1 we got an error
        long result = db.update(TABLE_NAME2, contentValues, "product_id=?", new String[]{productId});
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean getStatus(String productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor taken = db.rawQuery("select taken from " + TABLE_NAME2 + " where product_id =?", new String[]{productId});
        taken.moveToFirst();
        if (Integer.parseInt(taken.getString(0)) == 1) {
            return true;
        } else {
            return false;
        }
    }

    public int getTotalPrice() {
        SQLiteDatabase db = this.getWritableDatabase();

        int finalPrice = 0;
        Cursor result = db.rawQuery("select " + TABLE_NAME + ".PRICE, " + TABLE_NAME2 + ".AMOUNT FROM " + TABLE_NAME2 + " INNER JOIN " + TABLE_NAME + " ON " + TABLE_NAME2 + ".PRODUCT_ID = " + TABLE_NAME + ".ID", null);
        while (result.moveToNext()) {
            finalPrice += Integer.parseInt(result.getString(0)) * Integer.parseInt(result.getString(1));
        }
        return finalPrice;
    }

    public void removeItem(int productID) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("delete from " + TABLE_NAME2 + " where product_id =?", new String[]{String.valueOf(productID)});
        System.out.println("remove " + result.getCount());
    }

    public void checkOutList() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        String latest_id;
        Cursor latest_listId = db.rawQuery("SELECT MAX(LIST_ID) FROM " + TABLE_NAME3 + " LIMIT 1", null);
        latest_listId.moveToFirst();
        if(latest_listId.getString(0) == null)
            latest_id = "0";
        else
            latest_id = String.valueOf(Integer.parseInt(latest_listId.getString(0))+1);

        Cursor buyList = db.rawQuery("select * from " + TABLE_NAME2, null);
        while(buyList.moveToNext()) {
            String id = buyList.getString(0);
            String product_name = buyList.getString(1);
            String product_id = buyList.getString(2);
            String amount = buyList.getString(3);
            String taken = buyList.getString(4);

            contentValues.put("id", id);
            contentValues.put("product_name", product_name);
            contentValues.put("product_id", product_id);
            contentValues.put("amount", amount);
            contentValues.put("taken", taken);
            contentValues.put("date", getDateTime());
            contentValues.put("list_id", latest_id);

            db.insert(TABLE_NAME3, null, contentValues);
            db.delete(TABLE_NAME2, null, null);
        }
    }

    public void removeCurrentList() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME2, null, null);
    }

    public Cursor getHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor latest_listId = db.rawQuery("SELECT distinct date, list_id from " + TABLE_NAME3, null);
        return latest_listId;
    }

    public Cursor getSpecificHistory(String listId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("select " + TABLE_NAME + ".PRICE, " + TABLE_NAME3 + ".AMOUNT, "+ TABLE_NAME3 + ".PRODUCT_NAME FROM " + TABLE_NAME3 + " INNER JOIN " + TABLE_NAME + " ON " + TABLE_NAME3 + ".PRODUCT_ID = " + TABLE_NAME + ".ID AND " + TABLE_NAME3 + ".LIST_ID=?" , new String[] {listId});
        return result;
    }

    public void removeHistoryList(String listId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+TABLE_NAME3+" WHERE list_id = "+listId);
        db.close();
    }

    public int getTotalPriceOnHistoryList(String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        int finalPrice = 0;
        Cursor result = db.rawQuery("select " + TABLE_NAME + ".PRICE, " + TABLE_NAME3 + ".AMOUNT FROM " + TABLE_NAME3 + " INNER JOIN " + TABLE_NAME + " ON " + TABLE_NAME3 + ".PRODUCT_ID = " + TABLE_NAME + ".ID where "+TABLE_NAME3+".date=?", new String[] {date});
        while (result.moveToNext()) {
            finalPrice += Integer.parseInt(result.getString(0)) * Integer.parseInt(result.getString(1));
        }
        return finalPrice;
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
