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

package com.meizu.statsrpk.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.meizu.statsapp.v3.utils.log.Logger;

/**
 * Helper class for building and maintaining the SQLite
 * Database used by the Tracker.
 */
/*package*/ class RpkEventStoreHelper extends SQLiteOpenHelper {
    private static final String TAG = RpkEventStoreHelper.class.getName();

    //下面表用到的公共列名
    public static final String COLUMN_rpkPkgName = "rpkPkgName";
    public static final String COLUMN_appKey = "appKey";

    //table event
    public static final String TABLE_EVENTS = "events";
    public static final String COLUMN_ENCRYPT = "encrypt";
    public static final String COLUMN_EVENT_ID = "eventId";
    public static final String COLUMN_SESSION_ID = "eventSessionId";
    public static final String COLUMN_EVENT_DATA = "eventData";
    public static final String COLUMN_DATE_CREATED = "dateCreated";

    private static final String DATABASE_NAME = "statsrpk.db";
    private static final int DATABASE_VERSION = 1;

    private static final String queryCreateTable = "CREATE TABLE IF NOT EXISTS 'events' " +
            "(eventId INTEGER PRIMARY KEY autoincrement, rpkPkgName TEXT, appKey TEXT, encrypt INTEGER, " +
            "eventSessionId TEXT, eventData TEXT, " +
            "dateCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "unique(eventId))";

    // Prevents multiple instances being created and avoids memory leaks.
    private static RpkEventStoreHelper sInstance;

    /**
     * Use the application context, which will ensure that you
     * don't accidentally leak an Activity's context.
     * See this article for more information: http://bit.ly/6LRzfx
     *
     * @param context the android context
     * @return the EventStoreHelper instance
     */
    public synchronized static RpkEventStoreHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RpkEventStoreHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * @param context the android context
     */
    private RpkEventStoreHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Logger.d(TAG, "DATABASE_VERSION " + DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(queryCreateTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Logger.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
    }
}
