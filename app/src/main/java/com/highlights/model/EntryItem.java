package com.highlights.model;

/**
 * Created by austin on 12/22/14.
 */
public class EntryItem {

    int id;
    String text;
    long unixtime;
    int dayOfMonth;
    int month;
    int year;

    public void setId(int id) { this.id = id; }

    public int getId() { return id; }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setUnixtime(long unixtime) {this.unixtime = unixtime; }

    public long getUnixtime() { return unixtime; }

    public void setDayOfMonth(int dayOfMonth) { this.dayOfMonth = dayOfMonth; }

    public int getDayOfMonth() { return dayOfMonth; }

    public void setMonth(int month) { this.month = month; }

    public int getMonth() { return month; }

    public void setYear(int year) { this.year = year; }

    public int getYear() { return year; }
}
