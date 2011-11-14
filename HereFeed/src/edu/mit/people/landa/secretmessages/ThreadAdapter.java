package edu.mit.people.landa.secretmessages;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ThreadAdapter extends SimpleCursorAdapter {
  private boolean showBrief;

  public ThreadAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
    super(context, layout, c, from, to);
    showBrief = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.widget.SimpleCursorAdapter#setViewText(android.widget.TextView, java.lang.String)
   */
  @Override
  public void setViewText(TextView v, String text) {
    super.setViewText(v, text);
    switch (v.getId()) {
    case R.id.thread_message_timestamp:
      v.setText("Posted " + LandmarkMessage.relativeTime(text));
      break;
    case R.id.message_content:
      v.setText("\"" + text +"\"");
      break;
    case R.id.message_picture:
      if (!text.equals("0")) {
        v.setText("picture");
      }
      else {
        v.setText("");
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.widget.SimpleCursorAdapter#setViewImage(android.widget.ImageView, java.lang.String)
   */
  @Override
  public void setViewImage(ImageView v, String value) {
    super.setViewImage(v, value);
  }

  /**
   * @return the showBrief
   */
  public boolean isShowBrief() {
    return showBrief;
  }

  /**
   * @param showBrief the showBrief to set
   */
  public void setShowBrief(boolean showBrief) {
    this.showBrief = showBrief;
  }
}