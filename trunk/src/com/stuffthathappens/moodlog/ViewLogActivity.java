package com.stuffthathappens.moodlog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
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

import static com.stuffthathappens.moodlog.Constants.*;

public class ViewLogActivity extends ListActivity {

    private static final int[] TO = {
            R.id.log_item_word,
            R.id.log_item_date,
            R.id.log_item_time,
            R.id.log_item_word
    };

    private MoodLogData data;
    private Cursor logCursor;
    private long selectedId;
    private String selectedWord;

    private static final int WORD_COL_INDEX = 0;
    private static final int DATE_COL_INDEX = 1;
    private static final int TIME_COL_INDEX = 2;

    private static final int INTENSITY_COL_INDEX = 3;

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

        logCursor = getMoodLogData().getLogCursor();
        startManagingCursor(logCursor);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.log_list_item,
                logCursor,
                LOG_CURSOR_COLS,
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

        if (logCursor != null) {
            stopManagingCursor(logCursor);
            logCursor.close();
            logCursor = null;
        }
        if (data != null) {
            data.close();
            data = null;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu,
                                    View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;
        selectedId = info.id;
        selectedWord = ((TextView) info.targetView.findViewById(
                R.id.log_item_word)).getText().toString();

        contextMenu.setHeaderTitle(selectedWord);
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

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == CONFIRM_DELETE_DIALOG) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(selectedWord)
                    .setMessage(R.string.confirm_delete_entry_msg)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getMoodLogData().deleteLogEntry(selectedId);
                            refreshLog();
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

    private void refreshLog() {
        if (logCursor != null) {
            logCursor.requery();
        }
    }

    private class LogBinder implements SimpleCursorAdapter.ViewBinder {
        private final Date date = new Date();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");

        public boolean setViewValue(View view, Cursor cursor, int column) {
            switch (column) {
                case WORD_COL_INDEX:
                    ((TextView) view).setText(cursor.getString(column));
                    return true;
                case DATE_COL_INDEX:
                    date.setTime(cursor.getLong(column));
                    ((TextView) view).setText(dateFormat.format(date));
                    return true;
                case TIME_COL_INDEX:
                    date.setTime(cursor.getLong(column));
                    ((TextView) view).setText(timeFormat.format(date));
                    return true;
                case INTENSITY_COL_INDEX:
                    int intensity = cursor.getInt(column);
                    ((TextView) view).setTextSize(12f + intensity * 3);
                    return true;
            }
            return false;
        }
    }

    private MoodLogData getMoodLogData() {
        if (data == null) {
            data = new MoodLogData(this);
        }
        return data;
    }

}
