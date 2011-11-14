package edu.mit.people.landa.secretmessages;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class Feed extends Activity {
  private static final String TAG = "PostMessage";
  private static final String PREFS_NAME = "HereFeed";

  public static final int PREFS_ID = Menu.FIRST;
  
  public static final int ACTIVITY_COMPOSE = 0;
  public static final String ACTION_NOTIFY_MESSAGE = "edu.mit.people.landa.secretmessages.notifymessage";

  private TextView lastUpdated;
  private ListView messageList;
  private Button moreMessages;
  private Button changeSettings;
  private Button createLandmark;

  private LandmarksDbAdapter mDbHelper;
  private SettingsDbAdapter settings;
  private Timer timer;
  private Handler handler;
  private Runnable refreshLastUpdated = new Runnable() {
    public void run() {
      fillData();
      settings.open();
      lastUpdated.setText("last updated " + LandmarkMessage.relativeTime(settings.read("last_updated", "never")).toLowerCase());
      settings.close();
    }
  };
  private Location myLocation;
  private LocationManager locationManager;
  private LocationListener locationListener = new LocationListener() {
    public void onLocationChanged(Location location) {
      myLocation = location;
    }
    public void onProviderDisabled(String provider) {
    }
    public void onProviderEnabled(String provider) {
    }
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
  };

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.feed);
    SharedPreferences intro = getSharedPreferences(PREFS_NAME, 0);
    boolean showIntro = intro.getBoolean("show_intro", true);
    if (showIntro) {
      Intent intent = new Intent(this, Welcome.class);
      startActivity(intent);
    }
    
    mDbHelper = new LandmarksDbAdapter(this);
    mDbHelper.open();

    IntentFilter filter = new IntentFilter(ACTION_NOTIFY_MESSAGE);
    registerReceiver(receiver, filter);

    handler = new Handler();
    timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        handler.post(refreshLastUpdated);
      }
    }, 0, 30000);

    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
    if (myLocation == null) myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

    findAllViews();

    fillData();
  }
  private void findAllViews() {
    lastUpdated = (TextView) findViewById(R.id.last_updated);
    messageList = (ListView) findViewById(android.R.id.list);
    moreMessages = (Button) findViewById(R.id.more_messages);
    changeSettings = (Button) findViewById(R.id.change_settings);
    createLandmark = (Button) findViewById(R.id.create_landmark);

    handler.post(refreshLastUpdated);
    moreMessages.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        int firstVisible = messageList.getScrollY();
        fillData(messageList.getCount()*2);
        messageList.scrollTo(0, firstVisible);
        moreMessages.setEnabled(false);
      }
    });
    changeSettings.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(), LandmarkSettings.class);
        startActivity(intent);
      }
    });
    createLandmark.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        composeLandmark();
      }
    });
    messageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getApplicationContext(), ViewThread.class);
        intent.putExtra("rowid", id);
        startActivity(intent);
      }
    });
    messageList.setOnScrollListener(new OnScrollListener() {
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem >= totalItemCount - visibleItemCount) {
          moreMessages.setEnabled(true);
        }
        else {
          moreMessages.setEnabled(false);
        }
      }
      public void onScrollStateChanged(AbsListView view, int scrollState) {
        
      }
    });
  }
  private void composeLandmark() {
    Intent intent = new Intent(this, ComposeMessage.class);
    startActivityForResult(intent, ACTIVITY_COMPOSE);
  }
  private void fillData() {
    fillData(20);
  }
  private void fillData(int count) {
    Cursor cursor = mDbHelper.fetchAllLandmarks(count);
    startManagingCursor(cursor);
    if (cursor.getCount() != 0) {
      findViewById(android.R.id.empty).setVisibility(View.GONE);
      findViewById(R.id.empty_label).setVisibility(View.GONE);
    }
    else {
      findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
      findViewById(R.id.empty_label).setVisibility(View.VISIBLE);
    }
//    if (cursor.getCount() <= messageList.getCount()) return;
    String[] from = new String[] { LandmarksDbAdapter.KEY_PICID, LandmarksDbAdapter.KEY_CONTENT, LandmarksDbAdapter.KEY_TIMESTAMP, LandmarksDbAdapter.KEY_INSERT_TIMESTAMP };
    int[] to = new int[] { R.id.extra_content, R.id.message_content, R.id.message_timestamp, R.id.message_discovered };
    MessageAdapter landmarks = new MessageAdapter(this, R.layout.messages_row, cursor, from, to);
    messageList.setAdapter(landmarks);
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
  /* (non-Javadoc)
   * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, PREFS_ID, 0, R.string.menu_prefs);
    return super.onCreateOptionsMenu(menu);
  }
  /* (non-Javadoc)
   * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
   */
  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    switch (item.getItemId()) {
    case PREFS_ID:
      Intent intent = new Intent(getApplicationContext(), LandmarkSettings.class);
      startActivity(intent);
      break;
    }
    return super.onMenuItemSelected(featureId, item);
  }
  /*
   * (non-Javadoc)
   * 
   * @see com.google.android.maps.MapActivity#onPause()
   */
  @Override
  protected void onPause() {
    super.onPause();
  }
  /*
   * (non-Javadoc)
   * 
   * @see com.google.android.maps.MapActivity#onResume()
   */
  @Override
  protected void onResume() {
    super.onResume();
    getWindow().setTitle("HereFeed | Review your recent messages");
    settings = new SettingsDbAdapter(getApplicationContext());
    settings.open();
    if (Boolean.valueOf(settings.read("service_enabled", "true")) == true) {
      Intent intent = new Intent(this, LandmarkService.class);
      startService(intent);
    }
    settings.close();
    fillData();
  }
  /*
   * (non-Javadoc)
   * 
   * @see com.google.android.maps.MapActivity#onDestroy()
   */
  @Override
  protected void onDestroy() {
    if (locationManager != null) {
      locationManager.removeUpdates(locationListener);
    }
    locationManager = null;
    mDbHelper.close();
    timer.cancel();
    unregisterReceiver(receiver);
    settings.close();
    super.onDestroy();
  }
}