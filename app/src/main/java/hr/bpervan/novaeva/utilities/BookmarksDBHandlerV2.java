package hr.bpervan.novaeva.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Html;

import java.util.ArrayList;
import java.util.List;

import hr.bpervan.novaeva.model.ContentData;

/**
 * @author Branimir
 * @version 2.0
 *
 */
public class BookmarksDBHandlerV2 extends SQLiteOpenHelper {
	
	private static final int DB_VERSION = 1;
	
	private static final String DB_NAME= "novaeva.db";
	private static final String TABLE_BOOKMARKS = "bookmarks";
	private static final String KEY_NID = "nid";
	private static final String KEY_UVOD = "uvod";
	private static final String KEY_DATUM = "datum";
	private static final String KEY_NASLOV = "naslov";
	private static final String KEY_CID = "cid";
	private static final String KEY_HAS_AUDIO = "hasaudio";
	private static final String KEY_HAS_ATTACH = "hasattach";
	private static final String KEY_HAS_LINK = "haslink";
	private static final String KEY_VRSTA  = "vrsta";
	
	private static final String DB_CREATE = "create table " + TABLE_BOOKMARKS + "(" + 
			KEY_NID + " integer primary key, " + 
			KEY_CID + " integer, " + 
			KEY_VRSTA + " text, " + 
			KEY_HAS_AUDIO + " integer, " +
			KEY_HAS_ATTACH + " integer, " + 
			KEY_HAS_LINK + " integer, " + 
			KEY_NASLOV + " text, " + 
			KEY_DATUM + " text, " + 
			KEY_UVOD + " text);";

	public BookmarksDBHandlerV2(Context context){
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		sqLiteDatabase.execSQL(DB_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
		sqLiteDatabase.execSQL("drop table if exists " + TABLE_BOOKMARKS);
		onCreate(sqLiteDatabase);
	}
	
	public List<ListElement> getAllVijest(){
		List<ListElement> listElements = new ArrayList<ListElement>();
		SQLiteDatabase db = this.getReadableDatabase();		
		Cursor c = db.query(TABLE_BOOKMARKS, new String[]{KEY_NID, KEY_CID, KEY_HAS_AUDIO, KEY_HAS_ATTACH, KEY_HAS_LINK, KEY_NASLOV, KEY_DATUM, KEY_UVOD}, 
				null, null, null, null, null);
		
		c.moveToFirst();
		while(!c.isAfterLast()){
			ListElement listElement = new ListElement(c.getString(c.getColumnIndex(KEY_UVOD)), c.getString(c.getColumnIndex(KEY_NASLOV)), c.getString(c.getColumnIndex(KEY_DATUM)),
					c.getInt(c.getColumnIndex(KEY_NID)), c.getInt(c.getColumnIndex(KEY_CID)), ListTypes.VIJEST,
					c.getInt(c.getColumnIndex(KEY_HAS_LINK)) == 1,
					c.getInt(c.getColumnIndex(KEY_HAS_ATTACH)) == 1,
					c.getInt(c.getColumnIndex(KEY_HAS_AUDIO)) == 1,
					false, false);

			listElements.add(listElement);
			c.moveToNext();
		}
		
		c.close();
		db.close();
		return listElements;
	}
	
	public boolean nidExists(int nid){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.query(TABLE_BOOKMARKS, new String[]{KEY_NID}, KEY_NID + " = ? AND " + KEY_VRSTA + " = ?", new String[]{nid+ "", BookmarkTypes.VIJEST.toString()}, null, null, null);
		int count = c.getCount();
		c.close();
		db.close();
		return count == 1;
	}

	public boolean insertArticle(ContentData contentData){
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_NID, contentData.getNid());
		values.put(KEY_CID, contentData.getCid());
		values.put(KEY_HAS_AUDIO, contentData.getAudio() != null ? 1 : 0);
		values.put(KEY_HAS_ATTACH, contentData.getPrilozi() != null ? 1 : 0);
		values.put(KEY_HAS_LINK, contentData.getYoutube() != null ? 1 : 0);
		values.put(KEY_NASLOV, contentData.getNaslov());
		values.put(KEY_DATUM, contentData.getTime());
		values.put(KEY_UVOD, Html.fromHtml(contentData.getTekst()).toString());

		db.insert(TABLE_BOOKMARKS, null, values);
		db.close();
		return true;
	}
	
	public boolean deleteVijest(int nid){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_BOOKMARKS, KEY_NID + " = ? AND " + KEY_VRSTA + " = ?", new String[] {nid + "", BookmarkTypes.VIJEST.toString()});
		db.close();
		return true;
	}
}
