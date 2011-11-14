package edu.mit.people.landa.secretmessages;

import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Window;
import android.widget.ImageView;

public class ViewImage extends Activity {
  private static final String TAG = "ComposeMessage";
  private static final String PICTURE_STORAGE = Environment.getExternalStorageDirectory() + "/Terranota";
  
  private ImageView mPicture;
  private Bitmap pictureBitmap;
  private Bitmap originalBitmap;
  private Handler handler;
  
  private int picid;
  
  private class Downloader implements Runnable {
    int value;

    public Downloader(int value) {
      this.value = value;
    }
    public void run() {
      try {
        originalBitmap = BitmapFactory.decodeStream(new URL(Constants.WEB_VIEW_PICTURE + value).openStream());
        if (getOrientation(originalBitmap).equals(Orientation.LANDSCAPE) && getOrientation(getWindow().getAttributes().width, getWindow().getAttributes().height).equals(Orientation.PORTRAIT)) {
          Matrix matrix = new Matrix();
          matrix.postRotate(90);
          pictureBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
        }
        else {
          pictureBitmap = originalBitmap;
        }
        handler.post(new Runnable() {
          public void run() {
            mPicture.setImageBitmap(pictureBitmap);
          }
        });
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  };
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    setContentView(R.layout.view_image);
    handler = new Handler();
    Intent intent = getIntent();
    
    long id = intent.getLongExtra("id", 0);
    if (id != 0) {
      LandmarksDbAdapter db = new LandmarksDbAdapter(this);
      db.open();
      LandmarkMessage temp = db.fetchLandmarkMessage(id);
      db.close();
      picid = temp.getMPictureId();
    }
    Thread downloader = new Thread(new Downloader(picid));
    downloader.start();
    
    findAllViews();
  }
  private void findAllViews() {
    mPicture = (ImageView) findViewById(R.id.picture);
  }
  private Orientation getOrientation(Bitmap bitmap) {
    if (bitmap.getHeight() < bitmap.getWidth()) return Orientation.LANDSCAPE;
    else return Orientation.PORTRAIT;
  }
  private Orientation getOrientation(int width, int height) {
    if (height < width) return Orientation.LANDSCAPE;
    else return Orientation.PORTRAIT;
  }
  /* (non-Javadoc)
   * @see com.google.android.maps.MapActivity#onPause()
   */
  @Override
  protected void onPause() {
    super.onPause();
  }
  /* (non-Javadoc)
   * @see com.google.android.maps.MapActivity#onResume()
   */
  @Override
  protected void onResume() {
    super.onResume();
  }
  /* (non-Javadoc)
   * @see com.google.android.maps.MapActivity#onDestroy()
   */
  @Override
  protected void onDestroy() {
    if (originalBitmap != null) originalBitmap.recycle();
    if (pictureBitmap != null) pictureBitmap.recycle();
    super.onDestroy();
  }
}