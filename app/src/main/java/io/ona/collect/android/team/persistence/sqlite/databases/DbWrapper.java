/*
 * Copyright (C) 2007 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.ona.collect.android.team.persistence.sqlite.databases;

import java.io.File;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

import io.ona.collect.android.team.R;

/**
 * We've taken this from Android's DbWrapper. However, we can't appropriately lock the
 * dbWrapper so there may be issues if a thread opens the dbWrapper read-only and another thread tries
 * to open the dbWrapper read/write. I don't think this will ever happen in ODK, though. (fingers
 * crossed).
 * <p>
 * A helper class to manage dbWrapper creation and version management. You create a subclass
 * implementing {@link #onCreate}, {@link #onUpgrade} and optionally {@link #onOpen}, and this class
 * takes care of opening the dbWrapper if it exists, creating it if it does not, and upgrading it as
 * necessary. Transactions are used to make sure the dbWrapper is always in a sensible state.
 * <p>
 * For an example, see the NotePadProvider class in the NotePad sample application, in the
 * <em>samples/</em> directory of the SDK.
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 02/08/2017.
 * Attribution:
 * - https://github.com/opendatakit/collect/blob/master/collect_app/src/main/java/org/odk/collect/android/database/ODKSQLiteOpenHelper.java
 */
public abstract class DbWrapper {
    private static final String TAG = DbWrapper.class.getSimpleName();

    private final Application application;
    private final String path;
    private final String name;
    private final CursorFactory factory;
    private final int newVersion;

    private SQLiteDatabase database = null;
    private boolean isInitializing = false;


    /**
     * Create a helper object to create, open, and/or manage a dbWrapper. The dbWrapper is not
     * actually created or opened until one of {@link #getWritableDatabase} or
     * {@link #getReadableDatabase} is called.
     *
     * @param path    to the file
     * @param name    of the dbWrapper file, or null for an in-memory dbWrapper
     * @param factory to use for creating cursor objects, or null for the default
     * @param version number of the dbWrapper (starting at 1); if the dbWrapper is older,
     *                {@link #onUpgrade} will be used to upgrade the dbWrapper
     */
    protected DbWrapper(Application application, String path, String name,
                        CursorFactory factory, int version) {
        if (version < 1)
            throw new IllegalArgumentException("Version must be >= 1, was " + version);

        this.application = application;
        this.path = path;
        this.name = name;
        this.factory = factory;
        this.newVersion = version;
    }


    /**
     * Create and/or open a dbWrapper that will be used for reading and writing. Once opened
     * successfully, the dbWrapper is cached, so you can call this method every time you need to
     * write to the dbWrapper. Make sure to call {@link #close} when you no longer need it.
     * <p>
     * Errors such as bad permissions or a full disk may cause this operation to fail, but future
     * attempts may succeed if the problem is fixed.
     * </p>
     *
     * @return a read/write dbWrapper object valid until {@link #close} is called
     * @throws SQLiteException if the dbWrapper cannot be opened for writing
     */
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (database != null && database.isOpen() && !database.isReadOnly()) {
            return database; // The dbWrapper is already open for business
        }

        if (isInitializing) {
            throw new IllegalStateException("getWritableDatabase called recursively");
        }

        // If we have a read-only dbWrapper open, someone could be using it
        // (though they shouldn't), which would cause a lock to be held on
        // the file, and our attempts to open the dbWrapper read-write would
        // fail waiting for the file lock. To prevent that, we acquire the
        // lock on the read-only dbWrapper, which shuts out other users.

        boolean success = false;
        SQLiteDatabase db = null;
        // if (dbWrapper != null) dbWrapper.lock();
        try {
            isInitializing = true;
            if (name == null) {
                db = SQLiteDatabase.create(null);
            } else {
                Log.d(TAG, "creating the database " + name);
                db = SQLiteDatabase.openOrCreateDatabase(path + File.separator + name, factory);
                // db = mContext.openOrCreateDatabase(name, 0, factory);
            }

            int version = db.getVersion();
            if (version != newVersion) {
                db.beginTransaction();
                try {
                    if (version == 0) {
                        onCreate(db);
                    } else {
                        onUpgrade(db, version, newVersion);
                    }
                    db.setVersion(newVersion);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            onOpen(db);
            success = true;
            return db;
        } finally {
            isInitializing = false;
            if (success) {
                if (database != null) {
                    try {
                        database.close();
                    } catch (Exception e) {
                    }
                    // dbWrapper.unlock();
                }
                database = db;
            } else {
                // if (dbWrapper != null) dbWrapper.unlock();
                if (db != null)
                    db.close();
            }
        }
    }


    /**
     * Create and/or open a dbWrapper. This will be the same object returned by
     * {@link #getWritableDatabase} unless some problem, such as a full disk, requires the dbWrapper
     * to be opened read-only. In that case, a read-only dbWrapper object will be returned. If the
     * problem is fixed, a future call to {@link #getWritableDatabase} may succeed, in which case
     * the read-only dbWrapper object will be closed and the read/write object will be returned in
     * the future.
     *
     * @return a dbWrapper object valid until {@link #getWritableDatabase} or {@link #close} is
     * called.
     * @throws SQLiteException if the dbWrapper cannot be opened
     */
    public synchronized SQLiteDatabase getReadableDatabase() {
        if (database != null && database.isOpen()) {
            return database; // The dbWrapper is already open for business
        }

        if (isInitializing) {
            throw new IllegalStateException("getReadableDatabase called recursively");
        }

        try {
            return getWritableDatabase();
        } catch (SQLiteException e) {
            if (name == null)
                throw e; // Can't open a temp dbWrapper read-only!
            Log.e(TAG, "Couldn't open " + name + " for writing (will try read-only):", e);
        }

        SQLiteDatabase db = null;
        try {
            isInitializing = true;
            String path = this.path + File.separator + name;
            // mContext.getDatabasePath(name).getPath();
            try {
                db = SQLiteDatabase.openDatabase(path, factory, SQLiteDatabase.OPEN_READONLY);
            } catch (RuntimeException e) {
                Log.e(TAG, e.getMessage(), e);
                String cardstatus = Environment.getExternalStorageState();
                if (!cardstatus.equals(Environment.MEDIA_MOUNTED)) {
                    throw new RuntimeException(
                            application.getString(R.string.sdcard_unmounted, cardstatus));
                } else {
                    throw e;
                }
            }

            if (db.getVersion() != newVersion) {
                throw new SQLiteException("Can't upgrade read-only dbWrapper from version "
                        + db.getVersion() + " to " + newVersion + ": " + path);
            }

            onOpen(db);
            Log.w(TAG, "Opened " + name + " in read-only mode");
            database = db;
            return database;
        } finally {
            isInitializing = false;
            if (db != null && db != database)
                db.close();
        }
    }


    /**
     * Close any open dbWrapper object.
     */
    public synchronized void close() {
        if (isInitializing)
            throw new IllegalStateException("Closed during initialization");

        if (database != null && database.isOpen()) {
            database.close();
            database = null;
        }
    }


    /**
     * Called when the dbWrapper is created for the first time. This is where the creation of tables
     * and the initial population of the tables should happen.
     *
     * @param db The dbWrapper.
     */
    public abstract void onCreate(SQLiteDatabase db);


    /**
     * Called when the dbWrapper needs to be upgraded. The implementation should use this method to
     * drop tables, add tables, or do anything else it needs to upgrade to the new schema version.
     * <p>
     * The SQLite ALTER TABLE documentation can be found <a
     * href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns you can use
     * ALTER TABLE to insert them into a live table. If you rename or remove columns you can use
     * ALTER TABLE to rename the old table, then create the new table and then populate the new
     * table with the contents of the old table.
     *
     * @param db         The dbWrapper.
     * @param oldVersion The old dbWrapper version.
     * @param newVersion The new dbWrapper version.
     */
    public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);


    /**
     * Called when the dbWrapper has been opened. Override method should check
     * {@link SQLiteDatabase#isReadOnly} before updating the dbWrapper.
     *
     * @param db The dbWrapper.
     */
    public void onOpen(SQLiteDatabase db) {
    }
}
