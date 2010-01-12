package com.stuffthathappens.moodlog;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewLogActivity extends ListActivity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_log);
        
        MoodLog ml = ((MoodLogApp) getApplication()).getMoodLog();
        setListAdapter(new LogListAdapter(this, ml));
    }
    
    private class LogListAdapter extends BaseAdapter {
    	
    	private final MoodLog mMoodLog;
    	private final Context mContext;
    	
    	LogListAdapter(Context c, MoodLog ml) {
    		mContext = c;
    		mMoodLog = ml;
    	}

		@Override
		public int getCount() {
			return mMoodLog.getLogEntryCount();
		}

		@Override
		public Object getItem(int position) {
			return mMoodLog.getLogEntry(position);
		}

		@Override
		public long getItemId(int position) {
			return mMoodLog.getLogEntry(position).getDate().getTime();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LogEntryView v;
			if (convertView == null) {
				v = new LogEntryView(mContext, mMoodLog.getLogEntry(position));
			} else {
				v = (LogEntryView) convertView;
				v.setLogEntry(mMoodLog.getLogEntry(position));
			}
			return v;
		}
    }
    
    private class LogEntryView extends LinearLayout {
    	
    	private final TextView mWordView;
    	// TODO fields for date
    	
    	public LogEntryView(Context context, LogEntry logEntry) {
    		super(context);
    		
    		setOrientation(VERTICAL);
    		
    		mWordView = new TextView(context);
    		mWordView.setText(logEntry.getWord());
    		// TODO font size
    		
    		addView(mWordView, new LinearLayout.LayoutParams(
    				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
    	}
    	
    	public void setLogEntry(LogEntry logEntry) {
    		mWordView.setText(logEntry.getWord());
    	}
    }
}
