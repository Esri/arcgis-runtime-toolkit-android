/*
 * Copyright 2018 Esri
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
package com.esri.arcgisruntime.toolkit.compass;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.GeoView;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedEvent;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedListener;
import com.esri.arcgisruntime.toolkit.R;
import com.esri.arcgisruntime.toolkit.ToolkitUtil;

/**
 * The purpose of the Compass is to show the current orientation of a map or scene. By default it is visible any time
 * the map/scene is not orientated to 0 degrees. It can also be tapped to reset the map/scene to 0 degrees orientation,
 * which hides the compass. The auto hide behavior can be disabled with {@link #setAutoHide(boolean)}.
 * <p>
 * The expected use case for the Compass is that the application manages the layout of the Compass view, and calls
 * {@link #bindTo(GeoView)} which sets up the connection to the GeoView so the orientation can be tracked. A second
 * option is to simply create a new Compass object and then call {@link #addToGeoView(GeoView)}, and then the Compass
 * view will insert itself into the GeoView's ViewGroup and handle its own layout position and size.
 *
 * @since 100.1.0
 */
public final class Compass extends View {
  private static final double AUTO_HIDE_THRESHOLD = 0.1E-10;

  private static final int DEFAULT_HEIGHT_DP = 50;

  private static final int DEFAULT_WIDTH_DP = 50;

  private final Matrix mMatrix = new Matrix();

  private Bitmap mCompassBitmap;

  private GeoView mGeoView;

  private double mRotation = 0;

  private boolean mIsAutoHide = true;

  private boolean mDrawInGeoView;

  private float mDisplayDensity;

  private float mHeightDp = DEFAULT_HEIGHT_DP;

  private float mWidthDp = DEFAULT_WIDTH_DP;

  private final ViewpointChangedListener mViewpointChangedListener = new ViewpointChangedListener() {
    @Override
    public void viewpointChanged(ViewpointChangedEvent viewpointChangedEvent) {
      // Viewpoint has change - get current rotation or heading
      if (mGeoView instanceof MapView) {
        mRotation = ((MapView) mGeoView).getMapRotation();
      } else {
        mRotation = ((SceneView) mGeoView).getCurrentViewpointCamera().getHeading();
      }

      // Show or hide, depending on whether auto-hide is enabled, and if so depending on current rotation
      showOrHide();

      // Invalidate the Compass view to update it
      postInvalidate();
    }
  };

  private final OnLayoutChangeListener mAttributionViewLayoutChangeListener = new OnLayoutChangeListener() {
    @Override
    public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop,
        int oldRight, int oldBottom) {
      // Invalidate the Compass view when the bounds of the attribution view change; this happens when view insets are
      // set, which may affect where the Compass is drawn
      postInvalidate();
    }
  };

  /**
   * Constructs a Compass programmatically. Called by the app when Workflow 1 is used (see {@link Compass} above).
   *
   * @param context the execution Context
   * @since 100.1.0
   */
  public Compass(Context context) {
    super(context);
    initializeCompass(context);
  }

  /**
   * Constructor that's called when inflating a Compass from XML. Called by the system when Workflow 2 is used (see
   * {@link Compass} above).
   *
   * @param context the execution Context
   * @param attrs   the attributes of the XML tag that is inflating the view
   * @since 100.1.0
   */
  public Compass(Context context, AttributeSet attrs) {
    super(context, attrs);
    initializeCompass(context);
    mIsAutoHide = attrs.getAttributeBooleanValue(null, "compass.autoHide", true);
    mHeightDp = attrs.getAttributeIntValue(null, "compass.height", DEFAULT_HEIGHT_DP);
    mWidthDp = attrs.getAttributeIntValue(null, "compass.width", DEFAULT_WIDTH_DP);
  }

  /**
   * Adds this Compass to the given GeoView. Used in Workflow 1 (see {@link Compass} above).
   *
   * @param geoView the GeoView
   * @throws IllegalArgumentException if geoView is null
   * @throws IllegalStateException    if this Compass is already added to or bound to a GeoView
   * @since 100.1.0
   */
  public void addToGeoView(GeoView geoView) {
    ToolkitUtil.throwIfNull(geoView, "geoView");
    if (mGeoView != null) {
      throw new IllegalStateException("Compass already has a GeoView");
    }
    mDrawInGeoView = true;
    geoView.addView(this, new ViewGroup.LayoutParams(dpToPixels(mWidthDp), dpToPixels(mHeightDp)));
    setupGeoView(geoView);
  }

  /**
   * Removes and unbinds this Compass from the GeoView it was added or bound to (if any).
   *
   * @since 100.1.0
   */
  public void removeFromGeoView() {
    // If it was added to a GeoView, remove it
    if (mDrawInGeoView) {
      mGeoView.removeView(this);
      mDrawInGeoView = false;
    }

    // Unbind from GeoView by removing listeners
    if (mGeoView != null) {
      removeListenersFromGeoView();
      mGeoView = null;
    }
  }

  /**
   * Binds this Compass to the given GeoView. Used in Workflow 2 (see {@link Compass} above).
   *
   * @param geoView the GeoView to bind the Compass to
   * @throws IllegalArgumentException if geoView is null
   * @throws IllegalStateException    if this Compass is currently added to a GeoView
   * @since 100.1.0
   */
  public void bindTo(GeoView geoView) {
    ToolkitUtil.throwIfNull(geoView, "geoView");
    if (mDrawInGeoView) {
      throw new IllegalStateException("Compass already added to a MapView");
    }
    setupGeoView(geoView);
  }

  /**
   * Sets whether this Compass is automatically hidden when the map/scene rotation is 0 degrees.
   *
   * @param autoHide true to auto hide the Compass, false to have it always show; the default is true
   * @since 100.1.0
   */
  public void setAutoHide(boolean autoHide) {
    mIsAutoHide = autoHide;
    showOrHide();
  }

  /**
   * Indicates if this Compass is automatically hidden when the map/scene rotation is 0 degrees.
   *
   * @return true if the Compass is automatically hidden, false if it is always show
   */
  public boolean isAutoHide() {
    return mIsAutoHide;
  }

  /**
   * Sets the height to use when drawing the icon for this Compass. The default is 50dp.
   *
   * @param heightDp the height to set, in density-independent pixels
   * @since 100.1.0
   */
  public void setCompassHeight(int heightDp) {
    mHeightDp = heightDp;
    if (mDrawInGeoView) {
      getLayoutParams().height = dpToPixels(mHeightDp);
    }
    postInvalidate();
  }

  /**
   * Gets the height of the icon for this Compass.
   *
   * @return the height, in density-independent pixels
   * @since 100.1.0
   */
  public int getCompassHeight() {
    return Math.round(mHeightDp);
  }

  /**
   * Sets the width to use when drawing the icon for this Compass. The default is 50dp.
   *
   * @param widthDp the width to set, in density-independent pixels
   * @since 100.1.0
   */
  public void setCompassWidth(int widthDp) {
    mWidthDp = widthDp;
    if (mDrawInGeoView) {
      getLayoutParams().width = dpToPixels(mWidthDp);
    }
    postInvalidate();
  }

  /**
   * Gets the width of the icon for this Compass.
   *
   * @return the width, in density-independent pixels
   * @since 100.1.0
   */
  public int getCompassWidth() {
    return Math.round(mWidthDp);
  }

  /**
   * Resets the map/scene to be oriented toward 0 degrees when the Compass is clicked.
   *
   * @return true if there was an assigned OnClickListener that was called, false otherwise
   * @since 100.1.0
   */
  @Override
  public boolean performClick() {
    if (mGeoView != null) {
      if (mGeoView instanceof MapView) {
        ((MapView) mGeoView).setViewpointRotationAsync(0);
      } else {
        Camera camera = ((SceneView) mGeoView).getCurrentViewpointCamera();
        ((SceneView) mGeoView).setViewpointCameraAsync(
            new Camera(camera.getLocation(), 0, camera.getPitch(), camera.getRoll()));
      }
    }
    return super.performClick();
  }

  /**
   * Draws the Compass with the current rotation to the screen.
   *
   * @param canvas the canvas to draw on
   * @since 100.1.0
   */
  @Override
  protected void onDraw(Canvas canvas) {
    if (mGeoView == null) {
      return;
    }

    // Set the position of the compass if it's being drawn within the GeoView (workflow 1)
    if (mDrawInGeoView) {
      float xPos = (mGeoView.getRight() - (.02f * mGeoView.getWidth())) - dpToPixels(mWidthDp);
      float yPos = mGeoView.getTop() + (.02f * mGeoView.getHeight());
      // If the GeoView is a MapView, adjust the position to take account of any view insets that may be set
      if (mGeoView instanceof MapView) {
        MapView mapView = (MapView) mGeoView;
        xPos -= dpToPixels(mapView.getViewInsetRight());
        yPos += dpToPixels(mapView.getViewInsetTop());
      }
      setX(xPos);
      setY(yPos);
    }

    // Setup a matrix with the correct rotation
    mMatrix.reset();
    mMatrix.postRotate((float) -mRotation, mCompassBitmap.getWidth() / 2, mCompassBitmap.getHeight() / 2);

    // Scale the matrix by the size of the bitmap to the size of the compass view
    float xScale = (float) dpToPixels(mWidthDp) / mCompassBitmap.getWidth();
    float yScale = (float) dpToPixels(mHeightDp) / mCompassBitmap.getHeight();
    mMatrix.postScale(xScale, yScale);

    // Draw the bitmap
    canvas.drawBitmap(mCompassBitmap, mMatrix, null);
  }

  /**
   * Performs initialization that's required by all constructors.
   *
   * @param context the execution Context
   */
  private void initializeCompass(Context context) {
    mCompassBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_compass);//TODO: change name of drawable?
    mDisplayDensity = context.getResources().getDisplayMetrics().density;
    showOrHide();

    setOnTouchListener(new OnTouchListener() {
      @Override public boolean onTouch(View view, MotionEvent motionEvent) {
        performClick();
        return true;
      }
    });
  }

  /**
   * Sets up the Compass to work with the given GeoView.
   *
   * @param geoView the GeoView
   * @since 100.1.0
   */
  private void setupGeoView(GeoView geoView) {
    // Remove listeners from old GeoView
    if (mGeoView != null) {
      removeListenersFromGeoView();
    }

    // Add listeners to new GeoView
    mGeoView = geoView;
    mGeoView.addViewpointChangedListener(mViewpointChangedListener);
    mGeoView.addAttributionViewLayoutChangeListener(mAttributionViewLayoutChangeListener);
  }

  /**
   * Removes the listeners from mGeoView.
   *
   * @since 100.1.0
   */
  private void removeListenersFromGeoView() {
    mGeoView.removeViewpointChangedListener(mViewpointChangedListener);
    mGeoView.removeAttributionViewLayoutChangeListener(mAttributionViewLayoutChangeListener);
  }

  /**
   * Show or hide the Compass, depending on whether auto-hide is enabled, and if so whether the current rotation is less
   * than the threshold. Handle 0 and 360 degrees.
   */
  private void showOrHide() {
    if (mIsAutoHide) {
      // Auto-hide enabled - hide if rotation is less than the threshold
      if (Math.abs(mRotation) < AUTO_HIDE_THRESHOLD || Math.abs(360 - mRotation) < AUTO_HIDE_THRESHOLD) {
        setVisibility(GONE);
      } else {
        setVisibility(VISIBLE);
      }
    } else {
      // Auto-hide disabled - always show the compass
      setVisibility(VISIBLE);
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

}