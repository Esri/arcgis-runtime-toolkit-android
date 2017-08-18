package com.esri.arcgisruntime.toolkit.compass;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.MapRotationChangedEvent;
import com.esri.arcgisruntime.mapping.view.MapRotationChangedListener;
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

  /**
   * Creates a new instance of a Compass.
   *
   * @param context
   * @since 100.1.0
   */
  public Compass(Context context) {
    super(context);
    mPaint = new Paint();
    mMatrix = new Matrix();
    Bitmap tempBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_compass);
    mCompassBitmap = tempBitmap;

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
   * @param context
   * @param attributeSet
   * @since 100.1.0
   */
  public Compass(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    mPaint = new Paint();
    mMatrix = new Matrix();
    Bitmap tempBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_compass);
    mCompassBitmap  =tempBitmap;
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
   * @param autoHide
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
    mMapView.addView(this, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    //TODO figure out sizing requirements
    this.getLayoutParams().height = 200;
    this.getLayoutParams().width = 200;
    this.setLayoutParams(this.getLayoutParams());
    mMapView.invalidate();

    //TODO initial layout
    mDrawInMapView = true;
    this.
        mMapView.addMapRotationChangedListener(new MapRotationChangedListener() {
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
    });
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
    mMapView.addMapRotationChangedListener(new MapRotationChangedListener() {
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
    });
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

    //TODO automatic positioning
    if(mDrawInMapView) {
    }

    mMatrix.reset();
    mMatrix.postRotate(-mRotation, mCompassBitmap.getWidth()/2, mCompassBitmap.getHeight()/2);
    //scale matrix by height of bitmap to height of calculated view
    float dimension = (float)getHeight()/mCompassBitmap.getHeight();
    mMatrix.postScale(dimension, dimension);

    canvas.drawBitmap(mCompassBitmap, mMatrix, mPaint);
  }
}