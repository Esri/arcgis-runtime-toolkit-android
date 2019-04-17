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

import kotlin.math.roundToInt

/**
 * Calculate an [Int] as an [Int] of pixels using the supplied [displayDensity] value.
 *
 * @since 100.5.0
 */
fun Int.toPixels(displayDensity: Float): Int = (this * displayDensity).roundToInt()

/**
 * Calculate a [Double] as an [Int] of pixels using the supplied [displayDensity] value.
 *
 * @since 100.5.0
 */
fun Double.toPixels(displayDensity: Float): Int = (this * displayDensity).roundToInt()

/**
 * Calculate an [Int] as a DP value using the supplied [displayDensity] value.
 *
 * @since 100.5.0
 */
fun Int.toDp(displayDensity: Float): Int = (this / displayDensity).roundToInt()

/**
 * Throw a [IllegalArgumentException] when the [Int] is not positive
 *
 * @since 100.5.0
 */
fun Int.throwIfNotPositive() {
    if (this <= 0) throw IllegalArgumentException("Parameter ${this::class.qualifiedName} must be > 0")
}
