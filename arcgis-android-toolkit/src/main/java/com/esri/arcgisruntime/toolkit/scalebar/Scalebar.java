/*
 * Copyright 2017 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.arcgisruntime.toolkit.scalebar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.esri.arcgisruntime.UnitSystem;
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedEvent;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedListener;
import com.esri.arcgisruntime.toolkit.ToolkitUtil;

/**
 * Displays a bar or line indicating the current scale of a MapView. Two workflows are supported:
 * <p>
 * <u>Workflow 1:</u>
 * <p>
 * The simplest workflow is for the app to instantiate a Scalebar using {@link #Scalebar(Context)} and call
 * {@link #addToMapView(MapView)} to display it within the MapView. Optionally, setter methods may be called to override
 * some of the default settings. The app has limited control over the position of the scalebar (bottom-left,
 * bottom-right or bottom-centered) and no control over the size (it is sized automatically to fit comfortably within
 * the MapView).
 * <p>
 * For example:
 * <pre>
 * mScalebar = new Scalebar(mMapView.getContext());
 * mScalebar.setAlignment(ScalebarAlignment.CENTER); // optionally override default settings
 * mScalebar.addToGeoView(mMapView);
 * </pre>
 * <p>
 * <u>Workflow 2:</u>
 * <p>
 * Alternatively, the app could define a Scalebar anywhere it likes in its view hierarchy, because Scalebar extends the
 * Android View class. The system will instantiate the Scalebar using {@link #Scalebar(Context, AttributeSet)}. The app
 * then calls {@link #bindTo(MapView)} to make it come to life as a scalebar for the given MapView. This workflow gives
 * the app complete control over where the scalebar is displayed - it could be positioned on top of any part of the
 * MapView, or placed somewhere outside the bounds of the MapView. It also gives the app complete control over the size
 * of the scalebar.
 * <p>
 * Here's example XML code to define a Scalebar:
 * <pre>
 * &lt;com.esri.arcgisruntime.toolkit.scalebar.Scalebar
 *   android:id="@+id/scalebar"
 *   android:layout_width="200dp"
 *   android:layout_height="30dp"
 *   android:layout_margin="5dp"
 *   scalebar.style="DUAL_UNIT_LINE"
 *   scalebar.fillColor="@android:color/holo_orange_dark"
 *   scalebar.alternateFillColor="@android:color/holo_orange_light"
 *   scalebar.lineColor="#FFC0C0C0"
 * /&gt;
 * </pre>
 * <p>
 * Notice that some of the scalebar attributes are overridden. Here's a list of all the scalebar attributes that can be
 * set in this way:
 * <table>
 * <tr>
 * <th>XML Attribute</th>
 * <th>Description</th>
 * <th>Default Value</th>
 * </tr>
 * <tr>
 * <td>scalebar.style</td>
 * <td>The style of the Scalebar - BAR, ALTERNATING_BAR, LINE, GRADUATED_LINE or DUAL_UNIT_LINE.</td>
 * <td>ALTERNATING_BAR</td>
 * </tr>
 * <tr>
 * <td>scalebar.alignment</td>
 * <td>The alignment of the Scalebar - LEFT, RIGHT or CENTER.</td>
 * <td>LEFT</td>
 * </tr>
 * <tr>
 * <td>scalebar.unitSystem</td>
 * <td>The unit system of the Scalebar - METRIC or IMPERIAL.</td>
 * <td>METRIC</td>
 * </tr>
 * <tr>
 * <td>scalebar.fillColor</td>
 * <td>The fill color of the Scalebar. This is used to fill the bar when the style is BAR or ALTERNATING_BAR. In XML
 * this may be a color value or a reference to a color resource, for example "#FF0000" (RRGGBB) or "#FF808080"
 * (AARRGGBB) or "@android:color/white".</td>
 * <td>Semi-transparent light gray</td>
 * </tr>
 * <tr>
 * <td>scalebar.alternateFillColor</td>
 * <td>The alternate fill color of the Scalebar. This is used to fill alternate segments of the bar when the style
 * is ALTERNATING_BAR. In XML this may be a color value or a reference to a color resource.</td>
 * <td>Black</td>
 * </tr>
 * <tr>
 * <td>scalebar.lineColor</td>
 * <td>The line color of the Scalebar. In XML this may be a color value or a reference to a color resource.</td>
 * <td>White</td>
 * </tr>
 * <tr>
 * <td>scalebar.shadowColor</td>
 * <td>The shadow color of the Scalebar. This is used for the shadow of the bar. In XML this may be a color value or a
 * reference to a color resource.</td>
 * <td>Semi-transparent black</td>
 * </tr>
 * <tr>
 * <td>scalebar.textColor</td>
 * <td>The text color of the Scalebar. In XML this may be a color value or a reference to a color resource.</td>
 * <td>Black</td>
 * </tr>
 * <tr>
 * <td>scalebar.textShadowColor</td>
 * <td>The text shadow color of the Scalebar. This is used for the shadow of the text. In XML this may be a color value
 * or a reference to a color resource.</td>
 * <td>White</td>
 * </tr>
 * <tr>
 * <td>scalebar.textSize</td>
 * <td>The text size of the Scalebar, in density-independent pixels.</td>
 * <td>15</td>
 * </tr>
 * <tr>
 * <td>scalebar.barHeight</td>
 * <td>The bar height of the Scalebar, in density-independent pixels. This is the height of the bar itself, not
 * including the text.</td>
 * <td>10</td>
 * </tr>
 * </table>
 * <p>
 * Setting the typeface attribute from XML is not supported. Use {@link #setTypeface(Typeface)} if you need to override
 * the default typeface.
 * <p>
 * And here's example Java code to bind the Scalebar to the MapView:
 * <pre>
 * mScalebar = (Scalebar) findViewById(R.id.scalebar);
 * mScalebar.bindTo(mMapView);
 * </pre>
 *
 * @since 100.1.0
 */
public final class Scalebar extends View {

  private static final String TAG = Scalebar.class.getSimpleName();

  private static final LinearUnit LINEAR_UNIT_METERS = new LinearUnit(LinearUnitId.METERS);

  private static final LinearUnit LINEAR_UNIT_FEET = new LinearUnit(LinearUnitId.FEET);

  private static final int ALPHA_50_PC = 0x80000000;

  private static final int SCALEBAR_X_PAD_DP = 10;

  private static final int SCALEBAR_Y_PAD_DP = 10;

  private static final int LABEL_X_PAD_DP = 6;

  private static final int SHADOW_OFFSET_PIXELS = 2;

  private static final Style DEFAULT_STYLE = Style.ALTERNATING_BAR;

  private static final Alignment DEFAULT_ALIGNMENT = Alignment.LEFT;

  private static final UnitSystem DEFAULT_UNIT_SYSTEM = UnitSystem.METRIC;

  private static final int DEFAULT_FILL_COLOR = Color.LTGRAY | ALPHA_50_PC;

  private static final int DEFAULT_ALTERNATE_FILL_COLOR = Color.BLACK;

  private static final int DEFAULT_LINE_COLOR = Color.WHITE;

  private static final int DEFAULT_SHADOW_COLOR = Color.BLACK | ALPHA_50_PC;

  private static final int DEFAULT_TEXT_COLOR = Color.BLACK;

  private static final int DEFAULT_TEXT_SHADOW_COLOR = Color.WHITE;

  private static final Typeface DEFAULT_TYPEFACE = Typeface.DEFAULT_BOLD;

  private static final int DEFAULT_TEXT_SIZE_DP = 15;

  private static final int DEFAULT_BAR_HEIGHT_DP = 10;

  private Style mStyle;

  private Alignment mAlignment = DEFAULT_ALIGNMENT;

  private UnitSystem mUnitSystem = DEFAULT_UNIT_SYSTEM;

  private int mFillColor = DEFAULT_FILL_COLOR;

  private int mAlternateFillColor = DEFAULT_ALTERNATE_FILL_COLOR;

  private int mLineColor = DEFAULT_LINE_COLOR;

  private int mShadowColor = DEFAULT_SHADOW_COLOR;

  private int mTextColor = DEFAULT_TEXT_COLOR;

  private int mTextShadowColor = DEFAULT_TEXT_SHADOW_COLOR;

  private Typeface mTypeface = DEFAULT_TYPEFACE;

  private float mTextSizeDp = DEFAULT_TEXT_SIZE_DP;

  private float mBarHeightDp = DEFAULT_BAR_HEIGHT_DP;

  private float mLineWidthDp = mBarHeightDp / 4;

  private float mCornerRadiusDp = mBarHeightDp / 5;

  private MapView mMapView;

  private ScalebarRenderer mRenderer;

  private Paint mTextPaint;

  private final android.graphics.Point mGraphicsPoint = new android.graphics.Point();

  private volatile int mAttributionTextHeight = 0;

  private float mDisplayDensity;

  private boolean mScalebarIsChildOfMapView = false;

  private final ViewpointChangedListener mViewpointChangedListener = new ViewpointChangedListener() {
    @Override
    public void viewpointChanged(ViewpointChangedEvent viewpointChangedEvent) {
      // Invalidate the Scalebar view when the MapView viewpoint changes
      postInvalidate();
    }
  };

  private final OnLayoutChangeListener mAttributionViewLayoutChangeListener = new OnLayoutChangeListener() {
    @Override
    public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop,
        int oldRight, int oldBottom) {
      // Recalculate the attribution text height and invalidate the Scalebar view when the bounds of the attribution
      // view change
      mAttributionTextHeight = bottom - top;
      postInvalidate();
    }
  };

  /**
   * Constructs a Scalebar programmatically. Called by the app when Workflow 1 is used (see {@link Scalebar} above).
   *
   * @param context the current execution Context
   * @since 100.1.0
   */
  public Scalebar(Context context) {
    super(context);
    setStyle(DEFAULT_STYLE);
  }

  /**
   * Constructor that's called when inflating a Scalebar from XML. Called by the system when Workflow 2 is used (see
   * {@link Scalebar} above).
   *
   * @param context the current execution Context
   * @param attrs   the attributes of the XML tag that is inflating the view
   * @since 100.1.0
   */
  public Scalebar(Context context, AttributeSet attrs) {
    super(context, attrs);
    if (attrs == null) {
      setStyle(DEFAULT_STYLE);
      return;
    }
    setStyle(getStyleFromAttributes(attrs));
    mAlignment = getAlignmentFromAttributes(attrs);
    mUnitSystem = getUnitSystemFromAttributes(attrs);
    mFillColor = getColorFromAttributes(context, attrs, "scalebar.fillColor", DEFAULT_FILL_COLOR);
    mAlternateFillColor =
        getColorFromAttributes(context, attrs, "scalebar.alternateFillColor", DEFAULT_ALTERNATE_FILL_COLOR);
    mLineColor = getColorFromAttributes(context, attrs, "scalebar.lineColor", DEFAULT_LINE_COLOR);
    mShadowColor = getColorFromAttributes(context, attrs, "scalebar.shadowColor", DEFAULT_SHADOW_COLOR);
    mTextColor = getColorFromAttributes(context, attrs, "scalebar.textColor", DEFAULT_TEXT_COLOR);
    mTextShadowColor = getColorFromAttributes(context, attrs, "scalebar.textShadowColor", DEFAULT_TEXT_SHADOW_COLOR);
    mTextSizeDp = attrs.getAttributeIntValue(null, "scalebar.textSize", DEFAULT_TEXT_SIZE_DP);
    mBarHeightDp = attrs.getAttributeIntValue(null, "scalebar.barHeight", DEFAULT_BAR_HEIGHT_DP);
  }

  /**
   * Adds this Scalebar to the given MapView. Used in Workflow 1 (see {@link Scalebar} above).
   *
   * @param mapView the MapView
   * @throws IllegalArgumentException if mapView is null
   * @throws IllegalStateException    if this Scalebar is already added to or bound to a MapView
   * @since 100.1.0
   */
  public void addToMapView(MapView mapView) {
    ToolkitUtil.throwIfNull(mapView, "mapView");
    if (mMapView != null) {
      throw new IllegalStateException("Scalebar already has a MapView");
    }
    setupMapView(mapView);
    mMapView.addView(this, new ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
        android.view.ViewGroup.LayoutParams.MATCH_PARENT));
    mScalebarIsChildOfMapView = true;
  }

  /**
   * Removes and unbinds this Scalebar from the MapView it was added or bound to (if any).
   *
   * @since 100.1.0
   */
  public void removeFromMapView() {
    // If it was added to a MapView, remove it
    if (mScalebarIsChildOfMapView) {
      mMapView.removeView(this);
      mScalebarIsChildOfMapView = false;
    }

    // Unbind from MapView by removing listeners
    if (mMapView != null) {
      removeListenersFromMapView();
      mMapView = null;
    }
  }

  /**
   * Binds this Scalebar to the given MapView. Used in Workflow 2 (see {@link Scalebar} above).
   *
   * @param mapView the MapView
   * @throws IllegalArgumentException if mapView is null
   * @throws IllegalStateException    if this Scalebar is currently added to a MapView
   * @since 100.1.0
   */
  public void bindTo(MapView mapView) {
    ToolkitUtil.throwIfNull(mapView, "mapView");
    if (mScalebarIsChildOfMapView) {
      throw new IllegalStateException("Scalebar already added to a MapView");
    }
    setupMapView(mapView);
    mScalebarIsChildOfMapView = false;
  }

  /**
   * Sets the style of this Scalebar. The default value is {@link Style#ALTERNATING_BAR}.
   *
   * @param style the style to set
   * @throws IllegalArgumentException if style is null
   * @since 100.1.0
   */
  public void setStyle(Style style) {
    ToolkitUtil.throwIfNull(style, "style");
    mStyle = style;
    switch (mStyle) {
      case BAR:
        mRenderer = new BarRenderer();
        break;
      case ALTERNATING_BAR:
        mRenderer = new AlternatingBarRenderer();
        break;
      case LINE:
        mRenderer = new LineRenderer();
        break;
      case GRADUATED_LINE:
        mRenderer = new GraduatedLineRenderer();
        break;
      case DUAL_UNIT_LINE:
        mRenderer = new DualUnitLineRenderer();
        break;
    }
    postInvalidate();
  }

  /**
   * Gets the style of this Scalebar.
   *
   * @return the style
   * @since 100.1.0
   */
  public Style getStyle() {
    return mStyle;
  }

  /**
   * Sets the alignment of this Scalebar. The default value is {@link Alignment#LEFT}. In Workflow 1 (see
   * {@link Scalebar} above) the alignment controls the position of the Scalebar within the MapView, whereas in
   * Workflow 2 it only controls which end of the scalebar is fixed and which end shrinks and grows. See
   * {@link Alignment} for more details.
   *
   * @param alignment the alignment to set
   * @throws IllegalArgumentException if alignment is null
   * @since 100.1.0
   */
  public void setAlignment(Alignment alignment) {
    ToolkitUtil.throwIfNull(alignment, "alignment");
    mAlignment = alignment;
    postInvalidate();
  }

  /**
   * Gets the alignment of this Scalebar.
   *
   * @return the alignment
   * @since 100.1.0
   */
  public Alignment getAlignment() {
    return mAlignment;
  }

  /**
   * Sets the unit system of this Scalebar. The default value is UnitSystem.METRIC.
   *
   * @param unitSystem the unit system to set
   * @throws IllegalArgumentException if unitSystem is null
   * @since 100.1.0
   */
  public void setUnitSystem(UnitSystem unitSystem) {
    ToolkitUtil.throwIfNull(unitSystem, "unitSystem");
    mUnitSystem = unitSystem;
    postInvalidate();
  }

  /**
   * Gets the unit system of this Scalebar.
   *
   * @return the unit system
   * @since 100.1.0
   */
  public UnitSystem getUnitSystem() {
    return mUnitSystem;
  }

  /**
   * Sets the fill color of this Scalebar. This is used to fill the bar when the style is BAR or ALTERNATING_BAR. The
   * default is semi-transparent light gray.
   *
   * @param color the color to set
   * @since 100.1.0
   */
  public void setFillColor(int color) {
    mFillColor = color;
    postInvalidate();
  }

  /**
   * Gets the fill color of this Scalebar.
   *
   * @return the fill color
   * @since 100.1.0
   */
  public int getFillColor() {
    return mFillColor;
  }

  /**
   * Sets the alternate fill color of this Scalebar. This is used to fill alternate segments of the bar when the style
   * is ALTERNATING_BAR. The default is black.
   *
   * @param color the color to set
   * @since 100.1.0
   */
  public void setAlternateFillColor(int color) {
    mAlternateFillColor = color;
    postInvalidate();
  }

  /**
   * Gets the alternate fill color of this Scalebar.
   *
   * @return the alternate fill color
   * @since 100.1.0
   */
  public int getAlternateFillColor() {
    return mAlternateFillColor;
  }

  /**
   * Sets the line color of this Scalebar. The default is white.
   *
   * @param color the color to set
   * @since 100.1.0
   */
  public void setLineColor(int color) {
    mLineColor = color;
    postInvalidate();
  }

  /**
   * Gets the line color of this Scalebar.
   *
   * @return the line color
   * @since 100.1.0
   */
  public int getLineColor() {
    return mLineColor;
  }

  /**
   * Sets the shadow color of this Scalebar. This is used for the shadow of the bar. The default is semi-transparent
   * black.
   *
   * @param color the color to set
   * @since 100.1.0
   */
  public void setShadowColor(int color) {
    mShadowColor = color;
    postInvalidate();
  }

  /**
   * Gets the shadow color of this Scalebar.
   *
   * @return the shadow color
   * @since 100.1.0
   */
  public int getShadowColor() {
    return mShadowColor;
  }

  /**
   * Sets the text color of this Scalebar. The default is black.
   *
   * @param color the color to set
   * @since 100.1.0
   */
  public void setTextColor(int color) {
    mTextColor = color;
    createTextPaint();
    postInvalidate();
  }

  /**
   * Gets the text color of this Scalebar.
   *
   * @return the text color
   * @since 100.1.0
   */
  public int getTextColor() {
    return mTextColor;
  }

  /**
   * Sets the text shadow color of this Scalebar. This is used for the shadow of the text. The default is white.
   *
   * @param color the color to set
   * @since 100.1.0
   */
  public void setTextShadowColor(int color) {
    mTextShadowColor = color;
    createTextPaint();
    postInvalidate();
  }

  /**
   * Gets the text shadow color of this Scalebar.
   *
   * @return the text shadow color
   * @since 100.1.0
   */
  public int getTextShadowColor() {
    return mTextShadowColor;
  }

  /**
   * Sets the typeface of this Scalebar. The default is Typeface.DEFAULT_BOLD.
   *
   * @param typeface the typeface to set
   * @throws IllegalArgumentException if typeface is null
   * @since 100.1.0
   */
  public void setTypeface(Typeface typeface) {
    ToolkitUtil.throwIfNull(typeface, "typeface");
    mTypeface = typeface;
    createTextPaint();
    postInvalidate();
  }

  /**
   * Gets the typeface of this Scalebar.
   *
   * @return the typeface
   * @since 100.1.0
   */
  public Typeface getTypeface() {
    return mTypeface;
  }

  /**
   * Sets the text size of this Scalebar. The default is 15dp.
   *
   * @param textSizeDp the text size to set, in density-independent pixels
   * @since 100.1.0
   */
  public void setTextSize(float textSizeDp) {
    mTextSizeDp = textSizeDp;
    createTextPaint();
    postInvalidate();
  }

  /**
   * Gets the text size of this Scalebar.
   *
   * @return the text size, in density-independent pixels
   * @since 100.1.0
   */
  public float getTextSize() {
    return mTextSizeDp;
  }

  /**
   * Sets the bar height of this Scalebar. This is the height of the bar itself, not including the text. The default is
   * 10dp.
   *
   * @param barHeightDp the bar height to set, in density-independent pixels
   * @since 100.1.0
   */
  public void setBarHeight(float barHeightDp) {
    mBarHeightDp = barHeightDp;
    mLineWidthDp = Math.max(mBarHeightDp / 4, 1);
    mCornerRadiusDp = Math.max(mBarHeightDp / 5, 1);
    postInvalidate();
  }

  /**
   * Gets the bar height of this Scalebar.
   *
   * @return the bar height, in density-independent pixels
   * @since 100.1.0
   */
  public float getBarHeight() {
    return mBarHeightDp;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mMapView == null) {
      return;
    }

    // Calculate width and height of visible part of MapView
    int mapViewVisibleWidth =
        mMapView.getWidth() - dpToPixels(mMapView.getViewInsetLeft() + mMapView.getViewInsetRight());
    int mapViewVisibleHeight =
        mMapView.getHeight() - dpToPixels(mMapView.getViewInsetTop() + mMapView.getViewInsetBottom());

    // Calculate maximum length of scalebar in pixels
    LinearUnit baseUnits = mUnitSystem == UnitSystem.METRIC ? LINEAR_UNIT_METERS : LINEAR_UNIT_FEET;
    float maxScaleBarLengthPixels;
    if (mScalebarIsChildOfMapView) {
      // When scalebar is a child of the MapView, its length is based on the size of the visible part of the MapView
      maxScaleBarLengthPixels =
          mapViewVisibleWidth > mapViewVisibleHeight ? mapViewVisibleWidth / 4 : mapViewVisibleWidth / 3;
    } else {
      // When scalebar is a separate view, its length is based on the view's width; note we allow padding of
      // mLineWidthDp at each end of the scalebar to ensure the lines at the ends fit within the view
      maxScaleBarLengthPixels = getWidth() - mRenderer.calculateExtraSpaceForUnits(null) -
          (2 * dpToPixels(mLineWidthDp));
      // But don't allow the scalebar length to be greater than the MapView width
      maxScaleBarLengthPixels = Math.min(maxScaleBarLengthPixels, mapViewVisibleWidth);
    }

    // Calculate geodetic length of scalebar based on its maximum length in pixels
    int centerX = dpToPixels(mMapView.getViewInsetLeft()) + (mapViewVisibleWidth / 2);
    int centerY = dpToPixels(mMapView.getViewInsetTop()) + (mapViewVisibleHeight / 2);
    mGraphicsPoint.set((int) (centerX - (maxScaleBarLengthPixels / 2)), centerY);
    Point p1 = mMapView.screenToLocation(mGraphicsPoint);
    mGraphicsPoint.set((int) (centerX + (maxScaleBarLengthPixels / 2)), centerY);
    Point p2 = mMapView.screenToLocation(mGraphicsPoint);
    Polygon visibleArea = mMapView.getVisibleArea();
    if (p1 == null || p2 == null || visibleArea == null) {
      return;
    }
    Point centerPoint = visibleArea.getExtent().getCenter();
    PolylineBuilder builder = new PolylineBuilder(mMapView.getSpatialReference());
    builder.addPoint(p1);
    builder.addPoint(centerPoint); // include center point to ensure it goes the correct way round the globe
    builder.addPoint(p2);
    double maxLengthGeodetic =
        GeometryEngine.lengthGeodetic(builder.toGeometry(), baseUnits, GeodeticCurveType.GEODESIC);

    // Reduce length to make its geodetic length a nice number
    double scalebarLengthGeodetic =
        ScalebarUtil.calculateBestScalebarLength(maxLengthGeodetic, baseUnits, mRenderer.isSegmented());
    float scalebarLengthPixels = (float) (maxScaleBarLengthPixels * scalebarLengthGeodetic / maxLengthGeodetic);

    // Change units if the geodetic length is too big a number in the base units
    LinearUnit displayUnits = ScalebarUtil.selectLinearUnit(scalebarLengthGeodetic, mUnitSystem);
    if (displayUnits != baseUnits) {
      scalebarLengthGeodetic = baseUnits.convertTo(displayUnits, scalebarLengthGeodetic);
    }

    // Calculate screen coordinates of left, right, top and bottom of the scalebar
    float left = calculateLeftPos(mAlignment, scalebarLengthPixels, displayUnits);
    float right = left + scalebarLengthPixels;
    float bottom;
    float maxPixelsBelowBaseline = mTextPaint.getFontMetrics().bottom;
    if (mScalebarIsChildOfMapView) {
      bottom = mMapView.getHeight() - mAttributionTextHeight - maxPixelsBelowBaseline -
          dpToPixels(mMapView.getViewInsetBottom() + SCALEBAR_Y_PAD_DP + mTextSizeDp);
    } else {
      bottom = getHeight() - dpToPixels(mTextSizeDp) - maxPixelsBelowBaseline;
    }
    float top = bottom - dpToPixels(mBarHeightDp);

    // Draw the scalebar
    mRenderer.drawScalebar(canvas, left, top, right, bottom, scalebarLengthGeodetic, displayUnits);
  }

  /**
   * Sets up the Scalebar to work with the given MapView.
   *
   * @param mapView the MapView
   * @since 100.1.0
   */
  private void setupMapView(MapView mapView) {
    // Remove listeners from old MapView
    if (mMapView != null) {
      removeListenersFromMapView();
    }

    // Add listeners to new MapView
    mMapView = mapView;
    mMapView.addViewpointChangedListener(mViewpointChangedListener);
    mMapView.addAttributionViewLayoutChangeListener(mAttributionViewLayoutChangeListener);

    // Set display density and create the Paint used for text (note display density must be set first)
    mDisplayDensity = mMapView.getContext().getResources().getDisplayMetrics().density;
    createTextPaint();
  }

  /**
   * Creates the Paint used for drawing text.
   *
   * @since 100.1.0
   */
  private void createTextPaint() {
    mTextPaint = new Paint();
    mTextPaint.setColor(mTextColor);
    mTextPaint.setShadowLayer(2, SHADOW_OFFSET_PIXELS, SHADOW_OFFSET_PIXELS, mTextShadowColor);
    mTextPaint.setTypeface(mTypeface);
    mTextPaint.setTextSize(dpToPixels(mTextSizeDp));
  }

  /**
   * Removes the listeners from mMapView.
   *
   * @since 100.1.0
   */
  private void removeListenersFromMapView() {
    mMapView.removeViewpointChangedListener(mViewpointChangedListener);
    mMapView.removeAttributionViewLayoutChangeListener(mAttributionViewLayoutChangeListener);
  }

  /**
   * Gets the scalebar style from an AttributeSet object, or a default value if it's not specified there.
   *
   * @param attrs the AttributeSet object containing attributes and values to use
   * @return the scalebar style
   * @since 100.1.0
   */
  private Style getStyleFromAttributes(AttributeSet attrs) {
    Style style = DEFAULT_STYLE;
    String str = attrs.getAttributeValue(null, "scalebar.style");
    if (str != null) {
      try {
        style = Style.valueOf(str);
      } catch (IllegalArgumentException e) {
        // allow it to use the default value set above
      }
    }
    return style;
  }

  /**
   * Gets the scalebar alignment from an AttributeSet object, or a default value if it's not specified there.
   *
   * @param attrs the AttributeSet object containing attributes and values to use
   * @return the scalebar alignment
   * @since 100.1.0
   */
  private Alignment getAlignmentFromAttributes(AttributeSet attrs) {
    Alignment alignment = DEFAULT_ALIGNMENT;
    String str = attrs.getAttributeValue(null, "scalebar.alignment");
    if (str != null) {
      try {
        alignment = Alignment.valueOf(str);
      } catch (IllegalArgumentException e) {
        // allow it to use the default value set above
      }
    }
    return alignment;
  }

  /**
   * Gets the scalebar unit system from an AttributeSet object, or a default value if it's not specified there.
   *
   * @param attrs the AttributeSet object containing attributes and values to use
   * @return the scalebar unit system
   * @since 100.1.0
   */
  private UnitSystem getUnitSystemFromAttributes(AttributeSet attrs) {
    UnitSystem unitSystem = DEFAULT_UNIT_SYSTEM;
    String str = attrs.getAttributeValue(null, "scalebar.unitSystem");
    if (str != null) {
      try {
        unitSystem = UnitSystem.valueOf(str);
      } catch (IllegalArgumentException e) {
        // allow it to use the default value set above
      }
    }
    return unitSystem;
  }

  /**
   * Gets a color attribute from an AttributeSet object, or a default value if it's not specified there.
   *
   * @param context       the current execution Context
   * @param attrs         the AttributeSet object containing attributes and values to use
   * @param attributeName the name of the color attribute to get
   * @param defaultValue  the default value to use if attributeName not found in attrs
   * @return the color
   * @since 100.1.0
   */
  private int getColorFromAttributes(Context context, AttributeSet attrs, String attributeName, int defaultValue) {
    int color = 0;

    // If the attribute value is a resource ID, get the color corresponding to that resource
    int resId = attrs.getAttributeResourceValue(null, attributeName, -1);
    if (resId >= 0) {
      try {
        color = context.getResources().getColor(resId);
      } catch (Resources.NotFoundException e) {
        // allow color to retain its initial value
      }
    }

    // If that doesn't yield a color, treat the integer value of the attribute as a color
    if (color == 0) {
      color = attrs.getAttributeIntValue(null, attributeName, defaultValue);
    }
    return color;
  }

  /**
   * Calculates the x-coordinate of the left hand end of the scalebar.
   *
   * @param alignment      the alignment of the scalebar
   * @param scalebarLength the length of the scalebar in pixels
   * @param displayUnits   the units to be displayed
   * @return the x-coordinate of the left hand end of the scalebar
   * @since 100.1.0
   */
  private float calculateLeftPos(Alignment alignment, float scalebarLength, LinearUnit displayUnits) {
    int left = 0;
    int right = getWidth();
    int padding = dpToPixels(mLineWidthDp); // padding to ensure the lines at the ends fit within the view
    if (mScalebarIsChildOfMapView) {
      left = dpToPixels(mMapView.getViewInsetLeft());
      right -= dpToPixels(mMapView.getViewInsetRight());
      padding = dpToPixels(SCALEBAR_X_PAD_DP);
    }
    switch (alignment) {
      case LEFT:
      default:
        // Position start of scalebar at left hand edge of the view, plus padding
        return left + padding;
      case RIGHT:
        // Position end of scalebar at right hand edge of the view, less padding and the width of the units string (if
        // required)
        return right - padding - dpToPixels(mLineWidthDp) - scalebarLength -
            mRenderer.calculateExtraSpaceForUnits(displayUnits);
      case CENTER:
        // Position center of scalebar (plus units string if required) at center of the view
        return (right + left - scalebarLength - mRenderer.calculateExtraSpaceForUnits(displayUnits)) / 2;
    }
  }

  /**
   * Converts density-independent pixels to actual pixels.
   *
   * @param dp a number of density-independent pixels
   * @return the equivalent number of actual pixels
   * @since 100.1.0
   */
  private int dpToPixels(double dp) {
    double pixels = dp * mDisplayDensity;
    return Math.round((float) pixels);
  }

  /**
   * Represents the style of scalebar to be displayed.
   *
   * @since 100.1.0
   */
  public enum Style {
    /**
     * A simple, non-segmented bar. A single label is displayed showing the distance represented by the length of the
     * whole bar.
     *
     * @since 100.1.0
     */
    BAR,

    /**
     * A bar split up into equal-length segments, with the colors of the segments alternating between the fill color and
     * the alternate fill color. A label is displayed at the end of each segment, showing the distance represented by
     * the length of the bar up to that point.
     *
     * @since 100.1.0
     */
    ALTERNATING_BAR,

    /**
     * A simple, non-segmented line. A single label is displayed showing the distance represented by the length of the
     * whole line.
     *
     * @since 100.1.0
     */
    LINE,

    /**
     * A line split up into equal-length segments. A tick and a label are displayed at the end of each segment, showing
     * the distance represented by the length of the line up to that point.
     *
     * @since 100.1.0
     */
    GRADUATED_LINE,

    /**
     * A line showing distance in dual unit systems - metric and imperial. The primary unit system, as set by
     * {@link #setUnitSystem(UnitSystem)}, is used to determine the length of the line. A label above the line shows the
     * distance represented by the length of the whole line, in the primary unit system. A tick and another label are
     * displayed below the line, showing distance in the other unit system.
     *
     * @since 100.1.0
     */
    DUAL_UNIT_LINE
  }

  /**
   * Represents the alignment of scalebar to be displayed.
   *
   * @since 100.1.0
   */
  public enum Alignment {
    /**
     * The scalebar is left-aligned, meaning that the left hand end of the scalebar is fixed and it shrinks and grows at
     * the right hand end. If the scalebar is added to a MapView using {@link #addToMapView(MapView)}, it will be
     * positioned near the bottom-left corner of the MapView.
     *
     * @since 100.1.0
     */
    LEFT,

    /**
     * The scalebar is right-aligned, meaning that the right hand end of the scalebar is fixed and it shrinks and grows
     * at the left hand end. If the scalebar is added to a MapView using {@link #addToMapView(MapView)}, it will be
     * positioned near the bottom-right corner of the MapView.
     *
     * @since 100.1.0
     */
    RIGHT,

    /**
     * The scalebar is center-aligned, meaning that the center point of the scalebar is fixed and it shrinks and grows
     * at both ends. If the scalebar is added to a MapView using {@link #addToMapView(MapView)}, it will be
     * positioned near the bottom the MapView, centered between the left and right edges.
     *
     * @since 100.1.0
     */
    CENTER
  }

  /**
   * Renders a scalebar. There are concrete subclasses corresponding to each {@link Scalebar.Style}.
   *
   * @since 100.1.0
   */
  private abstract class ScalebarRenderer {

    /**
     * Draws a scalebar.
     *
     * @param canvas       the Canvas to draw on
     * @param left         the x-coordinate of the left hand edge of the scalebar
     * @param top          the y-coordinate of the top of the scalebar
     * @param right        the x-coordinate of the right hand edge of the scalebar
     * @param bottom       the y-coordinate of the bottom of the scalebar
     * @param distance     the distance represented by the length of the whole scalebar
     * @param displayUnits the units of distance
     * @since 100.1.0
     */
    public abstract void drawScalebar(Canvas canvas, float left, float top, float right, float bottom,
        double distance, LinearUnit displayUnits);

    // The following are defined as member fields to minimize object allocation during draw operations
    protected final Rect mRect = new Rect();

    protected final RectF mRectF = new RectF();

    protected final Path mLinePath = new Path();

    protected final Paint mPaint = new Paint();

    /**
     * Indicates if this style of scalebar is segmented.
     *
     * @return true if this style of scalebar is segmented, false otherwise
     * @since 100.1.0
     */
    public abstract boolean isSegmented();

    /**
     * Calculates the extra space required at the right hand end of the scalebar to draw the units (if any). This
     * affects the positioning of the scalebar when it is right-aligned.
     *
     * @param displayUnits the units
     * @return the extra space required, in pixels
     * @since 100.1.0
     */
    public abstract float calculateExtraSpaceForUnits(LinearUnit displayUnits);

    /**
     * Draws a solid bar and its shadow. Used by BarRenderer and AlternatingBarRenderer.
     *
     * @param canvas   the Canvas to draw on
     * @param left     the x-coordinate of the left hand edge of the scalebar
     * @param top      the y-coordinate of the top of the scalebar
     * @param right    the x-coordinate of the right hand edge of the scalebar
     * @param bottom   the y-coordinate of the bottom of the scalebar
     * @param barColor the fill color for the bar
     * @since 100.1.0
     */
    protected void drawBarAndShadow(Canvas canvas, float left, float top, float right, float bottom, int barColor) {
      // Draw the shadow of the bar, offset slightly from where the actual bar is drawn below
      mRectF.set(left, top, right, bottom);
      int offset = SHADOW_OFFSET_PIXELS + (dpToPixels(mLineWidthDp) / 2);
      mRectF.offset(offset, offset);
      mPaint.reset();
      mPaint.setColor(mShadowColor);
      mPaint.setStyle(Paint.Style.FILL);
      canvas.drawRoundRect(mRectF, dpToPixels(mCornerRadiusDp), dpToPixels(mCornerRadiusDp), mPaint);

      // Now draw the bar
      mRectF.set(left, top, right, bottom);
      mPaint.setColor(barColor);
      canvas.drawRoundRect(mRectF, dpToPixels(mCornerRadiusDp), dpToPixels(mCornerRadiusDp), mPaint);
    }

    /**
     * Draws a line and its shadow, including the ticks at each end. Used by LineRenderer and GraduatedLineRenderer.
     *
     * @param canvas the Canvas to draw on
     * @param left   the x-coordinate of the left hand edge of the scalebar
     * @param top    the y-coordinate of the top of the scalebar
     * @param right  the x-coordinate of the right hand edge of the scalebar
     * @param bottom the y-coordinate of the bottom of the scalebar
     * @since 100.1.0
     */
    protected void drawLineAndShadow(Canvas canvas, float left, float top, float right, float bottom) {
      // Create a path to draw the left-hand tick, the line itself and the right-hand tick
      mLinePath.reset();
      mLinePath.moveTo(left, top);
      mLinePath.lineTo(left, bottom);
      mLinePath.lineTo(right, bottom);
      mLinePath.lineTo(right, top);
      mLinePath.setLastPoint(right, top);

      // Create a copy to be the path of the shadow, offset slightly from the path of the line
      Path shadowPath = new Path(mLinePath);
      shadowPath.offset(SHADOW_OFFSET_PIXELS, SHADOW_OFFSET_PIXELS);

      // Draw the shadow
      mPaint.reset();
      mPaint.setColor(mShadowColor);
      mPaint.setStyle(Paint.Style.STROKE);
      mPaint.setStrokeWidth(dpToPixels(mLineWidthDp));
      mPaint.setStrokeCap(Paint.Cap.ROUND);
      mPaint.setStrokeJoin(Paint.Join.ROUND);
      canvas.drawPath(shadowPath, mPaint);

      // Now draw the line on top of the shadow
      mPaint.setColor(mLineColor);
      canvas.drawPath(mLinePath, mPaint);
    }

    /**
     * Calculates the optimal number of segments in a segmented scalebar of a particular length. Used by
     * AlternatingBarRenderer and GraduatedLineRenderer.
     *
     * @param distance      the distance represented by the length of the whole scalebar
     * @param displayLength the length of the scalebar in pixels
     * @return the number of segments
     * @since 100.1.0
     */
    protected int calculateNumberOfSegments(double distance, double displayLength) {
      // The constraining factor is the space required to draw the labels. Create a testString containing the longest
      // label, which is usually the one for 'distance' because the other labels will be smaller numbers.
      String testString = ScalebarUtil.labelString(distance);

      // But if 'distance' is small some of the other labels may use decimals, so allow for each label needing at least
      // 3 characters
      if (testString.length() < 3) {
        testString = "9.9";
      }

      // Calculate the bounds of the testString to determine its length
      mTextPaint.getTextBounds(testString, 0, testString.length(), mRect);

      // Calculate the minimum segment length to ensure the labels don't overlap; multiply the testString length by 1.5
      // to allow for the right-most label being right-justified whereas the other labels are center-justified
      double minSegmentLength = mRect.right * 1.5 + dpToPixels(LABEL_X_PAD_DP);

      // Calculate the number of segments
      int maxNumSegments = (int) (displayLength / minSegmentLength);
      return ScalebarUtil.calculateOptimalNumberOfSegments(distance, maxNumSegments);
    }

    /**
     * Calculates the width of the units string.
     *
     * @param displayUnits the units to be displayed, or null if not known yet
     * @return the width of the units string, in pixels
     * @since 100.1.0
     */
    protected float calculateWidthOfUnitsString(LinearUnit displayUnits) {
      String unitsText = ' ' + (displayUnits == null ? "mm" : displayUnits.getAbbreviation());
      mTextPaint.getTextBounds(unitsText, 0, unitsText.length(), mRect);
      return mRect.right;
    }

  }

  /**
   * Renders a BAR style scalebar.
   *
   * @see Style#BAR
   * @since 100.1.0
   */
  private final class BarRenderer extends ScalebarRenderer {

    @Override
    public boolean isSegmented() {
      return false;
    }

    @Override
    public float calculateExtraSpaceForUnits(LinearUnit displayUnits) {
      return 0;
    }

    @Override
    public void drawScalebar(Canvas canvas, float left, float top, float right, float bottom, double distance,
        LinearUnit displayUnits) {

      // Draw a solid bar and its shadow
      drawBarAndShadow(canvas, left, top, right, bottom, mFillColor);

      // Draw a line round the outside
      mRectF.set(left, top, right, bottom);
      mPaint.reset();
      mPaint.setColor(mLineColor);
      mPaint.setStyle(Paint.Style.STROKE);
      mPaint.setStrokeWidth(dpToPixels(mLineWidthDp));
      canvas.drawRoundRect(mRectF, dpToPixels(mCornerRadiusDp), dpToPixels(mCornerRadiusDp), mPaint);

      // Draw the label, centered on the center of the bar
      String label = ScalebarUtil.labelString(distance) + " " + displayUnits.getAbbreviation();
      mTextPaint.setTextAlign(Paint.Align.CENTER);
      canvas.drawText(label, left + ((right - left) / 2), bottom + dpToPixels(mTextSizeDp), mTextPaint);
    }
  }

  /**
   * Renders an ALTERNATING_BAR style scalebar.
   *
   * @see Style#ALTERNATING_BAR
   * @since 100.1.0
   */
  private final class AlternatingBarRenderer extends ScalebarRenderer {

    @Override
    public boolean isSegmented() {
      return true;
    }

    @Override
    public float calculateExtraSpaceForUnits(LinearUnit displayUnits) {
      return calculateWidthOfUnitsString(displayUnits);
    }

    @Override
    public void drawScalebar(Canvas canvas, float left, float top, float right, float bottom, double distance,
        LinearUnit displayUnits) {

      // Calculate the number of segments in the bar
      float barDisplayLength = right - left;
      int numSegments = calculateNumberOfSegments(distance, barDisplayLength);
      float segmentDisplayLength = barDisplayLength / numSegments;

      // Draw a solid bar, using mAlternateFillColor, and its shadow
      drawBarAndShadow(canvas, left, top, right, bottom, mAlternateFillColor);

      // Now draw every second segment on top of it using mFillColor
      mPaint.reset();
      mPaint.setStyle(Paint.Style.FILL);
      mPaint.setColor(mFillColor);
      float xPos = left + segmentDisplayLength;
      for (int i = 1; i < numSegments; i += 2) {
        mRectF.set(xPos, top, xPos + segmentDisplayLength, bottom);
        canvas.drawRect(mRectF, mPaint);
        xPos += (2 * segmentDisplayLength);
      }

      // Draw a line round the outside of the complete bar
      mRectF.set(left, top, right, bottom);
      mPaint.reset();
      mPaint.setColor(mLineColor);
      mPaint.setStyle(Paint.Style.STROKE);
      mPaint.setStrokeWidth(dpToPixels(mLineWidthDp));
      canvas.drawRoundRect(mRectF, dpToPixels(mCornerRadiusDp), dpToPixels(mCornerRadiusDp), mPaint);

      // Draw a label at the start of the bar
      float yPosText = bottom + dpToPixels(mTextSizeDp);
      mTextPaint.setTextAlign(Paint.Align.LEFT);
      canvas.drawText("0", left, yPosText, mTextPaint);

      // Draw a label at the end of the bar
      mTextPaint.setTextAlign(Paint.Align.RIGHT);
      canvas.drawText(ScalebarUtil.labelString(distance), right, yPosText, mTextPaint);
      mTextPaint.setTextAlign(Paint.Align.LEFT);
      canvas.drawText(' ' + displayUnits.getAbbreviation(), right, yPosText, mTextPaint);

      // Draw a vertical line and a label at each segment boundary
      xPos = left + segmentDisplayLength;
      double segmentDistance = distance / numSegments;
      mTextPaint.setTextAlign(Paint.Align.CENTER);
      for (int segNo = 1; segNo < numSegments; segNo++) {
        canvas.drawLine(xPos, top, xPos, bottom, mPaint);
        canvas.drawText(ScalebarUtil.labelString(segmentDistance * segNo), xPos, yPosText, mTextPaint);
        xPos += segmentDisplayLength;
      }
    }
  }

  /**
   * Renders a LINE style scalebar.
   *
   * @see Style#LINE
   * @since 100.1.0
   */
  private final class LineRenderer extends ScalebarRenderer {

    @Override
    public boolean isSegmented() {
      return false;
    }

    @Override
    public float calculateExtraSpaceForUnits(LinearUnit displayUnits) {
      return 0;
    }

    @Override
    public void drawScalebar(Canvas canvas, float left, float top, float right, float bottom, double distance,
        LinearUnit displayUnits) {

      // Draw the line and its shadow, including the ticks at each end
      drawLineAndShadow(canvas, left, top, right, bottom);

      // Draw the label, centered on the center of the line
      String label = ScalebarUtil.labelString(distance) + " " + displayUnits.getAbbreviation();
      mTextPaint.setTextAlign(Paint.Align.CENTER);
      canvas.drawText(label, left + ((right - left) / 2), bottom + dpToPixels(mTextSizeDp), mTextPaint);
    }
  }

  /**
   * Renders a GRADUATED_LINE style scalebar.
   *
   * @see Style#GRADUATED_LINE
   * @since 100.1.0
   */
  private final class GraduatedLineRenderer extends ScalebarRenderer {

    @Override
    public boolean isSegmented() {
      return true;
    }

    @Override
    public float calculateExtraSpaceForUnits(LinearUnit displayUnits) {
      return calculateWidthOfUnitsString(displayUnits);
    }

    @Override
    public void drawScalebar(Canvas canvas, float left, float top, float right, float bottom, double distance,
        LinearUnit displayUnits) {

      // Calculate the number of segments in the line
      float lineDisplayLength = right - left;
      int numSegments = calculateNumberOfSegments(distance, lineDisplayLength);
      float segmentDisplayLength = lineDisplayLength / numSegments;

      // Create Paint for drawing the ticks
      mPaint.reset();
      mPaint.setStyle(Paint.Style.STROKE);
      mPaint.setStrokeWidth(dpToPixels(mLineWidthDp));
      mPaint.setStrokeCap(Paint.Cap.ROUND);

      // Draw a tick, its shadow and a label at each segment boundary
      float xPos = left + segmentDisplayLength;
      float yPos = top + ((bottom - top) / 4); // segment ticks are 3/4 the height of the ticks at the start and end
      double segmentDistance = distance / numSegments;
      float yPosText = bottom + dpToPixels(mTextSizeDp);
      mTextPaint.setTextAlign(Paint.Align.CENTER);
      for (int segNo = 1; segNo < numSegments; segNo++) {
        // Draw the shadow, offset slightly from where the tick is drawn below
        mPaint.setColor(mShadowColor);
        canvas.drawLine(xPos + SHADOW_OFFSET_PIXELS, yPos + SHADOW_OFFSET_PIXELS,
            xPos + SHADOW_OFFSET_PIXELS, bottom + SHADOW_OFFSET_PIXELS, mPaint);

        // Draw the line on top of the shadow
        mPaint.setColor(mLineColor);
        canvas.drawLine(xPos, yPos, xPos, bottom, mPaint);

        // Draw the label
        canvas.drawText(ScalebarUtil.labelString(segmentDistance * segNo), xPos, yPosText, mTextPaint);
        xPos += segmentDisplayLength;
      }

      // Draw the line and its shadow, including the ticks at each end
      drawLineAndShadow(canvas, left, top, right, bottom);

      // Draw a label at the start of the line
      mTextPaint.setTextAlign(Paint.Align.LEFT);
      canvas.drawText("0", left, yPosText, mTextPaint);

      // Draw a label at the end of the line
      mTextPaint.setTextAlign(Paint.Align.RIGHT);
      canvas.drawText(ScalebarUtil.labelString(distance), right, yPosText, mTextPaint);
      mTextPaint.setTextAlign(Paint.Align.LEFT);
      canvas.drawText(' ' + displayUnits.getAbbreviation(), right, yPosText, mTextPaint);
    }
  }

  /**
   * Renders a DUAL_UNIT_LINE style scalebar.
   *
   * @see Style#DUAL_UNIT_LINE
   * @since 100.1.0
   */
  private final class DualUnitLineRenderer extends ScalebarRenderer {

    @Override
    public boolean isSegmented() {
      return false;
    }

    @Override
    public float calculateExtraSpaceForUnits(LinearUnit displayUnits) {
      return calculateWidthOfUnitsString(displayUnits);
    }

    @Override
    public void drawScalebar(Canvas canvas, float left, float top, float right, float bottom, double distance,
        LinearUnit displayUnits) {

      // Calculate scalebar length in the secondary units
      LinearUnit secondaryBaseUnits = mUnitSystem == UnitSystem.METRIC ? LINEAR_UNIT_FEET : LINEAR_UNIT_METERS;
      double fullLengthInSecondaryUnits = displayUnits.convertTo(secondaryBaseUnits, distance);

      // Reduce the secondary units length to make it a nice number
      double secondaryUnitsLength =
          ScalebarUtil.calculateBestScalebarLength(fullLengthInSecondaryUnits, secondaryBaseUnits, false);
      float lineDisplayLength = right - left;
      float xPosSecondaryTick = left + (float) (lineDisplayLength * secondaryUnitsLength / fullLengthInSecondaryUnits);

      // Change units if secondaryUnitsLength is too big a number in the base units
      UnitSystem secondaryUnitSystem = mUnitSystem == UnitSystem.METRIC ? UnitSystem.IMPERIAL : UnitSystem.METRIC;
      LinearUnit secondaryDisplayUnits = ScalebarUtil.selectLinearUnit(secondaryUnitsLength, secondaryUnitSystem);
      if (secondaryDisplayUnits != secondaryBaseUnits) {
        secondaryUnitsLength = secondaryBaseUnits.convertTo(secondaryDisplayUnits, secondaryUnitsLength);
      }

      // Create Paint for drawing the lines
      mPaint.reset();
      mPaint.setStyle(Paint.Style.STROKE);
      mPaint.setStrokeWidth(dpToPixels(mLineWidthDp));
      mPaint.setStrokeCap(Paint.Cap.ROUND);
      mPaint.setStrokeJoin(Paint.Join.ROUND);

      // Create a path to draw the line and the ticks
      float yPosLine = (top + bottom) / 2;
      mLinePath.reset();
      mLinePath.moveTo(left, top);
      mLinePath.lineTo(left, bottom); // draw big tick at left
      mLinePath.moveTo(xPosSecondaryTick, yPosLine); // move to top of secondary tick
      mLinePath.lineTo(xPosSecondaryTick, bottom); // draw secondary tick
      mLinePath.moveTo(left, yPosLine); // move to start of horizontal line
      mLinePath.lineTo(right, yPosLine); // draw the line
      mLinePath.lineTo(right, top); // draw tick at right
      mLinePath.setLastPoint(right, top);

      // Create a copy of the line path to be the path of its shadow, offset slightly from the line path
      Path shadowPath = new Path(mLinePath);
      shadowPath.offset(SHADOW_OFFSET_PIXELS, SHADOW_OFFSET_PIXELS);

      // Draw the shadow
      mPaint.setColor(mShadowColor);
      canvas.drawPath(shadowPath, mPaint);

      // Draw the line and the ticks
      mPaint.setColor(mLineColor);
      canvas.drawPath(mLinePath, mPaint);

      // Draw the primary units label above the tick at the right hand end
      float maxPixelsBelowBaseline = mTextPaint.getFontMetrics().bottom;
      float yPosText = top - maxPixelsBelowBaseline;
      mTextPaint.setTextAlign(Paint.Align.RIGHT);
      canvas.drawText(ScalebarUtil.labelString(distance), right, yPosText, mTextPaint);
      mTextPaint.setTextAlign(Paint.Align.LEFT);
      canvas.drawText(' ' + displayUnits.getAbbreviation(), right, yPosText, mTextPaint);

      // Draw the secondary units label below its tick
      yPosText = bottom + dpToPixels(mTextSizeDp);
      mTextPaint.setTextAlign(Paint.Align.RIGHT);
      canvas.drawText(ScalebarUtil.labelString(secondaryUnitsLength), xPosSecondaryTick, yPosText, mTextPaint);
      mTextPaint.setTextAlign(Paint.Align.LEFT);
      canvas.drawText(' ' + secondaryDisplayUnits.getAbbreviation(), xPosSecondaryTick, yPosText, mTextPaint);
    }
  }

}
