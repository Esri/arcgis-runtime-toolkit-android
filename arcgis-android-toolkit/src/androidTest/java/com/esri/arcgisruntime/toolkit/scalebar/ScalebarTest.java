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
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.esri.arcgisruntime.UnitSystem;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.toolkit.R;
import com.esri.arcgisruntime.toolkit.TestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for Scalebar.
 *
 * @since 100.1.0
 */
@RunWith(AndroidJUnit4.class)
public final class ScalebarTest {
  private static final int ALPHA_50_PC = 0x80000000;

  private static final Scalebar.Style DEFAULT_STYLE = Scalebar.Style.ALTERNATING_BAR;

  private static final Scalebar.Alignment DEFAULT_ALIGNMENT = Scalebar.Alignment.LEFT;

  private static final UnitSystem DEFAULT_UNIT_SYSTEM = UnitSystem.METRIC;

  private static final int DEFAULT_FILL_COLOR = Color.LTGRAY | ALPHA_50_PC;

  private static final int DEFAULT_ALTERNATE_FILL_COLOR = Color.BLACK;

  private static final int DEFAULT_LINE_COLOR = Color.WHITE;

  private static final int DEFAULT_SHADOW_COLOR = Color.BLACK | ALPHA_50_PC;

  private static final int DEFAULT_TEXT_COLOR = Color.BLACK;

  private static final int DEFAULT_TEXT_SHADOW_COLOR = Color.WHITE;

  private static final Typeface DEFAULT_TYPEFACE = Typeface.DEFAULT_BOLD;

  private static final int DEFAULT_TEXT_SIZE_DP = 15;

  private static final int DEFAULT_BAR_HEIGHT_DP = 10;

  /**
   * Tests the default values set by the constructor that takes just a Context.
   *
   * @since 100.1.0
   */
  @Test
  public void testSimpleConstructorDefaultValues() {
    Scalebar scalebar = new Scalebar(InstrumentationRegistry.getTargetContext());
    checkDefaultValues(scalebar);
  }

  /**
   * Tests the constructor that takes an AttributeSet when the AttributeSet is null.
   *
   * @since 100.1.0
   */
  @Test
  public void testNullAttributeSet() {
    Scalebar scalebar = new Scalebar(InstrumentationRegistry.getTargetContext(), null);
    checkDefaultValues(scalebar);
  }

  /**
   * Tests the default values set from an XML file that doesn't set any of the Scalebar attributes.
   *
   * @since 100.1.0
   */
  @Test
  public void testXmlNoScalebarAttributes() {
    // Inflate layout containing a Scalebar that doesn't set any of the Scalebar attributes
    Context context = InstrumentationRegistry.getTargetContext();
    ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.unit_test_scalebar_no_attrs, null);

    // Find and instantiate that Scalebar
    Scalebar scalebar = viewGroup.findViewById(R.id.scalebar);

    // Check it contains the correct default attribute values
    checkDefaultValues(scalebar);
  }

  /**
   * Tests the values set from a fully-populated XML file.
   *
   * @since 100.1.0
   */
  @Test
  public void testXmlFullyPopulated() {
    // Inflate layout containing a Scalebar that sets all of the Scalebar attributes
    Context context = InstrumentationRegistry.getTargetContext();
    ViewGroup viewGroup =
        (ViewGroup) LayoutInflater.from(context).inflate(R.layout.unit_test_scalebar_fully_populated, null);

    // Find and instantiate that Scalebar
    Scalebar scalebar = viewGroup.findViewById(R.id.scalebar);

    // Check it contains the correct attribute values
    checkSetValues(scalebar, DEFAULT_TYPEFACE);
  }

  /**
   * Tests all the setter methods.
   *
   * @since 100.1.0
   */
  @Test
  public void testSetters() {
    // Instantiate a Scalebar
    Scalebar scalebar = new Scalebar(InstrumentationRegistry.getTargetContext());

    // Call all the setters
    scalebar.setStyle(Scalebar.Style.GRADUATED_LINE);
    scalebar.setAlignment(Scalebar.Alignment.CENTER);
    scalebar.setUnitSystem(UnitSystem.IMPERIAL);
    scalebar.setFillColor(Color.BLACK);
    scalebar.setAlternateFillColor(Color.RED);
    scalebar.setLineColor(0xFFC0C0C0);
    scalebar.setShadowColor(Color.WHITE);
    scalebar.setTextColor(Color.GREEN);
    scalebar.setTextShadowColor(Color.BLUE);
    scalebar.setTypeface(Typeface.SANS_SERIF);
    scalebar.setTextSize(20);
    scalebar.setBarHeight(12);

    // Check all the values that were set
    checkSetValues(scalebar, Typeface.SANS_SERIF);
  }

  /**
   * Tests IllegalArgumentExceptions from all methods that throw IllegalArgumentException.
   *
   * @since 100.1.0
   */
  @Test
  public void testIllegalArgumentExceptions() {
    // Instantiate a Scalebar
    Scalebar scalebar = new Scalebar(InstrumentationRegistry.getTargetContext());

    // Test addToMapView()
    try {
      scalebar.addToMapView(null);
      fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION);
    } catch (IllegalArgumentException e) {
      //success
    }

    // Test bindTo()
    try {
      scalebar.bindTo(null);
      fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION);
    } catch (IllegalArgumentException e) {
      //success
    }

    // Test the setters
    try {
      scalebar.setStyle(null);
      fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION);
    } catch (IllegalArgumentException e) {
      //success
    }
    try {
      scalebar.setAlignment(null);
      fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION);
    } catch (IllegalArgumentException e) {
      //success
    }
    try {
      scalebar.setUnitSystem(null);
      fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION);
    } catch (IllegalArgumentException e) {
      //success
    }
    try {
      scalebar.setTypeface(null);
      fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION);
    } catch (IllegalArgumentException e) {
      //success
    }
    try {
      scalebar.setTextSize(0);
      fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION);
    } catch (IllegalArgumentException e) {
      //success
    }
    try {
      scalebar.setBarHeight(0);
      fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION);
    } catch (IllegalArgumentException e) {
      //success
    }
  }

  /**
   * Tests addToMapView(), removeFromMapView() and bindTo().
   *
   * @since 100.1.0
   */
  @Test
  public void testAddRemoveAndBind() {
    // Must initialize this thread as a Looper so it can instantiate a MapView
    Looper.prepare();

    Context context = InstrumentationRegistry.getTargetContext();
    MapView mapView = new MapView(context);

    // Instantiate a Scalebar and add it to a MapView
    Scalebar scalebar = new Scalebar(context);
    scalebar.addToMapView(mapView);

    // Check addToMapView() fails when it's already added to a MapView
    try {
      scalebar.addToMapView(mapView);
      fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION);
    } catch (IllegalStateException e) {
      //success
    }

    // Remove it from the MapView and check addToMapView() can then be called again
    scalebar.removeFromMapView();
    scalebar.addToMapView(mapView);

    // Check bindTo() fails when it's already added to a MapView
    try {
      scalebar.bindTo(mapView);
      fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION);
    } catch (IllegalStateException e) {
      //success
    }

    // Remove it from the MapView and check bindTo() can then be called
    scalebar.removeFromMapView();
    scalebar.bindTo(mapView);

    // Check bindTo() is allowed when already bound
    scalebar.bindTo(mapView);

    // Check addToMapView() fails when it's already bound to a MapView
    try {
      scalebar.addToMapView(mapView);
      fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION);
    } catch (IllegalStateException e) {
      //success
    }

    // Check removeFromMapView() can be called when it's not added to a MapView
    scalebar.removeFromMapView();
  }

  /**
   * Checks that the given Scalebar object contains default values for all attributes.
   *
   * @param scalebar the Scalebar
   * @since 100.1.0
   */
  private void checkDefaultValues(Scalebar scalebar) {
    assertEquals(DEFAULT_STYLE, scalebar.getStyle());
    assertEquals(DEFAULT_ALIGNMENT, scalebar.getAlignment());
    assertEquals(DEFAULT_UNIT_SYSTEM, scalebar.getUnitSystem());
    assertEquals(DEFAULT_FILL_COLOR, scalebar.getFillColor());
    assertEquals(DEFAULT_ALTERNATE_FILL_COLOR, scalebar.getAlternateFillColor());
    assertEquals(DEFAULT_LINE_COLOR, scalebar.getLineColor());
    assertEquals(DEFAULT_SHADOW_COLOR, scalebar.getShadowColor());
    assertEquals(DEFAULT_TEXT_COLOR, scalebar.getTextColor());
    assertEquals(DEFAULT_TEXT_SHADOW_COLOR, scalebar.getTextShadowColor());
    assertEquals(DEFAULT_TYPEFACE, scalebar.getTypeface());
    assertEquals(DEFAULT_TEXT_SIZE_DP, scalebar.getTextSize());
    assertEquals(DEFAULT_BAR_HEIGHT_DP, scalebar.getBarHeight());
  }

  /**
   * Checks that the given Scalebar object contains values that have been set (by setter methods or from XML) for all
   * attributes.
   *
   * @param scalebar the Scalebar
   * @param typeface the expected Typeface
   * @since 100.1.0
   */
  private void checkSetValues(Scalebar scalebar, Typeface typeface) {
    assertEquals(Scalebar.Style.GRADUATED_LINE, scalebar.getStyle());
    assertEquals(Scalebar.Alignment.CENTER, scalebar.getAlignment());
    assertEquals(UnitSystem.IMPERIAL, scalebar.getUnitSystem());
    assertEquals(Color.BLACK, scalebar.getFillColor());
    assertEquals(Color.RED, scalebar.getAlternateFillColor());
    assertEquals(0xFFC0C0C0, scalebar.getLineColor());
    assertEquals(Color.WHITE, scalebar.getShadowColor());
    assertEquals(Color.GREEN, scalebar.getTextColor());
    assertEquals(Color.BLUE, scalebar.getTextShadowColor());
    assertTrue("Unexpected Typeface", typeface.equals(scalebar.getTypeface()));
    assertEquals(20, scalebar.getTextSize());
    assertEquals(12, scalebar.getBarHeight());
  }
}
