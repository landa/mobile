/**
 * 
 */
package edu.mit.people.landa.secretmessages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author landa
 *
 */
public class LandmarkBootBroadcastReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context ctx, Intent intent) {
    SettingsDbAdapter settings = new SettingsDbAdapter(ctx);
    settings.open();
    boolean serviceEnabled = Boolean.valueOf(settings.read("service_enabled", "true"));
    settings.close();
    if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) && serviceEnabled) {
      Intent service = new Intent(ctx, LandmarkService.class);
      ctx.startService(service);
    }
  }

}
