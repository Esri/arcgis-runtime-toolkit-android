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

import com.esri.arcgisruntime.geometry.LinearUnit
import com.esri.arcgisruntime.geometry.LinearUnitId

const val SHADOW_OFFSET_PIXELS = 2f
const val LABEL_X_PAD_DP = 6
const val SCALEBAR_X_PAD_DP = 10
val LINEAR_UNIT_METERS = LinearUnit(LinearUnitId.METERS)
val LINEAR_UNIT_FEET = LinearUnit(LinearUnitId.FEET)