package com.esri.android.appframework.map;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;

public class MapViewHelper {
  private MapView mapView;
  private SketchLayer sketchLayer;

  /**
   * Creates a MapViewHelper.
   * @param mapView a MapView instance
   */
  public MapViewHelper(MapView mapView) {
    if (mapView == null)
      throw new IllegalArgumentException("mapview == null");
    
    this.mapView = mapView;
    mapView.setOnTouchListener(new MapOnTouchListener(mapView.getContext(), mapView) {
      @Override
      public void onLongPress(MotionEvent point) {
        if (hasSketchLayer() && getSketchLayer().onLongPress(point))
          return;
          
        super.onLongPress(point);
      }
      
      @Override
      public boolean onSingleTap(MotionEvent point) {
        if (hasSketchLayer() && getSketchLayer().onSingleTap(point))
          return true;
        
        return super.onSingleTap(point);
      }
      
      @Override
      public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
        if (hasSketchLayer() && getSketchLayer().onDragPointerMove(from, to))
          return true;
        
        return super.onDragPointerMove(from, to);
      }
      
      @Override
      public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
        if (hasSketchLayer() && getSketchLayer().onDragPointerUp(from, to))
          return true;
        
        return super.onDragPointerUp(from, to);
      }
    });
  }

  boolean hasSketchLayer() {
    return sketchLayer != null;
  }
  
  SketchLayer getSketchLayer() {
    if (sketchLayer == null) {
      sketchLayer = new SketchLayer(mapView);
      mapView.addLayer(sketchLayer);
    }
    return sketchLayer;
  }
  
  /**
   * Adds a polygon graphic to the map at the given location. The graphic will be rendered using
   * the given fill color, outline color and outline width. By default a callout window with the 
   * given title, text, and an image will be displayed when tapping on the graphic. 
   * This behavior can be turned on/off through setShowGraphicCallout(). 
   * 
   * @param lat latitude of the given location.
   * @param lon longitude of the given location.
   * @param title the title to show on the callout window.
   * @param snippet the text to show below title on the right side of the callout window.
   * @param url the url of an image to show below title on the left side of the callout window.
   * @param fillColor the filling color used to render the polygon. 
   * @param strokeColor the color of the outline of the polygon.
   * @param width the width used to render the outline of the polygon.
   * @param zorder the drawing order of the graphic.
   * @return A unique ID representing the added Graphic if adding is successful. -1 if adding fails.
   * @since 10.2
   */
  public int addPolygonGraphic(double[][] latlon, String title, String snippet, String url, int fillColor, int strokeColor, float width, int zorder) {
    if (!isLoaded())
      return -1;
    return getSketchLayer().addPolygonGraphic(latlon, title, snippet, url, fillColor, strokeColor, width, zorder);
  }
  
  public boolean isLoaded() {
    return mapView.isLoaded();
  }

  /**
   * Adds a polygon graphic to the map at the given location. The graphic will be rendered using
   * the given fill color, outline color and outline width. By default a callout window with the 
   * given title, text, and an image will be displayed when tapping on the graphic. 
   * This behavior can be turned on/off through setShowGraphicCallout(). 
   * 
   * @param lat latitude of the given location.
   * @param lon longitude of the given location.
   * @param title the title to show on the callout window.
   * @param snippet the text to show below title on the right side of the callout window.
   * @param resID the resource id of an image to show below title on the left side of the callout window.
   * @param fillColor the filling color used to render the polygon. 
   * @param strokeColor the color of the outline of the polygon.
   * @param width the width used to render the outline of the polygon.
   * @param zorder the drawing order of the graphic.
   * @return A unique ID representing the added Graphic if adding is successful. -1 if adding fails.
   * @since 10.2
   */
  public int addPolygonGraphic(double[][] latlon, String title, String snippet, int resID, int fillColor, int strokeColor, float width, int zorder) {
    if (!isLoaded())
      return -1;
    return getSketchLayer().addPolygonGraphic(latlon, title, snippet, resID, fillColor, strokeColor, width, zorder);
  }
  
  /**
   * Adds a polyline graphic to the map at the given location. The graphic will be rendered using
   * the given color and width. By default a callout window with the given title, text, and an image
   * will be displayed when tapping on the graphic. This behavior can be turned on/off through 
   * setShowGraphicCallout(). 
   * 
   * @param lat latitude of the given location.
   * @param lon longitude of the given location.
   * @param title the title to show on the callout window.
   * @param snippet the text to show below title on the right side of the callout window.
   * @param url the url of an image to show below title on the left side of the callout window.
   * @param color the color used to render the polyline. 
   * @param width the width used to render the polyline.
   * @param zorder the drawing order of the graphic.
   * @return A unique ID representing the added Graphic if adding is successful. -1 if adding fails.
   * @since 10.2
   */
  public int addPolylineGraphic(double[][] latlon, String title, String snippet, String url, int color, float width, int zorder) {
    if (!isLoaded())
      return -1;
    return getSketchLayer().addPolylineGraphic(latlon, title, snippet, url, color, width, zorder);
  }
  
  /**
   * Adds a polyline graphic to the map at the given location. The graphic will be rendered using
   * the given color and width. By default a callout window with the given title, text, and an image
   * will be displayed when tapping on the graphic. This behavior can be turned on/off through 
   * setShowGraphicCallout(). 
   * 
   * @param lat latitude of the given location.
   * @param lon longitude of the given location.
   * @param title the title to show on the callout window.
   * @param snippet the text to show below title on the right side of the callout window.
   * @param resID the resource id of an image to show below title on the left side of the callout window.
   * @param color the color used to render the polyline. 
   * @param width the width used to render the polyline.
   * @param zorder the drawing order of the graphic.
   * @return A unique ID representing the added Graphic if adding is successful. -1 if adding fails.
   * @since 10.2
   */
  public int addPolylineGraphic(double[][] latlon, String title, String snippet, int resID, int color, float width, int zorder) {
    if (!isLoaded())
      return -1;
    return getSketchLayer().addPolylineGraphic(latlon, title, snippet, resID, color, width, zorder);
  }
  
  /**
   * Adds a point graphic to the map at the given location. The graphic will be rendered using
   * the given symbol. By default a callout window with the given title, text, and an image
   * will be displayed when tapping on the graphic. This behavior can be turned on/off through 
   * setShowGraphicCallout(). If the graphic is draggable, long-clicking and then dragging the graphic 
   * moves it. However, user defined long-press listener or magnifer will take preceedence over
   * this dragging behavior.
   * 
   * @param lat latitude of the given location.
   * @param lon longitude of the given location.
   * @param title the title to show on the callout window.
   * @param snippet the text to show below title on the right side of the callout window.
   * @param url the url of an image to show below title on the left side of the callout window.
   * @param icon the symbol used to render the point graphic. By default an Android default icon
   *          which is device dependent will be used.
   * @param draggable set to true if you want to allow the user to move the graphic. 
   * @param zorder the drawing order of the graphic.
   * @return A unique ID representing the added Graphic if adding is successful. -1 if adding fails.
   * @since 10.2
   */
  public int addMarkerGraphic(double lat, double lon, String title, String snippet, String url, Drawable icon, boolean draggable, int zorder) {
    if (!isLoaded())
      return -1;
    return getSketchLayer().addMarkerGraphic(lat, lon, title, snippet, url, icon, draggable, zorder);
  }
  
  /**
   * Adds a point graphic to the map at the given location. The graphic will be rendered using
   * the given symbol. By default a callout window with the given title, text, and an image
   * will be displayed when tapping on the graphic. This behavior can be turned on/off through 
   * setShowGraphicCallout(). If the graphic is draggable, long-clicking and then dragging the graphic 
   * moves it. However, user defined long-press listener or magnifer will take preceedence over
   * this dragging behavior.
   * 
   * @param lat latitude of the given location.
   * @param lon longitude of the given location.
   * @param title the title to show on the callout window.
   * @param snippet the text to show below title on the right side of the callout window.
   * @param resID the resource id of an image to show below title on the left side of the callout window.
   * @param icon the symbol used to render the point graphic. By default an Android default icon
   *          which is device dependent will be used.
   * @param draggable set to true if you want to allow the user to move the graphic. 
   * @param zorder the drawing order of the graphic.
   * @return A unique ID representing the added Graphic if adding is successful. -1 if adding fails.
   * @since 10.2
   */
  public int addMarkerGraphic(double lat, double lon, String title, String snippet, int resID, Drawable icon, boolean draggable, int zorder) {
    if (!isLoaded())
      return -1;
    return getSketchLayer().addMarkerGraphic(lat, lon, title, snippet, resID, icon, draggable, zorder);
  }
  
  public MapView getMapView() {
    return mapView;
  }
  
  /**
   * Removes all graphics added through the addXXXGraphic() methods.
   * 
   * @since 10.2
   */
  public void removeAllGraphics() {
    if (sketchLayer != null)
      sketchLayer.removeAll();
    mapView.getCallout().hide();
  }
  
  /**
   * Removes graphic added through the addXXXGraphic() methods using a unique ID.
   * 
   * @param id the unique ID of the graphic to remove.
   * @since 10.2
   */
  public void removeGraphic(int id) {
    if (sketchLayer != null)
      sketchLayer.removeGraphic(id);
  }
  
  /**
   * Allows/disallows showing a callout window when tapping on a graphic added through the addXXXGraphic() methods.
   * By default it is on.
   * 
   * @param show true to turn on the callout window.
   * @since 10.2
   */
  public void setShowGraphicCallout(boolean show) {
    if (isLoaded())
      getSketchLayer().showGraphicCallout(show);
  }
  
  /**
   * Sets a callback that's invoked when tapping on a graphic added through the addXXXGraphic() methods.
   * 
   * @param listener the callback to invoke when tapping on a graphic.
   * @since 10.2
   */
  public void setOnGraphicClickListener(OnGraphicClickListener listener) {
    if (isLoaded())
      getSketchLayer().setOnGraphicClickListener(listener);
  }
  
  /**
   * Sets a callback that's invoked when tapping on the callout window of a graphic 
   * added through the addXXXGraphic() methods.
   * 
   * @param listener the callback to invoke when tapping on a graphic.
   * @since 10.2
   */
  public void setOnCalloutClickListener(OnCalloutClickListener listener) {
    if (isLoaded())
      getSketchLayer().setOnCalloutClickListener(listener);
  }
  
  /**
   * Creates popups for the map based on the given location. In the map each layer which is visible, 
   * within current scale range and associated with popup configuration represented by PopupInfo 
   * is queried spatially based on the given location. A popup is generated for each graphic returned
   * from the query and added to a PopupContainer object. Query is performed asynchronizedly. A callback
   * is invoked when some popups have been added to the PopupContainer. 
   * 
   * @param screenX the x coordinate of the touch pointer in screen pixels. 
   * @param screenY the y coordinate of the touch pointer in screen pixels.
   * @param listener the callback to invoke when some popups have been added to the PopupContainer.
   * @since 10.2
   */
  public void createPopup(float screenX, float screenY, PopupCreateListener listener) {
    if (isLoaded())
      getSketchLayer().createPopup(screenX, screenY, listener);
  }
}
