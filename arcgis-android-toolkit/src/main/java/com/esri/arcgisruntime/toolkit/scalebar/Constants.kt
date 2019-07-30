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

import android.graphics.Color
import android.graphics.Typeface
import com.esri.arcgisruntime.UnitSystem
import com.esri.arcgisruntime.geometry.LinearUnit
import com.esri.arcgisruntime.geometry.LinearUnitId
import com.esri.arcgisruntime.toolkit.scalebar.style.Style

internal const val SHADOW_OFFSET_PIXELS = 2f
internal const val LABEL_X_PAD_DP = 6
internal const val SCALEBAR_X_PAD_DP = 10
internal val LINEAR_UNIT_METERS = LinearUnit(LinearUnitId.METERS)
internal val LINEAR_UNIT_FEET = LinearUnit(LinearUnitId.FEET)
private const val ALPHA_50_PC = -0x80000000
internal val DEFAULT_STYLE = Style.ALTERNATING_BAR
internal val DEFAULT_ALIGNMENT = Scalebar.Alignment.LEFT
internal const val DEFAULT_FILL_COLOR = Color.LTGRAY or ALPHA_50_PC
internal const val DEFAULT_ALTERNATE_FILL_COLOR = Color.BLACK
internal const val DEFAULT_LINE_COLOR = Color.WHITE
internal const val DEFAULT_SHADOW_COLOR = Color.BLACK or ALPHA_50_PC
internal const val DEFAULT_TEXT_COLOR = Color.BLACK
internal const val DEFAULT_TEXT_SHADOW_COLOR = Color.WHITE
internal val DEFAULT_TYPEFACE = Typeface.DEFAULT_BOLD
internal val DEFAULT_UNIT_SYSTEM = UnitSystem.METRIC
internal const val DEFAULT_BAR_HEIGHT_DP = 10
internal const val SCALEBAR_Y_PAD_DP = 10
