package com.stuffthathappens.moodlog;

import java.io.File;
import java.util.Date;
import java.util.List;

import android.content.Context;

public interface MoodLog {

    /**
     * Call this from a thread to ensure the data is loaded or any other expensive 
     * initialization can occur.
     */
    public void init() throws StorageException;
    
    public boolean isInitialized();

    List<String> getWords();

    /**
     * Finds an existing word, looking for case-insensitive matches. This
     * ensures we don't add new words that only differ from existing words by
     * capitalization.
     * 
     * @param newWord
     *            the proposed new word.
     * @return a non-null word, either the newWord or one that already exists
     *         but has a different capitalization.
     */
    String findSimilarWord(String newWord);

    /**
     * @return true if the word is new, false if the word already existed.
     */
    boolean logWord(String word, int wordSize) throws StorageException;
    
    Date getOldestLogEntryDate();

    File generateHtmlEmail(Context context, int numDays) throws StorageException;
    
    int getLogEntryCount();
    LogEntry getLogEntry(int n);
}
