package com.stuffthathappens.moodlog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class LogWordActivity extends Activity implements OnClickListener,
        OnSeekBarChangeListener {

    private Button mSaveBtn;
    private Button mCancelBtn;
    private SeekBar mSeekBar;
    private TextView mWordLabel; // shows the word in varying sizes

    private static final float MIN_FONT_SIZE = 10f;
    private static final float MAX_FONT_SIZE = 48f;

    // these are used when touching the screen, to determine the word intensity.
    // Element 0 is at 20% width, 1 at 40% width, etc.
    private float[] mXPositions = new float[5];
    private int width;

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

        mSeekBar.setProgress(Constants.INITIAL_INTENSITY);
        mSeekBar.setMax(Constants.MAX_INTENSITY);

        Intent intent = getIntent();
        mWordLabel.setText(intent.getExtras().getString(Constants.EXTRA_WORD));
        updateWordSize(Constants.INITIAL_INTENSITY);

        mWordLabel.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float x = motionEvent.getX();

                for (int i = 0; i < 5; i++) {
                    if (x < mXPositions[i]) {
                        mSeekBar.setProgress(i);
                        return true;
                    }
                }

                return false;
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            int totalWidth = mWordLabel.getWidth();
            for (int i = 0; i < 5; i++) {
                mXPositions[i] = (0.20f * (i + 1)) * totalWidth;
            }
        }
    }

    public void onClick(View src) {
        if (src == mSaveBtn) {
            Intent intentForCaller = new Intent();
            intentForCaller.putExtra(Constants.EXTRA_WORD, mWordLabel.getText()
                    .toString());
            intentForCaller.putExtra(Constants.EXTRA_INTENSITY, mSeekBar.getProgress());
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
        int wordSizeRange = Constants.MAX_INTENSITY;
        float fontSizeRange = MAX_FONT_SIZE - MIN_FONT_SIZE;
        float ratio = (size + 1f) / wordSizeRange;
        float desiredFontSize = ratio * fontSizeRange + MIN_FONT_SIZE;

        mWordLabel.setTextSize(desiredFontSize);
    }
}