package h.d.t.model;

import android.database.Cursor;

public class Song {
	public String id;
	public String name;
	public String lyric;
	public String author;
	public int manufacture;
	public String tag;
	public boolean isLike;
	public String abbr;
	public int language;

	public Song(Cursor c) {
		int a = c.getInt(c.getColumnIndex("ZIS_LIKE"));
		isLike = a > 0;
		id = c.getString(c.getColumnIndex("ZROWID"));
		abbr = c.getString(c.getColumnIndex("ZSABBR"));
		String lang = c.getString(c.getColumnIndex("ZSLANGUAGE"));
		language = lang.equals("en") ? 1 : 0;
		lyric = c.getString(c.getColumnIndex("ZSLYRIC"));
		String temp = c.getString(c.getColumnIndex("ZSMANUFACTURE"));
		manufacture = Integer.valueOf(temp);
		author = c.getString(c.getColumnIndex("ZSMETA"));
		name = c.getString(c.getColumnIndex("ZSNAME"));

	}

}
