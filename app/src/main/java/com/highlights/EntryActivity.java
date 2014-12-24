package com.highlights;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.highlights.db.DbHelper;
import com.highlights.model.EntryItem;

import org.joda.time.DateTime;


public class EntryActivity extends ActionBarActivity {

    EntryItem entry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        Intent intent = getIntent();
        DateTime date = DateTime.parse(intent.getExtras().getString("entryDate"));

        DbHelper dbHelper = new DbHelper(this);
        entry = dbHelper.loadEntry(date);

        if(entry == null) {
            System.out.println("initializing");
            entry = initializeEntry(date);
        }

        System.out.println("unix:"+entry.getUnixtime());
        System.out.println("text:"+entry.getText());

        setDateTextViews(date);

        EditText entryEditText = (EditText) findViewById(R.id.entryEditText);
        entryEditText.setText(entry.getText());

    }

    private void setDateTextViews(DateTime date) {

        TextView entryDayOfMonthTextView = (TextView) findViewById(R.id.entryDayOfMonthTextView);
        TextView entryDayOfWeekTextView = (TextView) findViewById(R.id.entryDayOfWeekTextView);
        TextView entryMonthAndYearTextView = (TextView) findViewById(R.id.entryMonthAndYearTextView);

        entryDayOfMonthTextView.setText(String.valueOf(date.getDayOfMonth()));
        entryDayOfWeekTextView.setText(date.dayOfWeek().getAsText());
        entryMonthAndYearTextView.setText(date.monthOfYear().getAsText() + " " + date.getYear());

    }

    private EntryItem initializeEntry(DateTime date) {
        EntryItem entry = new EntryItem();
        entry.setDayOfMonth(date.getDayOfMonth());
        entry.setMonth(date.getMonthOfYear());
        entry.setYear(date.getYear());
        entry.setUnixtime(date.getMillis() / 1000);

        return entry;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveEntry();
    }

    private void saveEntry() {
        EditText entryEditText = (EditText) findViewById(R.id.entryEditText);
        entry.setText(entryEditText.getText().toString());
        DbHelper dbHelper = new DbHelper(this);
        dbHelper.saveEntry(entry);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_saveEntry) {
            DbHelper dbHelper = new DbHelper(this);
            dbHelper.saveEntry(entry);
            return true;
        }

        if (id == R.id.action_editEntry) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
