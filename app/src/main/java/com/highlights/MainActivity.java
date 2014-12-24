package com.highlights;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.DropBoxManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.highlights.db.Contract;
import com.highlights.db.DbHelper;
import com.highlights.model.EntryItem;
import com.highlights.service.EntryNotificationAlarmReceiver;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.YearMonth;

import com.highlights.db.Contract.EntryModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private CaldroidFragment dialogCaldroidFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    EntryExpandableListAdapter listAdapter;
    List<YearMonth> listDataHeader;
    HashMap<YearMonth, List<EntryItem>> listDataChild;

    PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mTitle = getTitle();

        ExpandableListView expListView = (ExpandableListView) findViewById(R.id.entryList);
        prepareListData();
        listAdapter = new EntryExpandableListAdapter(this, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);

        setEntryAlarm();
    }

    @Override
    protected void onStart() {
        super.onStart();
        prepareListData();

    }

    // TODO lazy load the list structure instead of loading entire db
    private void prepareListData() {
        listDataHeader = new ArrayList<YearMonth>();
        listDataChild = new HashMap<YearMonth, List<EntryItem>>();

        int maxYear = getMaxYear();
        int minYear = getMinYear();

        // TODO so this is pretty slow/bad
        // it goes through every single possible year and month combo
        // and checks to see if it's actually a real thing
        for(int year=maxYear;year>=minYear;year--) {
            for(int month=1;month<=12;month++) {
                YearMonth yearMonth = new YearMonth(year, month);
                List<EntryItem> l = fetchEntriesFromMonth(yearMonth);
                if(l.size() > 0) {
                    listDataHeader.add(yearMonth);
                    listDataChild.put(yearMonth, l);
                }
            }
        }
    }

    private int getMaxYear() {
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase rdb = dbHelper.getReadableDatabase();
        Cursor c = rdb.rawQuery("SELECT MAX(" + EntryModel.COLUMN_NAME_YEAR + ") FROM " + EntryModel.TABLE_NAME, null);
        c.moveToFirst();
        int maxYear = c.getInt(0);

        // c.close();

        rdb.close();

        return maxYear;
    }

    private int getMinYear() {
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase rdb = dbHelper.getReadableDatabase();
        rdb.beginTransaction();

        Cursor c = rdb.rawQuery("SELECT MIN(" + EntryModel.COLUMN_NAME_YEAR + ") FROM " + EntryModel.TABLE_NAME, null);
        c.moveToFirst();
        int minYear = c.getInt(0);

        c.close();
        rdb.setTransactionSuccessful();
        rdb.endTransaction();
        rdb.close();

        return minYear;
    }

    private List<EntryItem> fetchEntriesFromMonth(YearMonth yearMonth) {
        ArrayList<EntryItem> entries = new ArrayList<EntryItem>();

        int year = yearMonth.getYear();
        int month = yearMonth.getMonthOfYear();

        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase rdb = dbHelper.getReadableDatabase();
        rdb.beginTransaction();

        String[] projection = {
                EntryModel._ID,
                EntryModel.COLUMN_NAME_DAY_OF_MONTH,
                EntryModel.COLUMN_NAME_MONTH,
                EntryModel.COLUMN_NAME_YEAR,
                EntryModel.COLUMN_NAME_UNIX_TIME,
                EntryModel.COLUMN_NAME_ENTRY_TEXT
        };

        String selection =
                EntryModel.COLUMN_NAME_YEAR + " LIKE ?" + " AND " + EntryModel.COLUMN_NAME_MONTH + " LIKE ?";
        String[] selectionArgs = {Integer.toString(year), Integer.toString(month)};

        String orderBy = EntryModel.COLUMN_NAME_DAY_OF_MONTH + " DESC";

        Cursor c = rdb.query(EntryModel.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                orderBy,
                null);

        while(c.moveToNext()) {
            EntryItem entry = new EntryItem();

            int id = c.getInt(c.getColumnIndexOrThrow(EntryModel._ID));
            int dayOfMonth = c.getInt(c.getColumnIndexOrThrow(EntryModel.COLUMN_NAME_DAY_OF_MONTH));
            int eMonth = c.getInt(c.getColumnIndexOrThrow(EntryModel.COLUMN_NAME_MONTH));
            int eYear = c.getInt(c.getColumnIndexOrThrow(EntryModel.COLUMN_NAME_YEAR));
            String text = c.getString(c.getColumnIndexOrThrow(EntryModel.COLUMN_NAME_ENTRY_TEXT));
            long unixtime = c.getLong(c.getColumnIndexOrThrow(EntryModel.COLUMN_NAME_UNIX_TIME));
            entry.setId(id);
            entry.setDayOfMonth(dayOfMonth);
            entry.setMonth(eMonth);
            entry.setYear(eYear);
            entry.setText(text);
            entry.setUnixtime(unixtime);

            entries.add(entry);
        }

        c.close();
        rdb.setTransactionSuccessful();
        rdb.endTransaction();
        rdb.close();

        return entries;

    }

    private void setEntryAlarm() {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, EntryNotificationAlarmReceiver.class);
        final int _id = (int) System.currentTimeMillis();
        // TODO change to hour of day alarm based on user setting
        alarmIntent = PendingIntent.getBroadcast(this, _id, intent, 0);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        restoreActionBar();
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_addEntry) {
            Intent intent = new Intent(this, EntryActivity.class);
            // start of day not current time
            intent.putExtra("entryDate", new LocalDate().toDateTimeAtStartOfDay().toString());
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_calendar) {
            final String dialogTag = "CALDROID_DIALOG_FRAGMENT";

            final CaldroidListener listener = new CaldroidListener() {
                @Override
                public void onSelectDate(Date date, View view) {
                    Intent intent = new Intent(MainActivity.this, EntryActivity.class);
                    intent.putExtra("entryDate", new DateTime(date).toString());
                    startActivity(intent);
                }
            };

            dialogCaldroidFragment = new CaldroidFragment();
            dialogCaldroidFragment.setCaldroidListener(listener);
            dialogCaldroidFragment.show(getSupportFragmentManager(), dialogTag);
        }

        return super.onOptionsItemSelected(item);
    }

}
