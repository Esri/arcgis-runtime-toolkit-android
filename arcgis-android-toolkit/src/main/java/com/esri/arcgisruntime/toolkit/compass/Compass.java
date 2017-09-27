package com.esri.arcgisruntime.toolkit.compass;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.ViewTreeObserver;
import com.esri.arcgisruntime.mapping.view.*;
import com.esri.arcgisruntime.toolkit.R;

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

  private final Bitmap mCompassBitmap;
  private MapView mMapView;
  private float mRotation;
  private Compass mCompass;
  private boolean mIsAutoHide = true;
  private boolean mDrawInMapView;
  private Paint mPaint;
  private Matrix mMatrix;
  private DisplayMetrics mDisplayMetrics;
  private int mHeight;
  private int mWidth;

  private MapRotationChangedListener mMapRotationChangedListener = new MapRotationChangedListener() {
    @Override
    public void mapRotationChanged(MapRotationChangedEvent viewpointChangedEvent) {
      Log.d("ViewPoint", "Changed");
      mRotation = (float) ((MapView) viewpointChangedEvent.getSource()).getMapRotation();
      Log.d("Rotation", String.valueOf(mRotation));

      if(mIsAutoHide) {
        if (Double.compare(mRotation, 0) == 0) {
          mCompass.setVisibility(GONE);
        } else {
          mCompass.setVisibility(VISIBLE);
        }
      }
      mCompass.invalidate();
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
    mPaint = new Paint();
    mMatrix = new Matrix();
    mCompassBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_compass);
    mDisplayMetrics = new DisplayMetrics();
    ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

    this.setOnTouchListener(new OnTouchListener() {
      @Override public boolean onTouch(View view, MotionEvent motionEvent) {
        return performClick();
      }
    });
    mCompass = this;
  }

  /**
   * Creates a new instance of a Compass.
   *
   * @param context the current context
   * @param attributeSet the attribute set for the view
   * @since 100.1.0
   */
  public Compass(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    mPaint = new Paint();
    mMatrix = new Matrix();
    mCompassBitmap  = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_compass);
    mDisplayMetrics = new DisplayMetrics();
    ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
    this.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View view) {
        performClick();
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
    if(!mIsAutoHide) {
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
   * Resets the map to be oriented toward 0 degrees when the compass is clicked.
   *
   * @return
   * @since 100.1.0
   */
  @Override
  public boolean performClick() {
    if(mMapView != null) {
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
   * @since 100.1.0
   */
  public void addToMapView(MapView mapView) {
    mMapView = mapView;
    if(mMapView.getDrawStatus() == DrawStatus.COMPLETED) {
      onDrawCompleted();
    } else {
      mMapView.addDrawStatusChangedListener(new DrawStatusChangedListener() {
        @Override public void drawStatusChanged(DrawStatusChangedEvent drawStatusChangedEvent) {
          if(drawStatusChangedEvent.getDrawStatus() == DrawStatus.COMPLETED) {
            mMapView.removeDrawStatusChangedListener(this);
            //TODO figure out a better way to lay out the view dynamically - without this callback the view gets laid out
            //in the wrong location.
            mMapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override public void onGlobalLayout() {
                mMapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                onDrawCompleted();
              }
            });
          }
        }
      });
    }
  }

  private void onDrawCompleted() {
    mDrawInMapView = true;
    mMapView.addView(mCompass,
        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    this.mMapView.addMapRotationChangedListener(mMapRotationChangedListener);
    mHeight = (int)(50 * mDisplayMetrics.density);
    mWidth =  (int)(50 * mDisplayMetrics.density);
    this.getLayoutParams().height = mHeight;
    this.getLayoutParams().width = mWidth;
    this.setLayoutParams(this.getLayoutParams());
  }

  /**
   * Removes this Compass from the MapView it was added to.
   *
   * @since 100.1.0
   */
  public void removeFromMapView() {
    if(mDrawInMapView) {
      mMapView.removeView(this);
      mMapView.removeMapRotationChangedListener(mMapRotationChangedListener);
      mMapView = null;
      mDrawInMapView = false;
    }
  }

  /**
   * Binds the compass to the given MapView so that the Compass can listen to changes in the Map's rotation. When using
   * this method, only changes in rotation are handled by the compass, all sizing and layout are left to the
   * application.
   *
   * @param mapView the MapView to bind the compass to
   * @since 100.1.0
   */
  public void bindTo(MapView mapView) {
    mMapView = mapView;
    mMapView.addMapRotationChangedListener(mMapRotationChangedListener);
  }

  /**
   * Draws the compass with the current rotation to the screen.
   *
   * @param canvas the canvas to draw on
   * @since 100.1.0
   */
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    Log.d("OnDraw", "Canvas");

    if(mDrawInMapView) {
      int newTop = (int) (mMapView.getTop() + (.05 * mMapView.getHeight()));
      this.setTop(newTop);
      int newRight = (int) (mMapView.getRight() - (.05 * mMapView.getWidth()));
      this.setRight(newRight);
      this.setLeft(newRight - mWidth);
      this.setBottom(newTop + mHeight);
      Log.d("Location", String.valueOf(this.getTop()) +" "  +String.valueOf(this.getBottom()) +" "  + String.valueOf(this.getLeft()) + " "  +String.valueOf(this.getRight()));

    }

    mMatrix.reset();
    mMatrix.postRotate(-mRotation, mCompassBitmap.getWidth()/2, mCompassBitmap.getHeight()/2);
    //scale matrix by height of bitmap to height of compass view
    float dimension = (float)getHeight()/mCompassBitmap.getHeight();
    mMatrix.postScale(dimension, dimension);

    canvas.drawBitmap(mCompassBitmap, mMatrix, mPaint);
  }
}