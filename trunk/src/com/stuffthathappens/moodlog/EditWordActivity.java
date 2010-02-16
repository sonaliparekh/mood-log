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
    private Button saveBtn;

    private String origWord;
    private EditText updatedWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_word);

        saveBtn = (Button) findViewById(R.id.save_btn);
        Button cancelBtn = (Button) findViewById(R.id.cancel_btn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                saveClicked();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                hideSoftKeyboard();
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        Intent intent = getIntent();
        origWord = intent.getExtras().getString(EXTRA_WORD);

        ((TextView) findViewById(R.id.original_word)).setText(origWord);
        updatedWord = (EditText) findViewById(R.id.updated_word);
        updatedWord.setText(origWord);

        updatedWord.addTextChangedListener(new TextWatcher() {
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

        Intent i = new Intent();
        i.putExtra(EXTRA_WORD, origWord);
        i.putExtra(EXTRA_UPDATED_WORD, getUpdatedWord());
        setResult(RESULT_OK, i);
        finish();
    }

    /**
     * @return the proposed word, with whitespace trimmed out.
     */
    private String getUpdatedWord() {
        return updatedWord.getText().toString().trim();
    }

    private void updateEnabledStates() {
        String updated = getUpdatedWord();
        saveBtn.setEnabled(updated.length() > 0 &&
                !updated.equals(origWord));
    }

    private void hideSoftKeyboard() {
        InputMethodManager mgr = (InputMethodManager)
                getSystemService(INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(updatedWord.getWindowToken(), 0);
    }
}
