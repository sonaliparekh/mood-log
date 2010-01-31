package com.stuffthathappens.moodlog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;

public class MailReportActivity extends Activity implements OnClickListener {

    private Button mOkBtn;
    private Button mCancelBtn;
    private RadioButton mLastWeekRadio;
    private RadioButton mLastTwoWeeksRadio;
    private RadioButton mLastFourWeeksRadio;
    private RadioButton mAllTimeRadio;

    private ProgressDialog mProgressDialog;
    private Handler mHandler;
    private ReportGenerator mReportGenerator;

    private static final String TAG = "DateRangeDialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReportGenerator = new ReportGenerator();

        setContentView(R.layout.mail_report);

        mOkBtn = (Button) findViewById(R.id.ok_btn);
        mCancelBtn = (Button) findViewById(R.id.cancel_btn);
        mLastWeekRadio = (RadioButton) findViewById(R.id.last_week_radio);
        mLastTwoWeeksRadio = (RadioButton) findViewById(R.id.last_two_weeks_radio);
        mLastFourWeeksRadio = (RadioButton) findViewById(R.id.last_four_weeks_radio);
        mAllTimeRadio = (RadioButton) findViewById(R.id.all_time_radio);
        mHandler = new Handler();

        mOkBtn.setOnClickListener(this);
        mCancelBtn.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v == mOkBtn) {
            sendMail();
        } else if (v == mCancelBtn) {
            finish();
        }
    }

    private void sendMail() {
        int numDays = -1;
        if (mLastWeekRadio.isChecked()) {
            numDays = 7;
        } else if (mLastTwoWeeksRadio.isChecked()) {
            numDays = 14;
        } else if (mLastFourWeeksRadio.isChecked()) {
            numDays = 28;
        } else if (mAllTimeRadio.isChecked()) {
            numDays = -1;
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getText(R.string.generating_email));
        mProgressDialog.show();

        new Thread(new MailSender(this, numDays)).start();
    }

    private void launchMailer(File htmlEmail) {
        String nowStr = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Mood Log Report for "
                + nowStr);
        emailIntent.setType("text/html");
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(htmlEmail));
        startActivity(emailIntent);
    }

    private void doDismiss() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        finish();
    }

    private class MailSender implements Runnable {
        private final int numDays;
        private final Context context;

        public MailSender(Context context, int numDays) {
            this.context = context;
            this.numDays = numDays;
        }

        public void run() {
            try {
                final File htmlEmail = mReportGenerator.generateHtmlEmail(context,
                        numDays);
                mHandler.post(new Runnable() {
                    public void run() {
                        doDismiss();
                        launchMailer(htmlEmail);
                    }
                });
            } catch (StorageException e) {
                Log.e(TAG, "Failed to generate Email", e);

                mHandler.post(new Runnable() {
                    public void run() {
                        doDismiss();

                        // TODO show an error dialog
                    }
                });
            }
        }
    }
}
