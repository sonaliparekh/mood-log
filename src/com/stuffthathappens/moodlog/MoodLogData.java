package com.stuffthathappens.moodlog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.stuffthathappens.moodlog.Constants.*;
import static com.stuffthathappens.moodlog.Constants.LOG_ENTRIES_TABLE;

/**
 * @author Eric Burke
 */
public class MoodLogData extends SQLiteOpenHelper {

    private static final String DB_NAME = "moodlog.db";
    private static final int DB_VERSION = 1;

    public MoodLogData(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        
        db.execSQL("" +
                "create table " + LOG_ENTRIES_TABLE + " (" +
                _ID + " integer primary key autoincrement, " +
                ENTERED_ON_COL + " integer not null, " +
                INTENSITY_COL + " integer not null, " +
                WORD_COL + " text not null); ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + LOG_ENTRIES_TABLE);
        onCreate(db);
    }

    
}
