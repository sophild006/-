package com.solid.analytics.storage;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.solid.analytics.model.Apps;
import com.solid.analytics.model.Event;
import com.solid.analytics.model.PageEvent;
import com.solid.analytics.model.Properties;
import com.solid.analytics.thrift.ThriftUtil;
import com.solid.analytics.util.IOUtil;
import com.solid.analytics.util.log.Logger;
import com.solid.analytics.util.log.LoggerFactory;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DbStorage extends SQLiteOpenHelper implements Storage {

    static final Logger log = LoggerFactory.getLogger(DbStorage.class);

    private static final int DB_VERSION = 1;

    final String mName;

    private static final String TABLE_STATUS = "status";
    private static final String CREATE_TABLE_STATUS = "CREATE TABLE IF NOT EXISTS " + TABLE_STATUS + "(" +
            "key TEXT PRIMARY KEY NOT NULL," +
            "value TEXT," +
            "ts INTEGER NOT NULL" +
            ")";
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_ACTIVE_SYNC = "active_sync";
    private static final String STATUS_APPS = "apps";

    private static final String TABLE_PROPERTIES = "properties";
    private static final String CREATE_TABLE_PROPERTIES = "CREATE TABLE IF NOT EXISTS " + TABLE_PROPERTIES + "(" +
            "_id INTEGER PRIMARY KEY NOT NULL," +
            "properties TEXT," +
            "sync INTEGER," +
            "ts INTEGER NOT NULL" +
            ")";
    private static final long ID_PROPERTIES = 1;

    private static final String TABLE_EVENT = "event";
    private static final String CREATE_TABLE_EVENT = "CREATE TABLE IF NOT EXISTS " + TABLE_EVENT + "(" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "event TEXT," +
            "ts INTEGER NOT NULL" +
            ")";

    private static final String TABLE_PAGE_EVENT = "page_event";
    private static final String CREATE_TABLE_PAGE_EVENT = "CREATE TABLE IF NOT EXISTS " + TABLE_PAGE_EVENT + "(" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "event TEXT," +
            "ts INTEGER NOT NULL" +
            ")";

    public DbStorage(Context context, String name) {
        super(context, name, null, DB_VERSION);
        mName = name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_STATUS);
        db.execSQL(CREATE_TABLE_PROPERTIES);
        db.execSQL(CREATE_TABLE_EVENT);
        db.execSQL(CREATE_TABLE_PAGE_EVENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(CREATE_TABLE_STATUS);
        db.execSQL(CREATE_TABLE_PROPERTIES);
        db.execSQL(CREATE_TABLE_EVENT);
        db.execSQL(CREATE_TABLE_PAGE_EVENT);
    }

    private void saveStatus(String key, String value) throws Exception {
        if (key == null)
            return;

        final SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("key", key);
            values.put("value", value);
            values.put("ts", System.currentTimeMillis());
            db.insertWithOnConflict(TABLE_STATUS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } finally {
            IOUtil.closeQuietly(db);
        }
    }

    private String loadStatus(String key) throws Exception {
        if (key == null)
            return null;

        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.query(TABLE_STATUS, new String[]{"key", "value"}, "key=?", new String[]{key}, null, null, null, null);
        try {
            if (cursor == null || !cursor.moveToFirst())
                return null;

            // String key = cursor.getString(0);
            String value = cursor.getString(1);
            return value;
        } finally {
            IOUtil.closeQuietly((Object) cursor);
            IOUtil.closeQuietly(db);
        }
    }

    @Override
    public boolean isActiveSync() throws Exception {
        String value = loadStatus(STATUS_ACTIVE_SYNC);
        return "1".equals(value);
    }

    @Override
    public void markActiveSync() throws Exception {
        saveStatus(STATUS_ACTIVE_SYNC, "1");
    }

    @Override
    public void saveProperties(Properties properties) throws Exception {
        if (properties == null)
            return;

        final SQLiteDatabase db = getWritableDatabase();
        try {
            JSONObject obj = new JSONObject();
            properties.write(obj);

            ContentValues values = new ContentValues();
            values.put("_id", ID_PROPERTIES);
            values.put("properties", obj.toString());
            values.put("sync", 0);
            values.put("ts", System.currentTimeMillis());
            db.insertWithOnConflict(TABLE_PROPERTIES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } finally {
            IOUtil.closeQuietly(db);
        }
    }

    @Override
    public Properties loadProperties() throws Exception {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.query(TABLE_PROPERTIES, new String[]{"_id", "properties"}, "_id=" + ID_PROPERTIES, null, null, null, null, null);
        try {
            if (cursor == null || !cursor.moveToFirst())
                return null;

            long id = cursor.getLong(0);
            String properties = cursor.getString(1);
            JSONObject obj = new JSONObject(properties);
            Properties i = new Properties();
            i.read(obj);
            return i;
        } finally {
            IOUtil.closeQuietly((Object) cursor);
            IOUtil.closeQuietly(db);
        }
    }

    @Override
    public boolean isPropertiesSync() throws Exception {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.query(TABLE_PROPERTIES, new String[]{"_id", "sync"}, "_id=" + ID_PROPERTIES, null, null, null, null, null);
        try {
            if (cursor == null || !cursor.moveToFirst())
                return false;

            long id = cursor.getLong(0);
            int sync = cursor.getInt(1);
            return sync == 1;
        } finally {
            IOUtil.closeQuietly((Object) cursor);
            IOUtil.closeQuietly(db);
        }
    }

    @Override
    public void markPropertiesSync() throws Exception {
        final SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("_id", ID_PROPERTIES);
            values.put("sync", 1);
            db.update(TABLE_PROPERTIES, values, "_id=" + ID_PROPERTIES, null);
        } finally {
            IOUtil.closeQuietly(db);
        }
    }

    @Override
    public void saveApps(Apps apps) throws Exception {
        if (apps == null)
            return;

        String str = new String(ThriftUtil.serialize(apps), "utf-8");
        saveStatus(STATUS_APPS, str);
    }

    @Override
    public Apps loadApps() throws Exception {
        String str = loadStatus(STATUS_APPS);
        if (str == null)
            return null;

        byte[] bs = str.getBytes("utf-8");
        return ThriftUtil.deserialize(bs, Apps.class);
    }

    @Override
    public void saveEvent(Event event) throws Exception {
        if (event == null)
            return;

        final SQLiteDatabase db = getWritableDatabase();
        try {
            JSONObject obj = new JSONObject();
            event.write(obj);

            ContentValues values = new ContentValues();
            values.put("event", obj.toString());
            values.put("ts", System.currentTimeMillis());
            db.insert(TABLE_EVENT, null, values);
        } finally {
            IOUtil.closeQuietly(db);
        }
    }

    @Override
    public List<Event> loadEvents(int limit, List<Long> ids) throws Exception {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.query(TABLE_EVENT, new String[]{"_id", "event"}, null, null, null, null, null, Integer.toString(limit));
        try {
            if (cursor == null)
                return null;

            final List<Event> events = new ArrayList<Event>(limit);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String event = cursor.getString(1);
                JSONObject obj = new JSONObject(event);
                Event e = new Event();
                e.read(obj);

                if (ids != null) ids.add(id);
                events.add(e);
            }

            return events;
        } finally {
            IOUtil.closeQuietly((Object) cursor);
            IOUtil.closeQuietly(db);
        }
    }

    @Override
    public void deleteEvents(List<Long> ids) throws Exception {
        if (ids == null || ids.size() <= 0)
            return;

        final SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            for (long id : ids)
                db.delete(TABLE_EVENT, "_id=" + id, null);
            db.setTransactionSuccessful();
            db.endTransaction();
        } finally {
            IOUtil.closeQuietly(db);
        }
    }

    @Override
    public void savePageEvent(PageEvent pageEvent) throws Exception {
        if (pageEvent == null)
            return;

        final SQLiteDatabase db = getWritableDatabase();
        try {
            JSONObject obj = new JSONObject();
            pageEvent.write(obj);

            ContentValues values = new ContentValues();
            values.put("event", obj.toString());
            values.put("ts", System.currentTimeMillis());
            db.insert(TABLE_PAGE_EVENT, null, values);
        } finally {
            IOUtil.closeQuietly(db);
        }
    }

    @Override
    public List<PageEvent> loadPageEvents(int limit, List<Long> ids) throws Exception {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.query(TABLE_PAGE_EVENT, new String[]{"_id", "event"}, null, null, null, null, null, Integer.toString(limit));
        try {
            if (cursor == null)
                return null;

            final List<PageEvent> pageEvents = new ArrayList<PageEvent>(limit);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String pageEvent = cursor.getString(1);
                JSONObject obj = new JSONObject(pageEvent);
                PageEvent e = new PageEvent();
                e.read(obj);

                if (ids != null) ids.add(id);
                pageEvents.add(e);
            }

            return pageEvents;
        } finally {
            IOUtil.closeQuietly((Object) cursor);
            IOUtil.closeQuietly(db);
        }
    }

    @Override
    public void deletePageEvents(List<Long> ids) throws Exception {
        if (ids == null || ids.size() <= 0)
            return;

        final SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            for (long id : ids)
                db.delete(TABLE_PAGE_EVENT, "_id=" + id, null);
            db.setTransactionSuccessful();
            db.endTransaction();
        } finally {
            IOUtil.closeQuietly(db);
        }
    }
}
