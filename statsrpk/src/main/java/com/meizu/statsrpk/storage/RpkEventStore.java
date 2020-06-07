package com.meizu.statsrpk.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.meizu.statsapp.v3.lib.plugin.emitter.EmittableEvent;
import com.meizu.statsapp.v3.lib.plugin.emitter.EventBean;
import com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload;
import com.meizu.statsapp.v3.lib.plugin.secure.SimpleCryptoAES;
import com.meizu.statsapp.v3.utils.CommonUtils;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for storing, getting and removing
 * events from the SQLite database.
 */
public class RpkEventStore {
    private static String TAG = RpkEventStore.class.getSimpleName();

    private final int ONCE_EMIT_LIMIT = 200;
    private final int CLEAR_THRESHOLD = 10000;
    private final int CLEAR_KEEP_LIMIT = 1000;

    private SQLiteDatabase database;
    private RpkEventStoreHelper dbHelper;

    private Context context;

    /**
     * Creates a new Event Store
     *
     * @param context The android context object
     */
    public RpkEventStore(Context context) {
        this.context = context;
        SimpleCryptoAES.init(context);
        dbHelper = RpkEventStoreHelper.getInstance(context);
        open();

        Logger.d(TAG, "DB Path:" + database.getPath());
    }

    /**
     * Opens a new writable database if it
     * is currently closed.
     */
    private synchronized void open() {
        if (!isDatabaseOpen()) {
            database = dbHelper.getWritableDatabase();
            database.enableWriteAheadLogging();
        }
    }

    /**
     * Returns truth on if database is open.
     *
     * @return a boolean for database status
     */
    private boolean isDatabaseOpen() {
        return database != null && database.isOpen();
    }

    /**
     * Closes the database
     */
    public synchronized void close() {
        dbHelper.close();
    }

    /**
     * Inserts a payload into the database
     *
     * @param payload The event payload to
     *                be stored
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    @SuppressWarnings("unchecked")
    public synchronized long insertEvent(String appKey, String rpkPkgName, TrackerPayload payload) {
        long inserted = -1;
        if (isDatabaseOpen()) {
            try {
                EventBean eventBean = EventBean.fromPayload(CommonUtils.isDebugMode(context)? 0 : 2, payload); //默认都加密
                ContentValues values = new ContentValues();
                values.put(RpkEventStoreHelper.COLUMN_appKey, appKey);
                values.put(RpkEventStoreHelper.COLUMN_rpkPkgName, rpkPkgName);
                values.put(RpkEventStoreHelper.COLUMN_SESSION_ID, eventBean.getSessionId());
                values.put(RpkEventStoreHelper.COLUMN_ENCRYPT, eventBean.getEncrypt());
                values.put(RpkEventStoreHelper.COLUMN_EVENT_DATA, eventBean.getEventData());
                inserted = database.insertWithOnConflict(RpkEventStoreHelper.TABLE_EVENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        Logger.d(TAG, "succ add event, inserted:" + inserted);
        return inserted;
    }

    /**
     * Removes an event from the database
     *
     * @param appKey  the package event belong to
     * @param eventId the event id of the event
     * @return a boolean of success to remove
     */
    public synchronized boolean removeEvent(String appKey, long eventId) {
        int retval = -1;
        if (isDatabaseOpen()) {
            try {
                retval = database.delete(RpkEventStoreHelper.TABLE_EVENTS, RpkEventStoreHelper.COLUMN_appKey + "=" + "'" + appKey + "'"
                        + " and " + RpkEventStoreHelper.COLUMN_EVENT_ID + "=" + eventId, null);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        Logger.d(TAG, "Removed event, appKey:" + appKey + ", eventId:" + eventId);
        return retval == 1;
    }

    public synchronized void clearOldEventsIfNecessary() {
        if (isDatabaseOpen()) {
            try {
                long size = getSize(RpkEventStoreHelper.TABLE_EVENTS, null);
                if (size > CLEAR_THRESHOLD) {
                    Logger.d(TAG, "clear old events, amount of events currently in the database: " + size);
                    database.execSQL("delete from " + RpkEventStoreHelper.TABLE_EVENTS + " where (eventId not in (select eventId from " + RpkEventStoreHelper.TABLE_EVENTS
                            + " order by " + RpkEventStoreHelper.COLUMN_EVENT_ID + " desc limit " + CLEAR_KEEP_LIMIT + "))");
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the events that validate a
     * specific query.
     *
     * @param query   the query to be passed against
     *                the database
     * @param orderBy what to order the query by
     * @return the list of events that satisfied
     * the query
     */
    private synchronized List<EventBean> queryDatabase(String table, String query, String orderBy) {
        List<EventBean> res = new ArrayList<>();
        if (isDatabaseOpen()) {
            Cursor cursor = null;
            try {
                cursor = database.query(table, null, query,
                        null, null, null, orderBy);

                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        EventBean eventBean = new EventBean();
                        eventBean.setId(cursor.getInt(cursor.getColumnIndex(RpkEventStoreHelper.COLUMN_EVENT_ID)));
                        eventBean.setSessionId(cursor.getString(cursor.getColumnIndex(RpkEventStoreHelper.COLUMN_SESSION_ID)));
                        eventBean.setEncrypt(cursor.getInt(cursor.getColumnIndex(RpkEventStoreHelper.COLUMN_ENCRYPT)));
                        eventBean.setEventData(cursor.getString(cursor.getColumnIndex(RpkEventStoreHelper.COLUMN_EVENT_DATA)));
                        eventBean.setDateCreated(cursor.getString(cursor.getColumnIndex(RpkEventStoreHelper.COLUMN_DATE_CREATED)));
                        res.add(eventBean);
                        cursor.moveToNext();
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                CommonUtils.closeQuietly(cursor);
            }

        }
        return res;
    }

    // Getters

    /**
     * Returns the events that validate a
     * specific query.
     *
     * @return the list of PackageNames that satisfied
     * the query
     */
    public synchronized List<String> getAppKeys() {
        List<String> packageNames = new ArrayList<>();
        if (isDatabaseOpen()) {
            Cursor cursor = null;
            try {
                cursor = database.query(true, RpkEventStoreHelper.TABLE_EVENTS, new String[]{RpkEventStoreHelper.COLUMN_appKey}, null,
                        null, null, null, null, null);

                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    packageNames.add(cursor.getString(0));
                    cursor.moveToNext();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                CommonUtils.closeQuietly(cursor);
            }

        }
        return packageNames;
    }


    /**
     * Returns amount of events currently
     * in the database.
     *
     * @return the count of events in the
     * database
     */
    public synchronized long getEventsCountForAppKey(String appKey) {
        return getSize(RpkEventStoreHelper.TABLE_EVENTS,
                RpkEventStoreHelper.COLUMN_appKey + "=" + "'" + appKey + "'");
    }

    /**
     * Returns amount of rows of the table currently
     * in the database.
     *
     * @return the count of rows in the
     * database
     */
    private synchronized long getSize(String table, String query) {
        return DatabaseUtils.queryNumEntries(database, table, query);
    }

    /**
     * Returns an EmittableEvents object which
     * contains events and eventIds within a
     * defined range of the database.
     *
     * @return an EmittableEvents object containing
     * eventIds and event payloads.
     */
    @SuppressWarnings("unchecked")
    public synchronized ArrayList<EmittableEvent> getEmittableEvents(String appKey) {
        ArrayList<EmittableEvent> emittableEvents = new ArrayList<>();
        for (EventBean eventBean : getAscEventsLimit(RpkEventStoreHelper.COLUMN_appKey + "=" + "'" + appKey + "'", ONCE_EMIT_LIMIT)) {
            long id = eventBean.getId();
            TrackerPayload payload = EventBean.toPayload(eventBean);
            if (payload != null) {
                emittableEvents.add(new EmittableEvent("", id, payload));
            }
        }
        return emittableEvents;
    }

    /**
     * Returns a descending range of events
     * from the top of the database.
     *
     * @param limit amount of rows to take
     * @return a list of event
     */
    private synchronized List<EventBean> getDescEventsLimit(String query, int limit) {
        return queryDatabase(RpkEventStoreHelper.TABLE_EVENTS, query, "eventId DESC LIMIT " + limit);
    }

    /**
     * Returns a ascending range of events
     * from the top of the database.
     *
     * @param limit amount of rows to take
     * @return a list of event
     */
    private synchronized List<EventBean> getAscEventsLimit(String query, int limit) {
        return queryDatabase(RpkEventStoreHelper.TABLE_EVENTS, query, "eventId ASC LIMIT " + limit);
    }

}