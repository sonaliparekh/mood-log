package com.stuffthathappens.moodlog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.stuffthathappens.moodlog.Constants.*;

/**
 * @author Eric Burke
 */
public class MoodLogData extends SQLiteOpenHelper {

    private static final String DB_NAME = "moodlog.db";
    private static final int DB_VERSION = 2;

    public MoodLogData(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("" +
                "create table " + WORD_REF_TABLE + " (" +
                _ID + " integer primary key autoincrement, " +
                WORD_COL + " text not null);");

        db.execSQL("" +
                "create table " + LOG_ENTRIES_TABLE + " (" +
                _ID + " integer primary key autoincrement, " +
                WORD_ID_COL + " integer not null, " +
                ENTERED_ON_COL + " integer not null, " +
                INTENSITY_COL + " integer not null, " +
                "foreign key (" + WORD_ID_COL + ") references " +
                WORD_REF_TABLE + "(" + _ID + ")" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + LOG_ENTRIES_TABLE);
        db.execSQL("drop table if exists " + WORD_REF_TABLE);
        onCreate(db);
    }

    /**
     * Renames a word. All log entries using the original word will now use the new word.
     * If the proposed new name exists, log entries from the original word are merged
     * to use the updated word's identifier.
     */
    public void updateWord(long origWordId, String newWord) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // see if the proposed new word already exists
            long existingWordId = findWordId(db, newWord);
            ContentValues values = new ContentValues();
            if (existingWordId == -1L || existingWordId == origWordId) {
                // the new word name is not taken, so a simple update is possible
                values.put(WORD_COL, newWord);
                db.update(WORD_REF_TABLE, values, _ID + " = " + origWordId, null);
            } else {
                // the proposed name is already taken, so merge existing log entries
                // using the orig word to use the ID of the new word.
                values.put(WORD_ID_COL, existingWordId);
                db.update(LOG_ENTRIES_TABLE, values, WORD_ID_COL + " = " + origWordId, null);

                // delete the original word
                db.delete(WORD_REF_TABLE, _ID + " = " + origWordId, null);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

    }

    /**
     * \     * @return the newly added log entry ID.
     */
    public long insertLogEntry(String word, int intensity) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {

            // make sure the word exists in the reference table
            long wordId = findWordId(db, word);
            if (wordId == -1) {
                wordId = insertWord(db, word);
            }

            // now insert the log entry
            ContentValues values = new ContentValues();
            values.put(ENTERED_ON_COL, System.currentTimeMillis());
            values.put(WORD_ID_COL, wordId);
            values.put(INTENSITY_COL, intensity);
            long logEntryId = db.insertOrThrow(LOG_ENTRIES_TABLE, null, values);
            db.setTransactionSuccessful();
            return logEntryId;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Finds an existing word, looking for case-insensitive matches. This
     * ensures we don't add new words that only differ from existing words by
     * capitalization.
     *
     * @param newWord the proposed new word.
     * @return a non-null word, either the newWord or one that already exists
     *         but has a different capitalization.
     */
    public String findSimilarWord(String newWord) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(String.format(
                    "select %s from %s where upper(%s) like upper(?)",
                    WORD_COL, WORD_REF_TABLE, WORD_COL),
                    new String[]{newWord});

            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }

            return newWord;
        } finally {
            Utils.close(cursor);
        }
    }

    public void deleteWord(long wordId) {
        SQLiteDatabase db = getWritableDatabase();
        // delete all log entries first, and then the word
        db.delete(LOG_ENTRIES_TABLE, WORD_ID_COL + " = " + wordId, null);
        db.delete(WORD_REF_TABLE, _ID + " = " + wordId, null);
    }

    public boolean isLogEmpty() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(String.format(
                    "select count(*) from %s", LOG_ENTRIES_TABLE), null);

            cursor.moveToFirst();
            return cursor.getInt(0) == 0;
        } finally {
            Utils.close(cursor);
        }
    }


    /**
     * @return a cursor over all words, in case-insensitive ascending order.
     * @see Constants#WORD_CURSOR_COLS
     */
    public Cursor getWordsCursor() {
        return getWritableDatabase().query(false,
                WORD_REF_TABLE,
                WORD_CURSOR_COLS,
                null, null, null, null,
                "upper(" + WORD_COL + ")",
                null);
    }

    /**
     * @return a cursor over all log entries, newest entries come first.
     * @see Constants#LOG_CURSOR_COLS
     */
    public Cursor getLogCursor() {
        String sql = String.format("" +
                "select %s, %s as entry_date, %s as entry_time, %s, " +
                "%s.%s from %s, %s " +
                "where %s.%s = %s.%s " +
                "order by %s desc",
                WORD_COL, ENTERED_ON_COL, ENTERED_ON_COL, INTENSITY_COL,
                LOG_ENTRIES_TABLE, _ID, LOG_ENTRIES_TABLE, WORD_REF_TABLE,
                LOG_ENTRIES_TABLE, WORD_ID_COL, WORD_REF_TABLE, _ID,
                ENTERED_ON_COL);

        return getWritableDatabase().rawQuery(sql, null);
    }

    public void deleteLogEntry(long logEntryId) {
        SQLiteDatabase db = getWritableDatabase();

        long wordId = findWordIdForLogEntryId(db, logEntryId);

        db.delete(LOG_ENTRIES_TABLE, _ID + " = " +
                logEntryId, null);

        // no more log entries for this word, so delete the word
        if (getWordUsageCount(db, wordId) == 0) {
            deleteWord(wordId);
        }
    }

    private int getWordUsageCount(SQLiteDatabase db, long wordId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(String.format(
                    "select count(*) from %s where %s = %d",
                    LOG_ENTRIES_TABLE, WORD_ID_COL, wordId), null);

            cursor.moveToFirst();
            return cursor.getInt(0);
        } finally {
            Utils.close(cursor);
        }
    }

    /**
     * Only call this if you know the word doesn't already exist.
     *
     * @return the id of the newly inserted word.
     */
    private long insertWord(SQLiteDatabase db, String word) {
        ContentValues values = new ContentValues();
        values.put(WORD_COL, word);
        return db.insertOrThrow(WORD_REF_TABLE, null, values);
    }

    private long findWordIdForLogEntryId(SQLiteDatabase db, long logEntryId) {
        Cursor c = null;
        try {
            String sql = String.format("select %s.%s " +
                    "  from %s, %s" +
                    " where %s.%s = %s.%s" +
                    "   and %s.%s = %d",
                    WORD_REF_TABLE, _ID,
                    WORD_REF_TABLE, LOG_ENTRIES_TABLE,
                    WORD_REF_TABLE, _ID,
                    LOG_ENTRIES_TABLE, WORD_ID_COL,
                    LOG_ENTRIES_TABLE, _ID,
                    logEntryId);

            c = db.rawQuery(sql, null);
            return (c.moveToFirst()) ? c.getLong(0) : -1L;
        } finally {
            Utils.close(c);
        }
    }

    /**
     * Find a word in the reference table, performing case-insensitive comparison.
     *
     * @param db an open database.
     * @param word the word to find, case insensitive.
     * @return the id of the matching word, or -1 if not found.
     */
    private long findWordId(SQLiteDatabase db, String word) {
        Cursor c = null;
        try {
            c = db.query(WORD_REF_TABLE,
                    new String[]{_ID},
                    "upper(" + WORD_COL + ") = ?",
                    new String[]{word.toUpperCase()},
                    null,
                    null,
                    null);
            return (c.moveToFirst()) ? c.getLong(0) : -1L;
        } finally {
            Utils.close(c);
        }
    }
}
