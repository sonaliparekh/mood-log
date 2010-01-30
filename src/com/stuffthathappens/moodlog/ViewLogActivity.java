package com.stuffthathappens.moodlog;

import android.app.ListActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.BaseColumns._ID;
import static com.stuffthathappens.moodlog.Constants.*;

public class ViewLogActivity extends ListActivity {

    private static final String[] FROM_COLS = {
            WORD_COL,
            ENTERED_ON_COL,
            WORD_SIZE_COL,
            _ID
    };
    private static final int[] TO = {
            R.id.log_item_word,
            R.id.log_item_date,
            R.id.log_item_word
    };
    private static final String ORDER_BY = String.format(
            "%s desc", ENTERED_ON_COL);

    Date mDate = new Date();

    private MoodLogData mMoodLogData;
    private static final int WORD_COL_INDEX = 0;
    private static final int ENTERED_ON_COL_INDEX = 1;

    private static final int WORD_SIZE_COL_INDEX = 2;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_log);

        mMoodLogData = new MoodLogData(this);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.log_list_item,
                getAllWords(),
                FROM_COLS,
                TO);
        adapter.setViewBinder(new LogBinder());

        setListAdapter(adapter);
    }

    private Cursor getAllWords() {
        SQLiteDatabase db = mMoodLogData.getReadableDatabase();
        Cursor cursor = db.query(LOG_ENTRIES_TABLE, FROM_COLS, null, null, null,
                null, ORDER_BY);
        startManagingCursor(cursor);
        return cursor;
    }


    private class LogBinder implements SimpleCursorAdapter.ViewBinder {

        public boolean setViewValue(View view, Cursor cursor, int column) {
            switch (column) {
                case WORD_COL_INDEX:
                    ((TextView) view).setText(cursor.getString(column));
                    return true;
                case ENTERED_ON_COL_INDEX:
                    mDate.setTime(cursor.getLong(column));
                    ((TextView) view).setText(dateFormat.format(mDate));
                    return true;
                case WORD_SIZE_COL_INDEX:
                    int wordSize = cursor.getInt(column);
                    ((TextView) view).setTextSize(12f + wordSize * 3);
                    return true;
            }
            return false;
        }
    }
}
