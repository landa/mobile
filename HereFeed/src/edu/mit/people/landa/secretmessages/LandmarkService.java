package edu.mit.people.landa.secretmessages;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

public class LandmarkService extends Service {
  public static final String TAG = "LandmarkService";
  public static final String PREFS_NAME = "HereFeed";
  public static final String ACTION_NOTIFY_MESSAGE = "edu.mit.people.landa.secretmessages.notifymessage";
  public static final String ACTION_REQUEST_MESSAGES = "edu.mit.people.landa.secretmessages.requestmessages";
  public static final String ACTION_WAKEUP = "edu.mit.people.landa.secretmessages.wakeup";

  private static final int MESSAGE_ID = 1;
  private static final int LOCATION_LIST_INTERVAL_SHORT = 300000;
  private static final int HIBERNATION_INTERVAL = 5400000;

  private static final int icon = R.drawable.icon;
  private static final long[] vibrate = { 0, 20 };
  private LocationManager mLocationManager;
  private NotificationManager mNotificationManager;
  private AlarmManager am;
  private String locationProvider;
  private int locationAccuracy;
  private Criteria criteria;
  private Handler handler;
  private PendingIntent requestMessagesPendingIntent;
  private PendingIntent wakeUpPendingIntent;
  private int pendingIntentNotificationCounter;

  private Map<Integer, LandmarkMessage> discoverableMessages;
  private Map<Integer, PendingIntent> proximityAlerts;
  private long lastVibrateTime;
  private int locationListInterval;
  private int sameLocationCounter;
  private int sameMessagesCounter;
  private Location prevLocation;
  private Location myLocation;
  private LocationListener locationListener;
  private LocationListener mGPSLocationListener = new LocationListener() {
    public void onLocationChanged(Location location) {
      prevLocation = myLocation;
      myLocation = location;
      mLocationManager.removeUpdates(this);
      if (myLocation.distanceTo(prevLocation) < myLocation.getAccuracy()/2) sameLocationCounter++;
      else sameLocationCounter = 0;
      if (sameLocationCounter >= 5 && sameMessagesCounter >= 5 || sameLocationCounter + sameMessagesCounter >= 10) hibernate();
    }
    public void onProviderDisabled(String provider) {
    }
    public void onProviderEnabled(String provider) {
    }
    public void onStatusChanged(String provider, int status, Bundle extras) {
      if (status == LocationProvider.AVAILABLE) {
        useGPSProvider();
      }
      else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE || status == LocationProvider.OUT_OF_SERVICE) {
        useNetworkProvider();
      }
    }
  };
  private LocationListener mNetworkLocationListener = new LocationListener() {
    public void onLocationChanged(Location location) {
      prevLocation = myLocation;
      myLocation = location;
      if (myLocation.distanceTo(prevLocation) < myLocation.getAccuracy()/2) sameLocationCounter++;
      else sameLocationCounter = 0;
      if (sameLocationCounter >= 5 && sameMessagesCounter >= 5 || sameLocationCounter + sameMessagesCounter >= 20) hibernate();
    }
    public void onProviderDisabled(String provider) {
    }
    public void onProviderEnabled(String provider) {
    }
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
  };

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Service#onStart(android.content.Intent, int)
   */
  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);

    handler = new Handler();
    
    lastVibrateTime = 0;
    pendingIntentNotificationCounter = 0;
    sameLocationCounter = 0;
    sameMessagesCounter = 0;

    useGPSProvider();
    wakeup();
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Service#onCreate()
   */
  @Override
  public void onCreate() {
    super.onCreate();

    discoverableMessages = new HashMap<Integer, LandmarkMessage>();
    proximityAlerts = new HashMap<Integer, PendingIntent>();

    mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    
    int icon = R.drawable.icon;
    Notification notification = new Notification(icon, "I'm on the lookout for messages..", System.currentTimeMillis());
    Intent view = new Intent(this, LandmarkSettings.class);
    notification.setLatestEventInfo(this, "Looking for messages...", "Tap to disable or change your preferences.", PendingIntent.getActivity(this, 0, view, PendingIntent.FLAG_CANCEL_CURRENT));
    notification.defaults |= Notification.DEFAULT_SOUND;
    mNotificationManager.notify(0, notification);
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        mNotificationManager.cancel(0);
      }
    }, 30000);
    
    Intent requestMessagesIntent = new Intent(ACTION_REQUEST_MESSAGES);
    pendingIntentNotificationCounter += 1;
    requestMessagesPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), pendingIntentNotificationCounter, requestMessagesIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    
    Intent wakeupIntent = new Intent(ACTION_WAKEUP);
    pendingIntentNotificationCounter += 1;
    wakeUpPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), pendingIntentNotificationCounter, wakeupIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    IntentFilter notifyFilter = new IntentFilter(ACTION_NOTIFY_MESSAGE);
    getApplicationContext().registerReceiver(notifyReceiver, notifyFilter);
    
    IntentFilter requestFilter = new IntentFilter(ACTION_REQUEST_MESSAGES);
    getApplicationContext().registerReceiver(requestReceiver, requestFilter);
    
    IntentFilter wakeupFilter = new IntentFilter(ACTION_WAKEUP);
    getApplicationContext().registerReceiver(wakeupReceiver, wakeupFilter);
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Service#onDestroy()
   */
  @Override
  public void onDestroy() {
    removeAllAlerts();
    mLocationManager.removeUpdates(mGPSLocationListener);
    mLocationManager.removeUpdates(mNetworkLocationListener);
    super.onDestroy();
  }

  /**
   * Queues a notification for display. Does not vibrate more than once every vibrate_period minutes.
   * 
   * @param message The message to be displayed to the user
   */
  private void queueNotification(LandmarkMessage message) {
    // Prepare the notification
    Notification notification = new Notification(icon, message.getMContent(), System.currentTimeMillis());
    Intent view = new Intent(getApplicationContext(), ViewThread.class);
    view.putExtra("sid", String.valueOf(message.getSid()));
    pendingIntentNotificationCounter += 1;
    notification.flags |= Notification.FLAG_AUTO_CANCEL;
    notification.setLatestEventInfo(getApplicationContext(), "Found a message using HereFeed", message.getMContent(), PendingIntent.getActivity(getApplicationContext(), pendingIntentNotificationCounter, view, PendingIntent.FLAG_CANCEL_CURRENT));
    SettingsDbAdapter settings = new SettingsDbAdapter(getApplicationContext()); // Get preferences access
    settings.open();
    // Check whether it's okay to vibrate again (don't want to vibrate every single message)
    if (System.currentTimeMillis() - lastVibrateTime > 2000 && Boolean.valueOf(settings.read("vibration_enabled", "true"))) {
      notification.defaults |= Notification.DEFAULT_VIBRATE;
      notification.vibrate = vibrate;
      lastVibrateTime = System.currentTimeMillis();
    }
    settings.close();
    // Notify the user using the prepared notification message
    mNotificationManager.notify(MESSAGE_ID, notification);
  }

  private void renewLocationListener() {
    criteria = new Criteria();
    criteria.setCostAllowed(false);
    criteria.setAccuracy(locationAccuracy);
    locationProvider = mLocationManager.getBestProvider(criteria, true);
  }

  /**
   * Sets proximity alerts for nearby messages and prepares them for future discovery.
   */
  private Runnable updateLandmarks = new Runnable() {
    public void run() {
      try {
        // Get all of the surrounding messages
        Map<Integer, LandmarkMessage> temp = getNearbyMessages(0, 0);
        LandmarksDbAdapter db = new LandmarksDbAdapter(getApplicationContext());
        db.open();
        boolean somethingChanged = false;
        for (LandmarkMessage message : temp.values()) {
          // Check that the message has not already been processed (already in the messages list)
          // and that it has not already been discovered by the user (present in the database)
          if (!discoverableMessages.containsKey(message.getSid()) && !db.landmarkExists(message)) {
            somethingChanged = true;
            // Add it to the processed list
            discoverableMessages.put(message.getSid(), message);
            // Prepare the broadcast
            Intent intent = new Intent(ACTION_NOTIFY_MESSAGE);
            intent.putExtra("notification_sid", message.getSid());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), message.getSid(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            // Set a proximity alert that fires the broadcast when the user enters the message's proximity
            mLocationManager.addProximityAlert(message.getMLatitude(), message.getMLongitude(), message.getMAccuracy(), -1, pendingIntent);
            // Add the proximity alert to a list
            proximityAlerts.put(message.getSid(), pendingIntent);
          }
        }
        db.close();
        /*for (LandmarkMessage queueMessage : discoverableMessages.values()) {
          if (!temp.containsValue(queueMessage)) {
            somethingChanged = true;
            discoverableMessages.remove(queueMessage.getSid());
          }
        }*/
        if (somethingChanged) {
          sameMessagesCounter = 0;
        }
        else {
          sameMessagesCounter += 1;
          if (sameLocationCounter >= 5 && sameMessagesCounter >= 5 || sameLocationCounter + sameMessagesCounter >= 10) hibernate();
        }
        // Update the timestamp
        renewedMessageList();
      }
      catch (Exception e) {
        sameMessagesCounter += 1;
        if (sameLocationCounter >= 5 && sameMessagesCounter >= 5 || sameLocationCounter + sameMessagesCounter >= 10) hibernate();
      }
    }
  };

  /**
   * Retrieves nearby messages from the web.
   * 
   * @param near The location around which we want to find messages
   * @param start Not in use
   * @param end Not in use
   * @return A map from a sid to its message for all nearby messages
   * @throws Exception Can't access the Internet
   */
  private Map<Integer, LandmarkMessage> getNearbyMessages(int start, int end) throws Exception {
    if (myLocation == null) myLocation = mLocationManager.getLastKnownLocation(locationProvider);
    if (myLocation.getAccuracy() == 0 || System.currentTimeMillis() - myLocation.getTime() > locationListInterval*0.7) {
      mLocationManager.requestLocationUpdates(locationProvider, locationListInterval/2, 1000, locationListener);
    }
    if (end == 0) end = start + 10;
    HashMap<Integer, LandmarkMessage> ret = new HashMap<Integer, LandmarkMessage>();
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("latitude", String.valueOf(myLocation.getLatitude()));
    params.put("longitude", String.valueOf(myLocation.getLongitude()));
    params.put("start", String.valueOf(start));
    params.put("end", String.valueOf(end));
    JSONObject resp = Request.get(Constants.WEB_GET_MESSAGES, params);
    JSONArray messages = resp.getJSONArray("messages");
    for (int x = 0; x < messages.length(); x++) {
      JSONObject message = messages.getJSONObject(x);
      LandmarkMessage currentMessage = new LandmarkMessage(
                                             MessageType.INFO,
                                             message.getInt("sid"),
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
      ret.put(message.getInt("sid"), currentMessage);
    }
    return ret;
  }

  /**
   * Receives the proximity alert broadcasts.
   */
  private BroadcastReceiver notifyReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context arg0, Intent arg1) {
      int sid = arg1.getIntExtra("notification_sid", -1);
      if (sid != -1) {
        // Construct the landmark message object
        LandmarkMessage message = discoverableMessages.get(sid);
        if (message == null) return;
        wakeup();
        // Insert the message into the database if it doesn't already exist
        LandmarksDbAdapter db = new LandmarksDbAdapter(arg0);
        db.open();
        db.updateLandmark(message);
        db.close();
        queueNotification(message);
        // Remove the proximity alert so it doesn't trigger again
        mLocationManager.removeProximityAlert(proximityAlerts.get(sid));
        // Remove it from the discoverable message list
        discoverableMessages.remove(sid);
      }
    }
  };
  
  /**
   * Receives the message request broadcasts.
   */
  private BroadcastReceiver requestReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context arg0, Intent arg1) {
      handler.post(updateLandmarks);
    }
  };
  
  /**
   * Receives the wake up broadcasts.
   */
  private BroadcastReceiver wakeupReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context arg0, Intent arg1) {
      wakeup();
    }
  };

  private void useGPSProvider() {
    if (locationAccuracy != Criteria.ACCURACY_FINE) {
      locationAccuracy = Criteria.ACCURACY_FINE;
      locationListener = mGPSLocationListener;
      renewLocationListener();
    }
  }

  private void useNetworkProvider() {
    if (locationAccuracy != Criteria.ACCURACY_COARSE) {
      locationAccuracy = Criteria.ACCURACY_COARSE;
      locationListener = mNetworkLocationListener;
      renewLocationListener();
    }
  }

  private void hibernate() {
    mLocationManager.removeUpdates(mGPSLocationListener);
    mLocationManager.removeUpdates(mNetworkLocationListener);
    removeAllAlerts();
    // Cancel web requests
    am.cancel(requestMessagesPendingIntent);
//    am.setInexactRepeating(AlarmManager.RTC, AlarmManager.INTERVAL_HALF_HOUR, ++locationListInterval, requestMessagesPendingIntent);
    // Plan to wake up later
    am.set(AlarmManager.RTC, System.currentTimeMillis() + HIBERNATION_INTERVAL, wakeUpPendingIntent);
  }

  private void wakeup() {
    sameLocationCounter = 0;
    sameMessagesCounter = 0;
    // Stop the wake up action
    am.cancel(wakeUpPendingIntent);
    
    // Schedule web requests to occur frequently
    locationListInterval = LOCATION_LIST_INTERVAL_SHORT;
    am.setInexactRepeating(AlarmManager.RTC, AlarmManager.INTERVAL_FIFTEEN_MINUTES, locationListInterval, requestMessagesPendingIntent);
  }

  private void removeAllAlerts() {
    for (PendingIntent pendingIntent : proximityAlerts.values()) {
      mLocationManager.removeProximityAlert(pendingIntent);
      pendingIntent.cancel();
      proximityAlerts.remove(pendingIntent);
    }
    proximityAlerts = new HashMap<Integer, PendingIntent>();
    discoverableMessages = new HashMap<Integer, LandmarkMessage>();
  }

  /**
   * Updates the setting that provides the timestamp information for the "last updated ..." UI element
   */
  private void renewedMessageList() {
    SettingsDbAdapter settings = new SettingsDbAdapter(getApplicationContext());
    settings.open();
    settings.write("last_updated", String.valueOf(System.currentTimeMillis()));
    settings.close();
  }

}
