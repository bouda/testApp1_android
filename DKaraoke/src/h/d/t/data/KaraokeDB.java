package h.d.t.data;

import h.d.t.model.Song;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class KaraokeDB {

	private static final String LOG_TAG = "OfferDB";

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_NAME = "DataDkaraoke";
	private static final int DATABASE_VERSION = 3;

	private static volatile KaraokeDB mInstance;
	private volatile int mCounter = 0;
	private static final Object mLock = new Object();

	public KaraokeDB(Context context) {
		mDbHelper = new DatabaseHelper(context);
	}

	public static synchronized KaraokeDB getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new KaraokeDB(context);
		}
		return mInstance;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		private Context mContext;

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
			String output = mContext.getFilesDir().getParent() + "/databases/"
					+ DATABASE_NAME;
			File db = new File(output);
			if (!db.exists()) {
				copydatabase();
			}
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
		}

		private void copydatabase() {
			try {
				InputStream myinput = mContext.getAssets().open(
						"database/" + DATABASE_NAME);

				String output = mContext.getFilesDir().getParent()
						+ "/databases/" + DATABASE_NAME;
				OutputStream myoutput = new FileOutputStream(output, true);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = myinput.read(buffer)) > 0) {
					myoutput.write(buffer, 0, length);
				}
				myoutput.close();
				myinput.close();
			} catch (FileNotFoundException e) {
				Log.v("aa", e.getMessage());
			} catch (IOException e) {
				Log.v("aa", e.getMessage());
			}

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(LOG_TAG, "Upgrading database from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data");
			String output = mContext.getFilesDir().getParent() + "/databases/"
					+ DATABASE_NAME;
			new File(output).delete();
		}

		private void clear(SQLiteDatabase db) {
		}

	}

	public KaraokeDB open() throws SQLException {
		synchronized (mLock) {
			if (mCounter == 0) {
				mDb = mDbHelper.getWritableDatabase();
			}
			mCounter++;
		}
		return this;
	}

	/**
	 * Đóng kết nối tới database
	 */
	public void close() {
		synchronized (mLock) {
			mCounter--;
			if (mCounter == 0) {
				mDbHelper.close();
			}
		}
	}

	/**
	 * Clear database, use in debug mode
	 */
	public void clear() {
		mDbHelper.clear(mDb);
	}

	public ArrayList<Song> getSongs() {
		open();
		Cursor c = mDb.query("ZSONG", new String[] { "ZIS_LIKE", "ZROWID",
				"ZSABBR", "ZSLANGUAGE", "ZSLYRIC", "ZSMANUFACTURE", "ZSMETA",
				"ZSNAME" }, null, null, null, null, "ZSNAME ASC");
		ArrayList<Song> songs = new ArrayList<Song>();
		try {
			if (c.moveToFirst()) {
				do {
					songs.add(new Song(c));
				} while (c.moveToNext());
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return songs;
	}

	public void updateSong(Song song) {
		ContentValues values = new ContentValues();
		int like = song.isLike ? 1 : 0;
		values.put("ZIS_LIKE", like);

		String where = "ZROWID" + "=" + song.id;
		mDb.update("ZSONG", values, where, null);
	}

}
