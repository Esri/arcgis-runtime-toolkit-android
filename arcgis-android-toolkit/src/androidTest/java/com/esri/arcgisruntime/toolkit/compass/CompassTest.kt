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

package com.esri.arcgisruntime.toolkit.compass

import android.os.Looper
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.toolkit.R
import com.esri.arcgisruntime.toolkit.TestUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompassTest {

    /**
     * Tests the default values set by the constructor that takes just a Context.
     *
     * @since 100.5.0
     */
    @Test
    fun testSimpleConstructorDefaultValues() {
        val compass = Compass(InstrumentationRegistry.getContext())
        attachCompassToParentAndMeasure(compass)
        checkDefaultValues(compass)
    }

    /**
     * Tests the default values set from an XML file that doesn't set any of the [Compass] attributes.
     *
     * @since 100.5.0
     */
    @Test
    fun testXmlNoCompassAttributes() {
        // Inflate layout containing a Compass that doesn't set any of the Compass attributes
        val context = InstrumentationRegistry.getTargetContext()
        val viewGroup = LayoutInflater.from(context).inflate(R.layout.unit_test_compass_no_attrs, null)

        // Find and instantiate that Compass
        val compass = viewGroup.findViewById<Compass>(R.id.compass)

        // Check it contains the correct default attribute values
        checkDefaultValues(compass)
    }

    /**
     * Tests the values set from a fully-populated XML file.
     *
     * @since 100.5.0
     */
    @Test
    fun testXmlFullyPopulated() {
        // Inflate layout containing a Compass that sets all of the Compass attributes
        val context = InstrumentationRegistry.getTargetContext()
        val viewGroup =
            LayoutInflater.from(context).inflate(R.layout.unit_test_compass_fully_populated, null)

        // Find and instantiate that Compass
        val compass = viewGroup.findViewById<Compass>(R.id.compass)

        // Check it contains the correct attribute values
        checkSetValues(compass)
    }

    /**
     * Tests all the setter methods.
     *
     * @since 100.5.0
     */
    @Test
    fun testSetters() {
        val compass = Compass(InstrumentationRegistry.getContext())
        attachCompassToParentAndMeasure(compass)

        // Call all the setters
        compass.isAutoHidden = false
        compass.setHeightDp(99)
        compass.setWidthDp(100)

        // Check all the values that were set
        checkSetValues(compass)
    }

    /**
     * Tests [IllegalArgumentException]s from all methods that throw [IllegalArgumentException].
     *
     * @since 100.5.0
     */
    @Test
    fun testIllegalArgumentExceptions() {
        val compass = Compass(InstrumentationRegistry.getContext())
        attachCompassToParentAndMeasure(compass)

        // Test the setters
        try {
            compass.setHeightDp(0)
            fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION)
        } catch (e: IllegalArgumentException) {
            //success
        }

        try {
            compass.setWidthDp(0)
            fail(TestUtil.MISSING_ILLEGAL_ARGUMENT_EXCEPTION)
        } catch (e: IllegalArgumentException) {
            //success
        }
    }

    /**
     * Tests [Compass.addToGeoView], [Compass.removeFromGeoView] and [Compass.bindTo].
     *
     * @since 100.5.0
     */
    @Test
    fun testAddRemoveAndBind() {
        // Checking if Looper has been prepared, if not, prepare it as we must initialize this thread as a Looper
        // so it can instantiate a GeoView
        if (Looper.myLooper() == null) {
            Looper.prepare()
        }

        val context = InstrumentationRegistry.getContext()
        val mapView = MapView(context)

        // Instantiate a Compass and add it to a GeoView (Workflow 1)
        val compass = Compass(context)
        compass.addToGeoView(mapView)

        // Check addToGeoView() fails when it's already added to a GeoView
        try {
            compass.addToGeoView(mapView)
            fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION)
        } catch (e: IllegalStateException) {
            //success
        }

        // Remove it from the GeoView and check addToGeoView() can then be called again
        compass.removeFromGeoView()
        compass.addToGeoView(mapView)

        // Check bindTo() fails when it's already added to a GeoView
        try {
            compass.bindTo(mapView)
            fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION)
        } catch (e: IllegalStateException) {
            //success
        }

        // Remove it from the GeoView
        compass.removeFromGeoView()

        // Call removeFromGeoView() again and check it fails because it's not currently added to a GeoView
        try {
            compass.removeFromGeoView()
            fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION)
        } catch (e: IllegalStateException) {
            //success
        }

        // Call bindTo() to bind it to a GeoView (Workflow 2)
        compass.bindTo(mapView)

        // Check bindTo() is allowed when already bound
        compass.bindTo(mapView)

        // Check removeFromGeoView() fails when it's bound to a GeoView, because removeFromGeoView() isn't applicable to
        // Workflow 2
        try {
            compass.removeFromGeoView()
            fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION)
        } catch (e: IllegalStateException) {
            //success
        }

        // Check addToGeoView() fails when it's bound to a GeoView
        try {
            compass.addToGeoView(mapView)
            fail(TestUtil.MISSING_ILLEGAL_STATE_EXCEPTION)
        } catch (e: IllegalStateException) {
            //success
        }

        // Call bindTo(null) to unbind it and check addToGeoView() can then be called
        compass.bindTo(null)
        compass.addToGeoView(mapView)

        // Remove it from the GeoView and check bindTo(null) can be called even when it's not bound
        compass.removeFromGeoView()
        compass.bindTo(null)
    }

    @Test
    fun testIllegalStateExceptionThrownWhenViewHasNotBeenMeasured() {
        val compass = Compass(InstrumentationRegistry.getContext())

        try {
            compass.setWidthDp(Compass.DEFAULT_HEIGHT_AND_WIDTH_DP)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // success
        }

        try {
            compass.setHeightDp(Compass.DEFAULT_HEIGHT_AND_WIDTH_DP)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // success
        }
    }

    /**
     * Checks that the given [Compass] object contains default values for all attributes.
     *
     * @param compass the [Compass]
     * @since 100.5.0
     */
    private fun checkDefaultValues(compass: Compass) {
        assertTrue("Expected isAutoHide() to return true", compass.isAutoHidden)
        assertEquals(Compass.DEFAULT_HEIGHT_AND_WIDTH_DP, compass.getHeightDp())
        assertEquals(Compass.DEFAULT_HEIGHT_AND_WIDTH_DP, compass.getWidthDp())
    }

    /**
     * Checks that the given [Compass] object contains values that have been set (by setter methods or from XML) for all
     * attributes.
     *
     * @param compass the [Compass]
     * @since 100.5.0
     */
    private fun checkSetValues(compass: Compass) {
        assertFalse("Expected isAutoHide() to return false", compass.isAutoHidden)
        assertEquals(99, compass.getHeightDp())
        assertEquals(100, compass.getWidthDp())
    }

    /**
     * Attaches supplied [Compass] to a parent View and measures it forces a measure inside the parent
     *
     * @param compass [Compass] to measure
     */
    private fun attachCompassToParentAndMeasure(compass: Compass) {
        // Creating parent View to hold Compass
        val parentView = FrameLayout(InstrumentationRegistry.getContext())
        // Setting width and height of parent View to double that of Compass to ensure that the parent's size doesn't
        // contribute to the size of the Compass View
        parentView.layout(
            0,
            0,
            Compass.DEFAULT_HEIGHT_AND_WIDTH_DP * 2,
            Compass.DEFAULT_HEIGHT_AND_WIDTH_DP * 2
        )

        // Add the Compass to the parent View
        parentView.addView(compass)
        // Perform measure on Compass View to determine the measured width and height
        compass.measure(parentView.measuredWidthAndState, parentView.measuredHeightAndState)
    }

}