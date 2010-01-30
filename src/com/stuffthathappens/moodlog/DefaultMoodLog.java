package com.stuffthathappens.moodlog;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DefaultMoodLog implements MoodLog {

    private static final String STORAGE_PATH = "/sdcard/com.stuffthathappens.moodlog/";
    private static final String HTML_EMAIL_FILE = STORAGE_PATH + "moodlog.html";

    private final ReadWriteLock rwl = new ReentrantReadWriteLock();

    private static final SimpleDateFormat fmt = new SimpleDateFormat(
            "MMddyyyyHHmmss");

    static {
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public DefaultMoodLog() {
    }


    public File generateHtmlEmail(Context context, int numDays)
            throws StorageException {
        PrintWriter pw = null;

        rwl.readLock().lock();
        try {
            File file = new File(HTML_EMAIL_FILE);
            file.getParentFile().mkdirs();

            pw = new PrintWriter(new FileWriter(file));

            // TODO this HTML sucks

            pw.println("<html><body><h1>Mood Log</h1></body></html>");

            return file;
        } catch (Exception e) {
            throw new StorageException("Failed to generate HTML", e);
        } finally {
            try {
                Utils.close(pw);
            } finally {
                rwl.readLock().unlock();
            }
        }
    }

}
