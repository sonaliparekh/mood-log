package com.stuffthathappens.moodlog;

import java.text.SimpleDateFormat;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ViewLogActivity extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_log);

		MoodLog ml = ((MoodLogApp) getApplication()).getMoodLog();
		setListAdapter(new LogListAdapter(ml));
	}

	private class LogListAdapter extends BaseAdapter {

		private final MoodLog mMoodLog;
		private final SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

		LogListAdapter(MoodLog ml) {
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
			View v = convertView;

			if (v == null) {
				v = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.log_row, null);
			}

			LogEntry logEntry = mMoodLog.getLogEntry(position);

			TextView dateView = (TextView) v.findViewById(R.id.log_item_date);
			TextView wordView = (TextView) v.findViewById(R.id.log_item_text);

			dateView.setText(fmt.format(logEntry.getDate()));
			wordView.setText(logEntry.getWord());

			float textSize = 12f;
			switch (logEntry.getSize()) {
			case 0:
				textSize = 12f;
				break;
			case 1:
				textSize = 16f;
				break;
			case 2:
				textSize = 22f;
				break;
			case 3:
				textSize = 28f;
				break;
			case 4:
				textSize = 36f;
				break;
			}
			wordView.setTextSize(textSize);
			return v;
		}
	}
}
