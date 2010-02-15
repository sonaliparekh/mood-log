package com.stuffthathappens.moodlog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static com.stuffthathappens.moodlog.Constants.EXTRA_UPDATED_WORD;
import static com.stuffthathappens.moodlog.Constants.EXTRA_WORD;

/**
 * @author Eric Burke
 */
public class EditWordActivity extends Activity {
    private Button mSaveBtn;
    private Button mCancelBtn;

    private String mOrigWord;
    private EditText mUpdatedWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_word);

        mSaveBtn = (Button) findViewById(R.id.save_btn);
        mCancelBtn = (Button) findViewById(R.id.cancel_btn);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                saveClicked();
            }
        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                hideSoftKeyboard();
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        Intent intent = getIntent();
        mOrigWord = intent.getExtras().getString(EXTRA_WORD);

        ((TextView) findViewById(R.id.original_word)).setText(mOrigWord);
        mUpdatedWord = (EditText) findViewById(R.id.updated_word);
        mUpdatedWord.setText(mOrigWord);

        mUpdatedWord.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void afterTextChanged(Editable editable) {
                updateEnabledStates();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateEnabledStates();
    }

    private void saveClicked() {
        hideSoftKeyboard();

        // TODO verify the word is not a duplicate

        Intent i = new Intent();
        i.putExtra(EXTRA_WORD, mOrigWord);
        i.putExtra(EXTRA_UPDATED_WORD, getUpdatedWord());
        setResult(RESULT_OK, i);
        finish();
    }

    /**
     * @return the proposed word, with whitespace trimmed out.
     */
    private String getUpdatedWord() {
        return mUpdatedWord.getText().toString().trim();
    }

    private void updateEnabledStates() {
        String updated = getUpdatedWord();
        mSaveBtn.setEnabled(updated.length() > 0 &&
                !updated.equals(mOrigWord));
    }

    private void hideSoftKeyboard() {
        InputMethodManager mgr = (InputMethodManager)
                getSystemService(INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(mUpdatedWord.getWindowToken(), 0);
    }
}
