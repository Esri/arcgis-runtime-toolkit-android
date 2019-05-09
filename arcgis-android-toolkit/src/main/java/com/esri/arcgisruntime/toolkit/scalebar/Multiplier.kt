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

import android.support.annotation.VisibleForTesting

/**
 * Container for a "multiplier" and the array of segment options appropriate for that multiplier. The multiplier is
 * used when calculating the length of a scalebar or the number of segments in the scalebar.
 *
 * @since 100.5.0
 */
internal class Multiplier
/**
 * @constructor
 * @since 100.5.0
 */
    (
    /**
     * Used when calculating the length of a scalebar or the number of segments in the scalebar.
     *
     * @since 100.5.0
     */
    val multiplier: Double,

    /**
     * The array of segment options appropriate for that multiplier.
     *
     * @since 100.5.0
     */
    val segmentOptions: IntArray
)
