package com.stuffthathappens.moodlog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import static com.stuffthathappens.moodlog.Constants.*;

public class HomeActivity extends ListActivity implements OnClickListener,
        TextWatcher {

    private static final String TAG = "HomeActivity";

    private Button logButton;
    private EditText wordEditor;

    private MoodLogData data;
    private Cursor wordsCursor;

    private static final int[] TO = {R.id.word_item};
    private static final int WORD_COL_INDEX = 0;

    private static final int LOG_WORD_REQ_CD = 1;
    private static final int EDIT_WORD_REQ_CD = 2;

    private String selectedWord = null;
    private long selectedWordId = -1L;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");

        setContentView(R.layout.home);

        logButton = (Button) findViewById(R.id.log_btn);
        wordEditor = (EditText) findViewById(R.id.word_entry);

        wordEditor.addTextChangedListener(this);
        logButton.setOnClickListener(this);

        final ListView listView = getListView();
        listView.setTextFilterEnabled(true);
        listView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu,
                                    View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;
        selectedWord = ((TextView) info.targetView).getText().toString();
        selectedWordId = info.id;

        contextMenu.setHeaderTitle(selectedWord);
        contextMenu.add(0, CONTEXT_MENU_EDIT_ITEM, 0, R.string.edit);
        contextMenu.add(0, CONTEXT_MENU_DELETE_ITEM, 1, R.string.delete);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()");

        wordsCursor = getMoodLogData().getWordsCursor();

        // onPause will call deactivate(), onResume will call refreshWordList()
        startManagingCursor(wordsCursor);
        SimpleCursorAdapter wordListAdapter = new SimpleCursorAdapter(this,
                R.layout.word_list_item,
                wordsCursor,
                WORD_CURSOR_COLS,
                TO);
        setListAdapter(wordListAdapter);

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
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop()");

        if (wordsCursor != null) {
            stopManagingCursor(wordsCursor);
            wordsCursor.close();
            wordsCursor = null;
        }
        if (data != null) {
            data.close();
            data = null;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        hideSoftKeyboard();

        switch (item.getItemId()) {
            case CONTEXT_MENU_EDIT_ITEM:
                Intent i = new Intent(this, EditWordActivity.class);
                i.putExtra(EXTRA_WORD, selectedWord);
                startActivityForResult(i, EDIT_WORD_REQ_CD);
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
            builder.setTitle(selectedWord)
                    .setMessage(R.string.confirm_delete_word_msg)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getMoodLogData().deleteWord(selectedWordId);
                            refreshWordList();
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

        boolean empty = getMoodLogData().isLogEmpty();

        menu.findItem(R.id.mail_report_menu_item).setEnabled(!empty);
        menu.findItem(R.id.view_log_menu_item).setEnabled(!empty);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (wordsCursor != null) {
            wordsCursor.moveToPosition(position);
            wordEditor.setText(wordsCursor.getString(WORD_COL_INDEX));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help_menu_item:
                hideSoftKeyboard();
                startActivity(new Intent(this, HelpActivity.class));
                return true;
            case R.id.view_log_menu_item:
                hideSoftKeyboard();
                startActivity(new Intent(this, ViewLogActivity.class));
                return true;
            case R.id.mail_report_menu_item:
                hideSoftKeyboard();
                startActivity(new Intent(this, MailReportActivity.class));
                return true;
        }
        return false;
    }

    public void onClick(View src) {
        if (src == logButton) {
            String word = getTypedWord();

            if (word != null) {
                hideSoftKeyboard();
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
        //       resumed. That's why the data is always accessed via the
        //       getMoodLogData() method
        if (resultCode == RESULT_OK) {
            if (requestCode == LOG_WORD_REQ_CD) {
                String word = data.getStringExtra(EXTRA_WORD);
                int intensity = data.getIntExtra(EXTRA_INTENSITY,
                        INITIAL_INTENSITY);
                wordEditor.setText(null);

                getMoodLogData().insertLogEntry(word, intensity);
                Toast.makeText(this, "Logged " + word, Toast.LENGTH_SHORT).show();
            } else if (requestCode == EDIT_WORD_REQ_CD) {
                String origWord = data.getStringExtra(EXTRA_WORD);
                String updatedWord = data.getStringExtra(EXTRA_UPDATED_WORD);

                getMoodLogData().updateWord(origWord, updatedWord);
                wordEditor.setText(null);
                Toast.makeText(this, "Edited " + updatedWord, Toast.LENGTH_SHORT).show();
            }
            refreshWordList();
            updateEnabledStates();
        }
    }

    // lazy loads the MoodLogData object
    private MoodLogData getMoodLogData() {
        if (data == null) {
            data = new MoodLogData(this);
        }
        return data;
    }

    private void refreshWordList() {
        // the cursor will be null if the activity is stopped. If that's the case,
        // there is no reason to refreshWordList() because that will happen when the activity
        // starts again
        if (wordsCursor != null) {
            wordsCursor.requery();
        }
    }

    private String getTypedWord() {
        String word = Utils.trimToNull(wordEditor.getText().toString());
        if (word != null) {
            word = getMoodLogData().findSimilarWord(word);
        }
        return word;
    }

    private void updateEnabledStates() {
        logButton.setEnabled(getTypedWord() != null);
    }

    public void afterTextChanged(Editable src) {
        updateEnabledStates();
    }

    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
    }

    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    private void hideSoftKeyboard() {
        InputMethodManager mgr = (InputMethodManager)
                getSystemService(INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(wordEditor.getWindowToken(), 0);
    }
}