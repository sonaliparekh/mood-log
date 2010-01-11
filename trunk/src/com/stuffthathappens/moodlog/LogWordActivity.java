package com.stuffthathappens.moodlog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class LogWordActivity extends Activity implements OnClickListener,
        OnSeekBarChangeListener {

    private Button mSaveBtn;
    private Button mCancelBtn;
    private SeekBar mSeekBar;
    private TextView mWordLabel; // shows the word in varying sizes
    
    private static final float MIN_FONT_SIZE = 10f;
    private static final float MAX_FONT_SIZE = 48f;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_word);

        mSaveBtn = (Button) findViewById(R.id.save_btn);
        mCancelBtn = (Button) findViewById(R.id.cancel_btn);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mWordLabel = (TextView) findViewById(R.id.word_label);

        mSaveBtn.setOnClickListener(this);
        mCancelBtn.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);

        mSeekBar.setProgress(Constants.INITIAL_WORD_SIZE);
        mSeekBar.setMax(Constants.MAX_WORD_SIZE);

        Intent intent = getIntent();
        mWordLabel.setText(intent.getExtras().getString(Constants.EXTRA_WORD));
        updateWordSize(Constants.INITIAL_WORD_SIZE);
    }

    public void onClick(View src) {
        if (src == mSaveBtn) {
            Intent intentForCaller = new Intent();
            intentForCaller.putExtra(Constants.EXTRA_WORD, mWordLabel.getText()
                    .toString());
            intentForCaller.putExtra(Constants.EXTRA_WORD_SIZE, mSeekBar.getProgress());
            setResult(RESULT_OK, intentForCaller);
        } else if (src == mCancelBtn) {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
        updateWordSize(progress);
    }

    public void onStartTrackingTouch(SeekBar sb) {
    }

    public void onStopTrackingTouch(SeekBar sb) {
    }

    private void updateWordSize(int size) {
        int wordSizeRange = Constants.MAX_WORD_SIZE;
        float fontSizeRange = MAX_FONT_SIZE - MIN_FONT_SIZE;        
        float ratio = (size + 1f) / wordSizeRange;
        float desiredFontSize = ratio * fontSizeRange + MIN_FONT_SIZE;
        
        mWordLabel.setTextSize(desiredFontSize);
    }
}