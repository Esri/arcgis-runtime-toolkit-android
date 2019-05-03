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

package com.esri.arcgisruntime.toolkit.extension

import android.support.test.runner.AndroidJUnit4
import com.esri.arcgisruntime.UnitSystem
import com.esri.arcgisruntime.geometry.LinearUnit
import com.esri.arcgisruntime.geometry.LinearUnitId
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented unit tests for ScalebarRendererExtensions
 *
 * @since 100.5.0
 */
@RunWith(AndroidJUnit4::class)
class ScalebarRendererExtensionsTest {

    /**
     * Tests that [selectLinearUnit] returns an instance of [LinearUnit] with an id of [LinearUnitId.FEET] when the
     * provided distance is equal to 0 and the provided [UnitSystem] is equal to [UnitSystem.IMPERIAL].
     *
     * @since 100.5.0
     */
    @Test
    fun selectLinearUnitDistanceIsZeroUnitSystemImperialSelectsLinearUnitFeet() {
        assertEquals(LINEAR_UNIT_FEET.linearUnitId, selectLinearUnit(0.0, UnitSystem.IMPERIAL).linearUnitId)
    }

    /**
     * Tests that [selectLinearUnit] returns an instance of [LinearUnit] with an id of [LinearUnitId.FEET] when the
     * provided distance is less than half a mile and the provided [UnitSystem] is equal to [UnitSystem.IMPERIAL].
     *
     * @since 100.5.0
     */
    @Test
    fun selectLinearUnitDistanceLessThanHalfMileUnitSystemImperialSelectsLinearUnitFeet() {
        assertEquals(
            LINEAR_UNIT_FEET.linearUnitId,
            selectLinearUnit((HALF_MILE_FEET - 1).toDouble(), UnitSystem.IMPERIAL).linearUnitId
        )
    }

    /**
     * Tests that [selectLinearUnit] returns an instance of [LinearUnit] with an id of [LinearUnitId.MILES] when the
     * provided distance is equal to half a mile and the provided [UnitSystem] is equal to [UnitSystem.IMPERIAL].
     *
     * @since 100.5.0
     */
    @Test
    fun selectLinearUnitDistanceEqualToHalfMileUnitSystemImperialSelectsLinearUnitMiles() {
        assertEquals(
            LINEAR_UNIT_MILES.linearUnitId,
            selectLinearUnit(HALF_MILE_FEET.toDouble(), UnitSystem.IMPERIAL).linearUnitId
        )
    }

    /**
     * Tests that [selectLinearUnit] returns an instance of [LinearUnit] with an id of [LinearUnitId.MILES] when the
     * provided distance is greater than half a mile and the provided [UnitSystem] is equal to [UnitSystem.IMPERIAL].
     *
     * @since 100.5.0
     */
    @Test
    fun selectLinearUnitDistanceGreaterThanHalfMileUnitSystemImperialSelectsLinearUnitMiles() {
        assertEquals(
            LINEAR_UNIT_MILES.linearUnitId,
            selectLinearUnit((HALF_MILE_FEET + 1).toDouble(), UnitSystem.IMPERIAL).linearUnitId
        )
    }

    /**
     * Tests that [selectLinearUnit] returns an instance of [LinearUnit] with an id of [LinearUnitId.METERS] when the
     * provided distance is equal to 0 and the provided [UnitSystem] is equal to [UnitSystem.METRIC].
     *
     * @since 100.5.0
     */
    @Test
    fun selectLinearUnitDistanceIsZeroUnitSystemMetricSelectsLinearUnitMeters() {
        assertEquals(LINEAR_UNIT_METERS.linearUnitId, selectLinearUnit(0.0, UnitSystem.METRIC).linearUnitId)
    }

    /**
     * Tests that [selectLinearUnit] returns an instance of [LinearUnit] with an id of [LinearUnitId.METERS] when the
     * provided distance is less than 1 kilometer and the provided [UnitSystem] is equal to [UnitSystem.METRIC].
     *
     * @since 100.5.0
     */
    @Test
    fun selectLinearUnitDistanceLessThanOneKilometerUnitSystemImperialSelectsLinearUnitMeters() {
        assertEquals(
            LINEAR_UNIT_METERS.linearUnitId,
            selectLinearUnit((KILOMETER_METERS - 1).toDouble(), UnitSystem.METRIC).linearUnitId
        )
    }

    /**
     * Tests that [selectLinearUnit] returns an instance of [LinearUnit] with an id of [LinearUnitId.KILOMETERS] when the
     * provided distance is equal to 1 kilometer and the provided [UnitSystem] is equal to [UnitSystem.METRIC].
     *
     * @since 100.5.0
     */
    @Test
    fun selectLinearUnitDistanceEqualToOneKilometerUnitSystemImperialSelectsLinearUnitKilometers() {
        assertEquals(
            LINEAR_UNIT_KILOMETERS.linearUnitId,
            selectLinearUnit(KILOMETER_METERS.toDouble(), UnitSystem.METRIC).linearUnitId
        )
    }

    /**
     * Tests that [selectLinearUnit] returns an instance of [LinearUnit] with an id of [LinearUnitId.KILOMETERS] when the
     * provided distance is greater than 1 kilometer and the provided [UnitSystem] is equal to [UnitSystem.METRIC].
     *
     * @since 100.5.0
     */
    @Test
    fun selectLinearUnitDistanceGreaterThanOneKilometerUnitSystemImperialSelectsLinearUnitKilometers() {
        assertEquals(
            LINEAR_UNIT_KILOMETERS.linearUnitId,
            selectLinearUnit((KILOMETER_METERS + 1).toDouble(), UnitSystem.METRIC).linearUnitId
        )
    }
}
