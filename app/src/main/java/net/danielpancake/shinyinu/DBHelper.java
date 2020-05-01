package net.danielpancake.shinyinu;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "shinyinuDB";
    public static final String TABLE_SHINY = "shiny";

    public static final String KEY_ID = "_id";
    public static final String KEY_CODE = "code";
    public static final String KEY_BITMAP_PREVIEW = "preview";

    public DBHelper(Context context) {
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
}
