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
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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
public class Compass extends View {
  private static final double AUTO_HIDE_THRESHOLD = 0.1E-10;

  private Bitmap mCompassBitmap;
  private GeoView mGeoView;
  private double mRotation;
  private Compass mCompass;
  private boolean mIsAutoHide = true;
  private boolean mDrawInGeoView;
  private Paint mPaint;
  private Matrix mMatrix;
  private DisplayMetrics mDisplayMetrics;
  private int mHeight;
  private int mWidth;

  private final ViewpointChangedListener mViewpointChangedListener = new ViewpointChangedListener() {
    @Override
    public void viewpointChanged(ViewpointChangedEvent viewpointChangedEvent) {
      // Viewpoint has change - get current rotation or heading
      if (mGeoView instanceof MapView) {
        mRotation = ((MapView) mGeoView).getMapRotation();
      } else {
        mRotation = ((SceneView) mGeoView).getCurrentViewpointCamera().getHeading();
      }

      // If auto-hide enabled, hide compass if current rotation is less than the threshold; handle 0 and 360 degrees
      if (mIsAutoHide) {
        if (Math.abs(mRotation) < AUTO_HIDE_THRESHOLD || Math.abs(360 - mRotation) < AUTO_HIDE_THRESHOLD) {
          mCompass.setVisibility(GONE);
        } else {
          mCompass.setVisibility(VISIBLE);
        }
      }

      // Invalidate the Compass view to update it
      postInvalidate();
    }
  };

  /**
   * Constructs a Compass programmatically. Called by the app when Workflow 1 is used (see {@link Compass} above).
   *
   * @param context the current execution Context
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
   * @param context the current execution Context
   * @param attrs   the attributes of the XML tag that is inflating the view
   * @since 100.1.0
   */
  public Compass(Context context, AttributeSet attrs) {
    super(context, attrs);
    initializeCompass(context);
    //TODO: initialise fields from attrs
  }

  //TODO: javadoc
  private void initializeCompass(Context context) {
    mPaint = new Paint();
    mMatrix = new Matrix();
    //TODO should this be settable by the user?
    mCompassBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_compass);
    mDisplayMetrics = context.getResources().getDisplayMetrics();

    setOnTouchListener(new OnTouchListener() {
      @Override public boolean onTouch(View view, MotionEvent motionEvent) {
        performClick();
        return true;
      }
    });
    mCompass = this;
    setVisibility(GONE);
    mRotation = 0;
  }

  /**
   * Sets whether this Compass is automatically hidden when the map/scene rotation is 0 degrees.
   *
   * @param autoHide true to auto hide the Compass, false to have it always show
   * @since 100.1.0
   */
  public void setAutoHide(boolean autoHide) {
    mIsAutoHide = autoHide;
    if (!mIsAutoHide) {
      setVisibility(VISIBLE);
    } else {
      setVisibility(GONE);
    }
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
    * Adds this Compass to the given GeoView. Used in Workflow 1 (see {@link Compass} above).
   *
   * @param geoView the GeoView
   * @throws IllegalArgumentException if geoView is null
   * @throws IllegalStateException if this Compass is already added to or bound to a GeoView
   * @since 100.1.0
   */
  public void addToGeoView(GeoView geoView) {
    ToolkitUtil.throwIfNull(geoView, "geoView");
    if (mGeoView != null) {
      throw new IllegalStateException("Compass already has a GeoView");
    }
    mGeoView = geoView;
    setupGeoView(geoView);

    mDrawInGeoView = true;
    mGeoView.addView(mCompass,
        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    //TODO how big should the compass be?
    mHeight = (int) (50 * mDisplayMetrics.density);
    mWidth = (int) (50 * mDisplayMetrics.density);
    getLayoutParams().height = mHeight;
    getLayoutParams().width = mWidth;
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
    //TODO: do we want the following (copied from Scalebar)
//    mGeoView.addAttributionViewLayoutChangeListener(mAttributionViewLayoutChangeListener);

//    // Set display density and create the Paint used for text (note display density must be set first)
//    mDisplayDensity = mGeoView.getContext().getResources().getDisplayMetrics().density;
//    createTextPaint();
  }

  /**
   * Removes the listeners from mGeoView.
   *
   * @since 100.1.0
   */
  private void removeListenersFromGeoView() {
    mGeoView.removeViewpointChangedListener(mViewpointChangedListener);
    //TODO: do we want the following (copied from Scalebar)
//    mGeoView.removeAttributionViewLayoutChangeListener(mAttributionViewLayoutChangeListener);
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
   * @throws IllegalStateException if this Compass is currently added to a GeoView
   * @since 100.1.0
   */
  public void bindTo(GeoView geoView) {
    ToolkitUtil.throwIfNull(geoView, "geoView");
    if (mDrawInGeoView) {
      throw new IllegalStateException("Compass already added to a MapView");
    }
    setupGeoView(geoView);
    mDrawInGeoView = false;
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

    if (mDrawInGeoView) {
      setX((mGeoView.getRight() - (.02f * mGeoView.getWidth())) - mWidth);
      setY(mGeoView.getTop() + (.02f * mGeoView.getHeight()));
    }

    mMatrix.reset();
    mMatrix.postRotate((float)-mRotation, mCompassBitmap.getWidth() / 2, mCompassBitmap.getHeight() / 2);
    //scale matrix by height of bitmap to height of compass view
    float dimension = (float) mHeight / mCompassBitmap.getHeight();
    mMatrix.postScale(dimension, dimension);

    canvas.drawBitmap(mCompassBitmap, mMatrix, mPaint);
  }
}