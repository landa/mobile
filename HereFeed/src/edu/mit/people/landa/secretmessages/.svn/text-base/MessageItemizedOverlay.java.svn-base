package edu.mit.people.landa.secretmessages;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class MessageItemizedOverlay extends ItemizedOverlay {
  private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
  
  public MessageItemizedOverlay(Drawable defaultMarker) {
    super(boundCenterBottom(defaultMarker));
  }
  
  public void addOverlay(OverlayItem overlay) {
    mOverlays.add(overlay);
    populate();
  }

  @Override
  protected OverlayItem createItem(int i) {
    return mOverlays.get(i);
  }

  @Override
  public int size() {
    return mOverlays.size();
  }

  /* (non-Javadoc)
   * @see com.google.android.maps.ItemizedOverlay#draw(android.graphics.Canvas, com.google.android.maps.MapView, boolean)
   */
  @Override
  public void draw(Canvas canvas, MapView mapView, boolean shadow) {
    if (shadow == false) {
      Projection proj = mapView.getProjection();
      Paint paint = new Paint();
      paint.setARGB(100, 255, 255, 204);
      paint.setStyle(Style.FILL_AND_STROKE);
      paint.setStrokeWidth(3);
      for (OverlayItem item : mOverlays) {
        LandmarkMessage currentItem = (LandmarkMessage) item;
        GeoPoint geoPoint = ((LandmarkMessage) item).getPoint();
        float accuracy = currentItem.getMAccuracy();
        Point point = proj.toPixels(geoPoint, null);
        canvas.drawCircle(point.x-1, point.y-1, proj.metersToEquatorPixels(accuracy), paint);
      }
    }
    super.draw(canvas, mapView, shadow);
  }

}
