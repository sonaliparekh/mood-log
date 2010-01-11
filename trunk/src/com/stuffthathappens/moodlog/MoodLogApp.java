package com.stuffthathappens.moodlog;

import android.app.Application;

public class MoodLogApp extends Application {
    private final MoodLog moodLog = new DefaultMoodLog();
    
    public MoodLog getMoodLog() {
        return moodLog;
    }
}
