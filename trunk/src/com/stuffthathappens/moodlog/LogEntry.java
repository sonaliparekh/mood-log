package com.stuffthathappens.moodlog;

import java.io.Serializable;
import java.util.Date;

public class LogEntry implements Serializable, Comparable<LogEntry> {
    private static final long serialVersionUID = 1L;

    private final Date date;
    private final String word;
    private final int intensity;

    public LogEntry(Date date, String word, int intensity) {
        this.date = date;
        this.word = word;
        this.intensity = intensity;
    }

    public Date getDate() {
        return date;
    }

    public String getWord() {
        return word;
    }

    public int getIntensity() {
        return intensity;
    }

    public int compareTo(LogEntry rhs) {

        int c = date.compareTo(rhs.date);
        if (c == 0) {
            c = word.compareTo(rhs.word);
            if (c == 0) {
                c = Integer.valueOf(intensity).compareTo(rhs.intensity);
            }
        }
        return c;
    }
}
