package jp.tmhouse.android.elgoog.elgoog;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDB extends SQLiteOpenHelper {
	private static final int c_version = 1;
	private static String c_DBName = "localDB";
	private final String c_BookmarkTableStr = "BookmarkTable";

	final String c_fld_id = "id";
    final String c_fld_url = "url";
    final String c_fld_titile = "title";

	public LocalDB(Context context) {
		super(context, c_DBName, null, c_version);
	}

    public class Bookmark {
    	public Bookmark(int id, String url, String title) {
            m_id = id;
    		m_url = url;
    		m_title = title;
    	}
        int     m_id;
    	String	m_url;
    	String	m_title;
    }
    
	@Override
	public void onCreate(SQLiteDatabase db) {
        String str = "create table " + c_BookmarkTableStr +
                " (" + c_fld_id + " integer primary key autoincrement, " +
                c_fld_url + " text, " + c_fld_titile + " text);";
		db.execSQL(str);
	}
	
	@Override
    public void onOpen(SQLiteDatabase db) {
	}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if( newVersion > oldVersion ) {
        }
    }

	public void addBookmark(String url, String title) {
        SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues cv = new ContentValues();
			cv.put(c_fld_url, url);
			cv.put(c_fld_titile, title);
			db.insert(c_BookmarkTableStr, null, cv);
			db.setTransactionSuccessful();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
   			db.endTransaction();
       		//db.close();
		}
	}

    public void updateBookmark(Bookmark bkmark) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(c_fld_id, bkmark.m_id);
            cv.put(c_fld_url, bkmark.m_url);
            cv.put(c_fld_titile, bkmark.m_title);
            db.update(c_BookmarkTableStr, cv, c_fld_id + " = " + bkmark.m_id, null);
            db.setTransactionSuccessful();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            //db.close();
        }
    }
	
	public void deleteBookmark(int id) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			db.delete(c_BookmarkTableStr, c_fld_id + "=" + id, null);
			db.setTransactionSuccessful();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
   			db.endTransaction();
       		//db.close();
		}
	}

	public Bookmark[] getAllBookmarks() {
		final String sql = "select * from " + c_BookmarkTableStr + ";";
		
		Bookmark[] retObjArr = null;
		SQLiteCursor c = null;
		SQLiteDatabase db = getReadableDatabase();
        try {
        	c = (SQLiteCursor)db.rawQuery(sql, null);
        	if( c != null && c.getCount() > 0 ) {
	            int rowcount = c.getCount();
	            c.moveToFirst();
	            
	            retObjArr = new Bookmark[rowcount];
	            for (int i = 0; i < rowcount ; i++) {
	            	retObjArr[i] = new Bookmark(c.getInt(0),
                            c.getString(1), c.getString(2));
	                c.moveToNext();
	            }
        	}
        } catch (SQLException e) {
        	e.printStackTrace();
            android.util.Log.e("ERROR", e.toString());
        } finally {
        	if( c != null ) {c.close();	}
        	if( db != null ) {db.close();}
        }
        return(retObjArr);
	}
}
