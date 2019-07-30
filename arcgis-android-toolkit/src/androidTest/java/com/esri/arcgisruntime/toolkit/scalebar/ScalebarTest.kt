/*
 * Copyright 2019 Esri
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

package com.esri.arcgisruntime.toolkit.scalebar

import android.graphics.Color
import android.graphics.Typeface
import android.os.Looper
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.view.LayoutInflater
import android.view.ViewGroup
import com.esri.arcgisruntime.UnitSystem
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.toolkit.MISSING_ILLEGAL_STATE_EXCEPTION
import com.esri.arcgisruntime.toolkit.scalebar.style.Style
import com.esri.arcgisruntime.toolkit.test.R
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import junit.framework.TestCase.fail
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented unit tests for [Scalebar]
 *
 * @since 100.5.0
 */
@RunWith(AndroidJUnit4::class)
class ScalebarTest {

    /**
     * Tests the default values set by the constructor that takes just a Context.
     *
     * @since 100.5.0
     */
    @Test
    fun testSimpleConstructorDefaultValues() {
        val scalebar = Scalebar(InstrumentationRegistry.getTargetContext())
        checkDefaultValues(scalebar)
    }

    /**
     * Tests the constructor that takes an AttributeSet when the AttributeSet is null.
     *
     * @since 100.5.0
     */
    @Test
    fun testNullAttributeSet() {
        val scalebar = Scalebar(InstrumentationRegistry.getTargetContext(), null)
        checkDefaultValues(scalebar)
    }

    /**
     * Tests the default values set from an XML file that doesn't set any of the Scalebar attributes.
     *
     * @since 100.5.0
     */
    @Test
    fun testXmlNoScalebarAttributes() {
        // Inflate layout containing a Scalebar that doesn't set any of the Scalebar attributes
        val context = InstrumentationRegistry.getTargetContext()
        val viewGroup = LayoutInflater.from(context).inflate(R.layout.unit_test_scalebar_no_attrs, null) as ViewGroup

        // Find and instantiate that Scalebar
        val scalebar = viewGroup.findViewById<Scalebar>(R.id.scalebar)

        // Check it contains the correct default attribute values
        checkDefaultValues(scalebar)
    }

    /**
     * Tests the values set from a fully-populated XML file.
     *
     * @since 100.5.0
     */
    @Test
    fun testXmlFullyPopulated() {
        // Inflate layout containing a Scalebar that sets all of the Scalebar attributes
        val context = InstrumentationRegistry.getTargetContext()
        val viewGroup =
            LayoutInflater.from(context).inflate(R.layout.unit_test_scalebar_fully_populated, null) as ViewGroup

        // Find and instantiate that Scalebar
        val scalebar = viewGroup.findViewById<Scalebar>(R.id.scalebar)

        // Check it contains the correct attribute values
        checkSetValues(scalebar, DEFAULT_TYPEFACE)
    }

    /**
     * Tests all the properties.
     *
     * @since 100.5.0
     */
    @Test
    fun testSetters() {
        // Instantiate a Scalebar
        val scalebar = Scalebar(InstrumentationRegistry.getTargetContext())

        // Call all the setters
        scalebar.style = Style.GRADUATED_LINE
        scalebar.alignment = Scalebar.Alignment.CENTER
        scalebar.unitSystem = UnitSystem.IMPERIAL
        scalebar.fillColor = Color.BLACK
        scalebar.alternateFillColor = Color.RED
        scalebar.lineColor = -0x3f3f40
        scalebar.shadowColor = Color.WHITE
        scalebar.textColor = Color.GREEN
        scalebar.textShadowColor = Color.BLUE
        scalebar.typeface = Typeface.SANS_SERIF
        scalebar.textSize = scalebar.resources.getDimensionPixelSize(R.dimen.scalebar_test_text_size)

        // Check all the values that were set
        checkSetValues(scalebar, Typeface.SANS_SERIF)
    }

    /**
     * Tests [Scalebar.addToMapView], [Scalebar.removeFromMapView] and [Scalebar.bindTo].
     *
     * @since 100.5.0
     */
    @Test
    fun testAddRemoveAndBind() {
        // Checks whether a Looper already exists for the current thread, if not, it creates one
        if (Looper.myLooper() == null) {
            // Must initialize this thread as a Looper so it can instantiate a MapView
            Looper.prepare()
        }

        val context = InstrumentationRegistry.getTargetContext()
        val mapView = MapView(context)

        // Instantiate a Scalebar and add it to a MapView (Workflow 1)
        val scalebar = Scalebar(context)
        scalebar.addToMapView(mapView)

        // Check addToMapView() fails when it's already added to a MapView
        try {
            scalebar.addToMapView(mapView)
            fail(MISSING_ILLEGAL_STATE_EXCEPTION)
        } catch (e: IllegalStateException) {
            //success
        }

        // Remove it from the MapView and check addToMapView() can then be called again
        scalebar.removeFromMapView()
        scalebar.addToMapView(mapView)

        // Check bindTo() fails when it's already added to a MapView
        try {
            scalebar.bindTo(mapView)
            fail(MISSING_ILLEGAL_STATE_EXCEPTION)
        } catch (e: IllegalStateException) {
            //success
        }

        // Remove it from the MapView
        scalebar.removeFromMapView()

        // Call removeFromMapView() again and check it fails because it's not currently added to a MapView
        try {
            scalebar.removeFromMapView()
            fail(MISSING_ILLEGAL_STATE_EXCEPTION)
        } catch (e: IllegalStateException) {
            //success
        }

        // Call bindTo() to bind it to a MapView (Workflow 2)
        scalebar.bindTo(mapView)

        // Check bindTo() is allowed when already bound
        scalebar.bindTo(mapView)

        // Check removeFromMapView() fails when it's bound to a MapView, because removeFromGeoView() isn't applicable to
        // Workflow 2
        try {
            scalebar.removeFromMapView()
            fail(MISSING_ILLEGAL_STATE_EXCEPTION)
        } catch (e: IllegalStateException) {
            //success
        }

        // Check addToMapView() fails when it's bound to a MapView
        try {
            scalebar.addToMapView(mapView)
            fail(MISSING_ILLEGAL_STATE_EXCEPTION)
        } catch (e: IllegalStateException) {
            //success
        }

        // Call bindTo(null) to unbind it and check addToMapView() can then be called
        scalebar.bindTo(null)
        scalebar.addToMapView(mapView)

        // Remove it from the MapView and check bindTo(null) can be called even when it's not bound
        scalebar.removeFromMapView()
        scalebar.bindTo(null)
    }

    /**
     * Checks that the given Scalebar object contains default values for all attributes.
     *
     * @param scalebar the Scalebar
     * @since 100.5.0
     */
    private fun checkDefaultValues(scalebar: Scalebar) {
        assertEquals(DEFAULT_STYLE, scalebar.style)
        assertEquals(DEFAULT_ALIGNMENT, scalebar.alignment)
        assertEquals(DEFAULT_UNIT_SYSTEM, scalebar.unitSystem)
        assertEquals(DEFAULT_FILL_COLOR, scalebar.fillColor)
        assertEquals(DEFAULT_ALTERNATE_FILL_COLOR, scalebar.alternateFillColor)
        assertEquals(DEFAULT_LINE_COLOR, scalebar.lineColor)
        assertEquals(DEFAULT_SHADOW_COLOR, scalebar.shadowColor)
        assertEquals(DEFAULT_TEXT_COLOR, scalebar.textColor)
        assertEquals(DEFAULT_TEXT_SHADOW_COLOR, scalebar.textShadowColor)
        assertEquals(DEFAULT_TYPEFACE, scalebar.typeface)
        assertEquals(scalebar.resources.getDimensionPixelSize(R.dimen.scalebar_default_text_size), scalebar.textSize)
    }

    /**
     * Checks that the provided [scalebar] object contains values that have been set (by setter methods or from XML) for all
     * attributes. We provide a [typeface] to test as we cannot set the typeface via XML.
     *
     * @param scalebar the Scalebar
     * @param typeface the expected Typeface
     * @since 100.5.0
     */
    private fun checkSetValues(scalebar: Scalebar, typeface: Typeface) {
        assertEquals(Style.GRADUATED_LINE, scalebar.style)
        assertEquals(Scalebar.Alignment.CENTER, scalebar.alignment)
        assertEquals(UnitSystem.IMPERIAL, scalebar.unitSystem)
        assertEquals(Color.BLACK, scalebar.fillColor)
        assertEquals(Color.RED, scalebar.alternateFillColor)
        assertEquals(-0x3f3f40, scalebar.lineColor)
        assertEquals(Color.WHITE, scalebar.shadowColor)
        assertEquals(Color.GREEN, scalebar.textColor)
        assertEquals(Color.BLUE, scalebar.textShadowColor)
        assertTrue("Unexpected Typeface", typeface == scalebar.typeface)
        assertEquals(scalebar.resources.getDimensionPixelSize(R.dimen.scalebar_test_text_size), scalebar.textSize)
    }

}
