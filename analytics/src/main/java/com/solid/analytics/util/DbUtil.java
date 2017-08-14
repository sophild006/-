package com.solid.analytics.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

public class DbUtil {

    public static boolean dbExists(Context context, String dbName) {
        return context.getDatabasePath(dbName).exists();
    }

    public static boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type=? AND name=?", new String[]{"table", tableName});
            if (cursor == null || !cursor.moveToFirst())
                return false;

            return true;
        } finally {
            IOUtil.closeQuietly((Object) cursor);
        }
    }

    public static Map<String, String> tableColumns(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", new String[]{});
            Map<String, String> result = new HashMap<String, String>();
            if (cursor.moveToFirst()) {
                do {
                    result.put(cursor.getString(1), cursor.getString(2));
                } while (cursor.moveToNext());
            }
            return result;
        } finally {
            IOUtil.closeQuietly((Object) cursor);
        }
    }

    public static SQLiteDatabase openReadableDatabase(Context context, String dbName) {
        return SQLiteDatabase.openDatabase(context.getDatabasePath(dbName).getPath(), null, SQLiteDatabase.OPEN_READONLY);
    }

    public static SQLiteDatabase openWritableDatabase(Context context, String dbName) {
        return SQLiteDatabase.openDatabase(context.getDatabasePath(dbName).getPath(), null, SQLiteDatabase.OPEN_READWRITE);
    }
}
