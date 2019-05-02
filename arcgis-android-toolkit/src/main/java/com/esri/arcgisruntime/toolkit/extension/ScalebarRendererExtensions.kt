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

import com.esri.arcgisruntime.UnitSystem
import com.esri.arcgisruntime.geometry.LinearUnit
import com.esri.arcgisruntime.geometry.LinearUnitId
import com.esri.arcgisruntime.toolkit.scalebar.style.renderer.ScalebarRenderer

private val LINEAR_UNIT_METERS = LinearUnit(LinearUnitId.METERS)

private val LINEAR_UNIT_FEET = LinearUnit(LinearUnitId.FEET)

private val LINEAR_UNIT_KILOMETERS = LinearUnit(LinearUnitId.KILOMETERS)

private val LINEAR_UNIT_MILES = LinearUnit(LinearUnitId.MILES)

// Array containing the multipliers that may be used for a scalebar and arrays of segment options appropriate for each
// multiplier
private val MULTIPLIER_DATA_ARRAY = arrayOf(
    MultiplierData(1.0, intArrayOf(1, 2, 4, 5)),
    MultiplierData(1.2, intArrayOf(1, 2, 3, 4)),
    MultiplierData(1.5, intArrayOf(1, 2, 3, 5)),
    MultiplierData(1.6, intArrayOf(1, 2, 4)),
    MultiplierData(2.0, intArrayOf(1, 2, 4, 5)),
    MultiplierData(2.4, intArrayOf(1, 2, 3, 4)),
    MultiplierData(3.0, intArrayOf(1, 2, 3)),
    MultiplierData(3.6, intArrayOf(1, 2, 3)),
    MultiplierData(4.0, intArrayOf(1, 2, 4)),
    MultiplierData(5.0, intArrayOf(1, 2, 5)),
    MultiplierData(6.0, intArrayOf(1, 2, 3)),
    MultiplierData(8.0, intArrayOf(1, 2, 4)),
    MultiplierData(9.0, intArrayOf(1, 2, 3)),
    MultiplierData(10.0, intArrayOf(1, 2, 5))
)

fun ScalebarRenderer.calculateBestLength(maxLength: Double, unit: LinearUnit): Double {
    val magnitude = calculateMagnitude(maxLength)
    var multiplier = selectMultiplierData(maxLength, magnitude).multiplier

    // If the scalebar isn't segmented, force the multiplier to be an integer if it's > 2.0
    if (!isSegmented && multiplier > 2.0) {
        multiplier = Math.floor(multiplier)
    }
    var bestLength = multiplier * magnitude

    // If using imperial units, check if the number of feet is greater than the threshold for using feet; note this
    // isn't necessary for metric units because bestLength calculated using meters will also be a nice number of km
    if (unit.linearUnitId == LinearUnitId.FEET) {
        val displayUnits = selectLinearUnit(bestLength, UnitSystem.IMPERIAL)
        if (unit.linearUnitId != displayUnits.linearUnitId) {
            // 'unit' is feet but we're going to display in miles, so recalculate bestLength to give a nice number of miles
            bestLength = calculateBestLength(unit.convertTo(displayUnits, maxLength), displayUnits)
            // but convert that back to feet because the caller is using feet
            return displayUnits.convertTo(unit, bestLength)
        }
    }
    return bestLength
}

/**
 * Calculates the optimal number of segments in the scalebar when the distance represented by the whole scalebar has
 * a particular value. This is optimized so that the labels on the segments are all "nice" numbers.
 *
 * @param distance       the distance represented by the whole scalebar, that is the value to be displayed at the
 * end of the scalebar
 * @param maxNumSegments the maximum number of segments to avoid the labels of the segments overwriting each other
 * (this is passed in by the caller to allow this method to be platform independent)
 * @return the optimal number of segments in the scalebar
 * @since 100.2.1
 */
fun ScalebarRenderer.calculateOptimalNumberOfSegments(distance: Double, maxNumSegments: Int): Int {
    // Create an ordered array of options for the specified distance
    val options = segmentOptionsForDistance(distance)

    // Select the largest option that's <= maxNumSegments
    var ret = 1
    for (option in options) {
        if (option > maxNumSegments) {
            break
        }
        ret = option
    }
    return ret
}

/**
 * Selects the appropriate LinearUnit to use when the distance represented by the whole scalebar has a particular
 * value.
 *
 * @param distance   the distance represented by the whole scalebar, that is the value to be displayed at the end
 * of the scalebar; in feet if unitSystem is IMPERIAL or meters if unitSystem is METRIC
 * @param unitSystem the UnitSystem being used
 * @return the LinearUnit
 * @since 100.2.1
 */
fun ScalebarRenderer.selectLinearUnit(distance: Double, unitSystem: UnitSystem): LinearUnit {
    when (unitSystem) {
        UnitSystem.IMPERIAL -> {
            // use MILES if at least half a mile
            return if (distance >= 2640) {
                LINEAR_UNIT_MILES
            } else LINEAR_UNIT_FEET
        }

        UnitSystem.METRIC -> {
            // use KILOMETERS if at least one kilometer
            return if (distance >= 1000) {
                LINEAR_UNIT_KILOMETERS
            } else LINEAR_UNIT_METERS
        }
        else -> {
            return if (distance >= 1000) {
                LINEAR_UNIT_KILOMETERS
            } else LINEAR_UNIT_METERS
        }
    }
}

/**
 * Creates a string to display as a scalebar label corresponding to a given distance.
 *
 * @param distance the distance
 * @return the label string
 * @since 100.2.1
 */
fun ScalebarRenderer.labelString(distance: Double): String {
    // Format with 2 decimal places
    val label = String.format("%.2f", distance)

    // Strip off both decimal places if they're 0s
    if (label.endsWith(".00") || label.endsWith(",00")) {
        return label.substring(0, label.length - 3)
    }

    // Otherwise, strip off last decimal place if it's 0
    return if (label.endsWith("0")) {
        label.substring(0, label.length - 1)
    } else label
}

/**
 * Returns the segment options that are appropriate when a scalebar represents a given distance.
 *
 * @param distance the distance represented by the scalebar
 * @return the segment options; these are ints representing number of segments in the scalebar
 * @since 100.2.1
 */
private fun segmentOptionsForDistance(distance: Double): IntArray {
    return selectMultiplierData(distance, calculateMagnitude(distance)).segmentOptions
}

/**
 * Calculates the "magnitude" used when calculating the length of a scalebar or the number of segments. This is the
 * largest power of 10 that's less than or equal to a given distance.
 *
 * @param distance the distance represented by the scalebar
 * @return the magnitude, a power of 10
 * @since 100.2.1
 */
private fun calculateMagnitude(distance: Double): Double {
    return Math.pow(10.0, Math.floor(Math.log10(distance)))
}

/**
 * Selects the "multiplier" used when calculating the length of a scalebar or the number of segments in the
 * scalebar. This is chosen to give "nice" numbers for all the labels on the scalebar.
 *
 * @param distance  the distance represented by the scalebar
 * @param magnitude the "magnitude" used when calculating the length of a scalebar or the number of segments
 * @return a MultiplierData object containing the multiplier, which will give the scalebar length when multiplied by
 * the magnitude
 * @since 100.2.1
 */
private fun selectMultiplierData(distance: Double, magnitude: Double): MultiplierData {
    // Select the largest multiplier that's <= residual
    return MULTIPLIER_DATA_ARRAY.sortedArrayWith(compareByDescending { it.multiplier }).first {
        it.multiplier <= (distance / magnitude)
    }
}

/**
 * Container for a "multiplier" and the array of segment options appropriate for that multiplier. The multiplier is
 * used when calculating the length of a scalebar or the number of segments in the scalebar.
 *
 * @since 100.2.1
 */
private class MultiplierData
/**
 * Constructs a MultiplierData.
 *
 * @param multiplier     the multiplier
 * @param segmentOptions the array of segment options appropriate for the multiplier; these are ints representing
 * number of segments in the scalebar; it's important that they are in ascending order
 * @since 100.2.1
 */
    (
    /**
     * Gets the multiplier.
     *
     * @return the multiplier
     * @since 100.2.1
     */
    val multiplier: Double,
    /**
     * Gets the segment options.
     *
     * @return the segment options; these are ints representing number of segments in the scalebar
     * @since 100.2.1
     */
    val segmentOptions: IntArray
)