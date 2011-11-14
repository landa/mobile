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
public class LandmarksDbAdapter {

  public static final String KEY_CONTENT = "content";
  public static final String KEY_TIMESTAMP = "timestamp";
  public static final String KEY_INSERT_TIMESTAMP = "insert_timestamp";
  public static final String KEY_LATITUDE = "latitude";
  public static final String KEY_LONGITUDE = "longitude";
  public static final String KEY_DIAMETER = "diameter";
  public static final String KEY_DEVICEID = "device_id";
  public static final String KEY_ALTITUDE = "altitude";
  public static final String KEY_PICID = "picId";
  public static final String KEY_SID = "sid";
  public static final String KEY_TID = "tid";
  public static final String KEY_ROWID = "_id";

  private static final String TAG = "NotesDbAdapter";
  private DatabaseHelper mDbHelper;
  private SQLiteDatabase mDb;

  /**
   * Database creation sql statement
   */
  private static final String DATABASE_CREATE = 
    "create table feed ("
  + KEY_ROWID + " integer primary key autoincrement, "
  + KEY_CONTENT + " text not null, "
  + KEY_TIMESTAMP + " text not null, "
  + KEY_INSERT_TIMESTAMP + " integer not null, "
  + KEY_LATITUDE + " text not null, "
  + KEY_LONGITUDE + " text not null, "
  + KEY_DIAMETER + " text not null, "
  + KEY_DEVICEID + " text not null, "
  + KEY_ALTITUDE + " text not null, "
  + KEY_PICID + " text not null, "
  + KEY_SID + " text not null, "
  + KEY_TID + " text not null"
  + ");";

  private static final String DATABASE_NAME = "landmarks";
  private static final String DATABASE_TABLE = "feed";
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
      db.execSQL("DROP TABLE IF EXISTS feed");
      onCreate(db);
    }
  }

  /**
   * Constructor - takes the context to allow the database to be opened/created
   * 
   * @param ctx the Context within which to work
   */
  public LandmarksDbAdapter(Context ctx) {
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
  public LandmarksDbAdapter open() throws SQLException {
    mDbHelper = new DatabaseHelper(mCtx);
    mDb = mDbHelper.getWritableDatabase();
    return this;
  }

  public void close() {
    mDbHelper.close();
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
  public long insertLandmark(LandmarkMessage message) {
    ContentValues initialValues = new ContentValues();
    initialValues.put(KEY_SID, message.getSid());
    initialValues.put(KEY_TID, message.getTid());
    initialValues.put(KEY_CONTENT, message.getMContent());
    initialValues.put(KEY_TIMESTAMP, String.valueOf(message.getMTimestamp()));
    initialValues.put(KEY_LATITUDE, String.valueOf(message.getMLatitude()));
    initialValues.put(KEY_LONGITUDE, String.valueOf(message.getMLongitude()));
    initialValues.put(KEY_ALTITUDE, String.valueOf(message.getMAltitude()));
    initialValues.put(KEY_PICID, String.valueOf(message.getMPictureId()));
    initialValues.put(KEY_DIAMETER, String.valueOf(message.getMAccuracy()));
    long insertTimestamp = System.currentTimeMillis();
    initialValues.put(KEY_INSERT_TIMESTAMP, insertTimestamp);
    message.setMInsertTimestamp(insertTimestamp);
    initialValues.put(KEY_DEVICEID, message.getDeviceId());
    return mDb.insert(DATABASE_TABLE, null, initialValues);
  }

  /**
   * Return a Cursor over the list of all notes in the database
   * 
   * @return Cursor over all notes
   */
  public Cursor fetchAllLandmarks(int count) {
    return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_INSERT_TIMESTAMP, KEY_SID, KEY_TID, KEY_CONTENT, KEY_TIMESTAMP, KEY_LATITUDE, KEY_LONGITUDE, KEY_DIAMETER, KEY_DEVICEID, KEY_ALTITUDE, KEY_PICID }, null, null, null, null, KEY_INSERT_TIMESTAMP + " DESC," + KEY_TIMESTAMP + " DESC", String.valueOf(count));
  }
  
  public Cursor fetchAllLandmarks(int sid, int count) {
    return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_INSERT_TIMESTAMP, KEY_SID, KEY_TID, KEY_CONTENT, KEY_TIMESTAMP, KEY_LATITUDE, KEY_LONGITUDE, KEY_DIAMETER, KEY_DEVICEID, KEY_ALTITUDE, KEY_PICID }, KEY_SID + "='" + sid + "'", null, null, null, KEY_INSERT_TIMESTAMP + " DESC, " + KEY_TIMESTAMP + " DESC", String.valueOf(count));
  }
  
  public Cursor fetchThreadLandmarks(int tid) {
    return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_INSERT_TIMESTAMP, KEY_SID, KEY_TID, KEY_CONTENT, KEY_TIMESTAMP, KEY_LATITUDE, KEY_LONGITUDE, KEY_DIAMETER, KEY_DEVICEID, KEY_ALTITUDE, KEY_PICID }, KEY_TID + "='" + tid + "'", null, null, null, KEY_TIMESTAMP + " ASC");
  }
  
  public boolean landmarkExists(LandmarkMessage message) {
    Cursor cursor = mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID }, KEY_SID + "=" + message.getSid(), null, null, null, null);
    int numRows = cursor.getCount();
    cursor.close();
    return numRows > 0;
  }
  
  public LandmarkMessage fetchLandmarkMessage(String sid) {
    Cursor cursor = mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_SID, KEY_TID, KEY_INSERT_TIMESTAMP, KEY_CONTENT, KEY_TIMESTAMP, KEY_LATITUDE, KEY_LONGITUDE, KEY_DIAMETER, KEY_DEVICEID, KEY_ALTITUDE, KEY_PICID }, KEY_SID + "=" + sid, null, null, null, null);
    if (cursor != null)
      cursor.moveToFirst();
    return fromCursor(cursor);
  }
  
  public LandmarkMessage fetchLandmarkMessage(long rowid) {
    Cursor cursor = mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_SID, KEY_TID, KEY_INSERT_TIMESTAMP, KEY_CONTENT, KEY_TIMESTAMP, KEY_LATITUDE, KEY_LONGITUDE, KEY_DIAMETER, KEY_DEVICEID, KEY_ALTITUDE, KEY_PICID }, KEY_ROWID + "=" + rowid, null, null, null, null);
    if (cursor != null)
      cursor.moveToFirst();
    return fromCursor(cursor);
  }
  
  public LandmarkMessage fetchLastThreadMessage(int tid) {
    Cursor cursor = mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_SID, KEY_TID, KEY_INSERT_TIMESTAMP, KEY_CONTENT, KEY_TIMESTAMP, KEY_LATITUDE, KEY_LONGITUDE, KEY_DIAMETER, KEY_DEVICEID, KEY_ALTITUDE, KEY_PICID }, KEY_TID + "=" + tid, null, null, null, "_id DESC", "1");
    if (cursor != null)
      cursor.moveToFirst();
    return fromCursor(cursor);
  }
  
  private LandmarkMessage fromCursor(Cursor cursor) {
    if (cursor == null || cursor.getCount() == 0) return null;
    LandmarkMessage message = new LandmarkMessage(
        MessageType.INFO,
        Integer.valueOf(cursor.getString(cursor.getColumnIndex(KEY_SID))),
        Integer.valueOf(cursor.getString(cursor.getColumnIndex(KEY_TID))),
        Long.valueOf(cursor.getString(cursor.getColumnIndex(KEY_INSERT_TIMESTAMP))),
        cursor.getString(cursor.getColumnIndex(KEY_DEVICEID)),
        cursor.getString(cursor.getColumnIndex(KEY_CONTENT)),
        Double.valueOf(cursor.getString(cursor.getColumnIndex(KEY_LATITUDE))),
        Double.valueOf(cursor.getString(cursor.getColumnIndex(KEY_LONGITUDE))),
        Double.valueOf(cursor.getString(cursor.getColumnIndex(KEY_ALTITUDE))),
        Float.valueOf(cursor.getString(cursor.getColumnIndex(KEY_DIAMETER))),
        Long.valueOf(cursor.getString(cursor.getColumnIndex(KEY_TIMESTAMP))),
        Integer.valueOf(cursor.getString(cursor.getColumnIndex(KEY_PICID)))
    );
    cursor.close();
    return message;
  }
  
  public void updateLandmark(LandmarkMessage message) {
    int sid = message.getSid();
    Cursor cursor = mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID }, "sid = " + sid, null, null, null, null);
    int numRows = cursor.getCount();
    if (numRows == 0) {
      insertLandmark(message);
    }
    cursor.close();
  }
  
  public void deleteAllLandmarks() {
    mDb.execSQL("DELETE FROM " + DATABASE_TABLE);
  }
}
