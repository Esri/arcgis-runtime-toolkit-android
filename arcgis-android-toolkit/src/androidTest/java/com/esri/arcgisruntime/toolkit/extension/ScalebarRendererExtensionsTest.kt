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
import com.esri.arcgisruntime.toolkit.scalebar.Multiplier
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

/**
 * Instrumented unit tests for ScalebarRendererExtensions
 *
 * @since 100.5.0
 */
@RunWith(AndroidJUnit4::class)
class ScalebarRendererExtensionsTest {

    /**
     * Array containing the multipliers that may be used for a scalebar and arrays of segment options appropriate for
     * each multiplier. From ScalebarRendererExtensions.
     *
     * @since 100.5.0
     */
    private val multiplierArray = arrayOf(
        Multiplier(1.0, intArrayOf(1, 2, 4, 5)),
        Multiplier(1.2, intArrayOf(1, 2, 3, 4)),
        Multiplier(1.5, intArrayOf(1, 2, 3, 5)),
        Multiplier(1.6, intArrayOf(1, 2, 4)),
        Multiplier(2.0, intArrayOf(1, 2, 4, 5)),
        Multiplier(2.4, intArrayOf(1, 2, 3, 4)),
        Multiplier(3.0, intArrayOf(1, 2, 3)),
        Multiplier(3.6, intArrayOf(1, 2, 3)),
        Multiplier(4.0, intArrayOf(1, 2, 4)),
        Multiplier(5.0, intArrayOf(1, 2, 5)),
        Multiplier(6.0, intArrayOf(1, 2, 3)),
        Multiplier(8.0, intArrayOf(1, 2, 4)),
        Multiplier(9.0, intArrayOf(1, 2, 3)),
        Multiplier(10.0, intArrayOf(1, 2, 5))
    )

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

    /**
     * Tests [calculateOptimalNumberOfSegments] using a range of distances stepped up randomly to 40000 in order to ensure
     * that the implementation in ScalebarRendererExtensions remains consistent with expected behaviours defined previously.
     * The data used, [multiplierArray], has been duplicated from ScalebarRendererExtensions.
     *
     * @since 100.5.0
     */
    @Test
    fun calculateOptimalNumberOfSegmentsTest() {
        for (distance in 1..40000 step Random.nextInt(1, 10)) {
            for (maxNoSegments in 1..5) {
                val magnitude = Math.pow(10.0, Math.floor(Math.log10(distance.toDouble())))
                val residualValue = distance / magnitude
                val multiplierData =
                    multiplierArray.sortedArrayWith(compareByDescending { it.multiplier }).first {
                        it.multiplier <= residualValue
                    }
                val segmentOption = multiplierData.segmentOptions.sortedArrayDescending().first {
                    it <= maxNoSegments
                }
                val result = calculateOptimalNumberOfSegments(distance.toDouble(), maxNoSegments)
                assertEquals(
                    "Expected segment option $segmentOption, got $result instead.\n" +
                            "Distance: $distance - Max Number of segments: $maxNoSegments - Magnitude: $magnitude - " +
                            "Residual Value: $residualValue - Multiplier: ${multiplierData.multiplier} - Result: $result\n",
                    segmentOption,
                    result
                )
            }
        }
    }
}
