package com.stuffthathappens.moodlog;

import android.provider.BaseColumns;

public interface Constants extends BaseColumns {
    /**
     * Intent extra data name, the value is the word the user just selected or wants
     * to save.
     */
    String EXTRA_WORD = "com.stuffthathappens.moodlog.word";

    /**
     * When editing a word, this indicates the intent holds the updated word.
     */
    String EXTRA_UPDATED_WORD = "com.stuffthathappens.moodlog.updated.word";
    
    /**
     * Intent extra data name, the value is the word intensity the user selected.
     */
    String EXTRA_INTENSITY = "com.stuffthathappens.moodlog.intensity";
    
    int INITIAL_INTENSITY = 2;
    
    /**
     * Word sizes range from 0 to this number, inclusive.
     */
    int MAX_INTENSITY = 4;
    
    int DATE_RANGE_DIALOG = 1000;

    String LOG_ENTRIES_TABLE = "log_entries";
    String INTENSITY_COL = "word_size";
    String WORD_COL = "word";
    String ENTERED_ON_COL = "entered_on";
    int CONTEXT_MENU_DELETE_ITEM = 1;
    int CONTEXT_MENU_EDIT_ITEM = 2;
    int CONFIRM_DELETE_DIALOG = 1;
}