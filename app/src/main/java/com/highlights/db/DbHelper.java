package com.highlights.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.highlights.model.EntryItem;

import com.highlights.db.Contract.EntryModel;

import org.joda.time.DateTime;

import java.util.ArrayList;

/**
 * Created by austin on 12/22/14.
 */
public class DbHelper extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Highlights.db";

    private static final String INT_TYPE = " INTEGER";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + EntryModel.TABLE_NAME + " (" +
                    EntryModel._ID + " INTEGER PRIMARY KEY," +
                    EntryModel.COLUMN_NAME_DAY_OF_MONTH + INT_TYPE + COMMA_SEP +
                    EntryModel.COLUMN_NAME_MONTH + INT_TYPE + COMMA_SEP +
                    EntryModel.COLUMN_NAME_YEAR + INT_TYPE + COMMA_SEP +
                    EntryModel.COLUMN_NAME_UNIX_TIME + INT_TYPE + COMMA_SEP +
                    EntryModel.COLUMN_NAME_ENTRY_TEXT + TEXT_TYPE +
                    " )";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public EntryItem loadEntry(DateTime date) {

        EntryItem entry = new EntryItem();
        SQLiteDatabase rdb = getReadableDatabase();
        rdb.beginTransaction();

        String[] projection = {
                EntryModel._ID,
                EntryModel.COLUMN_NAME_DAY_OF_MONTH,
                EntryModel.COLUMN_NAME_MONTH,
                EntryModel.COLUMN_NAME_YEAR,
                EntryModel.COLUMN_NAME_UNIX_TIME,
                EntryModel.COLUMN_NAME_ENTRY_TEXT
        };

        String sortOrder = EntryModel.COLUMN_NAME_UNIX_TIME + " DESC";

        String selection = EntryModel.COLUMN_NAME_UNIX_TIME + " LIKE ?";

        String[] selectionArgs = {Long.toString( date.getMillis() / 1000 )};

        Cursor c = rdb.query(EntryModel.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        c.moveToFirst();

        // if entry was never recorded
        if(c.getCount() < 1) {
            c.close();
            rdb.endTransaction();
            rdb.close();
            return null;
        }

        entry.setId(c.getInt(c.getColumnIndexOrThrow(EntryModel._ID)));
        entry.setText(c.getString(c.getColumnIndexOrThrow(EntryModel.COLUMN_NAME_ENTRY_TEXT)));
        entry.setDayOfMonth(c.getInt(c.getColumnIndexOrThrow(EntryModel.COLUMN_NAME_DAY_OF_MONTH)));
        entry.setMonth(c.getInt(c.getColumnIndexOrThrow(EntryModel.COLUMN_NAME_MONTH)));
        entry.setYear(c.getInt(c.getColumnIndexOrThrow(EntryModel.COLUMN_NAME_YEAR)));
        entry.setUnixtime(c.getLong(c.getColumnIndexOrThrow(EntryModel.COLUMN_NAME_UNIX_TIME)));

        c.close();
        rdb.setTransactionSuccessful();
        rdb.endTransaction();
        rdb.close();

        return entry;
    }

    private void insertEntry(EntryItem entry) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.beginTransaction();

        ContentValues entryValues = new ContentValues();
        entryValues.put(EntryModel.COLUMN_NAME_ENTRY_TEXT, entry.getText());
        entryValues.put(EntryModel.COLUMN_NAME_DAY_OF_MONTH, entry.getDayOfMonth());
        entryValues.put(EntryModel.COLUMN_NAME_MONTH, entry.getMonth());
        entryValues.put(EntryModel.COLUMN_NAME_YEAR, entry.getYear());
        entryValues.put(EntryModel.COLUMN_NAME_UNIX_TIME, entry.getUnixtime());

        wdb.insert(EntryModel.TABLE_NAME, null, entryValues);

        wdb.setTransactionSuccessful();
        wdb.endTransaction();
        wdb.close();
    }

    private void updateEntry(int row, EntryItem entry) {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.beginTransaction();

        ContentValues entryValues = new ContentValues();
        entryValues.put(EntryModel.COLUMN_NAME_ENTRY_TEXT, entry.getText());
        entryValues.put(EntryModel.COLUMN_NAME_DAY_OF_MONTH, entry.getDayOfMonth());
        entryValues.put(EntryModel.COLUMN_NAME_MONTH, entry.getMonth());
        entryValues.put(EntryModel.COLUMN_NAME_YEAR, entry.getYear());
        entryValues.put(EntryModel.COLUMN_NAME_UNIX_TIME, entry.getUnixtime());

        wdb.update(EntryModel.TABLE_NAME, entryValues, "_id=" + row, null);

        wdb.setTransactionSuccessful();
        wdb.endTransaction();
        wdb.close();
    }

    public void saveEntry(EntryItem entry) {
        // create datetime from unixtime and check if it exists
        DateTime date = new DateTime(entry.getUnixtime() * 1000L);
        EntryItem check = loadEntry(date);
        if(check == null) {
            insertEntry(entry);
        } else {
            updateEntry(check.getId(), entry);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
