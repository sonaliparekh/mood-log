package com.stuffthathappens.moodlog;

import java.io.Serializable;
import java.util.Date;

public class LogEntry implements Serializable, Comparable<LogEntry> {
    private static final long serialVersionUID = 1L;

    private final Date date;
    private final String word;
    private final int size;

    public LogEntry(Date date, String word, int size) {
        this.date = date;
        this.word = word;
        this.size = size;
    }

    public Date getDate() {
        return date;
    }

    public String getWord() {
        return word;
    }

    public int getSize() {
        return size;
    }

    public int compareTo(LogEntry rhs) {

        int c = date.compareTo(rhs.date);
        if (c == 0) {
            c = word.compareTo(rhs.word);
            if (c == 0) {
                c = Integer.valueOf(size).compareTo(rhs.size);
            }
        }
        return c;
    }
}
