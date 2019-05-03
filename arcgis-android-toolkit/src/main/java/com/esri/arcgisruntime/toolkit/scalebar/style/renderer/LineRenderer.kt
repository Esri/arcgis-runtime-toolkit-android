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

package com.esri.arcgisruntime.toolkit.scalebar.style.renderer

import android.graphics.Canvas
import android.graphics.Paint
import com.esri.arcgisruntime.UnitSystem
import com.esri.arcgisruntime.geometry.LinearUnit
import com.esri.arcgisruntime.toolkit.extension.asDistanceString
import com.esri.arcgisruntime.toolkit.scalebar.style.Style
import com.esri.arcgisruntime.toolkit.scalebar.style.Style.LINE

/**
 * Renders a [LINE] style scalebar.
 *
 * @see Style.LINE
 *
 * @since 100.5.0
 */
class LineRenderer : ScalebarRenderer() {

    override val isSegmented: Boolean = false

    override fun drawScalebar(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        distance: Double,
        displayUnits: LinearUnit,
        unitSystem: UnitSystem,
        lineWidthPx: Int,
        cornerRadiusPx: Int,
        textSizePx: Int,
        fillColor: Int,
        alternateFillColor: Int,
        shadowColor: Int,
        lineColor: Int,
        textPaint: Paint,
        displayDensity: Float
    ) {

        // Draw the line and its shadow, including the ticks at each end
        drawLineAndShadow(canvas, left, top, right, bottom, lineWidthPx, lineColor, shadowColor)

        // Draw the label, centered on the center of the line
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "${distance.asDistanceString()} ${displayUnits.abbreviation}",
            left + (right - left) / 2,
            bottom + textSizePx,
            textPaint
        )
    }

    override fun calculateExtraSpaceForUnits(displayUnits: LinearUnit?, textPaint: Paint): Float = 0f
}
