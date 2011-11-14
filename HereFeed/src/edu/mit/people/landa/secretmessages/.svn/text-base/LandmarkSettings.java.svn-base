package edu.mit.people.landa.secretmessages;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class LandmarkSettings extends Activity {
  private static final String TAG = "SecretMessages";
  private static final String PREFS_NAME = "HereFeed";
  
  private SettingsDbAdapter settings;
  
  private CheckBox serviceEnabled;
  private CheckBox vibrationEnabled;
  private Button clearMessages;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings);
    findAllViews();
    settings = new SettingsDbAdapter(getApplicationContext());
    settings.open();
    serviceEnabled.setChecked(Boolean.valueOf(settings.read("service_enabled", "true")));
    vibrationEnabled.setChecked(Boolean.valueOf(settings.read("vibration_enabled", "true")));
    settings.close();
    serviceEnabled.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) enableService();
        else disableService();
      }
    });
    vibrationEnabled.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) enableVibration();
        else disableVibration();
      }
    });
    clearMessages.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        LandmarksDbAdapter dbHelper = new LandmarksDbAdapter(getApplicationContext());
        dbHelper.open();
        dbHelper.deleteAllLandmarks();
        dbHelper.close();
      }
    });
  }
  private void findAllViews() {
    serviceEnabled = (CheckBox) findViewById(R.id.pref_service);
    vibrationEnabled = (CheckBox) findViewById(R.id.pref_vibrate);
    clearMessages = (Button) findViewById(R.id.clear);
  }
  private void enableService() {
    settings.open();
    settings.write("service_enabled", "true");
    settings.close();
    startService(new Intent(this, LandmarkService.class));
  }
  private void enableVibration() {
    settings.open();
    settings.write("vibration_enabled", "true");
    settings.close();
  }
  private void disableService() {
    settings.open();
    settings.write("service_enabled", "false");
    settings.close();
    stopService(new Intent(this, LandmarkService.class));
  }
  private void disableVibration() {
    settings.open();
    settings.write("vibration_enabled", "false");
    settings.close();
  }
  /* (non-Javadoc)
   * @see android.app.ActivityGroup#onDestroy()
   */
  @Override
  protected void onDestroy() {
    settings.close();
    super.onDestroy();
  }
  /* (non-Javadoc)
   * @see android.app.ActivityGroup#onPause()
   */
  @Override
  protected void onPause() {
    super.onPause();
  }
  /* (non-Javadoc)
   * @see android.app.ActivityGroup#onResume()
   */
  @Override
  protected void onResume() {
    super.onResume();
  }
  /* (non-Javadoc)
   * @see android.app.ActivityGroup#onStop()
   */
  @Override
  protected void onStop() {
    super.onStop();
  }
}