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
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.esri.arcgisruntime.UnitSystem
import com.esri.arcgisruntime.geometry.LinearUnit
import com.esri.arcgisruntime.toolkit.extension.dpToPixels
import com.esri.arcgisruntime.toolkit.java.scalebar.ScalebarUtil
import com.esri.arcgisruntime.toolkit.scalebar.LABEL_X_PAD_DP
import com.esri.arcgisruntime.toolkit.scalebar.SHADOW_OFFSET_PIXELS
import com.esri.arcgisruntime.toolkit.scalebar.Scalebar

/**
 * Renders a scalebar. There are concrete subclasses corresponding to each [Scalebar.Style].
 *
 * @since 100.2.1
 */
abstract class ScalebarRenderer {

    // The following are defined as member fields to minimize object allocation during draw operations
    private val rect = Rect()
    protected val rectF = RectF()
    protected val linePath = Path()
    protected val paint = Paint()

    /**
     * Indicates if this style of scalebar is segmented.
     *
     * @return true if this style of scalebar is segmented, false otherwise
     * @since 100.2.1
     */
    abstract val isSegmented: Boolean

    /**
     * Draws a scalebar.
     *
     * @param canvas       the Canvas to draw on
     * @param left         the x-coordinate of the left hand edge of the scalebar
     * @param top          the y-coordinate of the top of the scalebar
     * @param right        the x-coordinate of the right hand edge of the scalebar
     * @param bottom       the y-coordinate of the bottom of the scalebar
     * @param distance     the distance represented by the length of the whole scalebar
     * @param displayUnits the units of distance
     * @since 100.2.1
     */
    abstract fun drawScalebar(
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
    )

    /**
     * Calculates the extra space required at the right hand end of the scalebar to draw the units (if any). This
     * affects the positioning of the scalebar when it is right-aligned.
     *
     * @param displayUnits the units
     * @return the extra space required, in pixels
     * @since 100.2.1
     */
    abstract fun calculateExtraSpaceForUnits(displayUnits: LinearUnit?, textPaint: Paint): Float

    /**
     * Draws a solid bar and its shadow. Used by BarRenderer and AlternatingBarRenderer.
     *
     * @param canvas   the Canvas to draw on
     * @param left     the x-coordinate of the left hand edge of the scalebar
     * @param top      the y-coordinate of the top of the scalebar
     * @param right    the x-coordinate of the right hand edge of the scalebar
     * @param bottom   the y-coordinate of the bottom of the scalebar
     * @param barColor the fill color for the bar
     * @since 100.2.1
     */
    protected fun drawBarAndShadow(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        lineWidthDp: Int,
        cornerRadiusDp: Int,
        barColor: Int,
        shadowColor: Int,
        displayDensity: Float
    ) {
        // Draw the shadow of the bar, offset slightly from where the actual bar is drawn below
        rectF.apply {
            set(left, top, right, bottom)
            with(SHADOW_OFFSET_PIXELS + lineWidthDp.dpToPixels(displayDensity) / 2) {
                offset(this, this)
            }
            paint.let { paint ->
                paint.reset()
                paint.color = shadowColor
                paint.style = Paint.Style.FILL
                canvas.drawRoundRect(
                    this,
                    cornerRadiusDp.dpToPixels(displayDensity).toFloat(),
                    cornerRadiusDp.dpToPixels(displayDensity).toFloat(),
                    paint
                )
                // Now draw the bar
                this.set(left, top, right, bottom)
                paint.color = barColor
                canvas.drawRoundRect(
                    this,
                    cornerRadiusDp.dpToPixels(displayDensity).toFloat(),
                    cornerRadiusDp.dpToPixels(displayDensity).toFloat(),
                    paint
                )
            }
        }
    }

    /**
     * Draws a line and its shadow, including the ticks at each end. Used by LineRenderer and GraduatedLineRenderer.
     *
     * @param canvas the Canvas to draw on
     * @param left   the x-coordinate of the left hand edge of the scalebar
     * @param top    the y-coordinate of the top of the scalebar
     * @param right  the x-coordinate of the right hand edge of the scalebar
     * @param bottom the y-coordinate of the bottom of the scalebar
     * @since 100.2.1
     */
    protected fun drawLineAndShadow(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        lineWidthDp: Int,
        lineColor: Int,
        shadowColor: Int
    ) {

        // Create a path to draw the left-hand tick, the line itself and the right-hand tick
        with(linePath) {
            reset()
            moveTo(left, top)
            lineTo(left, bottom)
            lineTo(right, bottom)
            lineTo(right, top)
            setLastPoint(right, top)

            // Draw the shadow
            paint.let { paint ->
                paint.reset()
                paint.color = shadowColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = lineWidthDp.toFloat()
                paint.strokeCap = Paint.Cap.ROUND
                paint.strokeJoin = Paint.Join.ROUND

                // Offset line path slightly from the path of the line
                this.offset(SHADOW_OFFSET_PIXELS, SHADOW_OFFSET_PIXELS)
                canvas.drawPath(this, paint)
                // Reset offset of the path of the line
                this.offset(-SHADOW_OFFSET_PIXELS, -SHADOW_OFFSET_PIXELS)

                // Now draw the line on top of the shadow
                paint.color = lineColor
                canvas.drawPath(this, paint)
            }
        }
    }

    /**
     * Calculates the optimal number of segments in a segmented scalebar of a particular length. Used by
     * AlternatingBarRenderer and GraduatedLineRenderer.
     *
     * @param distance      the distance represented by the length of the whole scalebar
     * @param displayLength the length of the scalebar in pixels
     * @return the number of segments
     * @since 100.2.1
     */
    protected fun calculateNumberOfSegments(
        distance: Double,
        displayLength: Double,
        displayDensity: Float,
        textPaint: Paint
    ): Int {
        // The constraining factor is the space required to draw the labels. Create a testString containing the longest
        // label, which is usually the one for 'distance' because the other labels will be smaller numbers.
        ScalebarUtil.labelString(distance).apply {
            // But if 'distance' is small some of the other labels may use decimals, so allow for each label needing at least
            // 3 characters
            // Calculate the bounds of the testString to determine its length
            textPaint.getTextBounds(if (this.length < 3) "9.9" else this, 0, this.length, rect)
        }

        // Calculate the minimum segment length to ensure the labels don't overlap; multiply the testString length by 1.5
        // to allow for the right-most label being right-justified whereas the other labels are center-justified
        with(rect.right * 1.5 + LABEL_X_PAD_DP.dpToPixels(displayDensity)) {
            // Calculate the number of segments
            ((displayLength / this).toInt()).let { maxNumSegments ->
                return ScalebarUtil.calculateOptimalNumberOfSegments(distance, maxNumSegments)
            }
        }
    }

    /**
     * Calculates the width of the units string.
     *
     * @param displayUnits the units to be displayed, or null if not known yet
     * @return the width of the units string, in pixels
     * @since 100.2.1
     */
    protected fun calculateWidthOfUnitsString(displayUnits: LinearUnit?, textPaint: Paint): Float {
        with(" ${if (displayUnits == null) "mm" else displayUnits.abbreviation}") {
            rect.let { rect ->
                textPaint.getTextBounds(this, 0, this.length, rect)
                return rect.right.toFloat()
            }
        }
    }

}