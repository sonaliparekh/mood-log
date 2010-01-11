package com.stuffthathappens.moodlog;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class HomeActivity extends ListActivity implements OnClickListener,
        TextWatcher {

    private Button mLogBtn;
    private AutoCompleteTextView mWordEntry;
    private ListView mWordList;
    private MoodLog mMoodLog;
    private Handler mHandler;
    private Loader mLoader;
    private Saver mSaver;
    private ArrayAdapter<String> mWordListAdapter;
    private ArrayAdapter<String> mWordEntryAdapter;

    private static final int LOG_WORD_REQ_CD = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        mLogBtn = (Button) findViewById(R.id.log_btn);
        mWordEntry = (AutoCompleteTextView) findViewById(R.id.word_entry);

        mWordList = getListView();
        mWordEntry.addTextChangedListener(this);

        mLogBtn.setOnClickListener(this);

        mMoodLog = ((MoodLogApp) getApplication()).getMoodLog();

        mHandler = new Handler();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        boolean empty = mMoodLog.getOldestLogEntryDate() == null;

        menu.findItem(R.id.mail_report_menu_item).setEnabled(!empty);
        menu.findItem(R.id.view_log_menu_item).setEnabled(!empty);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        mWordEntry.setText(mWordListAdapter.getItem(position));
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

    @Override
    protected void onPause() {
        // if a data load thread is in progress, cancel it
        mLoader = null;
        mSaver = null;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mMoodLog.isInitialized()) {
            // load data in a background thread
            mLoader = new Loader();
            new Thread(mLoader).start();
        } else if (mWordEntryAdapter == null) {
            rebuildAdapters();
        }
        updateEnabledStates();
    }

    public void onClick(View src) {
        if (src == mLogBtn) {
            String word = getWord();

            if (word != null) {
                Intent intent = new Intent(this, LogWordActivity.class);
                intent.putExtra(Constants.EXTRA_WORD, word);
                startActivityForResult(intent, LOG_WORD_REQ_CD);
            } else {
                // this shouldn't happen, but just to be defensive...
                updateEnabledStates();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOG_WORD_REQ_CD && resultCode == RESULT_OK) {
            String word = data.getStringExtra(Constants.EXTRA_WORD);
            int wordSize = data.getIntExtra(Constants.EXTRA_WORD_SIZE,
                    Constants.INITIAL_WORD_SIZE);
            mWordEntry.setText(null);

            mSaver = new Saver(word, wordSize, mMoodLog);
            new Thread(mSaver).start();
            updateEnabledStates();
        }
    }

    private void afterWordLogged(String word, boolean newWord) {
        Toast.makeText(this, "Logged " + word, Toast.LENGTH_SHORT).show();
        if (newWord) {
            rebuildAdapters();
        }
    }

    private String getWord() {
        String word = Utils.trimToNull(mWordEntry.getText().toString());
        if (word != null) {
            word = mMoodLog.findSimilarWord(word);
        }
        return word;
    }

    private void updateEnabledStates() {
        mLogBtn.setEnabled(mSaver == null && getWord() != null);
    }

    private void rebuildAdapters() {
        List<String> words = mMoodLog.getWords();
        mWordListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, words);
        mWordEntryAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, words);

        mWordList.setAdapter(mWordListAdapter);
        mWordEntry.setAdapter(mWordEntryAdapter);

        mWordList.setTextFilterEnabled(true);

        updateEnabledStates();
    }

    public void afterTextChanged(Editable src) {
        updateEnabledStates();
    }

    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
            int arg3) {
    }

    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    // load in a thread because the DefaultMoodLog constructor might take some
    // time to load from the file system
    private class Loader implements Runnable {
        public void run() {
            mHandler.post(new Runnable() {
                public void run() {
                    // ignore the results if the activity was paused and the
                    // loader no longer matches this thread
                    if (mLoader == Loader.this) {
                        try {
                            mMoodLog.init();
                            rebuildAdapters();
                        } catch (StorageException e) {
                            // TODO show an exception dialog
                            e.printStackTrace();
                        } finally {
                            mLoader = null;
                        }
                    }
                }
            });
        }
    }

    private class Saver implements Runnable {
        private final String word;
        private final int wordSize;
        private final MoodLog ml;

        public Saver(String word, int wordSize, MoodLog ml) {
            this.word = word;
            this.wordSize = wordSize;
            this.ml = ml;
        }

        public void run() {
            try {
                final boolean changed = ml.logWord(word, wordSize);

                mHandler.post(new Runnable() {
                    public void run() {
                        if (mSaver == Saver.this) {
                            try {
                                afterWordLogged(word, changed);
                            } finally {
                                mSaver = null;
                            }
                        }
                    }
                });
            } catch (StorageException se) {
                // TODO show an error dialog, log this

                mHandler.post(new Runnable() {
                    public void run() {
                        if (mSaver == Saver.this) {
                            mSaver = null;
                            updateEnabledStates();
                        }
                    }
                });
            }
        }
    }
}