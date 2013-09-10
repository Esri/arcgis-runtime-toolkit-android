package com.esri.android.appframework;
import java.text.NumberFormat;
import java.text.ParseException;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.TiledServiceLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.osm.OpenStreetMapLayer;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;

public class Map extends MapView {
  MapOptions options;
  private SketchLayer sketchLayer;
  String TAG = "ArcGIS Map";
  private OnStatusChangedListener listener;
  private OnStatusChangedListener internal_listener;

  static NumberFormat numberFormat = NumberFormat.getInstance();

  public Map(Context context) {
    super(context);
    options = new MapOptions(MapType.TOPO);
    setMapOptions();
  }

  public Map(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initAttrs(attrs);
  }

  public Map(Context context, AttributeSet attrs) {
    super(context, attrs);
    initAttrs(attrs);
  }

  public Map(Context context, MapOptions options) {
    super(context);
    this.options = options;
    setMapOptions();
  }
  
  public Map(Context context, String url, String user, String passwd) {
    super(context, url, user, passwd);
  }

  /**
   * If the MapView is initialized, centers the map at the given latitude and longitude.
   * 
   * @param lat latitude of the new center of the MapView.
   * @param lon longitude of the new center of the MapView.
   * @param animated boolean to decide if animation is needed.
   * @since 10.2
   */
  public void centerAt(double lat, double lon, boolean animated) {
    SpatialReference sp = getSpatialReference();
    if (sp == null)
      return;
    Point pt;
    if (!sp.isWGS84())
      pt = GeometryEngine.project(lon, lat, getSpatialReference());
    else
      pt = new Point(lon, lat);
    centerAt(pt, animated);
  }

  /**
   * If the MapView is initialized, centers the map at the given latitude and longitude
   * and zoom the map based on the given factor.
   * 
   * @param lat latitude of the new center of the MapView.
   * @param lon longitude of the new center of the MapView.
   * @param levelOrFactor When using an ArcGISTiledMapServiceLayer as basemap, the map is zoomed to the level specified.
   *          Otherwise the map is zoomed in or out by the specified factor. For example, use
   *          0.5 to zoom in twice as far and 2.0 to zoom out twice as far. 
   *          When levelOrFactor is less than or equal to 0, nothing happens.
   * @since 10.2
   */
  public void centerAndZoom(double lat, double lon, float levelOrFactor) {
    double res = getResolution();
    if (levelOrFactor > 0  && !Double.isNaN(res) && res > 0) {
      for (Layer layer : getLayers()) {
        if (layer.isWebMapBaselayer() && layer instanceof TiledServiceLayer) {
          int levels = ((TiledServiceLayer) layer).getTileInfo().getLevels();
          int level = ((int) levelOrFactor) > levels-1 ? levels-1 : (int) levelOrFactor;
          zoomToLevel((TiledServiceLayer) layer, lat, lon, level);
          return;
        }
      }
      centerAt(lat, lon, false);
      setResolution(getResolution()*levelOrFactor);
    }
  }

  private void initAttrs(AttributeSet attrs) {
    if (attrs == null) {
      return;
    }

    String type = null;
    double lat = Double.NaN;
    double lon = Double.NaN;
    int zoom = 0;
    
    for (int i = 0; i < attrs.getAttributeCount(); i++) {
      if ("mapoptions.type".equalsIgnoreCase(attrs.getAttributeName(i))) {
        type = attrs.getAttributeValue(i);
      } else if ("mapoptions.center".equalsIgnoreCase(attrs.getAttributeName(i))) {
        String in = attrs.getAttributeValue(i);
        if (in != null) {
          in = in.trim();
          if (in.length() > 0) {
            String[] xy = in.split(" ");
            try {
              lat = numberFormat.parse(xy[0]).doubleValue();
              lon = numberFormat.parse(xy[1]).doubleValue();
            } catch (ParseException e) {
              Log.e(TAG, "Can not parse Map.mapoptions.center from xml", e);
            }
          }
        }      
      } else if ("mapoptions.zoom".equalsIgnoreCase(attrs.getAttributeName(i))) {
        zoom = attrs.getAttributeIntValue(i, 0);
      }
    }
    
    if (!TextUtils.isEmpty(type)) {
      MapOptions mapoptions = new MapOptions(MapType.newInstance(type));  
      if (!Double.isNaN(lat) && !Double.isNaN(lon))
        mapoptions.setCenter(lat, lon);
      mapoptions.setZoom(zoom);
      this.options = mapoptions;
      setMapOptions();
    }
  }

  private void zoomToLevel(TiledServiceLayer lyr) {
    if (options == null)
      return;
    int level = options.zoom > lyr.getTileInfo().getLevels()-1 ? lyr.getTileInfo().getLevels()-1 : options.zoom;
    zoomToLevel(lyr, options.lat, options.lon, level);
  }
  
  private void zoomToLevel(TiledServiceLayer lyr, double lat, double lon, int level) {
    centerAt((Point) GeometryEngine.project(new Point(lon, lat), SpatialReference.create(SpatialReference.WKID_WGS84), getSpatialReference()), false);
    setResolution(lyr.getTileInfo().getResolutions()[level]);
  }
  
  /**
   * Switches basemap by the given options. If the map is instantiated using a MapOptions 
   * and the spatial reference of the current basemap is the same as the given basemap's,
   * the given basemap is replaced of the current one.
   * 
   * @param options the given options of the map.
   * @since 10.2
   */
  public boolean setMapOptions(MapOptions options) {
    if (options == null)
      return false;
    
    if (options.mapType == null)
      return false;
    
    if (getSpatialReference().getID() != SpatialReference.WKID_WGS84_WEB_MERCATOR_AUXILIARY_SPHERE)
      return false;
    
    this.options = options;
    
    for (Layer layer : getLayers()) {
      if (layer.isWebMapBaselayer())
        removeLayer(layer);
    }
    
    setMapOptions();
    return true;
  }
  
  boolean hasSketchLayer() {
    return sketchLayer != null;
  }
  
  SketchLayer getSketchLayer() {
    if (sketchLayer == null) {
      sketchLayer = new SketchLayer(this);
      addLayer(sketchLayer);
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
  
  /**
   * Removes all graphics added through the addXXXGraphic() methods.
   * 
   * @since 10.2
   */
  public void removeAllGraphics() {
    if (sketchLayer != null)
      sketchLayer.removeAll();
    getCallout().hide();
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
  
  private void setMapOptions() {
    if (options.mapType == MapType.OSM) {
      OpenStreetMapLayer layer = new OpenStreetMapLayer();
      addLayer(layer, 0);
//      ((Layer) layer).setWebMapBaselayer(true); //TODO set webmap baselayer
    } else {
      for (String url : options.mapType.getURLs()) {
        ArcGISTiledMapServiceLayer layer = new ArcGISTiledMapServiceLayer(url);
        addLayer(layer, 0);
//        ((Layer) layer).setWebMapBaselayer(true); //TODO set webmap baselayer
      }
    }
    
    internal_listener = new OnStatusChangedListener() {
      
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      @Override
      public void onStatusChanged(Object source, STATUS status) {
        if (source instanceof TiledServiceLayer) {
          TiledServiceLayer lyr = (TiledServiceLayer) source;
          if (options.mapType == MapType.OSM) {
            zoomToLevel(lyr);
          } else {
            for (String url : options.mapType.getURLs()) {
              if (lyr.getUrl().equals(url)) {
                zoomToLevel(lyr);
              }
            }
          }
        }
        if (listener != null)
          listener.onStatusChanged(source, status);
      }
    };
    super.setOnStatusChangedListener(internal_listener);
  }
  
  /**
   * The options to instantiate a MapView with the given pre-defined basemap, zoom level and map center.
   * 
   */

  public static class MapOptions {
    MapType mapType;
    double lat = 0;
    double lon = 0;
    int zoom = 0;

    /**
     * Sets zoom level.
     * @param zoom the zoom level of the basemap.
     */
    public void setZoom(int zoom) {
      this.zoom = zoom;
    }

    /**
     * Sets map center.
     * @param lat latitude of map center.
     * @param lon longitude of map center.
     */
    public void setCenter(double lat, double lon) {
      this.lon = lon;
      this.lat = lat;
    }
    
    /**
     * The constructor is used if you are instantiating the MapOptions using the give basemap. 
     * 
     * @param type the pre-defined basemap. By default it is TOPO.
     */
    public MapOptions (MapType type) {
      this.mapType = type == null ? MapType.TOPO : type;
    }
    
    /**
     * The constructor is used if you are instantiating the MapOptions using the give basemap, 
     * map center and zoom level. 
     * 
     * @param type the pre-defined basemap. By default it is TOPO.
     * @param lat the latitude of the map center.
     * @param lon the longitude of the map center.
     * @param zoom the zoom level of the basemap.
     */
    public MapOptions (MapType type, double lat, double lon, int zoom) {
      this.mapType = type == null ? MapType.TOPO : type;
      this.lon = lon;
      this.lat = lat;
      this.zoom = zoom;
    }
  }
  
  /**
   * Pre-defined basemaps. 
   * 
   * @param type the pre-defined basemap. By default it is TOPO.
   */
  public static enum MapType {
    /**
     * Street map world
     */
    STREETS(new String[] {"http://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer"}),
    /**
     * Topographic map world
     */
    TOPO(new String[] {"http://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer"}),
    /**
     * Satellite imagery map world
     */
    SATELLITE(new String[] {"http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer"}),
    /**
     * Satellite imagery plus boundaries and places map world
     */
    HYBRID(new String[] {"http://server.arcgisonline.com/ArcGIS/rest/services/Reference/World_Boundaries_and_Places/MapServer", "http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer"}),
    
    NATIONAL_GEOGRAPHIC(new String[] {"http://services.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer"}),
    
    OCEANS(new String[] {"http://services.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer"}),
    
    GRAY(new String[] {"http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Reference/MapServer", "http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer"}),
    
    OSM(null);
    
    private String[] urls;

    private MapType(String[] urls) {
      this.urls = urls;
    }

    /**
     * Allows you to retrieve the URL for each MapType enum
     * 
     * @return URL String
     */
    String[] getURLs() {
      return urls;
    }

    static MapType newInstance(String name) {
      if ("TOPO".equalsIgnoreCase(name))
        return MapType.TOPO;
      else if ("STREETS".equalsIgnoreCase(name))
        return MapType.STREETS;
      else if ("GRAY".equalsIgnoreCase(name))
        return MapType.GRAY;
      else if ("HYBRID".equalsIgnoreCase(name))
        return MapType.HYBRID;
      else if ("NATIONAL_GEOGRAPHIC".equalsIgnoreCase(name))
        return MapType.NATIONAL_GEOGRAPHIC;
      else if ("OCEANS".equalsIgnoreCase(name))
        return MapType.OCEANS;
      else if ("OSM".equalsIgnoreCase(name))
        return MapType.OSM;
      else if ("SATELLITE".equalsIgnoreCase(name))
        return MapType.SATELLITE;
      else
        return MapType.TOPO;
    }
  }

  @Override
  public void setOnStatusChangedListener(OnStatusChangedListener onStatusChangedListener) {
    listener = onStatusChangedListener;
  }
}
