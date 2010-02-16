package com.stuffthathappens.moodlog;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;

import static com.stuffthathappens.moodlog.Constants.*;

/**
 * @author Eric Burke
 */
public class ReportGenerator {
    private static final String STORAGE_PATH = "/sdcard/com.stuffthathappens.moodlog/";
    private static final String HTML_EMAIL_FILE = STORAGE_PATH + "moodlog.html";

    public File generateHtmlEmail(Context context, int numDays)
            throws StorageException {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -numDays);


        MoodLogData data = new MoodLogData(context);
        PrintWriter pw = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        // TODO if numDays is -1 this is broken

        try {
            db = data.getReadableDatabase();
            cursor = db.query(LOG_ENTRIES_TABLE,
                    new String[] { WORD_COL, INTENSITY_COL, ENTERED_ON_COL },
                    ENTERED_ON_COL + " > ?",
                    new String[] { Long.toString(cal.getTimeInMillis()) },
                    null,
                    null,
                    ENTERED_ON_COL + " desc");

            File file = new File(HTML_EMAIL_FILE);
            file.getParentFile().mkdirs();
            pw = new PrintWriter(new FileWriter(file));

            pw.println("<html><head><title>Mood Log Report</title></head><body>");
            pw.println("<h1>Mood Log Report</h1>");

            while (cursor.moveToNext()) {
                
            }



            // TODO this HTML sucks

            pw.println("<html><body><h1>Mood Log</h1></body></html>");

            return file;
        } catch (Exception e) {
            throw new StorageException("Failed to generate HTML", e);
        } finally {
            Utils.close(cursor);
            Utils.close(db);
            Utils.close(pw);
            data.close();
        }
    }
}
