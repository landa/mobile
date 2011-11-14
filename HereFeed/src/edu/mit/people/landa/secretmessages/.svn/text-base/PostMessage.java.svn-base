package edu.mit.people.landa.secretmessages;

import java.util.List;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class PostMessage extends MapActivity {
  private static final String TAG = "PostMessage";
  private static final String PICTURE_STORAGE = Environment.getExternalStorageDirectory() + "/HereFeed";
  public static final String ACTION_NOTIFY_MESSAGE = "edu.mit.people.landa.secretmessages.notifymessage";
  
  private Location myLocation;
  private LocationManager locationManager;
  private TelephonyManager telephonyManager;
  private LocationListener locationListener = new LocationListener() {

    public void onLocationChanged(Location location) {
      if (myLocation.getAccuracy() > location.getAccuracy() && location.getAccuracy() != 0) {
        myLocation = location;
        updateMap();
      }
    }
    public void onProviderDisabled(String provider) { }
    public void onProviderEnabled(String provider) { }
    public void onStatusChanged(String provider, int status, Bundle extras) {
      if (myLocation == null && (status == LocationProvider.TEMPORARILY_UNAVAILABLE || status == LocationProvider.OUT_OF_SERVICE)) {
        myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        updateMap();
      }
    }
    
  };
  
  private List<Overlay> mapOverlays;
  private MessageItemizedOverlay itemizedOverlay;
  private Drawable drawable;
  private LandmarkMessage currentMessage;
  
  private TextView replyContent;
  private TextView contentDisplay;
  private Button saveMessage;
  private MapView mapView;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.post_message);
    
    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, locationListener);
    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 2, locationListener);
    myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    
    findAllViews();
    Intent intent = getIntent();
    String content = intent.getStringExtra("content");
    contentDisplay.setText("\"" + content + "\"");
    mapView.getController().setZoom(18);
    mapView.setSatellite(true);
  }
  private void findAllViews() {
    replyContent = (TextView) findViewById(R.id.reply_content);
    contentDisplay = (TextView) findViewById(R.id.content_display);
    saveMessage = (Button) findViewById(R.id.save_post);
    mapView = (MapView) findViewById(R.id.mapview);
    
    Intent intent = getIntent();
    int tid = intent.getIntExtra("tid", 0);
    if (tid != 0) {
      replyContent.setVisibility(View.VISIBLE);
      LandmarksDbAdapter db = new LandmarksDbAdapter(this);
      db.open();
      LandmarkMessage reply = db.fetchLastThreadMessage(tid);
      String text = reply.getMContent();
      if (text.length() > 50) {
        String elipses = elipses(text);
        text = "\"" + elipses + "\"";
      }
      else
        text = "\"" + text + "\"";
      replyContent.setText("Replying to " + text);
      saveMessage.setText("Post Reply");
    }
    
    mapView.setBuiltInZoomControls(true);
    saveMessage.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        setCurrentMessage();
        new Thread(new Runnable() {
          public void run() {
            currentMessage.saveMessage();
            LandmarksDbAdapter db = new LandmarksDbAdapter(getApplicationContext());
//            db.open();
//            db.insertLandmark(currentMessage);
//            db.close();
            Intent intent = new Intent(ACTION_NOTIFY_MESSAGE);
            intent.putExtra("notification_sid", currentMessage.getSid());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), currentMessage.getSid(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
              pendingIntent.send();
            }
            catch (CanceledException e) {
              e.printStackTrace();
            }
          }
        }).start();
        Intent intent = new Intent();
        intent.putExtra("tid", String.valueOf(currentMessage.getTid()));
        setResult(RESULT_OK, intent);
        finish();
      }
    });
  }
  private void setCurrentMessage() {
    // Extract the content of the post
    Intent intent = getIntent();
    String content = intent.getStringExtra("content");
    contentDisplay.setText("\"" + content + "\"");
    boolean picture = intent.getBooleanExtra("picture", false);
    String picturePath = intent.getStringExtra("picture_path");
    int tid = intent.getIntExtra("tid", 0);
    
    // Create the LandmarkMessage object with this information
    currentMessage = new LandmarkMessage(MessageType.INFO, 0, tid, content, myLocation, telephonyManager.getDeviceId());
    mapView.getController().setCenter(currentMessage.getPoint());
    if (picture == true) {
      currentMessage.addPicture(picturePath);
    }
  }
  private void updateMap() {
    mapOverlays = mapView.getOverlays();
    if (itemizedOverlay != null)
      mapOverlays.remove(itemizedOverlay);
    drawable = getResources().getDrawable(R.drawable.text);
    itemizedOverlay = new MessageItemizedOverlay(drawable);
    setCurrentMessage();
    itemizedOverlay.addOverlay(currentMessage);
    mapOverlays.add(itemizedOverlay);
  }
  @Override
  protected boolean isRouteDisplayed() {
    return false;
  }
  /* (non-Javadoc)
   * @see com.google.android.maps.MapActivity#onDestroy()
   */
  @Override
  protected void onDestroy() {
    locationManager.removeUpdates(locationListener);
    locationManager = null;
    super.onDestroy();
  }
  public static String elipses(String text) {
    String elipses = text.substring(0, 50);
    if (!elipses.equals(text)) {
      if (elipses.charAt(elipses.length() - 1) == ' ')
        elipses = elipses.substring(0, elipses.length() - 1);
      else {
        int spaceIndex = text.indexOf(" ", elipses.length());
        if (spaceIndex == -1) {
          elipses = text;
        }
        else {
          elipses += text.substring(elipses.length(), spaceIndex) + "...";
        }
      }
    }
    return elipses;
  }
}