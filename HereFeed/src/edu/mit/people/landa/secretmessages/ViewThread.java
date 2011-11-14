package edu.mit.people.landa.secretmessages;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class ViewThread extends MapActivity {
  private static final String TAG = "PostMessage";
  public static final String ACTION_NOTIFY_MESSAGE = "edu.mit.people.landa.secretmessages.notifymessage";
  
  private Location myLocation;
  private LocationManager locationManager;
  private TelephonyManager telephonyManager;
  private LocationListener locationListener = new LocationListener() {

    public void onLocationChanged(Location location) {
      myLocation = location;
    }
    public void onProviderDisabled(String provider) { }
    public void onProviderEnabled(String provider) { }
    public void onStatusChanged(String provider, int status, Bundle extras) { }
    
  };
  
  private List<Overlay> mapOverlays;
  private MessageItemizedOverlay itemizedOverlay;
  private Drawable drawable;
  private LandmarkMessage currentMessage;
  private LandmarksDbAdapter mDbHelper;
  private long rowid;
  private String sid;
  
  private ListView contentDisplay;
  private Button reply;
  private MapView mapView;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.view_thread);
    
    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, locationListener);
    if (myLocation == null)
      myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    
    telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    
    findAllViews();
    
    mDbHelper = new LandmarksDbAdapter(this);
    mDbHelper.open();

    rowid = getIntent().getLongExtra("rowid", -1);
    if (rowid == -1) {
      sid = getIntent().getStringExtra("sid");
      currentMessage = mDbHelper.fetchLandmarkMessage(sid);
    }
    else {
      currentMessage = mDbHelper.fetchLandmarkMessage(rowid);
    }
    
    IntentFilter filter = new IntentFilter(ACTION_NOTIFY_MESSAGE);
    registerReceiver(receiver, filter);
    
    fillData();
    
    mapOverlays = mapView.getOverlays();
    drawable = getResources().getDrawable(R.drawable.text);
    itemizedOverlay = new MessageItemizedOverlay(drawable);
    itemizedOverlay.addOverlay(currentMessage);
    mapOverlays.add(itemizedOverlay);
    mapView.getController().setCenter(currentMessage.getPoint());
    mapView.getController().setZoom(18);
    mapView.setSatellite(true);
  }
  private void fillData() {
    Cursor cursor = mDbHelper.fetchThreadLandmarks(currentMessage.getTid());
    startManagingCursor(cursor);
    String[] from = new String[] { LandmarksDbAdapter.KEY_PICID, LandmarksDbAdapter.KEY_CONTENT, LandmarksDbAdapter.KEY_TIMESTAMP };
    int[] to = new int[] { R.id.message_picture, R.id.message_content, R.id.thread_message_timestamp };
    ThreadAdapter landmarks = new ThreadAdapter(this, R.layout.threads_row, cursor, from, to);
    contentDisplay.setAdapter(landmarks);
  }
  private void findAllViews() {
    contentDisplay = (ListView) findViewById(R.id.content_display);
    reply = (Button) findViewById(R.id.reply);
    mapView = (MapView) findViewById(R.id.mapview);
    
    contentDisplay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Intent intent = new Intent(getApplicationContext(), ViewImage.class);
        intent.putExtra("id", arg3);
        startActivity(intent);
      }
    });
    reply.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), ComposeMessage.class);
        intent.putExtra("tid", currentMessage.getTid());
        startActivity(intent);
      }
    });
  }
  /**
   * Receives the proximity alert broadcasts.
   */
  private BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context arg0, Intent arg1) {
      int sid = arg1.getIntExtra("notification_sid", -1);
      if (sid != -1) {
        fillData();
      }
    }
  };
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
    unregisterReceiver(receiver);
    mDbHelper.close();
    super.onDestroy();
  }
}