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
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.toolkit.R;
import com.esri.arcgisruntime.toolkit.TestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for Compass.
 *
 * @since 100.1.0
 */
@RunWith(AndroidJUnit4.class)
public class CompassTest {

  /**
   * Tests the default values set by the constructor that takes just a Context.
   *
   * @since 100.1.0
   */
  @Test
  public void testSimpleConstructorDefaultValues() {
    Compass compass = new Compass(InstrumentationRegistry.getTargetContext());
    checkDefaultValues(compass);
  }

  /**
   * Tests the constructor that takes an AttributeSet when the AttributeSet is null.
   *
   * @since 100.1.0
   */
  @Test
  public void testNullAttributeSet() {
    Compass compass = new Compass(InstrumentationRegistry.getTargetContext(), null);
    checkDefaultValues(compass);
  }

  /**
   * Tests the default values set from an XML file that doesn't set any of the Compass attributes.
   *
   * @since 100.1.0
   */
  @Test
  public void testXmlNoCompassAttributes() {
    // Inflate layout containing a Compass that doesn't set any of the Compass attributes
    Context context = InstrumentationRegistry.getTargetContext();
    ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.unit_test_compass_no_attrs, null);

    // Find and instantiate that Compass
    Compass compass = viewGroup.findViewById(R.id.compass);

    // Check it contains the correct default attribute values
    checkDefaultValues(compass);
  }

  /**
   * Tests the values set from a fully-populated XML file.
   *
   * @since 100.1.0
   */
  @Test
  public void testXmlFullyPopulated() {
    // Inflate layout containing a Compass that sets all of the Compass attributes
    Context context = InstrumentationRegistry.getTargetContext();
    ViewGroup viewGroup =
        (ViewGroup) LayoutInflater.from(context).inflate(R.layout.unit_test_compass_fully_populated, null);

    // Find and instantiate that Compass
    Compass compass = viewGroup.findViewById(R.id.compass);

    // Check it contains the correct attribute values
    checkSetValues(compass);
  }

  /**
   * Tests all the setter methods.
   *
   * @since 100.1.0
   */
  @Test
  public void testSetters() {
    // Instantiate a Compass
    Compass compass = new Compass(InstrumentationRegistry.getTargetContext());

    // Call all the setters
    compass.setAutoHide(false);
    compass.setCompassHeight(99);
    compass.setCompassWidth(100);

    // Check all the values that were set
    checkSetValues(compass);
  }

  /**
   * Tests IllegalArgumentExceptions from all methods that throw IllegalArgumentException.
   *
   * @since 100.1.0
   */
  @Test
  public void testIllegalArgumentExceptions() {
    // Instantiate a Compass
    Compass compass = new Compass(InstrumentationRegistry.getTargetContext());

    // Test addToMapView()
    try {
      compass.addToGeoView(null);
      fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION);
    } catch (IllegalArgumentException e) {
      //success
    }

    // Test bindTo()
    try {
      compass.bindTo(null);
      fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION);
    } catch (IllegalArgumentException e) {
      //success
    }

    // Test the setters
    try {
      compass.setCompassHeight(0);
      fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION);
    } catch (IllegalArgumentException e) {
      //success
    }
    try {
      compass.setCompassWidth(0);
      fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION);
    } catch (IllegalArgumentException e) {
      //success
    }
  }

  /**
   * Tests addToGeoView(), removeFromGeoView() and bindTo().
   *
   * @since 100.1.0
   */
  @Test
  public void testAddRemoveAndBind() {
    // Must initialize this thread as a Looper so it can instantiate a GeoView
    Looper.prepare();

    Context context = InstrumentationRegistry.getTargetContext();
    MapView mapView = new MapView(context);

    // Instantiate a Compass and add it to a GeoView
    Compass compass = new Compass(context);
    compass.addToGeoView(mapView);

    // Check addToGeoView() fails when it's already added to a GeoView
    try {
      compass.addToGeoView(mapView);
      fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION);
    } catch (IllegalStateException e) {
      //success
    }

    // Remove it from the GeoView and check addToGeoView() can then be called again
    compass.removeFromGeoView();
    compass.addToGeoView(mapView);

    // Check bindTo() fails when it's already added to a GeoView
    try {
      compass.bindTo(mapView);
      fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION);
    } catch (IllegalStateException e) {
      //success
    }

    // Remove it from the GeoView and check bindTo() can then be called
    compass.removeFromGeoView();
    compass.bindTo(mapView);

    // Check bindTo() is allowed when already bound
    compass.bindTo(mapView);

    // Check addToGeoView() fails when it's already bound to a GeoView
    try {
      compass.addToGeoView(mapView);
      fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION);
    } catch (IllegalStateException e) {
      //success
    }

    // Check removeFromGeoView() can be called when it's not added to a GeoView
    compass.removeFromGeoView();
  }

  /**
   * Checks that the given Compass object contains default values for all attributes.
   *
   * @param compass the Compass
   * @since 100.1.0
   */
  private void checkDefaultValues(Compass compass) {
    assertTrue("Expected isAutoHide() to return true", compass.isAutoHide());
    assertEquals(50, compass.getCompassHeight());
    assertEquals(50, compass.getCompassWidth());
  }

  /**
   * Checks that the given Compass object contains values that have been set (by setter methods or from XML) for all
   * attributes.
   *
   * @param compass the Compass
   * @since 100.1.0
   */
  private void checkSetValues(Compass compass) {
    assertFalse("Expected isAutoHide() to return false", compass.isAutoHide());
    assertEquals(99, compass.getCompassHeight());
    assertEquals(100, compass.getCompassWidth());
  }

}
