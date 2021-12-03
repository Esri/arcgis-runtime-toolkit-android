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

import java.util.Locale
import kotlin.math.roundToInt

/**
 * Calculate an [Int] DP value as an [Int] of pixels using the supplied [displayDensity] value.
 *
 * @since 100.5.0
 */
fun Int.dpToPixels(displayDensity: Float): Int = (this * displayDensity).roundToInt()

/**
 * Calculate a [Double] DP value as an [Int] of pixels using the supplied [displayDensity] value.
 *
 * @since 100.5.0
 */
fun Double.dpToPixels(displayDensity: Float): Int = (this * displayDensity).roundToInt()

/**
 * Calculate an [Int] of pixels as a DP value using the supplied [displayDensity] value.
 *
 * @since 100.5.0
 */
fun Int.pixelsToDp(displayDensity: Float): Int = (this / displayDensity).roundToInt()

/**
 * Calculate an [Int] of pixels as an SP value using the supplied [scaledDensity] value.
 *
 * @since 100.13.0
 */
fun Int.pixelsToSp(scaledDensity: Float): Int = (this / scaledDensity).roundToInt()

/**
 * Throw a [IllegalArgumentException] when the [Int] is not positive
 *
 * @since 100.5.0
 */
fun Int.throwIfNotPositive(parameterName: String) {
    if (this <= 0) throw IllegalArgumentException("Parameter $parameterName must be > 0")
}

/**
 * Formats a Double as a String to display as a distance.
 *
 * @since 100.5.0
 */
fun Double.asDistanceString(): String {
    // Format with 2 decimal places
    return String.format(Locale.ROOT, "%.2f", this).let {
        // Strip off both decimal places if they're 0s
        if (it.endsWith(".00") || it.endsWith(",00")) {
            it.substring(0, it.length - 3)
            // Otherwise, strip off last decimal place if it's 0
        } else if (it != "0" && it.endsWith("0")) {
            it.substring(0, it.length - 1)
        } else {
            it
        }
    }
}
