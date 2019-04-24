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

package com.esri.arcgisruntime.toolkit.scalebar.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
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
abstract class ScalebarRenderer(
    protected val displayDensity: Float,
    protected val lineWidthDp: Int,
    var shadowColor: Int,
    protected val cornerRadiusDp: Int,
    var fillColor: Int,
    var lineColor: Int,
    var textPaint: Paint,
    protected val textSizeDp: Int
) {

    // The following are defined as member fields to minimize object allocation during draw operations
    private val rect = Rect()
    protected val rectF = RectF()
    private val linePath = Path()
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
        canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float,
        distance: Double, displayUnits: LinearUnit
    )

    /**
     * Calculates the extra space required at the right hand end of the scalebar to draw the units (if any). This
     * affects the positioning of the scalebar when it is right-aligned.
     *
     * @param displayUnits the units
     * @return the extra space required, in pixels
     * @since 100.2.1
     */
    abstract fun calculateExtraSpaceForUnits(displayUnits: LinearUnit?): Float

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
        barColor: Int
    ) {
        // Draw the shadow of the bar, offset slightly from where the actual bar is drawn below
        rectF.set(left, top, right, bottom)
        val offset = SHADOW_OFFSET_PIXELS + lineWidthDp.dpToPixels(displayDensity) / 2
        rectF.offset(offset, offset)
        paint.reset()
        paint.color = shadowColor
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(
            rectF,
            cornerRadiusDp.dpToPixels(displayDensity).toFloat(),
            cornerRadiusDp.dpToPixels(displayDensity).toFloat(),
            paint
        )

        // Now draw the bar
        rectF.set(left, top, right, bottom)
        paint.color = barColor
        canvas.drawRoundRect(
            rectF,
            cornerRadiusDp.dpToPixels(displayDensity).toFloat(),
            cornerRadiusDp.dpToPixels(displayDensity).toFloat(),
            paint
        )
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
    protected fun drawLineAndShadow(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        // Create a path to draw the left-hand tick, the line itself and the right-hand tick
        linePath.reset()
        linePath.moveTo(left, top)
        linePath.lineTo(left, bottom)
        linePath.lineTo(right, bottom)
        linePath.lineTo(right, top)
        linePath.setLastPoint(right, top)

        // Create a copy to be the path of the shadow, offset slightly from the path of the line
        val shadowPath = Path(linePath)
        shadowPath.offset(SHADOW_OFFSET_PIXELS, SHADOW_OFFSET_PIXELS)

        // Draw the shadow
        paint.reset()
        paint.color = shadowColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = lineWidthDp.toFloat()
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        canvas.drawPath(shadowPath, paint)

        // Now draw the line on top of the shadow
        paint.color = lineColor
        canvas.drawPath(linePath, paint)
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
    protected fun calculateNumberOfSegments(distance: Double, displayLength: Double): Int {
        // The constraining factor is the space required to draw the labels. Create a testString containing the longest
        // label, which is usually the one for 'distance' because the other labels will be smaller numbers.
        var testString = ScalebarUtil.labelString(distance)

        // But if 'distance' is small some of the other labels may use decimals, so allow for each label needing at least
        // 3 characters
        if (testString.length < 3) {
            testString = "9.9"
        }

        // Calculate the bounds of the testString to determine its length
        textPaint.getTextBounds(testString, 0, testString.length, rect)

        // Calculate the minimum segment length to ensure the labels don't overlap; multiply the testString length by 1.5
        // to allow for the right-most label being right-justified whereas the other labels are center-justified
        val minSegmentLength = rect.right * 1.5 + LABEL_X_PAD_DP.dpToPixels(displayDensity)

        // Calculate the number of segments
        val maxNumSegments = (displayLength / minSegmentLength).toInt()
        return ScalebarUtil.calculateOptimalNumberOfSegments(distance, maxNumSegments)
    }

    /**
     * Calculates the width of the units string.
     *
     * @param displayUnits the units to be displayed, or null if not known yet
     * @return the width of the units string, in pixels
     * @since 100.2.1
     */
    protected fun calculateWidthOfUnitsString(displayUnits: LinearUnit?): Float {
        val unitsText = ' ' + if (displayUnits == null) "mm" else displayUnits.abbreviation
        textPaint.getTextBounds(unitsText, 0, unitsText.length, rect)
        return rect.right.toFloat()
    }

}