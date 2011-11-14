package edu.mit.people.landa.secretmessages;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.location.Location;
import android.text.Spannable;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MessageAdapter extends SimpleCursorAdapter {
  String temp;
  String temp1;
  private boolean showBrief;
  
  public MessageAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
    super(context, layout, c, from, to);
    temp = null;
    temp1 = null;
    showBrief = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * android.widget.SimpleCursorAdapter#setViewText(android.widget.TextView,
   * java.lang.String)
   */
  @Override
  public void setViewText(TextView v, String text) {
    super.setViewText(v, text);
    switch (v.getId()) {
    case R.id.message_timestamp:
      if (temp == null) {
        temp = "Posted " + LandmarkMessage.relativeTime(text).toLowerCase() + ", ";
      }
      else {
        v.setText("Posted " + LandmarkMessage.relativeTime(text).toLowerCase() + ", " + temp);
        temp = null;
      }
      break;
    case R.id.message_discovered:
      if (temp == null) temp = "found " + LandmarkMessage.relativeTime(text);
      else {
        v.setText(temp + "found " + LandmarkMessage.relativeTime(text));
        temp = null;
      }
      break;
    case R.id.message_content:
      if (text.length() > 50) {
        String elipses = elipses(text);
        v.setText("\"" + elipses + "\"" + temp1, TextView.BufferType.SPANNABLE);
        if (!temp1.equals("")) {
          Spannable str = (Spannable) v.getText();
          str.setSpan(new AbsoluteSizeSpan(14), str.length() - temp1.length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
          str.setSpan(new ForegroundColorSpan(Color.RED), str.length() - temp1.length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
      }
      else {
        v.setText("\"" + text + "\"" + temp1, TextView.BufferType.SPANNABLE);
        if (!temp1.equals("")) {
          Spannable str = (Spannable) v.getText();
          str.setSpan(new AbsoluteSizeSpan(14), str.length() - temp1.length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
          str.setSpan(new ForegroundColorSpan(Color.RED), str.length() - temp1.length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
      }
      break;
    case R.id.extra_content:
      if (!text.equals("0")) {
        temp1 = " (attachment)";
      }
      else {
        temp1 = "";
      }
    }
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