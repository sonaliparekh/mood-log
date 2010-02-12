package com.stuffthathappens.moodlog;

import android.provider.BaseColumns;

public interface Constants extends BaseColumns {
    /**
     * Intent extra data name, the value is the word the user just selected or wants
     * to save.
     */
    String EXTRA_WORD = "com.stuffthathappens.moodlog.word";
    
    /**
     * Intent extra data name, the value is the word size the user selected.
     */
    String EXTRA_WORD_SIZE = "com.stuffthathappens.moodlog.word_size";
    
    int INITIAL_WORD_SIZE = 2;
    
    /**
     * Word sizes range from 0 to this number, inclusive.
     */
    int MAX_WORD_SIZE = 4;
    
    int DATE_RANGE_DIALOG = 1000;

    String LOG_ENTRIES_TABLE = "log_entries";
    String WORD_SIZE_COL = "word_size";
    String WORD_COL = "word";
    String ENTERED_ON_COL = "entered_on";
    int CONTEXT_MENU_DELETE_ITEM = 1;
    int CONTEXT_MENU_EDIT_ITEM = 2;
    int CONFIRM_DELETE_DIALOG = 1;
}
