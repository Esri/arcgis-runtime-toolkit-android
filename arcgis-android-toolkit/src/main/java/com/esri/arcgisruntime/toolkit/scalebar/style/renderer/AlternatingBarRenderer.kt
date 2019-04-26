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
import com.esri.arcgisruntime.toolkit.scalebar.style.Style.ALTERNATING_BAR

/**
 * Renders an [ALTERNATING_BAR] style scalebar.
 *
 * @see Style.ALTERNATING_BAR
 *
 * @since 100.5.0
 */
class AlternatingBarRenderer : ScalebarRenderer() {

    override val isSegmented: Boolean = true

    override fun calculateExtraSpaceForUnits(displayUnits: LinearUnit?, textPaint: Paint): Float =
        calculateWidthOfUnitsString(displayUnits, textPaint)

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
        textSizePx: Float,
        fillColor: Int,
        alternateFillColor: Int,
        shadowColor: Int,
        lineColor: Int,
        textPaint: Paint,
        displayDensity: Float
    ) {

        // Draw a solid bar, using alternateFillColor, and its shadow
        drawBarAndShadow(
            canvas,
            left,
            top,
            right,
            bottom,
            lineWidthDp,
            cornerRadiusDp,
            alternateFillColor,
            shadowColor,
            displayDensity
        )

        // Calculate the number of segments in the bar
        (right - left).let { barDisplayLength ->
            calculateNumberOfSegments(
                distance,
                barDisplayLength.toDouble(),
                displayDensity,
                textPaint
            ).let { numSegments ->
                with(barDisplayLength / numSegments) {

                    // Now draw every second segment on top of it using fillColor
                    paint.let { paint ->
                        paint.reset()
                        paint.style = Paint.Style.FILL
                        paint.color = fillColor

                        rectF.let { rectF ->
                            var xPos = left + this
                            var i = 1
                            while (i < numSegments) {
                                rectF.set(xPos, top, xPos + this, bottom)
                                canvas.drawRect(rectF, paint)
                                xPos += 2 * this
                                i += 2
                            }
                        }
                    }

                    // Draw a line round the outside of the complete bar
                    rectF.let { rectF ->
                        rectF.set(left, top, right, bottom)
                        paint.let { paint ->
                            paint.reset()
                            paint.color = lineColor
                            paint.style = Paint.Style.STROKE
                            paint.strokeWidth = lineWidthDp.dpToPixels(displayDensity).toFloat()
                            canvas.drawRoundRect(
                                rectF,
                                cornerRadiusDp.toFloat(),
                                cornerRadiusDp.toFloat(),
                                paint
                            )
                        }
                    }

                    // Draw a label at the start of the bar
                    val yPosText = bottom + textSizePx
                    textPaint.textAlign = Paint.Align.LEFT
                    canvas.drawText("0", left, yPosText, textPaint)

                    // Draw a label at the end of the bar
                    textPaint.textAlign = Paint.Align.RIGHT
                    canvas.drawText(ScalebarUtil.labelString(distance), right, yPosText, textPaint)
                    textPaint.textAlign = Paint.Align.LEFT
                    canvas.drawText(' ' + displayUnits.abbreviation, right, yPosText, textPaint)

                    // Draw a vertical line and a label at each segment boundary
                    var xPos = left + this
                    val segmentDistance = distance / numSegments
                    textPaint.textAlign = Paint.Align.CENTER
                    for (segNo in 1 until numSegments) {
                        canvas.drawLine(xPos, top, xPos, bottom, paint)
                        canvas.drawText(ScalebarUtil.labelString(segmentDistance * segNo), xPos, yPosText, textPaint)
                        xPos += this
                    }
                }
            }
        }
    }
}