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
import com.esri.arcgisruntime.toolkit.extension.dpToPixels
import com.esri.arcgisruntime.toolkit.java.scalebar.ScalebarUtil
import com.esri.arcgisruntime.toolkit.scalebar.style.Style
import com.esri.arcgisruntime.toolkit.scalebar.style.Style.BAR

/**
 * Renders a [BAR] style scalebar.
 *
 * @see [Style.BAR]
 *
 * @since 100.2.1
 */
class BarRenderer : ScalebarRenderer() {

    override val isSegmented: Boolean = false

    override fun calculateExtraSpaceForUnits(displayUnits: LinearUnit?, textPaint: Paint): Float = 0f

    override fun drawScalebar(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        distance: Double,
        displayUnits: LinearUnit,
        unitSystem: UnitSystem,
        lineWidthDp: Int,
        cornerRadiusDp: Int,
        textSizeDp: Int,
        fillColor: Int,
        alternateFillColor: Int,
        shadowColor: Int,
        lineColor: Int,
        textPaint: Paint,
        displayDensity: Float
    ) {
        // Draw a solid bar and its shadow
        drawBarAndShadow(
            canvas,
            left,
            top,
            right,
            bottom,
            lineWidthDp,
            cornerRadiusDp,
            fillColor,
            shadowColor,
            displayDensity
        )

        // Draw a rectangle round the outside
        with(rectF) {
            set(left, top, right, bottom)
            paint.let { paint ->
                paint.reset()
                paint.color = lineColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = lineWidthDp.dpToPixels(displayDensity).toFloat()

                canvas.drawRoundRect(
                    this,
                    cornerRadiusDp.dpToPixels(displayDensity).toFloat(),
                    cornerRadiusDp.toFloat(),
                    paint
                )
            }
        }

        // Draw the label, centered on the center of the bar
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "${ScalebarUtil.labelString(distance)} ${displayUnits.abbreviation}",
            left + (right - left) / 2,
            bottom + textSizeDp.dpToPixels(displayDensity),
            textPaint
        )
    }
}
