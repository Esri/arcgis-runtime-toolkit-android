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
   * Tests that a compass can be created and test its default values.
   *
   * @since 100.1.0
   */
  @Test
  public void testConstructor() {
    Compass compass = new Compass(InstrumentationRegistry.getTargetContext());
    assertTrue(compass.isAutoHide());
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
    compass.setAutoHide(false);
    assertFalse(compass.isAutoHide());
  }

  /**
   * Tests Compass.bindTo(MapView) and Compass.addToGeoView(MapView)
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
      compass.addToGeoView(mapView);
      compass.bindTo(mapView);
      fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION);
    } catch (IllegalStateException e) {
      //success
    }

    //should be able to bind again after removing from MapView
    compass.removeFromGeoView();
    compass.bindTo(mapView);

    //reset test
    compass.removeFromGeoView();

    compass.addToGeoView(mapView);
    assertTrue(compass.getParent() == mapView);

    try {
      compass.addToGeoView(null);
      fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION);
    } catch (IllegalArgumentException e) {
      //success
    }

    //adding to a MapView if it is added to a MapView already should fail
    try {
      compass.addToGeoView(mapView);
      compass.addToGeoView(mapView);
      fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION);
    } catch (IllegalStateException e) {
      //success
    }

    //adding to a MapView if it is bound to a MapView already should fail
    try {
      compass.bindTo(mapView);
      compass.addToGeoView(mapView);
      fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION);
    } catch (IllegalStateException e) {
      //success
    }

    //Test that the view was removed from the MapView and that it can be added again after removal
    compass.removeFromGeoView();
    assertTrue(compass.getParent() == null);
    compass.addToGeoView(mapView);
    assertTrue(compass.getParent() == mapView);
  }
}
