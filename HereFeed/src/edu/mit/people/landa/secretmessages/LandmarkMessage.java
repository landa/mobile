package edu.mit.people.landa.secretmessages;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.location.Location;
import android.os.Environment;
import android.text.format.DateFormat;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class LandmarkMessage extends OverlayItem {
  public static final String TAG = "LandmarkMessage";
  public static final String PREFS_NAME = "HereFeed";
  private static final String PICTURE_STORAGE = Environment.getExternalStorageDirectory() + "/HereFeed";

  private int sid;
  private int tid;
  private MessageType mType;
  private String mContent;
  private String deviceId;

  private double mLatitude;
  private double mLongitude;
  private double mAltitude;
  private float mAccuracy;
  private long mTimestamp;
  private String picture = null;
  private int mPictureId;
  private long mInsertTimestamp;

  public LandmarkMessage(MessageType type, int sid, long insertTimestamp, String deviceId, String content, double latitude, double longitude, double altitude, float accuracy, long timestamp, int picId) {
    super(new GeoPoint((int) Math.round(Double.valueOf(latitude) * 1000000), (int) Math.round(Double.valueOf(longitude) * 1000000)), "Landmark", content);

    tid = 0;
    mType = type;
    mContent = content;
    mLatitude = latitude;
    mLongitude = longitude;
    mAltitude = altitude;
    mAccuracy = accuracy;
    mTimestamp = timestamp;
    mInsertTimestamp = insertTimestamp;
    mPictureId = picId;
    this.deviceId = deviceId;
    this.sid = sid;
  }
  public LandmarkMessage(MessageType type, int sid, int tid, String deviceId, String content, double latitude, double longitude, double altitude, float accuracy, long timestamp, int picId) {
    super(new GeoPoint((int) Math.round(Double.valueOf(latitude) * 1000000), (int) Math.round(Double.valueOf(longitude) * 1000000)), "Landmark", content);

    mType = type;
    mContent = content;
    mLatitude = latitude;
    mLongitude = longitude;
    mAltitude = altitude;
    mAccuracy = accuracy;
    mTimestamp = timestamp;
    mInsertTimestamp = 0;
    mPictureId = picId;
    this.deviceId = deviceId;
    this.sid = sid;
    this.tid = tid;
  }
  public LandmarkMessage(MessageType type, int sid, int tid, long insertTimestamp, String deviceId, String content, double latitude, double longitude, double altitude, float accuracy, long timestamp, int picId) {
    super(new GeoPoint((int) Math.round(Double.valueOf(latitude) * 1000000), (int) Math.round(Double.valueOf(longitude) * 1000000)), "Landmark", content);

    mType = type;
    mContent = content;
    mLatitude = latitude;
    mLongitude = longitude;
    mAltitude = altitude;
    mAccuracy = accuracy;
    mTimestamp = timestamp;
    mInsertTimestamp = 0;
    mPictureId = picId;
    this.deviceId = deviceId;
    this.sid = sid;
    this.tid = tid;
  }
  public LandmarkMessage(MessageType type, int sid, long insertTimestamp, String content, Location location, String deviceId) {
    this(type, sid, insertTimestamp, deviceId, content, location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy(), location.getTime(), 0);
  }
  public LandmarkMessage(MessageType type, int sid, int tid, String content, Location location, String deviceId) {
    this(type, sid, tid, 0, deviceId, content, location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy(), location.getTime(), 0);
  }
  public void addPicture(String path) {
    if (path != null && !path.equals("")) {
      picture = path;
    }
  }
  public void saveMessage() {
    try {
      HashMap<String, String> params = new HashMap<String, String>();
      params.put("type", mType.toString());
      params.put("content", mContent);
      params.put("latitude", String.valueOf(mLatitude));
      params.put("longitude", String.valueOf(mLongitude));
      params.put("altitude", String.valueOf(mAltitude));
      params.put("accuracy", String.valueOf(mAccuracy));
      params.put("timestamp", String.valueOf(mTimestamp));
      params.put("tid", String.valueOf(tid));
      params.put("device_id", deviceId);
      JSONObject responseJSON = Request.post(Constants.WEB_SAVE_MESSAGE, params);
      sid = responseJSON.getInt("sid");
      tid = responseJSON.getInt("tid");
      if (picture != null) {
        File curPic = new File(picture);
        curPic.renameTo(new File(PICTURE_STORAGE + "/" + String.valueOf(sid)));
        File oldPic = new File(picture);
        curPic.renameTo(oldPic);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  public static String relativeTime(String milliseconds) {
    if (milliseconds.equals("never")) return "never";
    long seconds = Long.valueOf(milliseconds) / 1000;
    long now = System.currentTimeMillis() / 1000;
    long diff = now - seconds;
    if (diff < 30) return "a few seconds ago";
    else if (diff < 60 * 2) return "about a minute ago";
    else if (diff < 3600 * 3) return String.valueOf(Math.round(diff / 60)) + " minutes ago";
    else if (diff < 3600 * 24) return String.valueOf(Math.round(diff / 3600)) + " hours ago";
    else if (diff < 3600 * 24 * 2) return "yesterday";
    else if (diff < 3600 * 24 * 31) return String.valueOf(Math.round(diff / (3600 * 24))) + " days ago";
    else return DateFormat.format("MMMM " + DateFormat.DATE + ", yyyy", new Date(seconds * 1000)).toString();
  }
  public static List<LandmarkMessage> getNearbyMessages(Location near, int start, int end) throws Exception {
    if (end == 0) end = start + 10;
    ArrayList<LandmarkMessage> ret = new ArrayList<LandmarkMessage>();
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("latitude", String.valueOf(near.getLatitude()));
    params.put("longitude", String.valueOf(near.getLongitude()));
    params.put("start", String.valueOf(start));
    params.put("end", String.valueOf(end));
    JSONObject resp = Request.get(Constants.WEB_GET_MESSAGES, params);
    JSONArray messages = resp.getJSONArray("messages");
    for (int x = 0; x < messages.length(); x++) {
      JSONObject message = messages.getJSONObject(x);
      LandmarkMessage currentMessage = new LandmarkMessage(
                                             MessageType.INFO, message.getInt("sid"),
                                             message.getInt("tid"),
                                             message.getString("device_id"),
                                             message.getString("content"),
                                             message.getDouble("latitude"),
                                             message.getDouble("longitude"),
                                             message.getDouble("altitude"),
                                             Float.valueOf(message.getString("diameter")),
                                             message.getLong("timestamp"),
                                             message.getInt("picId")
                                           );
      ret.add(currentMessage);
    }
    return ret;
  }
  public boolean hasPicture() {
    if ((picture == null || picture.equals("")) && mPictureId == 0) {
      return false;
    }
    else {
      return true;
    }
  }
  /**
   * @return the mPictureId
   */
  public int getMPictureId() {
    return mPictureId;
  }
  /**
   * @param pictureId the mPictureId to set
   */
  public void setMPictureId(int pictureId) {
    mPictureId = pictureId;
  }
  /**
   * @return the mType
   */
  public MessageType getMType() {
    return mType;
  }
  /**
   * @return the sid
   */
  public int getSid() {
    return sid;
  }
  /**
   * @param sid the sid to set
   */
  public void setSid(int sid) {
    this.sid = sid;
  }
  /**
   * @return the tid
   */
  public int getTid() {
    return tid;
  }
  /**
   * @param tid the tid to set
   */
  public void setTid(int tid) {
    this.tid = tid;
  }
  /**
   * @param type the mType to set
   */
  public void setMType(MessageType type) {
    mType = type;
  }
  /**
   * @return the mContent
   */
  public String getMContent() {
    return mContent;
  }
  /**
   * @param content the mContent to set
   */
  public void setMContent(String content) {
    mContent = content;
  }
  /**
   * @return the deviceId
   */
  public String getDeviceId() {
    return deviceId;
  }
  /**
   * @param deviceId the deviceId to set
   */
  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }
  /**
   * @return the mLatitude
   */
  public double getMLatitude() {
    return mLatitude;
  }
  /**
   * @param latitude the mLatitude to set
   */
  public void setMLatitude(double latitude) {
    mLatitude = latitude;
  }
  /**
   * @return the mLongitude
   */
  public double getMLongitude() {
    return mLongitude;
  }
  /**
   * @param longitude the mLongitude to set
   */
  public void setMLongitude(double longitude) {
    mLongitude = longitude;
  }
  /**
   * @return the mAltitude
   */
  public double getMAltitude() {
    return mAltitude;
  }
  /**
   * @param altitude the mAltitude to set
   */
  public void setMAltitude(double altitude) {
    mAltitude = altitude;
  }
  /**
   * @return the mAccuracy
   */
  public float getMAccuracy() {
    return mAccuracy;
  }
  /**
   * @param accuracy the mAccuracy to set
   */
  public void setMAccuracy(float accuracy) {
    mAccuracy = accuracy;
  }
  /**
   * @return the mTimestamp
   */
  public long getMTimestamp() {
    return mTimestamp;
  }
  /**
   * @param timestamp the mTimestamp to set
   */
  public void setMTimestamp(long timestamp) {
    mTimestamp = timestamp;
  }
  /**
   * @return the mInsertTimestamp
   */
  public long getMInsertTimestamp() {
    return mInsertTimestamp;
  }
  /**
   * @param insertTimestamp the mInsertTimestamp to set
   */
  public void setMInsertTimestamp(long insertTimestamp) {
    mInsertTimestamp = insertTimestamp;
  }
}
