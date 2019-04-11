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

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.widget.FrameLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

        val compass = Compass(InstrumentationRegistry.getContext())
        // Add the Compass to the parent View
        parentView.addView(compass)
        // Perform measure on Compass View to determine the measured width and height
        compass.measure(parentView.measuredWidthAndState, parentView.measuredHeightAndState)
        checkDefaultValues(compass)
    }

    /**
     * Checks that the given Compass object contains default values for all attributes.
     *
     * @param compass the Compass
     * @since 100.2.1
     */
    private fun checkDefaultValues(compass: Compass) {
        assertTrue("Expected isAutoHide() to return true", compass.isAutoHidden)
        assertEquals(Compass.DEFAULT_HEIGHT_AND_WIDTH_DP, compass.getHeightDp())
        assertEquals(Compass.DEFAULT_HEIGHT_AND_WIDTH_DP, compass.getWidthDp())
    }

}