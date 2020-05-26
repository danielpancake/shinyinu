package net.danielpancake.shinyinu;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "shinyinuDB";

    static final String TABLE_SHINY = "shiny";
    static final String KEY_ID = "_id";
    static final String KEY_CODE = "code";
    static final String KEY_BITMAP_PREVIEW = "preview";

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_SHINY + "(" +
                KEY_ID + " INTEGER PRIMARY KEY, " +
                KEY_CODE + " TEXT, " +
                KEY_BITMAP_PREVIEW + " BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void deleteItem(String code) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE from " + TABLE_SHINY + " WHERE " + KEY_CODE + " = " + "'" + code + "'");
    }
}
