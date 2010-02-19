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

    private Button saveBtn;
    private Button cancelBtn;
    private SeekBar seekBar;
    private TextView wordLabel; // shows the word in varying sizes

    private static final float MIN_FONT_SIZE = 10f;
    private static final float MAX_FONT_SIZE = 48f;

    // these are used when touching the screen, to determine the word intensity.
    // Element 0 is at 20% width, 1 at 40% width, etc.
    private float[] xPositions = new float[5];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_word);

        saveBtn = (Button) findViewById(R.id.save_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        wordLabel = (TextView) findViewById(R.id.word_label);

        saveBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);

        seekBar.setProgress(Constants.INITIAL_INTENSITY);
        seekBar.setMax(Constants.MAX_INTENSITY);

        Intent intent = getIntent();
        wordLabel.setText(intent.getExtras().getString(Constants.EXTRA_WORD));
        updateIntensity(Constants.INITIAL_INTENSITY);

        wordLabel.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float x = motionEvent.getX();

                for (int i = 0; i < 5; i++) {
                    if (x < xPositions[i]) {
                        seekBar.setProgress(i);
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
            int totalWidth = wordLabel.getWidth();
            for (int i = 0; i < 5; i++) {
                xPositions[i] = (0.20f * (i + 1)) * totalWidth;
            }
        }
    }

    public void onClick(View src) {
        if (src == saveBtn) {
            Intent intentForCaller = new Intent();
            intentForCaller.putExtra(Constants.EXTRA_WORD, wordLabel.getText()
                    .toString());
            intentForCaller.putExtra(Constants.EXTRA_INTENSITY, seekBar.getProgress());
            setResult(RESULT_OK, intentForCaller);
        } else if (src == cancelBtn) {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
        updateIntensity(progress);
    }

    public void onStartTrackingTouch(SeekBar sb) {
    }

    public void onStopTrackingTouch(SeekBar sb) {
    }

    private void updateIntensity(int intensity) {
        int intensityRange = Constants.MAX_INTENSITY;
        float fontSizeRange = MAX_FONT_SIZE - MIN_FONT_SIZE;
        float ratio = (intensity + 1f) / intensityRange;
        float desiredFontSize = ratio * fontSizeRange + MIN_FONT_SIZE;

        wordLabel.setTextSize(desiredFontSize);
    }
}