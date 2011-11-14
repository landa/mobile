/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.mit.people.landa.secretmessages;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Landmark messages database access helper class. Defines the basic CRUD
 * operations for landmark messages.
 */
public class SettingsDbAdapter {

  public static final String KEY_SETTING = "setting";
  public static final String KEY_VALUE = "value";
  public static final String KEY_ROWID = "_id";

  private static final String TAG = "SettingsDbAdapter";
  private DatabaseHelper mDbHelper;
  private SQLiteDatabase mDb;

  /**
   * Database creation sql statement
   */
  private static final String DATABASE_CREATE = 
    "create table if not exists settings ("
  + KEY_ROWID + " integer primary key autoincrement, "
  + KEY_SETTING + " text not null, "
  + KEY_VALUE + " text not null"
  + ");";

  private static final String DATABASE_NAME = "landmarks";
  private static final String DATABASE_TABLE = "settings";
  private static final int DATABASE_VERSION = 2;

  private final Context mCtx;

  private static class DatabaseHelper extends SQLiteOpenHelper {

    DatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

      db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("DROP TABLE IF EXISTS settings");
      onCreate(db);
    }
  }

  /**
   * Constructor - takes the context to allow the database to be opened/created
   * 
   * @param ctx the Context within which to work
   */
  public SettingsDbAdapter(Context ctx) {
    this.mCtx = ctx;
  }

  /**
   * Open the notes database. If it cannot be opened, try to create a new
   * instance of the database. If it cannot be created, throw an exception to
   * signal the failure
   * 
   * @return this (self reference, allowing this to be chained in an
   *         initialization call)
   * @throws SQLException if the database could be neither opened or created
   */
  public SettingsDbAdapter open() throws SQLException {
    mDbHelper = new DatabaseHelper(mCtx);
    mDb = mDbHelper.getWritableDatabase();
    mDb.execSQL(DATABASE_CREATE);
    return this;
  }

  public void close() {
    mDbHelper.close();
    mDb.close();
  }

  /**
   * Create a new note using the title and body provided. If the note is
   * successfully created return the new rowId for that note, otherwise return a
   * -1 to indicate failure.
   * 
   * @param title the title of the note
   * @param body the body of the note
   * @return rowId or -1 if failed
   */
  public void write(String key, String value) {
    Cursor cursor = mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID }, KEY_SETTING + "='" + key + "'", null, null, null, null);
    int numRows = cursor.getCount();
    cursor.close();
    if (numRows == 0) {
      ContentValues initialValues = new ContentValues();
      initialValues.put(KEY_SETTING, key);
      initialValues.put(KEY_VALUE, value);
      mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    else {
      mDb.execSQL("UPDATE " + DATABASE_TABLE + " SET " + KEY_VALUE + "='" + value + "' WHERE " + KEY_SETTING + "='" + key + "'");
    }
  }
  
  public String read(String key, String silent) {
    Cursor cursor = mDb.query(DATABASE_TABLE, new String[] { KEY_VALUE }, KEY_SETTING + "='" + key + "'", null, null, null, null);
    if (cursor != null && cursor.getCount() > 0) {
      cursor.moveToFirst();
      String ret = cursor.getString(cursor.getColumnIndex(KEY_VALUE));
      cursor.close();
      return ret;
    }
    else {
      return silent;
    }
  }
}
