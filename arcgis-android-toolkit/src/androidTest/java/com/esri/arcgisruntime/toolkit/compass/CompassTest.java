package com.esri.arcgisruntime.toolkit.compass;

import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.toolkit.TestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Unit tests for Compass.
 *
 * @since 100.1.0
 */
@RunWith(AndroidJUnit4.class)
public class CompassTest {

  /**
   * Tests that a compass can be created.
   *
   * @since 100.1.0
   */
  @Test
  public void testConstructor() {
    Compass compass = new Compass(InstrumentationRegistry.getTargetContext());
  }

  /**
   * Tests default value and setting new value for Compass.isAutoHide().
   *
   * @since 100.1.0
   */
  @Test
  public void testSetAutoHide() {
    Compass compass = new Compass(InstrumentationRegistry.getTargetContext());
    assertTrue(compass.isAutoHide());
    compass.setIsAutoHide(false);
    assertFalse(compass.isAutoHide());
  }

  /**
   * Tests Compass.bindTo(MapView) and Compass.addToMapView(MapView)
   *
   * @since 100.1.0
   */
  @Test
  public void testBindToMapView() {
    // Must initialize this thread as a Looper so it can instantiate a MapView
    Looper.prepare();

    Compass compass = new Compass(InstrumentationRegistry.getTargetContext());
    MapView mapView = new MapView(InstrumentationRegistry.getTargetContext());
    compass.bindTo(mapView);

    try {
      compass.bindTo(null);
      fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION);
    } catch (IllegalArgumentException e) {
      //success
    }

    //binding to a MapView if it is added to a MapView should fail
    try {
      compass.addToMapView(mapView);
      compass.bindTo(mapView);
      fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION);
    } catch (IllegalStateException e) {
      //success
    }

    //should be able to bind again after removing from MapView
    compass.removeFromMapView();
    compass.bindTo(mapView);

    //reset test
    compass.removeFromMapView();

    compass.addToMapView(mapView);
    assertTrue(compass.getParent() == mapView);

    try {
      compass.addToMapView(null);
      fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION);
    } catch (IllegalArgumentException e) {
      //success
    }

    //adding to a MapView if it is added to a MapView already should fail
    try {
      compass.addToMapView(mapView);
      compass.addToMapView(mapView);
      fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION);
    } catch (IllegalStateException e) {
      //success
    }

    //adding to a MapView if it is bound to a MapView already should fail
    try {
      compass.bindTo(mapView);
      compass.addToMapView(mapView);
      fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION);
    } catch (IllegalStateException e) {
      //success
    }

    //Test that the view was removed from the MapView and that it can be added again after removal
    compass.removeFromMapView();
    assertTrue(compass.getParent() == null);
    compass.addToMapView(mapView);
    assertTrue(compass.getParent() == mapView);
  }


}
