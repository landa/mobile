package edu.mit.people.landa.secretmessages;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class Welcome extends Activity {
  private static final String TAG = "SecretMessages";
  private static final String PREFS_NAME = "HereFeed";
  
  private SettingsDbAdapter settings;
  
  private Button continueButton;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.welcome);
    findAllViews();
    continueButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("show_intro", false);
        editor.commit();
        finish();
      }
    });
  }
  private void findAllViews() {
    continueButton = (Button) findViewById(R.id.agree);
  }
}