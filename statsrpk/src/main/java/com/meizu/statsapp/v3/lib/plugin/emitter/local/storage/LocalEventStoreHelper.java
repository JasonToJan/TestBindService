/*
 * Copyright (c) 2015 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package com.meizu.statsapp.v3.lib.plugin.emitter.local.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.meizu.statsapp.v3.lib.plugin.utils.FlymeOSUtils;
import com.meizu.statsapp.v3.utils.log.Logger;

/**
 * Helper class for building and maintaining the SQLite
 * Database used by the Tracker.
 */
/*package*/ class LocalEventStoreHelper extends SQLiteOpenHelper {
    private static final String TAG = LocalEventStoreHelper.class.getName();

    //table event
    public static final String TABLE_EVENTS = "events";
    public static final String COLUMN_ENCRYPT = "encrypt";
    public static final String COLUMN_EVENT_ID = "eventId";
    public static final String COLUMN_SESSION_ID = "eventSessionId";
    public static final String COLUMN_EVENT_SOURCE = "eventSource";
    public static final String COLUMN_EVENT_DATA = "eventData";
    public static final String COLUMN_DATE_CREATED = "dateCreated";

    //table emitterMiscellaneous
    public static final String TABLE_EMITTER_MISCELLANEOUS = "emitterMiscellaneous";
    public static final String COLUMN_LAST_RESET_TIME = "lastResetTime";
    public static final String COLUMN_TRAFFIC = "traffic";

    //table package info
//    public static final String TABLE_PACKAGE_INFO = "packageInfo";
//    public static final String COLUMN_NAME = "name";

    private static final String DATABASE_NAME = "statsapp_v3.db";
    private static final int DATABASE_VERSION = 1;

    private static final String queryDropTable =
            "DROP TABLE IF EXISTS '" + TABLE_EVENTS + "'";
    private static final String queryCreateTable = "CREATE TABLE IF NOT EXISTS 'events' " +
            "(eventId INTEGER PRIMARY KEY autoincrement, encrypt INTEGER, " + //自增的eventId号作为event_id
            "eventSessionId TEXT, eventSource TEXT, eventData TEXT, " + //sessionId, eventSource可能被后续改变，其他字段写入后不变
            "dateCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    private static final String queryDropEmitterMiscellaneousTable =
            "DROP TABLE IF EXISTS '" + TABLE_EMITTER_MISCELLANEOUS + "'";
    private static final String queryCreateEmitterMiscellaneousTable = "CREATE TABLE IF NOT EXISTS 'emitterMiscellaneous' " +
            "(lastResetTime BIGINT, " +
            "traffic INTEGER)";

    // Prevents multiple instances being created and avoids memory leaks.
    private static LocalEventStoreHelper sInstance;

    /**
     * Use the application context, which will ensure that you
     * don't accidentally leak an Activity's context.
     * See this article for more information: http://bit.ly/6LRzfx
     *
     * @param context the android context
     * @return the EventStoreHelper instance
     */
    public synchronized static LocalEventStoreHelper getInstance(Context context) {
        if (sInstance == null) {
            String dbName;
            String processName = FlymeOSUtils.getCurProcessName(context);
            if (processName != null && !processName.equals(context.getPackageName())) {
                dbName = DATABASE_NAME + "_" + processName;
            } else {
                dbName = DATABASE_NAME;
            }
            sInstance = new LocalEventStoreHelper(context.getApplicationContext(), dbName);
        }
        return sInstance;
    }

    /**
     * @param context the android context
     */
    private LocalEventStoreHelper(Context context, String dbName) {
        super(context, dbName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(queryCreateTable);
        database.execSQL(queryCreateEmitterMiscellaneousTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Logger.d(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion);
        //TODO
    }

}
