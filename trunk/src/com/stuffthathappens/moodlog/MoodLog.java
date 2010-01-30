package com.stuffthathappens.moodlog;

import android.content.Context;

import java.io.File;

public interface MoodLog {
    File generateHtmlEmail(Context context, int numDays) throws StorageException;
}
