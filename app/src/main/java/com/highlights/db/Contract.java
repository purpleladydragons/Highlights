package com.highlights.db;

import android.provider.BaseColumns;

/**
 * Created by austin on 12/22/14.
 */
public final class Contract {

    public Contract() {}

    public static abstract class EntryModel implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_UNIX_TIME = "unix_time";
        public static final String COLUMN_NAME_DAY_OF_MONTH = "day_of_month";
        public static final String COLUMN_NAME_MONTH = "month";
        public static final String COLUMN_NAME_YEAR = "year";
        public static final String COLUMN_NAME_ENTRY_TEXT = "entry_text";
    }

}
