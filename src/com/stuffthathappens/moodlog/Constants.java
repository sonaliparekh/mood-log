package com.stuffthathappens.moodlog;

public interface Constants {
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
}
