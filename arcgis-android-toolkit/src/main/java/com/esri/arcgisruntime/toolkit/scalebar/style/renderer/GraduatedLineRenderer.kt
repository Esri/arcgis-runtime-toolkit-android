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
import com.esri.arcgisruntime.toolkit.scalebar.SHADOW_OFFSET_PIXELS
import com.esri.arcgisruntime.toolkit.scalebar.style.Style

/**
 * Renders a GRADUATED_LINE style scalebar.
 *
 * @see Style.GRADUATED_LINE
 *
 * @since 100.2.1
 */
class GraduatedLineRenderer : ScalebarRenderer() {

    override val isSegmented: Boolean = true

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
        // Calculate the number of segments in the line
        val lineDisplayLength = right - left
        val numSegments = calculateNumberOfSegments(distance, lineDisplayLength.toDouble(), displayDensity, textPaint)
        val segmentDisplayLength = lineDisplayLength / numSegments

        // Create Paint for drawing the ticks
        paint.reset()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = lineWidthDp.dpToPixels(displayDensity).toFloat()
        paint.strokeCap = Paint.Cap.ROUND

        // Draw a tick, its shadow and a label at each segment boundary
        var xPos = left + segmentDisplayLength
        val yPos = top + (bottom - top) / 4 // segment ticks are 3/4 the height of the ticks at the start and end
        val segmentDistance = distance / numSegments
        val yPosText = bottom + textSizeDp.dpToPixels(displayDensity)
        textPaint.textAlign = Paint.Align.CENTER
        for (segNo in 1 until numSegments) {
            // Draw the shadow, offset slightly from where the tick is drawn below
            paint.color = shadowColor
            canvas.drawLine(
                xPos + SHADOW_OFFSET_PIXELS, yPos + SHADOW_OFFSET_PIXELS,
                xPos + SHADOW_OFFSET_PIXELS, bottom + SHADOW_OFFSET_PIXELS, paint
            )

            // Draw the line on top of the shadow
            paint.color = lineColor
            canvas.drawLine(xPos, yPos, xPos, bottom, paint)

            // Draw the label
            canvas.drawText(ScalebarUtil.labelString(segmentDistance * segNo), xPos, yPosText, textPaint)
            xPos += segmentDisplayLength
        }

        // Draw the line and its shadow, including the ticks at each end
        drawLineAndShadow(canvas, left, top, right, bottom, lineWidthDp, lineColor, shadowColor)

        // Draw a label at the start of the line
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("0", left, yPosText, textPaint)

        // Draw a label at the end of the line
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(ScalebarUtil.labelString(distance), right, yPosText, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(' ' + displayUnits.abbreviation, right, yPosText, textPaint)
    }

    override fun calculateExtraSpaceForUnits(displayUnits: LinearUnit?, textPaint: Paint): Float {
        return calculateWidthOfUnitsString(displayUnits, textPaint)
    }

}
