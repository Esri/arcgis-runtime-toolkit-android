package com.esri.arcgisruntime.toolkit.compass;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedEvent;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedListener;
import com.esri.arcgisruntime.toolkit.R;
import com.esri.arcgisruntime.toolkit.ToolkitUtil;

/**
 * The purpose of the Compass is to show the current orientation of the Map. By default it is visible any time the map
 * is not orientated to 0 degrees. It can also be tapped to reset the map to 0 degrees orientation, which hides the
 * compass. The auto hide behavior can be disabled with {@link #setIsAutoHide(boolean)}. The expected use case for the
 * Compass is that the application manages the layout of the Compass view, and calls {@link #bindTo(MapView)}
 * which sets up the connection to the MapView so the orientation can be tracked. A second option is to simply create
 * a new Compass object and then call {@link #addToMapView(MapView)}, and then the Compass view will insert itself
 * into the MapView's ViewGroup and handle its own layout position and size.
 *
 * @since 100.1.0
 */
public class Compass extends View {

  private Bitmap mCompassBitmap;
  private MapView mMapView;
  private double mRotation;
  private Compass mCompass;
  private boolean mIsAutoHide = true;
  private boolean mDrawInMapView;
  private Paint mPaint;
  private Matrix mMatrix;
  private DisplayMetrics mDisplayMetrics;
  private int mHeight;
  private int mWidth;

  private final ViewpointChangedListener mViewpointChangedListener = new ViewpointChangedListener() {
    @Override
    public void viewpointChanged(ViewpointChangedEvent viewpointChangedEvent) {
      mRotation = mMapView.getMapRotation();//TODO: what to do for SceneView?

      if (mIsAutoHide) {
        if (Double.compare(mRotation, 0) == 0) {
          mCompass.setVisibility(GONE);
        } else {
          mCompass.setVisibility(VISIBLE);
        }
      }

      // Invalidate the Compass view when the GeoView viewpoint changes
      postInvalidate();
    }
  };

  /**
   * Creates a new instance of a Compass.
   *
   * @param context the current Activity context
   * @since 100.1.0
   */
  public Compass(Context context) {
    super(context);
    initializeCompass(context);
  }

  /**
   * Creates a new instance of a Compass. Called when the Compass is created within an XML layout.
   *
   * @param context the current Activity context
   * @param attributeSet the attribute set for the view
   * @since 100.1.0
   */
  public Compass(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    initializeCompass(context);
  }

  private void initializeCompass(Context context) {
    mPaint = new Paint();
    mMatrix = new Matrix();
    //TODO should this be settable by the user?
    mCompassBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_compass);
    mDisplayMetrics = context.getResources().getDisplayMetrics();

    this.setOnTouchListener(new OnTouchListener() {
      @Override public boolean onTouch(View view, MotionEvent motionEvent) {
        return performClick();
      }
    });
    mCompass = this;
    this.setVisibility(GONE);
  }

  /**
   * Sets whether the compass will be shown when the map rotation is at 0 degrees.
   *
   * @param autoHide true to auto hide the Compass, false to have it always show
   * @since 100.1.0
   */
  public void setIsAutoHide(boolean autoHide) {
    mIsAutoHide = autoHide;
    if (!mIsAutoHide) {
      this.setVisibility(VISIBLE);
    } else {
      this.setVisibility(GONE);
    }
  }

  /**
   * Returns if the compass is not being shown when the map rotation is at 0 degrees.
   *
   * @return true if the compass is not being shown at 0 degrees, false if it is
   */
  public boolean isAutoHide() {
    return mIsAutoHide;
  }

  /**
   * Returns the current heading of the MapView and the Compass. If the compass is not bound to a MapView this will
   * always be zero.
   *
   * @return the current heading.
   */
  public double getHeading() {
    return mRotation;
  }

  /**
   * Sets the heading of the Compass and MapView to the given value in degrees. If the Compass is not bound to a
   * MapView this will have no effect.
   *
   * @param heading the heading to set the compass to
   */
  public void setHeading(double heading) {//TODO: not used - remove it? and getHeading()?
    if(mMapView != null) {
      mMapView.setViewpointRotationAsync(heading);
    }
  }
  /**
   * Resets the map to be oriented toward 0 degrees when the compass is clicked.
   *
   * @return
   * @since 100.1.0
   */
  @Override
  public boolean performClick() {
    if (mMapView != null) {
      mMapView.setViewpointRotationAsync(0);
      return true;
    }
    return super.performClick();
  }

  /**
   * Adds the Compass to a MapView, which inserts the Compass into the MapView's ViewGroup. When using this method,
   * all layout and sizing will be handled by the Compass.
   *
   * @param mapView the MapView to attach to
   * @throws IllegalArgumentException if mapView is null
   * @throws IllegalStateException if this Compass is already added to or bound to a MapView
   * @since 100.1.0
   */
  public void addToMapView(MapView mapView) {
    ToolkitUtil.throwIfNull(mapView, "mapView");
    if (mMapView != null) {
      throw new IllegalStateException("Compass already has a MapView");
    }
    mMapView = mapView;
    setupGeoView(mapView);

    mDrawInMapView = true;
    mMapView.addView(mCompass,
        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    //TODO how big should the compass be?
    mHeight = (int) (50 * mDisplayMetrics.density);
    mWidth = (int) (50 * mDisplayMetrics.density);
    getLayoutParams().height = mHeight;
    getLayoutParams().width = mWidth;
    setLayoutParams(this.getLayoutParams());
  }

  /**
   * Sets up the Compass to work with the given GeoView.
   *
   * @param geoView the GeoView
   * @since 100.1.0
   */
  private void setupGeoView(MapView geoView) {
    // Remove listeners from old GeoView
    if (mMapView != null) {
      removeListenersFromGeoView();
    }

    // Add listeners to new MapView
    mMapView = geoView;
    mMapView.addViewpointChangedListener(mViewpointChangedListener);
//    mMapView.addAttributionViewLayoutChangeListener(mAttributionViewLayoutChangeListener);

//    // Set display density and create the Paint used for text (note display density must be set first)
//    mDisplayDensity = mMapView.getContext().getResources().getDisplayMetrics().density;
//    createTextPaint();
  }

  /**
   * Removes the listeners from mMapView.
   *
   * @since 100.1.0
   */
  private void removeListenersFromGeoView() {
    mMapView.removeViewpointChangedListener(mViewpointChangedListener);
//    mMapView.removeAttributionViewLayoutChangeListener(mAttributionViewLayoutChangeListener);
  }

  /**
   * Removes this Compass from the MapView it was added to. If this Compass was bound to a MapView this method will
   * unbind the compass from the MapView.
   *
   * @since 100.1.0
   */
  public void removeFromMapView() {
    if (mDrawInMapView) {
      mMapView.removeView(this);
      removeListenersFromGeoView();
      mMapView = null;
      mDrawInMapView = false;
    } else if (mMapView != null) {
      //TODO: scalebar doesn't do this bit; let's make them consistent
      removeListenersFromGeoView();
      mMapView = null;
    }
    mRotation = 0;
  }

  /**
   * Binds the compass to the given MapView so that the Compass can listen to changes in the Map's rotation. When using
   * this method, only changes in rotation are handled by the compass, all sizing and layout are left to the
   * application.
   *
   * @param mapView the MapView to bind the Compass to
   * @throws IllegalArgumentException if mapView is null
   * @throws IllegalStateException if this Compass is currently added to a MapView
   * @since 100.1.0
   */
  public void bindTo(MapView mapView) {
    ToolkitUtil.throwIfNull(mapView, "mapView");
    if (mDrawInMapView) {
      throw new IllegalStateException("Compass already added to a MapView");
    }

    setupGeoView(mapView);
  }

  /**
   * Draws the compass with the current rotation to the screen.
   *
   * @param canvas the canvas to draw on
   * @since 100.1.0
   */
  @Override
  protected void onDraw(Canvas canvas) {
    if (mDrawInMapView) {
      setX((mMapView.getRight() - (.02f * mMapView.getWidth())) - mWidth);
      setY(mMapView.getTop() + (.02f * mMapView.getHeight()));
    }

    mMatrix.reset();
    mMatrix.postRotate((float)-mRotation, mCompassBitmap.getWidth() / 2, mCompassBitmap.getHeight() / 2);
    //scale matrix by height of bitmap to height of compass view
    float dimension = (float) mHeight / mCompassBitmap.getHeight();
    mMatrix.postScale(dimension, dimension);

    canvas.drawBitmap(mCompassBitmap, mMatrix, mPaint);
  }
}