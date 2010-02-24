package com.stuffthathappens.moodlog;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Eric Burke
 */
public class ReportGenerator {
    private static final String STORAGE_PATH = "/sdcard/com.stuffthathappens.moodlog/";
    private static final String HTML_EMAIL_FILE = STORAGE_PATH + "moodlog.html";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");
    private final Date reusableDate = new Date();

    /**
     * @param numDays number of days to include, such as 7 for the last week, or -1
     *                for all.
     */
    public File generateHtmlEmail(Context context, int numDays)
            throws StorageException {

        MoodLogData data = new MoodLogData(context);
        PrintWriter pw = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = data.getWritableDatabase();
            cursor = data.getLogCursor(numDays);

            File file = new File(HTML_EMAIL_FILE);
            file.getParentFile().mkdirs();
            pw = new PrintWriter(new FileWriter(file));

            pw.println("<html><head><title>Mood Log Report</title></head><body>");
            pw.println("<h1>Mood Log Report</h1>");

            pw.println("<table cellspacing='0' cellpadding='4' border='1'>");
            pw.println("<tr>");
            pw.println("<th>Date</th><th>Time</th><th>Intensity</th><th>Mood</th>");
            pw.println("</tr>");

            while (cursor.moveToNext()) {
                long curDate = cursor.getLong(cursor.getColumnIndex("entry_date"));
                reusableDate.setTime(curDate);

                int intensity = cursor.getInt(cursor.getColumnIndex(Constants.INTENSITY_COL));
                String word = cursor.getString(cursor.getColumnIndex(Constants.WORD_COL));

                pw.println("<tr valign='top'>");
                pw.format("<td>%s</td>", dateFormat.format(reusableDate));
                pw.format("<td>%s</td>", timeFormat.format(reusableDate));
                pw.format("<td>%d</td>", intensity + 1);
                pw.format("<td>%s</td>", Utils.escapeHtml(word));
                pw.println("</tr>");
            }
            pw.println("</table>");

            pw.println("</body></html>");

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
