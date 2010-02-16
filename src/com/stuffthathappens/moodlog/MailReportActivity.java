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

    private Button sendBtn;
    private Button cancelBtn;
    private RadioButton lastWeekRadio;
    private RadioButton lastTwoWeeksRadio;
    private RadioButton lastFourWeeksRadio;
    private RadioButton allTimeRadio;

    private ProgressDialog progressDialog;
    private Handler handler;
    private ReportGenerator reportGenerator;

    private static final String TAG = "DateRangeDialog";

    private RadioButton[] radios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reportGenerator = new ReportGenerator();

        setContentView(R.layout.mail_report);

        // reusing the save_btn from the save_cancel_button_bar
        sendBtn = (Button) findViewById(R.id.save_btn);
        sendBtn.setText(R.string.send);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        lastWeekRadio = (RadioButton) findViewById(R.id.last_week_radio);
        lastTwoWeeksRadio = (RadioButton) findViewById(R.id.last_two_weeks_radio);
        lastFourWeeksRadio = (RadioButton) findViewById(R.id.last_four_weeks_radio);
        allTimeRadio = (RadioButton) findViewById(R.id.all_time_radio);
        handler = new Handler();

        radios = new RadioButton[] {lastWeekRadio, lastTwoWeeksRadio,
                lastFourWeeksRadio, allTimeRadio};

        for (RadioButton r : radios) {
            r.setOnClickListener(this);
        }

        sendBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v == sendBtn) {
            sendMail();
        } else if (v == cancelBtn) {
            finish();
        } else if (v instanceof RadioButton) {
            for (RadioButton r : radios) {
                if (r != v) {
                    r.setChecked(false);
                }
            }
        }
    }

    private void sendMail() {
        int numDays = -1;
        if (lastWeekRadio.isChecked()) {
            numDays = 7;
        } else if (lastTwoWeeksRadio.isChecked()) {
            numDays = 14;
        } else if (lastFourWeeksRadio.isChecked()) {
            numDays = 28;
        } else if (allTimeRadio.isChecked()) {
            numDays = -1;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getText(R.string.generating_email));
        progressDialog.show();

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
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
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
                final File htmlEmail = reportGenerator.generateHtmlEmail(context,
                        numDays);
                handler.post(new Runnable() {
                    public void run() {
                        doDismiss();
                        launchMailer(htmlEmail);
                    }
                });
            } catch (StorageException e) {
                Log.e(TAG, "Failed to generate Email", e);

                handler.post(new Runnable() {
                    public void run() {
                        doDismiss();

                        // TODO show an error dialog
                    }
                });
            }
        }
    }
}
