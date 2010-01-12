package com.stuffthathappens.moodlog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.content.Context;

public class DefaultMoodLog implements MoodLog {

	private static final String STORAGE_PATH = "/sdcard/com.stuffthathappens.moodlog/";
	private static final String HTML_EMAIL_FILE = STORAGE_PATH + "moodlog.html";
	private static final String DATA_FILE = STORAGE_PATH + "moodlog.txt";
	private static final String TMP_FILE = STORAGE_PATH + "moodlog.tmp";

	private Set<String> words;
	private List<LogEntry> entries;
	private final ReadWriteLock rwl = new ReentrantReadWriteLock();

	private static final SimpleDateFormat fmt = new SimpleDateFormat(
			"MMddyyyyHHmmss");
	static {
		fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public DefaultMoodLog() {
	}

	public void init() throws StorageException {
		rwl.writeLock().lock();

		BufferedReader br = null;
		try {
			if (isInitialized()) {
				return;
			}

			words = new TreeSet<String>();
			entries = new ArrayList<LogEntry>();

			File dataFile = new File(DATA_FILE);
			if (!dataFile.isFile()) {
				// no data exists
				return;
			}

			br = new BufferedReader(new FileReader(dataFile));
			String nextLine;
			while ((nextLine = br.readLine()) != null) {
				LogEntry entry = parseLogEntry(nextLine);
				if (entry != null) {
					entries.add(entry);
					words.add(entry.getWord());
				}
			}
			Collections.sort(entries);
		} catch (IOException ioe) {
			throw new StorageException("Unable to load data file", ioe);
		} finally {
			try {
				Utils.close(br);
			} finally {
				rwl.writeLock().unlock();
			}
		}
	}

	private static LogEntry parseLogEntry(String line) {
		String trimmed = Utils.trimToNull(line);
		if (trimmed == null) {
			return null;
		}

		int len = trimmed.length();
		int firstComma = trimmed.indexOf(',');
		if (firstComma < 1 || firstComma == len - 1) {
			return null;
		}
		int secondComma = trimmed.indexOf(',', firstComma + 1);
		if (secondComma < 0 || secondComma == len - 1) {
			return null;
		}

		// example line:
		// [size],[time],[word]
		// size = 0..4
		// time = MMddyyyyHHmmss
		// word = anything else
		// 4,03192010170319,Angry

		String sizeStr = Utils.trimToNull(line.substring(0, firstComma));
		String dateStr = Utils.trimToNull(line.substring(firstComma + 1,
				secondComma));
		String word = Utils.trimToNull(line.substring(secondComma + 1));

		if (sizeStr == null || dateStr == null || word == null
				|| dateStr.length() != 14 || sizeStr.length() != 1) {
			return null;
		}

		try {
			Date date = fmt.parse(dateStr);
			int size = Integer.parseInt(sizeStr);
			return new LogEntry(date, word, size);
		} catch (Exception e) {
			return null;
		}
	}

	public boolean isInitialized() {
		rwl.readLock().lock();
		try {
			return words != null;
		} finally {
			rwl.readLock().unlock();
		}
	}

	public List<String> getWords() {
		rwl.readLock().lock();
		try {
			return new ArrayList<String>(words);
		} finally {
			rwl.readLock().unlock();
		}
	}

	public int getLogEntryCount() {
		rwl.readLock().lock();
		try {
			return entries.size();
		} finally {
			rwl.readLock().unlock();
		}
	}

	public LogEntry getLogEntry(int n) {
		rwl.readLock().lock();
		try {
			return entries.get(n);
		} finally {
			rwl.readLock().unlock();
		}
	}

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
	public String findSimilarWord(String newWord) {
		rwl.readLock().lock();
		try {
			for (String word : words) {
				if (word.equalsIgnoreCase(newWord)) {
					return word;
				}
			}
			return newWord;
		} finally {
			rwl.readLock().unlock();
		}
	}

	public boolean logWord(String word, int wordSize) throws StorageException {
		rwl.writeLock().lock();
		try {
			entries.add(new LogEntry(new Date(), word, wordSize));
			boolean newWord = words.add(word);

			saveDataFile();

			return newWord;
		} finally {
			rwl.writeLock().unlock();
		}
	}

	public File generateHtmlEmail(Context context, int numDays)
			throws StorageException {
		PrintWriter pw = null;

		rwl.readLock().lock();
		try {
			File file = new File(HTML_EMAIL_FILE);
			file.getParentFile().mkdirs();

			pw = new PrintWriter(new FileWriter(file));

			// TODO this HTML sucks

			pw.println("<html><body><h1>Mood Log</h1></body></html>");

			return file;
		} catch (Exception e) {
			throw new StorageException("Failed to generate HTML", e);
		} finally {
			try {
				Utils.close(pw);
			} finally {
				rwl.readLock().unlock();
			}
		}
	}

	/**
	 * @return the date of the earliest log entry, or null if the log is empty.
	 */
	public Date getOldestLogEntryDate() {
		rwl.readLock().lock();
		try {
			return (entries.isEmpty()) ? null : entries.get(0).getDate();
		} finally {
			rwl.readLock().unlock();
		}
	}

	private void saveDataFile() throws StorageException {
		File tmpFile = new File(TMP_FILE);
		saveDataTo(tmpFile);
		File dataFile = new File(DATA_FILE);
		Utils.rename(tmpFile, dataFile);
	}

	private void saveDataTo(File f) throws StorageException {
		PrintWriter pw = null;
		try {
			f.getParentFile().mkdirs();
			pw = new PrintWriter(new BufferedWriter(new FileWriter(f)));
			for (LogEntry e : entries) {
				pw.print(e.getSize());
				pw.print(",");
				pw.print(fmt.format(e.getDate()));
				pw.print(",");
				pw.println(e.getWord());
			}
		} catch (IOException ioe) {
			throw new StorageException("Failed to save data file", ioe);
		} finally {
			Utils.close(pw);
		}
	}
}
