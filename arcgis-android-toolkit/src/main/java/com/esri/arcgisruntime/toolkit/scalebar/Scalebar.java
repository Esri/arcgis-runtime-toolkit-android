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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.esri.arcgisruntime.UnitSystem;
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.NavigationChangedEvent;
import com.esri.arcgisruntime.mapping.view.NavigationChangedListener;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedEvent;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedListener;

/**
 * Created by alan0001 on 05/07/2017.
 */

public class Scalebar extends View {

  private static final String TAG = "Scalebar";

  private static final int ALPHA_50_PC = 0x80000000;

  private static final int LABEL_X_PAD_DP = 6;

  private static final int SHADOW_OFFSET_PIXELS = 2;

  private static final ScalebarStyle DEFAULT_STYLE = ScalebarStyle.ALTERNATING_BAR;

  private static final ScalebarAlignment DEFAULT_ALIGNMENT = ScalebarAlignment.LEFT;

  private static final UnitSystem DEFAULT_UNIT_SYSTEM = UnitSystem.METRIC;//TODO: base the default on device locale

  private static final int DEFAULT_FILL_COLOR = Color.LTGRAY | ALPHA_50_PC;

  private static final int DEFAULT_ALTERNATE_FILL_COLOR = Color.BLACK;

  private static final int DEFAULT_LINE_COLOR = Color.WHITE;

  private static final int DEFAULT_SHADOW_COLOR = Color.BLACK | ALPHA_50_PC;

  private static final int DEFAULT_TEXT_COLOR = Color.BLACK;

  private static final int DEFAULT_TEXT_SHADOW_COLOR = Color.WHITE;

  private static final Typeface DEFAULT_TYPEFACE = Typeface.DEFAULT_BOLD;

  private static final int DEFAULT_TEXT_SIZE_DP = 15;

  private static final int DEFAULT_BAR_HEIGHT_DP = 10;

  private ScalebarAlignment mAlignment = DEFAULT_ALIGNMENT;

  private UnitSystem mUnitSystem = DEFAULT_UNIT_SYSTEM;

  private int mFillColor = DEFAULT_FILL_COLOR;

  private int mAlternateFillColor = DEFAULT_ALTERNATE_FILL_COLOR;

  private int mLineColor = DEFAULT_LINE_COLOR;

  private int mShadowColor = DEFAULT_SHADOW_COLOR;

  private int mTextColor = DEFAULT_TEXT_COLOR;

  private int mTextShadowColor = DEFAULT_TEXT_SHADOW_COLOR;

  private Typeface mTypeface = DEFAULT_TYPEFACE;

  private ScalebarRenderer mRenderer;

  private float mTextSizeDp = DEFAULT_TEXT_SIZE_DP;

  private float mBarHeightDp = DEFAULT_BAR_HEIGHT_DP;

  private float mLineWidthDp = mBarHeightDp / 4;

  private float mCornerRadiusDp = mBarHeightDp / 5;

  private int mPadXDp = 10;

  private int mPadYDp = 10;

  private MapView mMapView;

  private volatile int mAttributionTextHeight = 0;

  private float mDisplayDensity;

  private boolean mDrawInMapView = false;

  public Scalebar(Context context) {
    super(context);
    setStyle(DEFAULT_STYLE);
  }

  public Scalebar(Context context, AttributeSet attrs) {
    super(context, attrs);
    setStyle(getStyleFromAttributes(attrs));
    mAlignment = getAlignmentFromAttributes(attrs);
    mUnitSystem = getUnitSystemFromAttributes(attrs);
    mFillColor = getColorFromAttributes(context, attrs, "scalebar.fillColor", DEFAULT_FILL_COLOR);
    mAlternateFillColor = getColorFromAttributes(context, attrs, "scalebar.alternateFillColor", DEFAULT_ALTERNATE_FILL_COLOR);
    mLineColor = getColorFromAttributes(context, attrs, "scalebar.lineColor", DEFAULT_LINE_COLOR);
    mShadowColor = getColorFromAttributes(context, attrs, "scalebar.shadowColor", DEFAULT_SHADOW_COLOR);
    mTextColor = getColorFromAttributes(context, attrs, "scalebar.textColor", DEFAULT_TEXT_COLOR);
    mTextShadowColor = getColorFromAttributes(context, attrs, "scalebar.textShadowColor", DEFAULT_TEXT_SHADOW_COLOR);
    mTextSizeDp = attrs.getAttributeIntValue(null,"scalebar.textSize", DEFAULT_TEXT_SIZE_DP);
    mBarHeightDp = attrs.getAttributeIntValue(null,"scalebar.barHeight", DEFAULT_BAR_HEIGHT_DP);
  }

  public void addToMapView(MapView mapView) {
    setupMapView(mapView);
    mMapView.addView(this, new ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
        android.view.ViewGroup.LayoutParams.MATCH_PARENT));
    mDrawInMapView = true;
  }

  public void bindToMapView(MapView mapView) {
    setupMapView(mapView);
    mDrawInMapView = false;
//    mBarHeightDp = pixelsToDp(getHeight()) * 10 / 30; // beware can evaluate as 0 when changing layout
//    mTextSizeDp = pixelsToDp(getHeight()) * 15 / 30;
  }

  private void setupMapView(MapView mapView) {
    //TODO: remove listeners from old MapView?
    mMapView = mapView;
    mMapView.addViewpointChangedListener(new ViewpointChangedListener() {
      @Override
      public void viewpointChanged(ViewpointChangedEvent viewpointChangedEvent) {
        Log.d(TAG,"viewpointChanged");
        postInvalidate();
      }
    });
    mMapView.addNavigationChangedListener(new NavigationChangedListener() {
      @Override
      public void navigationChanged(NavigationChangedEvent navigationChangedEvent) {
        Log.d(TAG,"navigationChanged");
        postInvalidate();
      }
    });
    mMapView.addAttributionViewLayoutChangeListener(new OnLayoutChangeListener() {
      @Override
      public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop,
          int oldRight, int oldBottom) {
        mAttributionTextHeight = bottom - top;
        Log.d(TAG,"attributionViewLayoutChanged mAttributionTextHeight=" + mAttributionTextHeight);
        postInvalidate();
      }
    });
    mDisplayDensity = mMapView.getContext().getResources().getDisplayMetrics().density;
  }

  public void setFillColor(int color) {
    mFillColor = color;
    postInvalidate();
  }

  public void setAlternateFillColor(int color) {
    mAlternateFillColor = color;
    postInvalidate();
  }

  public void setLineColor(int color) {
    mLineColor = color;
    postInvalidate();
  }

  public void setShadowColor(int color) {
    mShadowColor = color;
    postInvalidate();
  }

  public void setTextColor(int color) {
    mTextColor = color;
    postInvalidate();
  }

  public void setTextShadowColor(int color) {
    mTextShadowColor = color;
    postInvalidate();
  }

  public void setTypeface(Typeface typeface) {
    mTypeface = typeface;
    postInvalidate();
  }

  public void setUnitSystem(UnitSystem unitSystem) {
    mUnitSystem = unitSystem;
    postInvalidate();
  }

  public void setAlignment(ScalebarAlignment alignment) {
    mAlignment = alignment;
    postInvalidate();
  }

  public void setStyle(ScalebarStyle style) {
    switch (style) {
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

  public void setTextSize(float textSizeDp) {
    mTextSizeDp = textSizeDp;
    postInvalidate();
  }

  public void setBarHeight(float barHeightDp) {
    mBarHeightDp = barHeightDp;
    mLineWidthDp = Math.max(mBarHeightDp / 4, 1);
    mCornerRadiusDp = Math.max(mBarHeightDp / 5, 1);
    postInvalidate();
  }

  public float getTextSize() {
    return mTextSizeDp;
  }

  public float getBarHeight() {
    return mBarHeightDp;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    Log.d(TAG, "onDraw");
    if (mMapView == null) {
      Log.d(TAG, "onDraw() bailing because MapView not set yet");
      return;
    }

    // Create Paint for drawing text right at the start, because it's used when sizing/positioning some things
    Paint textPaint = new Paint();
    textPaint.setColor(mTextColor);
    textPaint.setShadowLayer(2, SHADOW_OFFSET_PIXELS, SHADOW_OFFSET_PIXELS, mTextShadowColor);
    textPaint.setTypeface(mTypeface);
    textPaint.setTextSize(dpToPixels(mTextSizeDp));

    LinearUnit baseUnits = mUnitSystem == UnitSystem.IMPERIAL ?
        new LinearUnit(LinearUnitId.FEET) : new LinearUnit(LinearUnitId.METERS);
    float maxScaleBarWidthPixels;
    if (mDrawInMapView) {
      int mapViewWidth = mMapView.getWidth();
      maxScaleBarWidthPixels = mapViewWidth > mMapView.getHeight() ? mapViewWidth / 4 : mapViewWidth / 3;
    } else {
      maxScaleBarWidthPixels = getWidth() - widthOfUnitsString(null, textPaint);
    }

    // Calculate geodetic length of scalebar based on its maximum length on screen
    int centerX = (int) ((mMapView.getLeft() + mMapView.getRight()) / 2);
    int centerY = (int) ((mMapView.getTop() + mMapView.getBottom()) / 2);
    PolylineBuilder builder = new PolylineBuilder(mMapView.getSpatialReference());
    Point p1 = mMapView.screenToLocation(new android.graphics.Point((int)(centerX - maxScaleBarWidthPixels / 2), centerY));
    Point p2 = mMapView.screenToLocation(new android.graphics.Point((int)(centerX + maxScaleBarWidthPixels / 2), centerY));
    if (p1 == null || p2 == null) {
      return;
    }
    builder.addPoint(p1);
    builder.addPoint(p2);
    double maxLengthGeodetic = GeometryEngine.lengthGeodetic(builder.toGeometry(), baseUnits, GeodeticCurveType.GEODESIC);
    Log.d(TAG, "maxLengthGeodetic=" + maxLengthGeodetic);

    // Reduce length to make its geodetic length a nice number
    double scalebarLengthGeodetic =
        ScalebarUtil.calculateBestScalebarLength(maxLengthGeodetic, baseUnits, mRenderer.isSegmented());
    Log.d(TAG, "scalebarLengthGeodetic=" + scalebarLengthGeodetic);
    float scalebarLengthPixels = (float) (maxScaleBarWidthPixels * scalebarLengthGeodetic / maxLengthGeodetic);
    Log.d(TAG, "maxScaleBarWidthPixels=" + maxScaleBarWidthPixels);
    Log.d(TAG, "scalebarLengthPixels=" + scalebarLengthPixels);

    // Change units if the geodetic length is too big a number in the base units
    LinearUnit displayUnits = ScalebarUtil.selectLinearUnit(scalebarLengthGeodetic, mUnitSystem);
    if (displayUnits != baseUnits) {
      scalebarLengthGeodetic = baseUnits.convertTo(displayUnits, scalebarLengthGeodetic);
    }

    // Calculate screen coordinates of left, right, top and bottom of the scalebar
    float left = calculateLeftPos(mAlignment, scalebarLengthPixels, displayUnits, textPaint);
    float right = left + scalebarLengthPixels;
    float bottom;
    if (mDrawInMapView) {
      bottom = mMapView.getHeight() - mAttributionTextHeight - (float) mMapView.getViewInsetBottom() -
          dpToPixels(mPadYDp) - dpToPixels(mTextSizeDp);
    } else {
      bottom = getHeight() - dpToPixels(mTextSizeDp);
    }
    float top = bottom - dpToPixels(mBarHeightDp);

    // Draw the scalebar
    mRenderer.drawScalebar(canvas, left, right, top, bottom, scalebarLengthGeodetic, displayUnits, textPaint);
  }

  /**
   * Gets the scalebar style from an AttributeSet object, or a default value if it's not specified there.
   *
   * @param attrs the AttributeSet object containing parameters and values to use
   * @return the scalebar style
   * @since 100.1.0
   */
  private ScalebarStyle getStyleFromAttributes(AttributeSet attrs) {
    ScalebarStyle style = DEFAULT_STYLE;
    String str = attrs.getAttributeValue(null, "scalebar.style");
    if (str != null) {
      try {
        style = ScalebarStyle.valueOf(str);
      } catch (IllegalArgumentException e) {
        // allow it to use the default value set above
      }
    }
    return style;
  }

  /**
   * Gets the scalebar alignment from an AttributeSet object, or a default value if it's not specified there.
   *
   * @param attrs the AttributeSet object containing parameters and values to use
   * @return the scalebar alignment
   * @since 100.1.0
   */
  private ScalebarAlignment getAlignmentFromAttributes(AttributeSet attrs) {
    ScalebarAlignment alignment = DEFAULT_ALIGNMENT;
    String str = attrs.getAttributeValue(null, "scalebar.alignment");
    if (str != null) {
      try {
        alignment = ScalebarAlignment.valueOf(str);
      } catch (IllegalArgumentException e) {
        // allow it to use the default value set above
      }
    }
    return alignment;
  }

  /**
   * Gets the scalebar unit system from an AttributeSet object, or a default value if it's not specified there.
   *
   * @param attrs the AttributeSet object containing parameters and values to use
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
   * @param attrs         the AttributeSet object containing parameters and values to use
   * @param attributeName the name of the color attribute to get
   * @param defaultValue  the default value to use if attributeName not found in attrs
   * @return the color
   * @since 100.0.0
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
   * @param alignment the alignment of the scalebar
   * @param scalebarLength the length of the scalebar in pixels
   * @param displayUnits the units to be displayed
   * @param textPaint the Paint used to draw the text
   * @return the x-coordinate of the left hand end of the scalebar
   */
  private float calculateLeftPos(
      ScalebarAlignment alignment, float scalebarLength, LinearUnit displayUnits, Paint textPaint) {
    int left = 0;
    int right = getWidth();
    int padding = 0;
    if (mDrawInMapView) {
      left = mMapView.getLeft();
      right = mMapView.getRight();
      padding = dpToPixels(mPadXDp);
    }
    switch (alignment) {
      case LEFT:
      default:
        // Position start of scalebar at left hand edge of the view, plus padding (if any)
        return left + padding;
      case RIGHT:
        // Position end of scalebar at right hand edge of the view, less padding and the width of the units string (if
        // required)
        return right - padding - dpToPixels(mLineWidthDp) - scalebarLength -
            mRenderer.calculateExtraSpaceForUnitsWhenRightAligned(displayUnits, textPaint);
      case CENTER:
        // position center of scalebar at center of the view
        return (right + left - scalebarLength) / 2;
    }
  }

  private float widthOfUnitsString(LinearUnit displayUnits, Paint textPaint) {
    Rect unitsBounds = new Rect();
    String unitsText = ' ' + (displayUnits == null ? "mm" : displayUnits.getAbbreviation());
    textPaint.getTextBounds(unitsText, 0, unitsText.length(), unitsBounds);
    return unitsBounds.right;
  }

  /**
   * Converts density-independent pixels to actual pixels.
   *
   * @param dp a number of density-independent pixels
   * @return the equivalent number of actual pixels
   */
  private int dpToPixels(double dp) {
    double pixels = dp * mDisplayDensity;
    return (int) (pixels + Integer.signum((int) pixels) * 0.5);
  }

  //TODO: do we need this?
  private int pixelsToDp(double pixels) {
    double dp = pixels / mDisplayDensity;
    return (int) (dp + Integer.signum((int) dp) * 0.5);
  }

  public enum ScalebarStyle {
    BAR,
    ALTERNATING_BAR,
    LINE,
    GRADUATED_LINE,
    DUAL_UNIT_LINE;
  }

  public enum ScalebarAlignment {
    LEFT,
    RIGHT,
    CENTER
  }

  private abstract class ScalebarRenderer {

    public abstract void drawScalebar(Canvas canvas, float left, float right, float top, float bottom,
        double geodeticLength, LinearUnit displayUnits, Paint textPaint);

    // Subclasses need to override this if their scalebar is segmented
    public boolean isSegmented() {
      return false;
    }

    // Subclasses need to override this if they write the units to the right of the end of the scalebar
    public float calculateExtraSpaceForUnitsWhenRightAligned(LinearUnit displayUnits, Paint textPaint) {
      return 0;
    }

    /**
     * Draw a solid bar and its shadow. Used by BarRenderer and AlternatingBarRenderer.
     *
     * @param canvas
     * @param left
     * @param right
     * @param top
     * @param bottom
     * @param barColor
     */
    protected void drawBarAndShadow(Canvas canvas, float left, float right, float top, float bottom, int barColor) {
      // Draw the shadow of the bar, offset slightly from where the actual bar is drawn below
      RectF shadowRect = new RectF(left, top, right, bottom);
      int offset = SHADOW_OFFSET_PIXELS + (dpToPixels(mLineWidthDp) / 2);
      shadowRect.offset(offset, offset);
      Paint paint = new Paint();
      paint.setColor(mShadowColor);
      paint.setStyle(Paint.Style.FILL);
      canvas.drawRoundRect(shadowRect, dpToPixels(mCornerRadiusDp), dpToPixels(mCornerRadiusDp), paint);

      // Now draw the bar
      RectF barRect = new RectF(left, top, right, bottom);
      paint.setColor(barColor);
      canvas.drawRoundRect(barRect, dpToPixels(mCornerRadiusDp), dpToPixels(mCornerRadiusDp), paint);
    }

    /**
     * Draw a line and its shadow, including the ticks at each end. Used by LineRenderer and GraduatedLineRenderer.
     *
     * @param canvas
     * @param left
     * @param right
     * @param top
     * @param bottom
     */
    protected void drawLineAndShadow(Canvas canvas, float left, float right, float top, float bottom) {
      // Create a path to draw the left-hand tick, the line itself and the right-hand tick
      Path linePath = new Path();
      linePath.moveTo(left, top);
      linePath.lineTo(left, bottom);
      linePath.lineTo(right, bottom);
      linePath.lineTo(right, top);
      linePath.setLastPoint(right, top);

      // Create a copy to be the path of the shadow, offset slightly from the path of the line
      Path shadowPath = new Path(linePath);
      shadowPath.offset(SHADOW_OFFSET_PIXELS, SHADOW_OFFSET_PIXELS);

      // Draw the shadow
      Paint paint = new Paint();
      paint.setColor(mShadowColor);
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(dpToPixels(mLineWidthDp));
      paint.setStrokeCap(Paint.Cap.ROUND);
      paint.setStrokeJoin(Paint.Join.ROUND);
      canvas.drawPath(shadowPath, paint);

      // Now draw the line on top of the shadow
      paint.setColor(mLineColor);
      canvas.drawPath(linePath, paint);
    }

    // Used by GRADUATED_LINE and ALTERNATING_BAR
    //TODO: this algorithm is copied from the iOS implementation; it's a bit crude but let's stick with it unless I find
    // a test case it doesn't work for
    public int calculateNumberOfSegments(double geodeticLength, double lineDisplayLength, Paint textPaint) {
      // use a string with at least a few characters in case the number string only has 1
      // the dividers will be decimal values and we want to make sure they all fit
      // very basic heuristics...
      String testString = ScalebarUtil.labelString(geodeticLength);
      if (testString.length() < 3) {
        testString = "9.9";
      }
      Rect bounds = new Rect();
      textPaint.getTextBounds(testString, 0, testString.length(), bounds);
      // use 1.5 because the last segment, the text is right justified insted of center, which makes it harder to squeeze text in
      double minSegmentWidth = bounds.right * 1.5 + dpToPixels(LABEL_X_PAD_DP);
      int maxNumSegments = (int) (lineDisplayLength / minSegmentWidth);
      maxNumSegments = Math.min(maxNumSegments, 4); // cap it at 4 TODO: no point using 5 in segmentOptionsForMultiplier() then
      return ScalebarUtil.calculateOptimalNumberOfSegments(geodeticLength, maxNumSegments);
    }

  }

  private final class BarRenderer extends ScalebarRenderer {

    public void drawScalebar(Canvas canvas, float left, float right, float top, float bottom, double geodeticLength,
        LinearUnit displayUnits, Paint textPaint) {

      // Draw a solid bar and its shadow
      drawBarAndShadow(canvas, left, right, top, bottom, mFillColor);

      // Draw a line round the outside
      RectF barRect = new RectF(left, top, right, bottom);
      Paint paint = new Paint();
      paint.setColor(mLineColor);
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(dpToPixels(mLineWidthDp));
      canvas.drawRoundRect(barRect, dpToPixels(mCornerRadiusDp), dpToPixels(mCornerRadiusDp), paint);

      // Draw the label, centered on the center of the bar
      String label = ScalebarUtil.labelString(geodeticLength) + " " + displayUnits.getAbbreviation();
      textPaint.setTextAlign(Paint.Align.CENTER);
      canvas.drawText(label, left + ((right - left) / 2), bottom + dpToPixels(mTextSizeDp), textPaint);
    }
  }

  private final class AlternatingBarRenderer extends ScalebarRenderer {

    @Override
    public boolean isSegmented() {
      return true;
    }

    @Override
    public float calculateExtraSpaceForUnitsWhenRightAligned(LinearUnit displayUnits, Paint textPaint) {
      return widthOfUnitsString(displayUnits, textPaint);
    }

    public void drawScalebar(Canvas canvas, float left, float right, float top, float bottom, double geodeticLength,
        LinearUnit displayUnits, Paint textPaint) {

      // Calculate the number of segments in the bar
      float barDisplayLength = right - left;
      int numSegments = calculateNumberOfSegments(geodeticLength, barDisplayLength, textPaint);
      float segmentDisplayLength = barDisplayLength / numSegments;

      // Draw a solid bar, using mAlternateFillColor, and its shadow
      drawBarAndShadow(canvas, left, right, top, bottom, mAlternateFillColor);

      // Now draw every second segment on top of it using mFillColor
      Paint paint = new Paint();
      paint.setStyle(Paint.Style.FILL);
      paint.setColor(mFillColor);
      float xPos = left + segmentDisplayLength;
      for (int i = 1; i < numSegments; i += 2) { //TODO: i is redundant
        RectF segRect = new RectF(xPos, top, xPos + segmentDisplayLength, bottom);
        canvas.drawRect(segRect, paint);
        xPos += (2 * segmentDisplayLength);
      }

      // Draw a line round the outside of the complete bar
      RectF barRect = new RectF(left, top, right, bottom);
      paint = new Paint();
      paint.setColor(mLineColor);
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(dpToPixels(mLineWidthDp));
      canvas.drawRoundRect(barRect, dpToPixels(mCornerRadiusDp), dpToPixels(mCornerRadiusDp), paint);

      // Draw a label at the start of the bar
      float yPosText = bottom + dpToPixels(mTextSizeDp);
      textPaint.setTextAlign(Paint.Align.LEFT);
      canvas.drawText("0", left, yPosText, textPaint);

      // Draw a label at the end of the bar
      textPaint.setTextAlign(Paint.Align.RIGHT);
      canvas.drawText(ScalebarUtil.labelString(geodeticLength), left + barDisplayLength, yPosText, textPaint);
      textPaint.setTextAlign(Paint.Align.LEFT);
      canvas.drawText(' ' + displayUnits.getAbbreviation(), left + barDisplayLength, yPosText, textPaint);

      // Draw a vertical line and a label at each segment boundary
      xPos = left + segmentDisplayLength;
      double segmentGeodeticLength = geodeticLength / numSegments;
      textPaint.setTextAlign(Paint.Align.CENTER);
      for (int segNo = 1; segNo < numSegments; segNo++) {
        canvas.drawLine(xPos, top, xPos, bottom, paint);
        canvas.drawText(ScalebarUtil.labelString(segmentGeodeticLength * segNo), xPos, yPosText, textPaint);
        xPos += segmentDisplayLength;
      }
    }

  }

  private final class LineRenderer extends ScalebarRenderer {

    public void drawScalebar(Canvas canvas, float left, float right, float top, float bottom, double geodeticLength,
        LinearUnit displayUnits, Paint textPaint) {

      // Draw the line and its shadow, including the ticks at each end
      drawLineAndShadow(canvas, left, right, top, bottom);

      // Draw the label, centered on the center of the line
      String label = ScalebarUtil.labelString(geodeticLength) + " " + displayUnits.getAbbreviation();
      textPaint.setTextAlign(Paint.Align.CENTER);
      canvas.drawText(label, left + ((right - left) / 2), bottom + dpToPixels(mTextSizeDp), textPaint);
    }
  }

  private final class GraduatedLineRenderer extends ScalebarRenderer {

    @Override
    public boolean isSegmented() {
      return true;
    }

    @Override
    public float calculateExtraSpaceForUnitsWhenRightAligned(LinearUnit displayUnits, Paint textPaint) {
      return widthOfUnitsString(displayUnits, textPaint);
    }

    public void drawScalebar(Canvas canvas, float left, float right, float top, float bottom, double geodeticLength,
        LinearUnit displayUnits, Paint textPaint) {

      // Calculate the number of segments in the line
      float barDisplayLength = right - left;
      int numSegments = calculateNumberOfSegments(geodeticLength, barDisplayLength, textPaint);
      float segmentDisplayLength = barDisplayLength / numSegments;

      // Create Paint for drawing the ticks
      Paint tickPaint = new Paint();
      tickPaint.setStyle(Paint.Style.STROKE);
      tickPaint.setStrokeWidth(dpToPixels(mLineWidthDp));
      tickPaint.setStrokeCap(Paint.Cap.ROUND);

      // Draw a tick, its shadow and a label at each segment boundary
      float xPos = left + segmentDisplayLength;
      float yPos = top + ((bottom - top) / 4); // segment ticks are 3/4 the height of the ticks at the start and end
      double segmentGeodeticLength = geodeticLength / numSegments;
      float yPosText = bottom + dpToPixels(mTextSizeDp);
      textPaint.setTextAlign(Paint.Align.CENTER);
      for (int segNo = 1; segNo < numSegments; segNo++) {
        // Draw the shadow, offset slightly from where the tick is drawn below
        tickPaint.setColor(mShadowColor);
        canvas.drawLine(xPos + SHADOW_OFFSET_PIXELS, yPos + SHADOW_OFFSET_PIXELS,
            xPos + SHADOW_OFFSET_PIXELS, bottom + SHADOW_OFFSET_PIXELS, tickPaint);

        // Draw the line on top of the shadow
        tickPaint.setColor(mLineColor);
        canvas.drawLine(xPos, yPos, xPos, bottom, tickPaint);

        // Draw the label
        canvas.drawText(ScalebarUtil.labelString(segmentGeodeticLength * segNo), xPos, yPosText, textPaint);
        xPos += segmentDisplayLength;
      }

      // Draw the line and its shadow, including the ticks at each end
      drawLineAndShadow(canvas, left, right, top, bottom);

      // Draw a label at the start of the line
      textPaint.setTextAlign(Paint.Align.LEFT);
      canvas.drawText("0", left, yPosText, textPaint);

      // Draw a label at the end of the line
      textPaint.setTextAlign(Paint.Align.RIGHT);
      canvas.drawText(ScalebarUtil.labelString(geodeticLength), left + barDisplayLength, yPosText, textPaint);
      textPaint.setTextAlign(Paint.Align.LEFT);
      canvas.drawText(' ' + displayUnits.getAbbreviation(), left + barDisplayLength, yPosText, textPaint);
    }
  }

  private final class DualUnitLineRenderer extends ScalebarRenderer {

    @Override
    public float calculateExtraSpaceForUnitsWhenRightAligned(LinearUnit displayUnits, Paint textPaint) {
      return widthOfUnitsString(displayUnits, textPaint);
    }

    public void drawScalebar(Canvas canvas, float left, float right, float top, float bottom, double geodeticLength,
        LinearUnit displayUnits, Paint textPaint) {
      Log.d(TAG, "DualUnitLineRenderer.drawScalebar() left=" + left);
      Log.d(TAG, "geodeticLength=" + geodeticLength);

      // Calculate scalebar length in the secondary units
      LinearUnit secondaryBaseUnits = mUnitSystem == UnitSystem.IMPERIAL ?
          new LinearUnit(LinearUnitId.METERS) : new LinearUnit(LinearUnitId.FEET);
      double fullLengthInSecondaryUnits = displayUnits.convertTo(secondaryBaseUnits, geodeticLength);
      Log.d(TAG, "fullLengthInSecondaryUnits=" + fullLengthInSecondaryUnits);

      // Reduce the secondary units length to make it a nice number
      double secondaryUnitsLength =
          ScalebarUtil.calculateBestScalebarLength(fullLengthInSecondaryUnits, secondaryBaseUnits, false);
      Log.d(TAG, "secondaryUnitsLength=" + secondaryUnitsLength);
      float barDisplayLength = right - left;
      float xPosSecondaryTick = left + (float) (barDisplayLength * secondaryUnitsLength / fullLengthInSecondaryUnits);

      // Change units if secondaryUnitsLength is too big a number in the base units
      UnitSystem secondaryUnitSystem = mUnitSystem == UnitSystem.IMPERIAL ? UnitSystem.METRIC : UnitSystem.IMPERIAL;
      LinearUnit secondaryDisplayUnits = ScalebarUtil.selectLinearUnit(secondaryUnitsLength, secondaryUnitSystem);
      if (secondaryDisplayUnits != secondaryBaseUnits) {
        secondaryUnitsLength = secondaryBaseUnits.convertTo(secondaryDisplayUnits, secondaryUnitsLength);
        Log.d(TAG, "secondaryUnitsLength=" + secondaryUnitsLength);
      }

      // Create Paint for drawing the lines
      Paint paint = new Paint();
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(dpToPixels(mLineWidthDp));
      paint.setStrokeCap(Paint.Cap.ROUND);
      paint.setStrokeJoin(Paint.Join.ROUND);

      // Create a path to draw the line and the ticks
      float yPosLine = (top + bottom) / 2;
      Path linePath = new Path();
      linePath.moveTo(left, top);
      linePath.lineTo(left, bottom); // draw big tick at left
      linePath.moveTo(xPosSecondaryTick, yPosLine); // move to top of secondary tick
      linePath.lineTo(xPosSecondaryTick, bottom); // draw secondary tick
      linePath.moveTo(left, yPosLine); // move to start of horizontal line
      linePath.lineTo(right, yPosLine); // draw the line
      linePath.lineTo(right, top); // draw tick at right
      linePath.setLastPoint(right, top);

      // Create a copy of the line path to be the path of its shadow, offset slightly from the line path
      Path shadowPath = new Path(linePath);
      shadowPath.offset(SHADOW_OFFSET_PIXELS, SHADOW_OFFSET_PIXELS);

      // Draw the shadow
      paint.setColor(mShadowColor);
      canvas.drawPath(shadowPath, paint);

      // Draw the line and the ticks
      paint.setColor(mLineColor);
      canvas.drawPath(linePath, paint);

      // Draw the primary units label above the tick at the right hand end
      float yPosText = top;
      textPaint.setTextAlign(Paint.Align.RIGHT);
      canvas.drawText(ScalebarUtil.labelString(geodeticLength), left + barDisplayLength, yPosText, textPaint);
      textPaint.setTextAlign(Paint.Align.LEFT);
      canvas.drawText(' ' + displayUnits.getAbbreviation(), left + barDisplayLength, yPosText, textPaint);

      // Draw the secondary units label below its tick
      yPosText = bottom + dpToPixels(mTextSizeDp);
      textPaint.setTextAlign(Paint.Align.RIGHT);
      canvas.drawText(ScalebarUtil.labelString(secondaryUnitsLength), xPosSecondaryTick, yPosText, textPaint);
      textPaint.setTextAlign(Paint.Align.LEFT);
      canvas.drawText(' ' + secondaryDisplayUnits.getAbbreviation(), xPosSecondaryTick, yPosText, textPaint);
    }
  }

}
