package com.stuffthathappens.moodlog;

import android.app.ListActivity;
import android.os.Bundle;

public class ViewLogActivity extends ListActivity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_log);
    }
    
}
