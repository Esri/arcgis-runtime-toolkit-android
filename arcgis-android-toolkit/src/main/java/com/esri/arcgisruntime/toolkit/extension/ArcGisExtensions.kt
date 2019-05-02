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

/**
 * Return an instance of [UnitSystem] mapped by an int.
 *
 * @since 100.5.0
 */
fun unitSystemFromInt(value: Int): UnitSystem {
    return when (value) {
        0 -> UnitSystem.IMPERIAL
        else -> UnitSystem.METRIC
    }
}