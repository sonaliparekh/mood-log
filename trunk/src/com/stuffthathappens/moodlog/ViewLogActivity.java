package com.stuffthathappens.moodlog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.BaseColumns._ID;
import static com.stuffthathappens.moodlog.Constants.*;

public class ViewLogActivity extends ListActivity {

    private static final String[] FROM_COLS = {
            WORD_COL,
            "entry_date",
            "entry_time",
            WORD_SIZE_COL,
            _ID
    };
    private static final int[] TO = {
            R.id.log_item_word,
            R.id.log_item_date,
            R.id.log_item_time,
            R.id.log_item_word
    };

    Date mDate = new Date();

    private MoodLogData mMoodLogData;
    private Cursor mLogCursor;
    private long mSelectedId;
    private String mSelectedWord;

    private static final int WORD_COL_INDEX = 0;
    private static final int DATE_COL_INDEX = 1;
    private static final int TIME_COL_INDEX = 2;

    private static final int WORD_SIZE_COL_INDEX = 3;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");

    private static final int[] ROW_BACKGROUNDS = new int[]{
            R.color.log_even_row_background,
            R.color.log_odd_row_background
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_log);

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {               
                finish();
            }
        });
        getListView().setOnCreateContextMenuListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getAllEntries();
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.log_list_item,
                mLogCursor,
                FROM_COLS,
                TO) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                v.setBackgroundResource(ROW_BACKGROUNDS[position % 2]);
                return v;
            }
        };
        adapter.setViewBinder(new LogBinder());
        setListAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mLogCursor != null) {
            stopManagingCursor(mLogCursor);
            mLogCursor.close();
            mLogCursor = null;
        }
        if (mMoodLogData != null) {
            mMoodLogData.close();
            mMoodLogData = null;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu,
                                    View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;
        mSelectedId = info.id;
        mSelectedWord = ((TextView) info.targetView.findViewById(
                R.id.log_item_word)).getText().toString();

        contextMenu.setHeaderTitle(mSelectedWord);
        contextMenu.add(0, CONTEXT_MENU_DELETE_ITEM, 0, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CONTEXT_MENU_DELETE_ITEM:
                // this approach ensures the dialog is managed by the activity, so
                // it properly handles screen rotations and other lifecycle events
                showDialog(CONFIRM_DELETE_DIALOG);
                return true;
        }
        return false;
    }

    private void getAllEntries() {
        String sql = String.format("" +
                "select %s, %s as entry_date, %s as entry_time, %s, %s from %s order by %s desc",
                WORD_COL, ENTERED_ON_COL, ENTERED_ON_COL, WORD_SIZE_COL, _ID,
                LOG_ENTRIES_TABLE, ENTERED_ON_COL);

        SQLiteDatabase db = getMoodLogData().getReadableDatabase();
        mLogCursor = db.rawQuery(sql, null);
        startManagingCursor(mLogCursor);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == CONFIRM_DELETE_DIALOG) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(mSelectedWord)
                    .setMessage(R.string.confirm_delete_entry_msg)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            doDeleteLogEntry(mSelectedId);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            return builder.create();
        }
        return super.onCreateDialog(id);
    }

    private void doDeleteLogEntry(long victim) {
        SQLiteDatabase db = getMoodLogData().getWritableDatabase();

        db.delete(LOG_ENTRIES_TABLE, _ID + " = " + victim, null);
        refreshLog();
    }

    private void refreshLog() {
        if (mLogCursor != null) {
            mLogCursor.requery();
        }
    }

    private class LogBinder implements SimpleCursorAdapter.ViewBinder {

        public boolean setViewValue(View view, Cursor cursor, int column) {
            switch (column) {
                case WORD_COL_INDEX:
                    ((TextView) view).setText(cursor.getString(column));
                    return true;
                case DATE_COL_INDEX:
                    mDate.setTime(cursor.getLong(column));
                    ((TextView) view).setText(dateFormat.format(mDate));
                    return true;
                case TIME_COL_INDEX:
                    mDate.setTime(cursor.getLong(column));
                    ((TextView) view).setText(timeFormat.format(mDate));
                    return true;
                case WORD_SIZE_COL_INDEX:
                    int wordSize = cursor.getInt(column);
                    ((TextView) view).setTextSize(12f + wordSize * 3);
                    return true;
            }
            return false;
        }
    }

    private MoodLogData getMoodLogData() {
        if (mMoodLogData == null) {
            mMoodLogData = new MoodLogData(this);
        }
        return mMoodLogData;
    }

}
