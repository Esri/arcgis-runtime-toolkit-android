package com.esri.arcgisruntime.toolkit.scalebar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

  private static final int LABEL_X_PAD = 4;

  private static final int DEFAULT_FILL_COLOR = Color.LTGRAY | ALPHA_50_PC;

  private static final int DEFAULT_ALTERNATE_FILL_COLOR = Color.BLACK;

  private static final int DEFAULT_LINE_COLOR = Color.WHITE;

  private static final int DEFAULT_SHADOW_COLOR = Color.BLACK;// | ALPHA_50_PC;

  private static final int DEFAULT_TEXT_COLOR = Color.BLACK;

  private static final int DEFAULT_TEXT_SHADOW_COLOR = Color.WHITE;

  private static final Typeface DEFAULT_TYPEFACE = Typeface.DEFAULT_BOLD;

  private static final ScalebarUnits DEFAULT_UNITS = ScalebarUnits.METRIC;

  private static final ScalebarAlignment DEFAULT_ALIGNMENT = ScalebarAlignment.LEFT;

  private static final ScalebarStyle DEFAULT_STYLE = ScalebarStyle.BAR;

  private int mFillColor = DEFAULT_FILL_COLOR;

  private int mAlternateFillColor = DEFAULT_ALTERNATE_FILL_COLOR;

  private int mLineColor = DEFAULT_LINE_COLOR;

  private int mShadowColor = DEFAULT_SHADOW_COLOR; //TODO: do we need a shadow on the bar???

  private int mTextColor = DEFAULT_TEXT_COLOR;

  private int mTextShadowColor = DEFAULT_TEXT_SHADOW_COLOR;

  private Typeface mTypeface = DEFAULT_TYPEFACE;

  private ScalebarUnits mUnits = DEFAULT_UNITS;

  private ScalebarAlignment mAlignment = DEFAULT_ALIGNMENT;

  private ScalebarRenderer mRenderer;

  private int mBarHeight = 10;

  private float mLineWidth = mBarHeight / 4;

  private int mTextSize = 15;

  private float mCornerRadius = mBarHeight / 5;

  private int mPadX = 25;

  private int mPadY = 25;

  private MapView mMapView;

  private int mAttributionTextHeight = 0;

  private float mDisplayDensity;

  private boolean mDrawInMapView = false;

  public Scalebar(Context context) {
    super(context);
    setStyle(DEFAULT_STYLE);
  }

  public Scalebar(Context context, AttributeSet attrs) {
    super(context, attrs);
    setStyle(DEFAULT_STYLE);
    mFillColor = getColor(context, attrs, "scalebar.fillColor", DEFAULT_FILL_COLOR);
    mAlternateFillColor = getColor(context, attrs, "scalebar.alternateFillColor", DEFAULT_ALTERNATE_FILL_COLOR);
    mLineColor = getColor(context, attrs, "scalebar.lineColor", DEFAULT_LINE_COLOR);
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
  private int getColor(Context context, AttributeSet attrs, String attributeName, int defaultValue) {
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


  public void addToMapView(MapView mapView) {
    attachToMapView(mapView);
    mMapView.addView(this, new ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
        android.view.ViewGroup.LayoutParams.MATCH_PARENT));
    mDrawInMapView = true;
  }

  private void attachToMapView(MapView mapView) {
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
        invalidate();
      }
    });
    mMapView.addAttributionViewLayoutChangeListener(new OnLayoutChangeListener() {
      @Override
      public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop,
          int oldRight, int oldBottom) {
        mAttributionTextHeight = bottom - top;
      }
    });
    mDisplayDensity = mapView.getContext().getResources().getDisplayMetrics().density;
  }

  public void connectWithMapView(MapView mapView) {
    attachToMapView(mapView);
    mBarHeight = pixelsToDp(getHeight()) * 10 / 30;
    mTextSize = pixelsToDp(getHeight()) * 15 / 30;
  }

  public void setFillColor(int color) {
    mFillColor = color;
  }

  public void setAlternateFillColor(int color) {
    mAlternateFillColor = color;
  }

  public void setLineColor(int color) {
    mLineColor = color;
  }

  public void setTextColor(int color) {
    mTextColor = color;
  }

  public void setTextShadowColor(int color) {
    mTextShadowColor = color;
  }

  //TODO: consider alternative of letting the Paint used for text be set???
  public void setTypeface(Typeface typeface) {
    mTypeface = typeface;
  }

  public void setUnits(ScalebarUnits units) {
    mUnits = units;
  }

  public void setAlignment(ScalebarAlignment alignment) {
    mAlignment = alignment;
  }

  public void setStyle(ScalebarStyle style) {
    switch (style) {
      case BAR:
        mRenderer = new BarRenderer(this);
        break;
      case ALTERNATING_BAR:
        mRenderer = new AlternatingBarRenderer(this);
        break;
    }
  }

  public void setTextSize(int textSizeDp) {
    mTextSize = textSizeDp;
  }

  public void setBarHeight(int barHeightDp) {
    mBarHeight = barHeightDp;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    Log.d(TAG, "onDraw");
    if (mMapView == null) {
      Log.d(TAG, "onDraw() bailing because MapView not set yet");
      return;
    }

    float maxScaleBarWidthPixels;
    if (mDrawInMapView) {
      int mapViewWidth = mMapView.getWidth();
      maxScaleBarWidthPixels = mapViewWidth > mMapView.getHeight() ? mapViewWidth / 4 : mapViewWidth / 3;
    } else {
      maxScaleBarWidthPixels = getWidth();
    }

    // Calculate position of scalebar based on its maximum width
    int centerX = (int) ((mMapView.getLeft() + mMapView.getRight()) / 2);
    int centerY = (int) ((mMapView.getTop() + mMapView.getBottom()) / 2);

    // Calculate geodetic length of scalebar
    PolylineBuilder builder = new PolylineBuilder(mMapView.getSpatialReference());
    Point p1 = mMapView.screenToLocation(new android.graphics.Point((int)(centerX - maxScaleBarWidthPixels / 2), centerY));
    Point p2 = mMapView.screenToLocation(new android.graphics.Point((int)(centerX + maxScaleBarWidthPixels / 2), centerY));
    if (p1 == null || p2 == null) {
      return;
    }
    builder.addPoint(p1);
    builder.addPoint(p2);
    LinearUnit baseUnits = mUnits.baseUnits();
    double geodeticLength = GeometryEngine.lengthGeodetic(builder.toGeometry(), baseUnits, GeodeticCurveType.GEODESIC);
    Log.d(TAG, "geodeticLength=" + geodeticLength);

    // Reduce width to make its geodetic length a nice number
    double scalebarWidthGeodetic = mUnits.closestDistanceWithoutGoingOver(geodeticLength, baseUnits);
    Log.d(TAG, "scalebarWidthGeodetic=" + scalebarWidthGeodetic);
    float scalebarWidthPixels = (float) (maxScaleBarWidthPixels * scalebarWidthGeodetic / geodeticLength);

    // Change units if the geodetic width is too big a number in the base units
    LinearUnit displayUnits = mUnits.linearUnitsForDistance(scalebarWidthGeodetic);
    if (displayUnits != baseUnits) {
      scalebarWidthGeodetic = baseUnits.convertTo(displayUnits, scalebarWidthGeodetic);
    }

    float left;
    float right;
    float bottom;
    float top;
    if (mDrawInMapView) {
      left = calculateLeftPos(mAlignment, scalebarWidthPixels);
      bottom =
          mMapView.getBottom() - mAttributionTextHeight - (float) mMapView.getViewInsetBottom() - dpToPixels(mPadY);
    } else {
      left = 0;
      bottom = getHeight() - dpToPixels(mPadY);
    }
    right = left + scalebarWidthPixels;
    top = bottom - dpToPixels(mBarHeight);

    // Draw the scalebar
    mRenderer.drawScalebar(canvas, left, right, top, bottom, scalebarWidthGeodetic, displayUnits);
  }

  private float calculateLeftPos(ScalebarAlignment alignment, float width) {
    switch (alignment) {
      case LEFT:
      default:
        return mMapView.getLeft() + dpToPixels(mPadX);
      case RIGHT:
        return mMapView.getRight() - dpToPixels(mPadX) - width;
      case CENTER:
        return (mMapView.getRight() + mMapView.getLeft() - width) / 2;
    }
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
  private int pixelsToDp(double pixels) {
    double dp = pixels / mDisplayDensity;
    return (int) (dp + Integer.signum((int) dp) * 0.5);
  }

  public enum ScalebarUnits {
    IMPERIAL,
    METRIC;

    private static double[] roundNumberMultipliers = {1, 1.2, 1.25, 1.5, 1.75, 2, 2.4, 2.5, 3, 3.75, 4, 5, 6, 7.5, 8, 9, 10};

    public LinearUnit baseUnits() {
      if (this == IMPERIAL) {
        return new LinearUnit(LinearUnitId.FEET);
      }
      return new LinearUnit(LinearUnitId.METERS);
    }

    private double calculateMagnitude(double distance) {
      return Math.pow(10, Math.floor(Math.log10(distance)));
    }

    private double calculateMultiplier(double distance, double magnitude) {
      double residual = distance / magnitude;
      double multiplier = roundNumberMultipliers[roundNumberMultipliers.length - 1];
//      // This gives us the LAST one that's <= residual
//      for (int i=0; i < roundNumberMultipliers.length; i++) {
//        if (roundNumberMultipliers[i] <= residual) {
//          multiplier = roundNumberMultipliers[i];//TODO: what if none <= residual?
//        }
//      }
      // This gives us the FIRST one that's > residual
      for (int i=0; i < roundNumberMultipliers.length; i++) {
        if (roundNumberMultipliers[i] > residual) {
          multiplier = roundNumberMultipliers[i - 1];//TODO: need to check for i = 0 here?
          break;
        }
      }
      return multiplier;
    }

    public double closestDistanceWithoutGoingOver(double distance, LinearUnit units) {
      double magnitude = calculateMagnitude(distance);
      double multiplier = calculateMultiplier(distance, magnitude);
      double roundNumber = multiplier * magnitude;

      // because feet and miles are not relationally multiples of 10 with each other,
      // we have to convert to miles if we are dealing in miles
      if (units.getLinearUnitId() == LinearUnitId.FEET) {
        LinearUnit displayUnits = linearUnitsForDistance(roundNumber);
        if (units.getLinearUnitId() != displayUnits.getLinearUnitId()) {
          double displayDistance = closestDistanceWithoutGoingOver(units.convertTo(displayUnits, distance), displayUnits);
          return displayUnits.convertTo(units, displayDistance);
        }
      }
      return roundNumber;
    }

    private int[] segmentOptionsForMultiplier(double multiplier) {
      //TODO: must be a better way!
      if (multiplier <= 1) {
        return new int[] {1, 2, 4, 5};
      }
      if (multiplier <= 1.2) {
        return new int[] {1, 2, 3, 4};
      }
      if (multiplier <= 1.25) {
        return new int[] {1, 2};
      }
      if (multiplier <= 1.5) {
        return new int[] {1, 2, 3, 5};
      }
      if (multiplier <= 1.75) {
        return new int[] {1, 2};
      }
      if (multiplier <= 2) {
        return new int[] {1, 2, 4, 5};
      }
      if (multiplier <= 2.4) {
        return new int[] {1, 2, 3};
      }
      if (multiplier <= 2.5) {
        return new int[] {1, 2, 5};
      }
      if (multiplier <= 3) {
        return new int[] {1, 2, 3};
      }
      if (multiplier <= 3.75) {
        return new int[] {1, 3};
      }
      if (multiplier <= 4) {
        return new int[] {1, 2, 4};
      }
      if (multiplier <= 5) {
        return new int[] {1, 2, 5};
      }
      if (multiplier <= 6) {
        return new int[] {1, 2, 3};
      }
      if (multiplier <= 7.5) {
        return new int[] {1, 2};
      }
      if (multiplier <= 8) {
        return new int[] {1, 2, 4};
      }
      if (multiplier <= 9) {
        return new int[] {1, 2, 3};
      }
      if (multiplier <= 10) {
        return new int[] {1, 2, 5};
      }
      return new int[] {1};
    }

    public int numSegmentsForDistance(double distance, int maxNumSegments) {
      double magnitude = calculateMagnitude(distance);
      double multiplier = calculateMultiplier(distance, magnitude);
      int[] options = segmentOptionsForMultiplier(multiplier);

      // This gives us the LAST one that's <= maxNumSegments
      int num = 1;
      for (int i=0; i < options.length; i++) {
        if (options[i] <= maxNumSegments) {
          num = options[i];
        }
      }
      return num;
    }

    private LinearUnit linearUnitsForDistance(double distance) {

      switch (this) {
        case IMPERIAL:
          if (distance >= 2640) {
            return new LinearUnit(LinearUnitId.MILES);
          }
          return new LinearUnit(LinearUnitId.FEET);

        case METRIC:
        default:
          if (distance >= 1000) {
            return new LinearUnit(LinearUnitId.KILOMETERS);
          }
          return new LinearUnit(LinearUnitId.METERS);
      }
    }

  }

  public enum ScalebarStyle {
    LINE,
    GRADUATED_LINE,
    DUAL_UNIT_LINE,
    BAR,
    ALTERNATING_BAR;
  }

  public enum ScalebarAlignment {
    LEFT,
    RIGHT,
    CENTER
  }

  private abstract class ScalebarRenderer {
    private final Scalebar mScalebar;//TODO: do we need this???

    public ScalebarRenderer(Scalebar scalebar) {
      mScalebar = scalebar;
    }

    public abstract void drawScalebar(Canvas canvas, float left, float right, float top, float bottom,
        double geodeticLength, LinearUnit displayUnits);

    public int calculateNumberOfSegments(double geodeticLength, double lineDisplayLength, Paint textPaint) {
      // use a string with at least a few characters in case the number string only has 1
      // the dividers will be decimal values and we want to make sure they all fit
      // very basic heuristics...
      String testString = String.valueOf(geodeticLength);
      if (testString.length() < 3) {
        testString = "9.9";
      }
      Rect bounds = new Rect();
      textPaint.getTextBounds(testString, 0, testString.length(), bounds);
      // use 1.5 because the last segment, the text is right justified insted of center, which makes it harder to squeeze text in
      double minSegmentWidth = bounds.right * 1.5 + LABEL_X_PAD * 2;
      int maxNumSegments = (int) (lineDisplayLength / minSegmentWidth);
      maxNumSegments = Math.min(maxNumSegments, 4); // cap it at 4
      return mUnits.numSegmentsForDistance(geodeticLength, maxNumSegments);
    }

  }

  private final class BarRenderer extends ScalebarRenderer {

    public BarRenderer(Scalebar scalebar) {
      super(scalebar);
    }

    public void drawScalebar(Canvas canvas, float left, float right, float top, float bottom, double geodeticLength,
        LinearUnit displayUnits) {
      //TODO: advisable to access Scalebar member fields from here?

      // Draw a solid bar
      RectF rect = new RectF(left, top, right, bottom);
      Paint paint = new Paint();
      paint.setColor(mFillColor);
      paint.setStyle(Paint.Style.FILL);
      canvas.drawRoundRect(rect, dpToPixels(mCornerRadius), dpToPixels(mCornerRadius), paint);

      // Draw line round the outside
      paint = new Paint();
      paint.setColor(mLineColor);
      //paint.setShadowLayer(5, 5, 5, mShadowColor); //seems to only work on text
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(dpToPixels(mLineWidth));
      canvas.drawRoundRect(rect, dpToPixels(mCornerRadius), dpToPixels(mCornerRadius), paint);

      // Draw text
      paint = new Paint();
      paint.setColor(mTextColor);
      paint.setShadowLayer(2, 2, 2, mTextShadowColor);
      paint.setTypeface(mTypeface);
      paint.setTextSize(dpToPixels(mTextSize));
      paint.setTextAlign(Paint.Align.CENTER);
      canvas.drawText(geodeticLength + " m", left + ((right - left) / 2), bottom + dpToPixels(mTextSize), paint);
    }
  }

  private final class AlternatingBarRenderer extends ScalebarRenderer {

    public AlternatingBarRenderer(Scalebar scalebar) {
      super(scalebar);
    }

    public void drawScalebar(Canvas canvas, float left, float right, float top, float bottom, double geodeticLength,
        LinearUnit displayUnits) {
      //TODO: advisable to access Scalebar member fields from here?

      // Create Paint for drawing text right at the start, because it's used when calculating the number of segments
      Paint textPaint = new Paint();
      textPaint.setColor(mTextColor);
      textPaint.setShadowLayer(2, 2, 2, mTextShadowColor);
      textPaint.setTypeface(mTypeface);
      textPaint.setTextSize(dpToPixels(mTextSize));

      float barDisplayLength = right - left;
      int numSegments = calculateNumberOfSegments(geodeticLength, barDisplayLength, textPaint);
      float segmentDisplayLength = barDisplayLength / numSegments;

      // Draw a complete solid bar using mAlternateFillColor
      RectF barRect = new RectF(left, top, right, bottom);
      Paint paint = new Paint();
      paint.setColor(mAlternateFillColor);
      paint.setStyle(Paint.Style.FILL);
      canvas.drawRoundRect(barRect, dpToPixels(mCornerRadius), dpToPixels(mCornerRadius), paint);

      // Now draw every second segment on top of it using mFillColor
      paint.setColor(mFillColor);
      float xPos = left + segmentDisplayLength;
      for (int i = 1; i < numSegments; i += 2) { //TODO: i is redundant
        RectF segRect = new RectF(xPos, top, xPos + segmentDisplayLength, bottom);
        canvas.drawRect(segRect, paint);
        xPos += (2 * segmentDisplayLength);
      }

      // Draw line round the outside of the complete bar
      paint = new Paint();
      paint.setColor(mLineColor);
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(dpToPixels(mLineWidth));
      canvas.drawRoundRect(barRect, dpToPixels(mCornerRadius), dpToPixels(mCornerRadius), paint);

      // Draw label at the start of the bar
      float yPosText = bottom + dpToPixels(mTextSize);
      textPaint.setTextAlign(Paint.Align.LEFT);
      canvas.drawText("0", left, yPosText, textPaint);

      // Draw label at the end of the bar
      textPaint.setTextAlign(Paint.Align.RIGHT);
      canvas.drawText(labelString(geodeticLength), left + barDisplayLength, yPosText, textPaint);
      textPaint.setTextAlign(Paint.Align.LEFT);
      canvas.drawText(' ' + displayUnits.getAbbreviation(), left + barDisplayLength, yPosText, textPaint);

      // Draw a vertical line and a label at each segment boundary...
      xPos = left + segmentDisplayLength;
      double segmentGeodeticLength = geodeticLength / numSegments;
      textPaint.setTextAlign(Paint.Align.CENTER);
      for (int segNo = 1; segNo < numSegments; segNo++) {
        canvas.drawLine(xPos, top, xPos, bottom, paint);
        canvas.drawText(labelString(segmentGeodeticLength * segNo), xPos, yPosText, textPaint);
        xPos += segmentDisplayLength;
      }
    }

    private String labelString(double distance) {
      // Format with 2 decimal places
      //TODO currently there can be > 2 decimal places (yuk) and this truncates rather than rounds (double yuk)
      String label = String.format("%.2f", distance);

      // Strip off both decimal places if they're 0s
      if (label.endsWith(".00")) {
        return label.substring(0, label.length() - 3);
      }

      // Otherwise, strip off last decimal place if it's 0
      if (label.endsWith("0")) {
        return label.substring(0, label.length() - 1);
      }
      return label;
    }
  }

}
