package com.meizu.statsapp.v3.lib.plugin.emitter.local.storage;

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
public class LocalEventStore {
    private final static String TAG = "LocalEventStore";

    private final int ONCE_EMIT_LIMIT = 200;
    private final int CLEAR_THRESHOLD = 10000;
    private final int CLEAR_KEEP_LIMIT = 1000;

    private SQLiteDatabase mDatabase;
    private LocalEventStoreHelper mDBHelper;
    private Context mContext;
    private boolean encrypt = true;

    /**
     * Creates a new Event Store
     *
     * @param context The android context object
     */
    public LocalEventStore(Context context) {
        this.mContext = context;
        SimpleCryptoAES.init(context);
        mDBHelper = LocalEventStoreHelper.getInstance(context);
        open();
        if (isDatabaseOpen()) {
            Logger.d(TAG, "DB Path:" + mDatabase.getPath());
        }
    }

    /**
     * Opens a new writable database if it
     * is currently closed.
     */
    private synchronized void open() {
        if (!isDatabaseOpen()) {
            try {
                mDatabase = mDBHelper.getWritableDatabase();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * Returns truth on if database is open.
     *
     * @return a boolean for database status
     */
    private boolean isDatabaseOpen() {
        boolean open = mDatabase != null && mDatabase.isOpen();
        if (!open) {
            Logger.d(TAG, "database NOT open!");
        }
        return open;
    }

    /**
     * Closes the database
     */
    public synchronized void close() {
        mDBHelper.close();
    }

    public synchronized void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    /**
     * Inserts a payload into the database
     *
     * @param payload The event payload to
     *                be stored
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    @SuppressWarnings("unchecked")
    public synchronized long insertEvent(TrackerPayload payload) {
        long inserted = -1;
        if (isDatabaseOpen()) {
            try {
                EventBean eventBean = EventBean.fromPayload(encrypt ? 2 : 0, payload);
                ContentValues values = new ContentValues();
                values.put(LocalEventStoreHelper.COLUMN_SESSION_ID, eventBean.getSessionId());
                values.put(LocalEventStoreHelper.COLUMN_EVENT_SOURCE, eventBean.getEventSource());
                values.put(LocalEventStoreHelper.COLUMN_ENCRYPT, eventBean.getEncrypt());
                values.put(LocalEventStoreHelper.COLUMN_EVENT_DATA, eventBean.getEventData());
                inserted = mDatabase.insert(LocalEventStoreHelper.TABLE_EVENTS, null, values);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        Logger.d(TAG, "Added event:" + inserted);
        return inserted;
    }

    public synchronized void updateLastResetTime(long time) {
        if (isDatabaseOpen()) {
            try {
                ContentValues values = new ContentValues();
                values.put(LocalEventStoreHelper.COLUMN_LAST_RESET_TIME, time);
                if (getSize(LocalEventStoreHelper.TABLE_EMITTER_MISCELLANEOUS, null) == 1) {
                    mDatabase.update(LocalEventStoreHelper.TABLE_EMITTER_MISCELLANEOUS, values, null, null);
                } else {
                    long insert = mDatabase.insertWithOnConflict(
                            LocalEventStoreHelper.TABLE_EMITTER_MISCELLANEOUS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public synchronized void updateTraffic(int traffic) {
        if (isDatabaseOpen()) {
            try {
                ContentValues values = new ContentValues();
                values.put(LocalEventStoreHelper.COLUMN_TRAFFIC, traffic);
                if (getSize(LocalEventStoreHelper.TABLE_EMITTER_MISCELLANEOUS, null) == 1) {
                    mDatabase.update(LocalEventStoreHelper.TABLE_EMITTER_MISCELLANEOUS, values, null, null);
                } else {
                    long insert = mDatabase.insertWithOnConflict(
                            LocalEventStoreHelper.TABLE_EMITTER_MISCELLANEOUS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public synchronized long getLastResetTime() {
        long value = 0;
        if (isDatabaseOpen()) {
            Cursor cursor = null;
            try {
                cursor = mDatabase.query(true, LocalEventStoreHelper.TABLE_EMITTER_MISCELLANEOUS,
                        new String[]{LocalEventStoreHelper.COLUMN_LAST_RESET_TIME},
                        null,
                        null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    value = cursor.getLong(0);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                CommonUtils.closeQuietly(cursor);
            }
        }
        return value;
    }

    public synchronized int getTraffic() {
        int value = 0;
        if (isDatabaseOpen()) {
            Cursor cursor = null;
            try {
                cursor = mDatabase.query(true, LocalEventStoreHelper.TABLE_EMITTER_MISCELLANEOUS,
                        new String[]{LocalEventStoreHelper.COLUMN_TRAFFIC},
                        null,
                        null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    value = cursor.getInt(0);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                CommonUtils.closeQuietly(cursor);
            }
        }
        return value;
    }

    /**
     * Removes an event from the database
     *
     * @param eventId the event id of the event
     * @return a boolean of success to remove
     */
    public synchronized boolean removeEvent(long eventId) {
        int retval = -1;
        if (isDatabaseOpen()) {
            try {
                retval = mDatabase.delete(LocalEventStoreHelper.TABLE_EVENTS, LocalEventStoreHelper.COLUMN_EVENT_ID + "=" + eventId, null);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        Logger.d(TAG, "Removed event, eventId:" + eventId);
        return retval == 1;
    }

    public synchronized void clearOldEventsIfNecessary() {
        if (isDatabaseOpen()) {
            try {
                long size = getEventsCount(null);
                if (size > CLEAR_THRESHOLD) {
                    Logger.d(TAG, "clear old events, amount of events currently in the database: " + size);
                    mDatabase.execSQL("delete from " + LocalEventStoreHelper.TABLE_EVENTS + " where " +
                            "(eventId not in (select eventId from " + LocalEventStoreHelper.TABLE_EVENTS
                            + " order by " + LocalEventStoreHelper.COLUMN_EVENT_ID + " desc limit " + CLEAR_KEEP_LIMIT + "))");
                }
            } catch (Throwable t) {
                t.printStackTrace();
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
                cursor = mDatabase.query(table, null, query,
                        null, null, null, orderBy);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        EventBean eventBean = new EventBean();
                        eventBean.setId(cursor.getInt(cursor.getColumnIndex(LocalEventStoreHelper.COLUMN_EVENT_ID)));
                        eventBean.setSessionId(cursor.getString(cursor.getColumnIndex(LocalEventStoreHelper.COLUMN_SESSION_ID)));
                        eventBean.setEventSource(cursor.getString(cursor.getColumnIndex(LocalEventStoreHelper.COLUMN_EVENT_SOURCE)));
                        eventBean.setEncrypt(cursor.getInt(cursor.getColumnIndex(LocalEventStoreHelper.COLUMN_ENCRYPT)));
                        eventBean.setEventData(cursor.getString(cursor.getColumnIndex(LocalEventStoreHelper.COLUMN_EVENT_DATA)));
                        eventBean.setDateCreated(cursor.getString(cursor.getColumnIndex(LocalEventStoreHelper.COLUMN_DATE_CREATED)));
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
     * Returns amount of rows of the table currently
     * in the database.
     *
     * @return the count of rows in the
     * database
     */
    private synchronized long getSize(String table, String query) {
        if (isDatabaseOpen()) {
            try {
                return DatabaseUtils.queryNumEntries(mDatabase, table, query);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * Returns an EmittableEvents object which
     * contains events and eventIds within a
     * defined range of the database.
     * 因为解密，此方法较耗时
     *
     * @return an EmittableEvents object containing
     * eventIds and event payloads.
     */
    @SuppressWarnings("unchecked")
    public synchronized ArrayList<EmittableEvent> getEmittableEvents() {
        ArrayList<EmittableEvent> emittableEvents = new ArrayList<>();
        if (isDatabaseOpen()) {
            try {
                for (EventBean eventBean : getAscEventsLimit(null, ONCE_EMIT_LIMIT)) {
                    long id = eventBean.getId();
                    TrackerPayload payload = EventBean.toPayload(eventBean);
                    if (payload != null) {
                        emittableEvents.add(new EmittableEvent("", id, payload));
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return emittableEvents;
    }

    public synchronized ArrayList<EmittableEvent> getEventsMax500() {
        ArrayList<EmittableEvent> emittableEvents = new ArrayList<>();
        if (isDatabaseOpen()) {
            try {
                for (EventBean eventBean : getAscEventsLimit(null, 500)) {
                    long id = eventBean.getId();
                    TrackerPayload payload = EventBean.toPayload(eventBean);
                    if (payload != null) {
                        emittableEvents.add(new EmittableEvent("", id, payload));
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return emittableEvents;
    }

    /**
     * @param query
     * @return
     */
    public synchronized long getEventsCount(String query) {
        if (isDatabaseOpen()) {
            try {
                return DatabaseUtils.queryNumEntries(mDatabase, LocalEventStoreHelper.TABLE_EVENTS, query);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * Returns a Map containing the event
     * payload values, the table row ID and
     * the date it was created.
     *
     * @param rowID the row id of the event to get
     * @return event
     */
    public synchronized EventBean getEventByRowId(long rowID) {
        if (isDatabaseOpen()) {
            try {
                List<EventBean> res =
                        queryDatabase(LocalEventStoreHelper.TABLE_EVENTS, LocalEventStoreHelper.COLUMN_EVENT_ID + "=" + rowID, null); //当有一列是INTEGER PRIMARY KEY autoincrement（COLUMN_EVENT_ID）
                // 他就是是作为rowID的别名，否则为_id
                if (!res.isEmpty()) {
                    return res.get(0);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Returns a list of all the events in the
     * database.
     *
     * @return the events in the database
     */
    public synchronized List<EventBean> getEvents(long start, int limit) {
        try {
            return queryDatabase(LocalEventStoreHelper.TABLE_EVENTS, "eventId >= " + start, "eventId ASC LIMIT " + limit);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return new ArrayList<EventBean>();
    }

    /**
     * Returns a descending range of events
     * from the top of the database.
     *
     * @param limit amount of rows to take
     * @return a list of event
     */
    public synchronized List<EventBean> getDescEventsLimit(String query, int limit) {
        try {
            return queryDatabase(LocalEventStoreHelper.TABLE_EVENTS, query, "eventId DESC LIMIT " + limit);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return new ArrayList<EventBean>();
    }

    /**
     * Returns a ascending range of events
     * from the top of the database.
     *
     * @param limit amount of rows to take
     * @return a list of event
     */
    public synchronized List<EventBean> getAscEventsLimit(String query, int limit) {
        try {
            return queryDatabase(LocalEventStoreHelper.TABLE_EVENTS, query, "eventId ASC LIMIT " + limit);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return new ArrayList<EventBean>();
    }

    public synchronized boolean updateEventSource(String sessionId, String eventSource) {
        if (isDatabaseOpen()) {
            try {
                ContentValues values = new ContentValues();
                values.put(LocalEventStoreHelper.COLUMN_EVENT_SOURCE, eventSource);
                int update = mDatabase.update(LocalEventStoreHelper.TABLE_EVENTS, values,
                        LocalEventStoreHelper.COLUMN_SESSION_ID + "=" + "'" + sessionId + "'", null);
                return update > 0;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return false;
    }
}