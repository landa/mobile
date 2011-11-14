package edu.mit.people.landa.secretmessages;

import java.io.File;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.maps.MapActivity;

public class ComposeMessage extends MapActivity {
  private static final String TAG = "ComposeMessage";
  private static final String PICTURE_STORAGE = Environment.getExternalStorageDirectory() + "/HereFeed";
  
  private static final int ACTIVITY_POST = 0;
  private static final int ACTIVITY_CAPTURE = 1;
  private static final int ACTIVITY_GALLERY = 2;
  
  private EditText mText;
  private Button mPost;
  private Button mCapture;
  private Button mGallery;
  private Button mRemove;
  
  private String picturePath;
  private boolean picture;
  private int tid;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    setContentView(R.layout.compose_message);
    Intent intent = getIntent();
    
    picture = false;
    tid = intent.getIntExtra("tid", 0);
    
    findAllViews();
  }
  private void findAllViews() {
    mText = (EditText) findViewById(R.id.text_content);
    mCapture = (Button) findViewById(R.id.pic_capture);
    mRemove = (Button) findViewById(R.id.pic_remove);
    mGallery = (Button) findViewById(R.id.pic_choose);
    mPost = (Button) findViewById(R.id.post);

    mText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      public void onFocusChange(View view, boolean hasFocus) {
        if (mText.getText().toString().equals("Type the content of your post here") && hasFocus == true)
          mText.selectAll();
        else if (mText.getText().toString().equals("") && hasFocus == false)
          mText.setText("Type the content of your post here");
      }
    });
    
    mGallery.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, ACTIVITY_GALLERY);
      }
    });
    
    mCapture.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        new File(PICTURE_STORAGE).mkdir();
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(PICTURE_STORAGE, "temp.jpg")));
        i.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(i, ACTIVITY_CAPTURE);
      }
    });
    
    mRemove.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        setPictureUnavailable();
      }
    });
    
    mPost.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        capturePicture();
      }
    });
  }
  private void capturePicture() {
    Intent intent = new Intent(this, PostMessage.class);
    intent.putExtra("content", getContent());
    intent.putExtra("picture", picture);
    if (picture) intent.putExtra("picture_path", picturePath);
    intent.putExtra("tid", tid);
    startActivityForResult(intent, ACTIVITY_POST);
  }
  private String getContent() {
    return mText.getText().toString();
  }
  /* (non-Javadoc)
   * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    case ACTIVITY_POST:
      if (resultCode == RESULT_OK) {
        setResult(RESULT_OK);
        finish();
      }
      break;
    case ACTIVITY_CAPTURE:
      if (resultCode == RESULT_OK) {
        setPictureAvailable(PICTURE_STORAGE + "/temp.jpg");
      }
      break;
    case ACTIVITY_GALLERY:
      if (resultCode == RESULT_OK) {
        Uri imageUri = data.getData();
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(imageUri, proj, null, null, null);
        int column = cursor.getColumnIndexOrThrow(proj[0]);
        cursor.moveToNext();
        setPictureAvailable(cursor.getString(column));
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
  public void setPictureAvailable(String path) {
    picturePath = path;
    picture = true;
    getIntent().putExtra("picture", picture);
    mRemove.setVisibility(View.VISIBLE);
    mGallery.setVisibility(View.GONE);
    mCapture.setVisibility(View.GONE);
  }
  public void setPictureUnavailable() {
    picture = false;
    getIntent().putExtra("picture", picture);
    mRemove.setVisibility(View.GONE);
    mGallery.setVisibility(View.VISIBLE);
    mCapture.setVisibility(View.VISIBLE);
  }
  @Override
  protected boolean isRouteDisplayed() {
    return false;
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
    Bundle extras = getIntent().getExtras();
    if (extras == null || extras.getBoolean("picture") == false) setPictureUnavailable();
    else setPictureAvailable(picturePath);
    super.onResume();
  }
  /* (non-Javadoc)
   * @see com.google.android.maps.MapActivity#onDestroy()
   */
  @Override
  protected void onDestroy() {
    getIntent().putExtra("picture", picture);
    super.onDestroy();
  }
}