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
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.UnitSystem
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented unit tests for ScalebarRendererExtensions
 *
 * @since 100.5.0
 */
@RunWith(AndroidJUnit4::class)
class ScalebarRendererExtensionsTest {

    @Test
    fun selectLinearUnitDistanceIsZeroUnitSystemImperialSelectsLinearUnitFeet() {
        assertEquals(LINEAR_UNIT_FEET.linearUnitId, selectLinearUnit(0.0, UnitSystem.IMPERIAL).linearUnitId)
    }

    @Test
    fun selectLinearUnitDistanceLessThanHalfMileUnitSystemImperialSelectsLinearUnitFeet() {
        assertEquals(
            LINEAR_UNIT_FEET.linearUnitId,
            selectLinearUnit((HALF_MILE_FEET - 1).toDouble(), UnitSystem.IMPERIAL).linearUnitId
        )
    }

    @Test
    fun selectLinearUnitDistanceEqualToHalfMileUnitSystemImperialSelectsLinearUnitMiles() {
        assertEquals(
            LINEAR_UNIT_MILES.linearUnitId,
            selectLinearUnit(HALF_MILE_FEET.toDouble(), UnitSystem.IMPERIAL).linearUnitId
        )
    }

    @Test
    fun selectLinearUnitDistanceGreaterThanHalfMileUnitSystemImperialSelectsLinearUnitMiles() {
        assertEquals(
            LINEAR_UNIT_MILES.linearUnitId,
            selectLinearUnit((HALF_MILE_FEET + 1).toDouble(), UnitSystem.IMPERIAL).linearUnitId
        )
    }

    @Test
    fun selectLinearUnitDistanceIsZeroUnitSystemMetricSelectsLinearUnitMeters() {
        assertEquals(LINEAR_UNIT_METERS.linearUnitId, selectLinearUnit(0.0, UnitSystem.METRIC).linearUnitId)
    }

    @Test
    fun selectLinearUnitDistanceLessThanOneKilometerUnitSystemImperialSelectsLinearUnitMeters() {
        assertEquals(
            LINEAR_UNIT_METERS.linearUnitId,
            selectLinearUnit((KILOMETER_METERS - 1).toDouble(), UnitSystem.METRIC).linearUnitId
        )
    }

    @Test
    fun selectLinearUnitDistanceEqualToOneKilometerUnitSystemImperialSelectsLinearUnitKilometers() {
        assertEquals(
            LINEAR_UNIT_KILOMETERS.linearUnitId,
            selectLinearUnit(KILOMETER_METERS.toDouble(), UnitSystem.METRIC).linearUnitId
        )
    }

    @Test
    fun selectLinearUnitDistanceGreaterThanOneKilometerUnitSystemImperialSelectsLinearUnitKilometers() {
        assertEquals(
            LINEAR_UNIT_KILOMETERS.linearUnitId,
            selectLinearUnit((KILOMETER_METERS + 1).toDouble(), UnitSystem.METRIC).linearUnitId
        )
    }

}