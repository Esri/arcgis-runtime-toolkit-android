package com.esri.android.appframework;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.GroupLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.TiledServiceLayer.TileInfo;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.android.map.ags.ArcGISPopupInfo;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.popup.PopupContainer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.map.popup.PopupInfo;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;

class SketchLayer extends GraphicsLayer {
  private static final int POOL_SIZE = 5;
  private static ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);
  private static final String BITMAP = "bitmap";
  private static final String SNIPPET = "snippet";
  private static final String TITLE = "title";
  private static final String DRAGGABLE = "draggable";
  private static final int TOLERANCE = 20;
  private static final int MAXFEATURE = 10;
  private static final int MAXCALLOUTLENGTH = 200;
  private Map _map;
  private int[] selection;
  private boolean showGraphicCallout = true;
  private OnGraphicClickListener onGraphicClickListener;
  private OnCalloutClickListener onCalloutClickListener;

  public SketchLayer(Map map) {
    super();
    this._map = map;
    map.setOnTouchListener(new MapOnTouchListener(map.getContext(), map) {
      @Override
      public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
        if (_map.hasSketchLayer() && _map.getSketchLayer().onDragPointerMove(from, to))
          return true;        
        return super.onDragPointerMove(from, to);
      }
      
      @Override
      public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
        if (_map.hasSketchLayer() && _map.getSketchLayer().onDragPointerUp(from, to))
          return true;
        return super.onDragPointerUp(from, to);
      }
      
      @Override
      public boolean onSingleTap(MotionEvent point) {
        if (_map.hasSketchLayer())
          _map.getSketchLayer().onSingleTap(point);
        return super.onSingleTap(point);
      }
      
      @Override
      public void onLongPress(MotionEvent point) {
        if (_map.getOnLongPressListener() != null)
          super.onLongPress(point);
        else if (_map.hasSketchLayer())
          _map.getSketchLayer().onLongPress(point);
      }
    });
  }
  
  int addPolygonGraphic(double[][] latlon, String title, String snippet, int resID, int fillColor, int strokeColor, float width, int zorder) {
    if (_map.isLoaded() && latlon.length > 1) {
      HashMap<String, Object> attributes = setAttributes(title, snippet, resID, false);
      Polygon polygon = Latlon.createPolygon(latlon);
      SimpleFillSymbol symbol = new SimpleFillSymbol(fillColor);
      symbol.setOutline(new SimpleLineSymbol(strokeColor, width));
      Graphic graphic = new Graphic(GeometryEngine.project(polygon,  SpatialReference.create(SpatialReference.WKID_WGS84), getSpatialReference()), symbol, attributes, zorder);
      return addGraphic(graphic);
    }
    return -1;
  }
  
  int addPolygonGraphic(double[][] latlon, String title, String snippet, String url, int fillColor, int strokeColor, float width, int zorder) {
    if (_map.isLoaded() && latlon.length > 1) {
      HashMap<String, Object> attributes = setAttributes(title, snippet, url, false);
      Polygon polygon = Latlon.createPolygon(latlon);
      SimpleFillSymbol symbol = new SimpleFillSymbol(fillColor);
      symbol.setOutline(new SimpleLineSymbol(strokeColor, width));
      Graphic graphic = new Graphic(GeometryEngine.project(polygon,  SpatialReference.create(SpatialReference.WKID_WGS84), getSpatialReference()), symbol, attributes, zorder);
      return addGraphic(graphic);
    }
    return -1;
  }
  
  int addPolylineGraphic(double[][] latlon, String title, String snippet, int resID, int color, float width, int zorder) {
    if (_map.isLoaded() && latlon.length > 1) {
      HashMap<String, Object> attributes = setAttributes(title, snippet, resID, false);
      Polyline polyline = Latlon.createPolyline(latlon);
      Graphic graphic = new Graphic(GeometryEngine.project(polyline,  SpatialReference.create(SpatialReference.WKID_WGS84), getSpatialReference()), new SimpleLineSymbol(color, width), attributes, zorder);
      return addGraphic(graphic);
    }
    return -1;
  }

  int addPolylineGraphic(double[][] latlon, String title, String snippet, String url, int color, float width, int zorder) {
    if (_map.isLoaded() && latlon.length > 1) {
      HashMap<String, Object> attributes = setAttributes(title, snippet, url, false);
      Polyline polyline = Latlon.createPolyline(latlon);
      Graphic graphic = new Graphic(GeometryEngine.project(polyline,  SpatialReference.create(SpatialReference.WKID_WGS84), getSpatialReference()), new SimpleLineSymbol(color, width), attributes, zorder);
      return addGraphic(graphic);
    }
    return -1;
  }
  
  int addMarkerGraphic(double lat, double lon, String title, String snippet, int resID, Drawable icon, boolean draggable, int zorder) {
    if (!_map.isLoaded())
      return -1;
    HashMap<String, Object> attributes = setAttributes(title, snippet, resID, draggable);
    Drawable icn = icon != null ? icon : _map.getResources().getDrawable(android.R.drawable.btn_star_big_on);
    Graphic graphic = new Graphic(GeometryEngine.project(lon, lat, getSpatialReference()), new PictureMarkerSymbol(icn), attributes, zorder);
    return addGraphic(graphic);
  }
  
  int addMarkerGraphic(double lat, double lon, String title, String snippet, String url, Drawable icon, boolean draggable, int zorder) {
    if (!_map.isLoaded())
      return -1;
    HashMap<String, Object> attributes = setAttributes(title, snippet, url, draggable);
    Drawable icn = icon != null ? icon : _map.getResources().getDrawable(android.R.drawable.btn_star_big_on);
    Graphic graphic = new Graphic(GeometryEngine.project(lon, lat, getSpatialReference()), new PictureMarkerSymbol(icn), attributes, zorder);
    return addGraphic(graphic);
  }
  
  private HashMap<String, Object> setAttributes(String title, String snippet, Object bitmap, boolean draggable) {
    HashMap<String, Object> attributes = new HashMap<String, Object>();
    attributes.put(BITMAP, bitmap);
    attributes.put(TITLE, title);
    attributes.put(SNIPPET, snippet);
    attributes.put(DRAGGABLE, draggable);
    return attributes;
  }

  public void onSingleTap(MotionEvent point) {
    if (!showGraphicCallout && onGraphicClickListener == null)
      return;
    
    int[] sel = getGraphicIDs(point.getX(), point.getY(), TOLERANCE, 1);
    if (sel != null && sel.length > 0) {
      final Graphic graphic = getGraphic(sel[0]);
      
      if (showGraphicCallout) {
        final LinearLayout calloutView = new LinearLayout(_map.getContext());
        calloutView.setOnClickListener(new OnClickListener() {
          
          @Override
          public void onClick(View v) {
            if (onCalloutClickListener != null)
              onCalloutClickListener.onCalloutClick(graphic);
          }
        });
        calloutView.setPadding(3, 3, 3, 3);
        calloutView.setGravity(Gravity.CENTER);
        // image
        Object bitmap = graphic.getAttributeValue(BITMAP);
        if (bitmap != null) {
          if (bitmap instanceof Integer) {
            Drawable drawable = _map.getResources().getDrawable((Integer) bitmap);
            ImageView imageView = new ImageView(_map.getContext());
            imageView.setImageDrawable(drawable);
            calloutView.addView(imageView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
          } else {
            final String url = (String) bitmap;
            if (!TextUtils.isEmpty(url)) {
              final ImageView imageView = new ImageView(_map.getContext());
              calloutView.addView(imageView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
              pool.submit(new Runnable() {
  
                @Override
                public void run() {
                  // TODO Auto-generated method stub
                  try {
                    InputStream is = (InputStream) new URL(url).getContent();
                    final Bitmap bitmap = BitmapFactory.decodeStream(is);
                    if (bitmap != null) {
                      _map.post(new Runnable() {
                        @Override
                        public void run() {
                          imageView.setImageBitmap(bitmap);
                          imageView.setPadding(0, 0, 5, 0);
                          calloutView.invalidate();
                        }
                      });
                    }
                  } catch (Exception e) {
                    Log.e(_map.getClass().getSimpleName(), "", e);
                  }
                }
              });
            }
          }
        }
        // title and snippet
        LinearLayout titleView = new LinearLayout(_map.getContext());
        titleView.setOrientation(LinearLayout.VERTICAL);
        titleView.setGravity(Gravity.CENTER);
        TextView title = new TextView(_map.getContext());
        title.setSingleLine();
        title.setEllipsize(TruncateAt.MARQUEE);
        title.setSelected(true);
        title.setMarqueeRepeatLimit(-1);
        title.setMaxWidth(MAXCALLOUTLENGTH);
        title.setText((CharSequence) graphic.getAttributeValue(TITLE));
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        titleView.addView(title, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        TextView snippet = new TextView(_map.getContext());
        snippet.setText((CharSequence) graphic.getAttributeValue(SNIPPET));
        snippet.setSingleLine();
        snippet.setEllipsize(TruncateAt.MARQUEE);
        snippet.setSelected(true);
        snippet.setMarqueeRepeatLimit(-1);
        snippet.setMaxWidth(MAXCALLOUTLENGTH);
        titleView.addView(snippet, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        
        Callout callout = _map.getCallout();
        callout.setOffset(0, 50);
        calloutView.addView(titleView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        
        if (onCalloutClickListener != null)
          calloutView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
              if (onCalloutClickListener != null)
                onCalloutClickListener.onCalloutClick(graphic);
            }
          });
        
        callout.show(_map.toMapPoint(point.getX(), point.getY()), calloutView);
      }
      
      // call listener
      if (onGraphicClickListener != null)
        onGraphicClickListener.onGraphicClick(graphic);
      
    } else if (_map.getCallout().isShowing()){
      _map.getCallout().hide();
    }
  }

  public void onLongPress(MotionEvent point) {
    _map.getCallout().hide();
    int[] sel = getGraphicIDs(point.getX(), point.getY(), TOLERANCE, 1);
    if (sel != null && sel.length > 0) {
      Graphic graphic = getGraphic(sel[0]);
      if (!(Boolean) graphic.getAttributeValue(DRAGGABLE))
        return;
      selection = new int[] {sel[0]};
      updateGraphic(selection[0], _map.toMapPoint(point.getX(), point.getY()-100));
    }
  }

  public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
    if (selection != null && selection.length > 0) {
      updateGraphic(selection[0], _map.toMapPoint(to.getX(), to.getY()-100));
      return true;
    }
    return false;
  }

  public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
    if (selection != null && selection.length > 0) {
      selection = null;
      return true;
    }
    return false;
  }

  public void showGraphicCallout(boolean show) {
    this.showGraphicCallout = show;
    if (! show)
      _map.getCallout().hide();
  }

  public void setOnGraphicClickListener(OnGraphicClickListener listener) {
    this.onGraphicClickListener = listener;
  }
  
  public void setOnCalloutClickListener(OnCalloutClickListener listener) {
    this.onCalloutClickListener = listener;
  }

  public void createPopup(float screenX, float screenY, PopupCreateListener listener) {
    PopupContainer container = new PopupContainer(_map);
    for (Layer layer : _map.getLayers()) {
      query(layer, screenX, screenY, listener, container);
    }
  }
  
  void query(Layer layer, float screenX, float screenY, PopupCreateListener listener, PopupContainer container) {
    if (layer instanceof ArcGISFeatureLayer) {
      query((ArcGISFeatureLayer) layer, screenX, screenY, listener, container);
    } else if (layer instanceof ArcGISDynamicMapServiceLayer) {
      query((ArcGISDynamicMapServiceLayer) layer, screenX, screenY, listener, container);
    } else if (layer instanceof ArcGISTiledMapServiceLayer) {
      query((ArcGISTiledMapServiceLayer) layer, screenX, screenY, listener, container);
    } else if (layer instanceof GroupLayer) {
      query((GroupLayer) layer, screenX, screenY, listener, container);
    }
  }

  private void query(GroupLayer layer, float screenX, float screenY, PopupCreateListener listener, PopupContainer container) {
    Layer[] subLayers = layer.getLayers();
    if (subLayers == null || subLayers.length == 0)
      return;
    for (int i = 0; i < subLayers.length; i++) {
      query(subLayers[i], screenX, screenY, listener, container);
    }
  }
  
  /*
   * query ArcGISFeatureLayer
   */
  private void query(final ArcGISFeatureLayer layer, final float screenX, final float screenY, final PopupCreateListener listener, final PopupContainer container) {
    pool.submit(new Runnable() {

      @Override
      public void run() {
        queryLayer(layer, screenX, screenY, listener, container);
      }
    });
  }
  
  /*
   * query ArcGISDynamicMapServiceLayer
   */
  private void query(final ArcGISDynamicMapServiceLayer layer, final float screenX, final float screenY, final PopupCreateListener listener, final PopupContainer container) {
    pool.submit(new Runnable() {

      @Override
      public void run() {
        try {
          ArcGISLayerInfo[] layerInfos = ((ArcGISDynamicMapServiceLayer) layer).getAllLayers();
          if (layerInfos == null)
            return;
          for (ArcGISLayerInfo layerInfo : layerInfos) {
            ArcGISPopupInfo popupInfo = layer.getPopupInfo(layerInfo.getId());
            if (popupInfo == null)
              continue;
            ArcGISLayerInfo linfo = layerInfo;
            while (linfo != null && linfo.isVisible()) {
              linfo = linfo.getParentLayer();
            }
            if (linfo != null && !linfo.isVisible())
              continue;

            double maxScale = (layerInfo.getMaxScale() != 0) ? layerInfo.getMaxScale() : popupInfo.getMaxScale();
            double minScale = (layerInfo.getMinScale() != 0) ? layerInfo.getMinScale() : popupInfo.getMinScale();

            boolean matchesMaxScale = maxScale == 0 || _map.getScale() > maxScale;
            boolean matchesMinScale = minScale == 0 || _map.getScale() < minScale;
            if ((matchesMaxScale && matchesMinScale)) {
              queryLayer(layer, layerInfo.getId(), layer.getUrl() + "/" + layerInfo.getId(), popupInfo, screenX, screenY, layer.getSpatialReference(), listener, container);
            }
          }
        } catch (Exception e) {
          Log.e(_map.getClass().getSimpleName(), "", e);
        }
      }
    });
  }
  
  /*
   * query ArcGISTiledMapServiceLayer
   */
  private void query(final ArcGISTiledMapServiceLayer layer, final float screenX, final float screenY, final PopupCreateListener listener, final PopupContainer container) {
    pool.submit(new Runnable() {

      @Override
      public void run() {
        try {
          ArcGISTiledMapServiceLayer tiledLayer = (ArcGISTiledMapServiceLayer) layer;
          ArcGISLayerInfo[] layerinfos = tiledLayer.getAllLayers();
          if (layerinfos == null)
            return;
          for (ArcGISLayerInfo layerInfo : layerinfos) {
            int layerID = layerInfo.getId();
            String layerUrl = tiledLayer.getQueryUrl(layerID);
            if (layerUrl == null)
              layerUrl = tiledLayer.getUrl() + "/" + layerID;
            PopupInfo popupInfo = tiledLayer.getPopupInfo(layerID);

            if (layerInfo.getLayers() == null || layerInfo.getLayers().length > 0) {
              continue;
            }

            if (popupInfo == null) {
              continue;
            }

            ArcGISLayerInfo linfo = layerInfo;
            while (linfo != null && linfo.isVisible()) {
              linfo = linfo.getParentLayer();
            }
            if (linfo != null && !linfo.isVisible())
              continue;

            double maxScale = (layerInfo.getMaxScale() != 0) ? layerInfo.getMaxScale() : popupInfo.getMaxScale();
            double minScale = (layerInfo.getMinScale() != 0) ? layerInfo.getMinScale() : popupInfo.getMinScale();

            int currentLevel = tiledLayer.getCurrentLevel();

            TileInfo tileInfo = tiledLayer.getTileInfo();
            double lodscale = tileInfo.getScales()[currentLevel];

            boolean matchesMaxScale = maxScale == 0 || lodscale > maxScale;
            boolean matchesMinScale = minScale == 0 || lodscale < minScale;
            if ((matchesMaxScale && matchesMinScale)) {
              queryLayer(layer, layerID, layerUrl, popupInfo, screenX, screenY, layer.getSpatialReference(), listener, container);
            }
          }
        } catch (Exception e) {
          Log.e(_map.getClass().getSimpleName(), "", e);
        }
      }
    });

  }

  /*
   * run query task using url
   */
  private void queryLayer(final Layer layer, final int layerID, final String url, final PopupInfo popupInfo, float screenX, float screenY, final SpatialReference sr, PopupCreateListener listener, PopupContainer container) {

    try {
      Query query = new Query();
      query.setInSpatialReference(sr);
      query.setOutSpatialReference(sr);
      query.setGeometry(getEnvelope(screenX, screenY));
      query.setMaxFeatures(MAXFEATURE);
      query.setOutFields(new String[] { "*" });
      QueryTask queryTask = new QueryTask(url, layer.getCredentials());
      FeatureSet results = queryTask.execute(query);

      if (results != null && results.getGraphics() != null && results.getGraphics().length > 0) {
        Graphic[] graphics = results.getGraphics();
        for (int i = 0; i < graphics.length; i++) {
          container.addPopup(layer.createPopup(_map, layerID, graphics[i]));
        }
        listener.onResult(container);
      }
    } catch (Exception e) {
      Log.e(_map.getClass().getSimpleName(), "", e);
    }

  }
  
  /*
   * select features
   */
  private void queryLayer(final ArcGISFeatureLayer featureLayer, float screenX, float screenY, PopupCreateListener listener, PopupContainer container) {

    try {
      featureLayer.clearSelection();
      int[] ids = featureLayer.getGraphicIDs(screenX, screenY, TOLERANCE);
      if (ids != null && ids.length > 0) {
        for (int id : ids) {
          Graphic g = featureLayer.getGraphic(id);
          if (g == null)
            continue;
          container.addPopup(featureLayer.createPopup(_map, 0, g));
        }
        listener.onResult(container);
      }
    } catch (Exception e) {
      Log.e(_map.getClass().getSimpleName(), "", e);
    }

  }
  
  private Envelope getEnvelope(float screenX, float screenY) {
    double res = _map.getResolution();
    return new Envelope(_map.toMapPoint(screenX, screenY), TOLERANCE*res, TOLERANCE*res);
  }
}
