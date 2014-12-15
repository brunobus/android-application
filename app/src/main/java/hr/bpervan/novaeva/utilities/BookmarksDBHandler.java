package hr.bpervan.novaeva.utilities;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BookmarksDBHandler extends SQLiteOpenHelper {
	
	private static final int DB_VERSION = 1;
	
	private static final String DB_NAME= "novaeva.db";
	private static final String TABLE_BOOKMARKS = "bookmarks";
	private static final String KEY_NID = "nid";
	private static final String KEY_UVOD = "uvod";
	private static final String KEY_DATUM = "datum";
	private static final String KEY_NASLOV = "naslov";
	private static final String KEY_CID = "cid";
	private static final String KEY_VRSTA  = "vrsta";
	
	private static final String HAS_AUDIO = "audio";
	private static final String HAS_DOCUMENTS = "documents";
	private static final String HAS_VIDEO = "video";
	
	private static final String DB_CREATE = "create table " + TABLE_BOOKMARKS + "(" + KEY_NID + " integer primary key, " + 
			KEY_CID + " integer, " + 
			KEY_VRSTA + " text, " + 
			KEY_NASLOV + " text, " + 
			KEY_DATUM + " text, " + 
			HAS_AUDIO + " text, " + 
			HAS_DOCUMENTS + " text, " + 
			HAS_VIDEO + " text, " + 
			KEY_UVOD + " text);";
	

	public BookmarksDBHandler(Context context){
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(BookmarksDBHandler.class.getName(), "Sa" + oldVersion + "na" + newVersion);
		db.execSQL("drop table if exists " + TABLE_BOOKMARKS);
		onCreate(db);
	}
	
	public int getNidCount(){
		String countQuery = "select * from " + TABLE_BOOKMARKS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		db.close();
		return count;
	}
	
	public boolean insertNid(Vijest vijest){
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_NID, vijest.getNid());
		values.put(KEY_CID, vijest.getKategorija());
		values.put(KEY_NASLOV, vijest.getNaslov());
		values.put(KEY_UVOD, vijest.getUvod());
		values.put(KEY_DATUM, vijest.getDatum());
		values.put(KEY_VRSTA, vijest.getVrstaZaBookmark().toString());
		
		values.put(HAS_AUDIO, vijest.hasAudio());
		values.put(HAS_DOCUMENTS, vijest.hasAttach());
		values.put(HAS_VIDEO, vijest.hasLink());
		
		Log.d(HAS_AUDIO, vijest.hasAudio()+"");
		Log.d(HAS_DOCUMENTS, vijest.hasAttach()+"");
		Log.d(HAS_VIDEO, vijest.hasLink()+"");
				
		db.insert(TABLE_BOOKMARKS, null, values);
		db.close();
		return true;
	}
	//provjeri dobro ovu metodu
	public boolean nidExists(int nid){
		SQLiteDatabase db = this.getReadableDatabase();
		String q = "select * from " + TABLE_BOOKMARKS + " where " + KEY_NID + " = " + nid;
		Cursor cursor = db.rawQuery(q, null);
		int count = cursor.getCount();
		cursor.close();
		db.close();
		
		if(count == 1)
			return true;
		else
			return false;
		
	}

	public List<ListElement> getAllNids(){
		List<ListElement> listaVijesti = new ArrayList<ListElement>();
		
		String selectQuery = "select * from " + TABLE_BOOKMARKS;
		
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		int count = cursor.getCount();
		if(count == 0){
			cursor.close();
			db.close();
			return listaVijesti;
		}
		cursor.moveToFirst();
		while(cursor.isAfterLast() == false){
			int nid = cursor.getInt(cursor.getColumnIndex(KEY_NID));
			String datum = cursor.getString(cursor.getColumnIndex(KEY_DATUM));
			String uvod = cursor.getString(cursor.getColumnIndex(KEY_UVOD));
			String naslov = cursor.getString(cursor.getColumnIndex(KEY_NASLOV));
			boolean hasAudio = Boolean.valueOf(cursor.getString(cursor.getColumnIndex(HAS_AUDIO)));
			boolean hasVideo = Boolean.valueOf(cursor.getString(cursor.getColumnIndex(HAS_VIDEO)));
			boolean hasDocuments = Boolean.valueOf(cursor.getString(cursor.getColumnIndex(HAS_DOCUMENTS)));
			
			ListElement listElement = new ListElement();
			listElement.setKategorija(cursor.getInt(cursor.getColumnIndex(KEY_CID)));
			listElement.setNid(nid);
			listElement.setUnixDatum(datum);
			listElement.setNaslov(naslov);
			listElement.setUvod(uvod);
			
			if(hasAudio)
				listElement.itHasMusic();
			if(hasVideo)
				listElement.itHasVideo();
			if(hasDocuments)
				listElement.itHasDocuments();
			/*if(cursor.getString(cursor.getColumnIndex(KEY_VRSTA)) == BookmarkTypes.MOLITVENIK.toString()){
				listElement.setVrstaZaBookmark(BookmarkTypes.MOLITVENIK);
			} else{
				listElement.setVrstaZaBookmark(BookmarkTypes.VIJEST);
			}*/
			listaVijesti.add(listElement);
			cursor.moveToNext();
		}
		cursor.close();
		db.close();
		return listaVijesti;
	}
	
	public void deleteNid(int nid){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_BOOKMARKS, KEY_NID + " = ?", new String[] {String.valueOf(nid)});
		db.close();
	}
}
