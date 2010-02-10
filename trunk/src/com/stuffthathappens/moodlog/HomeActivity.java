package com.stuffthathappens.moodlog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import static com.stuffthathappens.moodlog.Constants.*;

public class HomeActivity extends ListActivity implements OnClickListener,
        TextWatcher {

    private static final String TAG = "HomeActivity";

    private static final int CONFIRM_DELETE_DIALOG = 1;

    private Button mLogBtn;
    private EditText mWordEntry;

    private MoodLogData mMoodLogData;
    private Cursor mWordsCursor;

    private static final String[] FROM_COLS = {
            WORD_COL, _ID
    };
    private static final int[] TO = {R.id.word_item};
    private static final int WORD_COL_INDEX = 0;

    private static final String ORDER_BY = String.format(
            "upper(%s)", WORD_COL);
    private static final int LOG_WORD_REQ_CD = 1;

    private static final int CONTEXT_MENU_DELETE_ITEM = 1;
    private static final int CONTEXT_MENU_EDIT_ITEM = 2;
    private String mSelectedWord = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");

        setContentView(R.layout.home);

        mLogBtn = (Button) findViewById(R.id.log_btn);
        mWordEntry = (EditText) findViewById(R.id.word_entry);

        final ListView listView = getListView();
        listView.setTextFilterEnabled(true);

        mWordEntry.addTextChangedListener(this);

        mLogBtn.setOnClickListener(this);

        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu contextMenu,
                                            View view,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                AdapterView.AdapterContextMenuInfo info =
                        (AdapterView.AdapterContextMenuInfo) menuInfo;
                mSelectedWord = ((TextView) info.targetView).getText().toString();
                contextMenu.setHeaderTitle(mSelectedWord);
                contextMenu.add(0, CONTEXT_MENU_EDIT_ITEM, 0, R.string.edit);
                contextMenu.add(0, CONTEXT_MENU_DELETE_ITEM, 1, R.string.delete);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()");

        mWordsCursor = getMoodLogData().getReadableDatabase().query(true,
                Constants.LOG_ENTRIES_TABLE, FROM_COLS, null,
                null, WORD_COL, null, ORDER_BY, null);

        // onPause will call deactivate(), onResume will call refreshWordList()
        startManagingCursor(mWordsCursor);
        SimpleCursorAdapter wordListAdapter = new SimpleCursorAdapter(this,
                R.layout.word_list_item,
                mWordsCursor,
                FROM_COLS,
                TO);
        setListAdapter(wordListAdapter);

        // enable type-ahead
        wordListAdapter.setCursorToStringConverter(
                new SimpleCursorAdapter.CursorToStringConverter() {
                    public CharSequence convertToString(Cursor cursor) {
                        return cursor.getString(WORD_COL_INDEX);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");

        updateEnabledStates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop()");

        if (mWordsCursor != null) {
            stopManagingCursor(mWordsCursor);
            mWordsCursor.close();
            mWordsCursor = null;
        }
        if (mMoodLogData != null) {
            mMoodLogData.close();
            mMoodLogData = null;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CONTEXT_MENU_EDIT_ITEM:
                // TODO
                return true;
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
            builder.setTitle(mSelectedWord)
                    .setMessage(R.string.confirm_delete_msg)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            doDeleteWord(mSelectedWord);
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        boolean empty = isLogEmpty();

        menu.findItem(R.id.mail_report_menu_item).setEnabled(!empty);
        menu.findItem(R.id.view_log_menu_item).setEnabled(!empty);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (mWordsCursor != null) {
            mWordsCursor.moveToPosition(position);
            mWordEntry.setText(mWordsCursor.getString(WORD_COL_INDEX));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help_menu_item:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
            case R.id.view_log_menu_item:
                startActivity(new Intent(this, ViewLogActivity.class));
                return true;
            case R.id.mail_report_menu_item:
                startActivity(new Intent(this, MailReportActivity.class));
                return true;
        }
        return false;
    }

    public void onClick(View src) {
        if (src == mLogBtn) {
            String word = getWord();

            if (word != null) {
                Intent intent = new Intent(this, LogWordActivity.class);
                intent.putExtra(EXTRA_WORD, word);
                startActivityForResult(intent, LOG_WORD_REQ_CD);
            } else {
                // this shouldn't happen, but just to be defensive...
                updateEnabledStates();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // note: onActivityResult will be called before this activity is started and
        //       resumed. That's why the mMoodLogData is always accessed via the
        //       getMoodLogData() method
        if (requestCode == LOG_WORD_REQ_CD && resultCode == RESULT_OK) {
            String word = data.getStringExtra(EXTRA_WORD);
            int wordSize = data.getIntExtra(EXTRA_WORD_SIZE,
                    INITIAL_WORD_SIZE);
            mWordEntry.setText(null);

            insertLogEntry(word, wordSize);
            Toast.makeText(this, "Logged " + word, Toast.LENGTH_SHORT).show();
            updateEnabledStates();
        }
    }

    private void insertLogEntry(String word, int wordSize) {
        SQLiteDatabase db = getMoodLogData().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ENTERED_ON_COL, System.currentTimeMillis());
        values.put(WORD_COL, word);
        values.put(WORD_SIZE_COL, wordSize);
        db.insertOrThrow(LOG_ENTRIES_TABLE, null, values);
        refreshWordList();
    }

    // lazy loads the MoodLogData object

    private MoodLogData getMoodLogData() {
        if (mMoodLogData == null) {
            mMoodLogData = new MoodLogData(this);
        }
        return mMoodLogData;
    }

    private void doDeleteWord(String victim) {
        SQLiteDatabase db = getMoodLogData().getWritableDatabase();
        db.delete(LOG_ENTRIES_TABLE, WORD_COL + " = ?", new String[]{victim});
        refreshWordList();
    }

    private void refreshWordList() {
        // the cursor will be null if the activity is stopped. If that's the case,
        // there is no reason to refreshWordList() because that will happen when the activity
        // starts again
        if (mWordsCursor != null) {
            mWordsCursor.requery();
        }
    }

    private String getWord() {
        String word = Utils.trimToNull(mWordEntry.getText().toString());
        if (word != null) {
            word = findSimilarWord(word);
        }
        return word;
    }

    private void updateEnabledStates() {
        mLogBtn.setEnabled(getWord() != null);
    }

    /**
     * Finds an existing word, looking for case-insensitive matches. This
     * ensures we don't add new words that only differ from existing words by
     * capitalization.
     *
     * @param newWord the proposed new word.
     * @return a non-null word, either the newWord or one that already exists
     *         but has a different capitalization.
     */
    private String findSimilarWord(String newWord) {
        SQLiteDatabase db = getMoodLogData().getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(String.format(
                    "select %s from %s where upper(%s) like upper(?)",
                    WORD_COL, LOG_ENTRIES_TABLE, WORD_COL),
                    new String[]{newWord});

            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }

            return newWord;
        } finally {
            close(cursor);
        }
    }

    private boolean isLogEmpty() {
        SQLiteDatabase db = mMoodLogData.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(String.format(
                    "select count(*) from %s", LOG_ENTRIES_TABLE), null);

            cursor.moveToFirst();
            return cursor.getInt(0) == 0;
        } finally {
            close(cursor);
        }
    }

    public void afterTextChanged(Editable src) {
        updateEnabledStates();
    }

    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
    }

    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    private static void close(Cursor c) {
        if (c != null) {
            c.close();
        }
    }
}