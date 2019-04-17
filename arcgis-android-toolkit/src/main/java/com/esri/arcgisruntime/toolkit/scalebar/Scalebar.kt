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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import com.esri.arcgisruntime.UnitSystem
import com.esri.arcgisruntime.geometry.GeodeticCurveType
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.LinearUnit
import com.esri.arcgisruntime.geometry.LinearUnitId
import com.esri.arcgisruntime.geometry.PolylineBuilder
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.ViewpointChangedListener
import com.esri.arcgisruntime.toolkit.R
import com.esri.arcgisruntime.toolkit.extension.dpToPixels
import com.esri.arcgisruntime.toolkit.java.scalebar.ScalebarUtil
import com.esri.arcgisruntime.toolkit.scalebar.renderer.BarRenderer
import com.esri.arcgisruntime.toolkit.scalebar.renderer.ScalebarRenderer

private const val ALPHA_50_PC = -0x80000000
private val LINEAR_UNIT_METERS = LinearUnit(LinearUnitId.METERS)
private val LINEAR_UNIT_FEET = LinearUnit(LinearUnitId.FEET)
private val DEFAULT_STYLE = Scalebar.Style.ALTERNATING_BAR
private val DEFAULT_ALIGNMENT = Scalebar.Alignment.LEFT
private const val DEFAULT_FILL_COLOR = Color.LTGRAY.toByte() + ALPHA_50_PC.toByte()
private const val DEFAULT_ALTERNATE_FILL_COLOR = Color.BLACK
private const val DEFAULT_LINE_COLOR = Color.WHITE
private const val DEFAULT_SHADOW_COLOR = Color.BLACK.toByte() + ALPHA_50_PC.toByte()
private const val DEFAULT_TEXT_COLOR = Color.BLACK
private const val DEFAULT_TEXT_SHADOW_COLOR = Color.WHITE
private const val DEFAULT_TEXT_SIZE_DP = 15
private const val DEFAULT_BAR_HEIGHT_DP = 10
private const val SCALEBAR_Y_PAD_DP = 10

class Scalebar : View {

    private lateinit var renderer: ScalebarRenderer

    var style: Style = DEFAULT_STYLE
        set(value) {
            field = value
            renderer = when (value) {
                else -> BarRenderer(
                    displayDensity,
                    lineWidthDp,
                    shadowColor,
                    cornerRadiusDp,
                    fillColor,
                    lineColor,
                    textPaint,
                    textSizeDp
                )
                /*Style.ALTERNATING_BAR -> AlternatingBarRenderer()
                Style.LINE -> LineRenderer()
                Style.GRADUATED_LINE -> GraduatedLineRenderer()
                Style.DUAL_UNIT_LINE -> DualUnitLineRenderer()*/
            }
            postInvalidate()
        }

    var alignment = DEFAULT_ALIGNMENT
    var fillColor: Int = DEFAULT_FILL_COLOR
    var alternateFillColor: Int = DEFAULT_ALTERNATE_FILL_COLOR
    var lineColor: Int = DEFAULT_LINE_COLOR
    var shadowColor: Int = DEFAULT_SHADOW_COLOR
    var textColor: Int = DEFAULT_TEXT_COLOR
    var textShadowColor = DEFAULT_TEXT_SHADOW_COLOR
    var textSizeDp = DEFAULT_TEXT_SIZE_DP
    var barHeightDp = DEFAULT_BAR_HEIGHT_DP
    var unitSystem: UnitSystem? = UnitSystem.METRIC
    var typeface: Typeface = Typeface.DEFAULT

    private var mapView: MapView? = null
    private var drawInMapView: Boolean = false
    private val displayDensity: Float by lazy {
        context.resources.displayMetrics.density
    }
    private var textPaint: Paint = Paint().apply {
        color = textColor
        setShadowLayer(2f, SHADOW_OFFSET_PIXELS, SHADOW_OFFSET_PIXELS, textShadowColor)
        typeface = this.typeface
        textSize = textSizeDp.dpToPixels(displayDensity).toFloat()
    }
    private val graphicsPoint = android.graphics.Point()
    private val lineWidthDp = DEFAULT_BAR_HEIGHT_DP / 4
    private val cornerRadiusDp = DEFAULT_BAR_HEIGHT_DP / 5

    @Volatile
    private var attributionTextHeight = 0

    private val viewPointChangedListener = ViewpointChangedListener {
        // Invalidate the Scalebar view when the MapView viewpoint changes
        postInvalidate()
    }

    private val attributionViewLayoutChangeListener =
        OnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            // Recalculate the attribution text height and invalidate the Scalebar view when the bounds of the attribution
            // view change
            attributionTextHeight = bottom - top
            postInvalidate()
        }

    /**
     * Constructs a Scalebar programmatically. Called by the app when Workflow 1 is used (see [Scalebar] above).
     *
     * @param context the execution [Context]
     * @since 100.5.0
     */
    constructor(context: Context) : super(context) {
        style = DEFAULT_STYLE
    }

    /**
     * Constructor that's called when inflating a Scalebar from XML. Called by the system when Workflow 2 is used (see
     * [Scalebar] above).
     *
     * @param context the execution [Context]
     * @param attrs   the attributes of the XML tag that is inflating the view
     * @since 100.5.0
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.Scalebar,
            0, 0
        ).apply {
            try {
                fillColor = getColor(R.styleable.Scalebar_fillColor, DEFAULT_FILL_COLOR)
                alternateFillColor = getColor(R.styleable.Scalebar_alternateFillColor, DEFAULT_ALTERNATE_FILL_COLOR)
                lineColor = getColor(R.styleable.Scalebar_lineColor, DEFAULT_LINE_COLOR)
                shadowColor = getColor(R.styleable.Scalebar_shadowColor, DEFAULT_SHADOW_COLOR)
                textColor = getColor(R.styleable.Scalebar_textColor, DEFAULT_TEXT_COLOR)
                textShadowColor = getColor(R.styleable.Scalebar_textShadowColor, DEFAULT_TEXT_SHADOW_COLOR)
                textSizeDp = getDimensionPixelSize(
                    R.styleable.Scalebar_textSize,
                    DEFAULT_TEXT_SIZE_DP.dpToPixels(context.resources.displayMetrics.density)
                )
                barHeightDp = getDimensionPixelSize(
                    R.styleable.Scalebar_barHeight,
                    DEFAULT_BAR_HEIGHT_DP.dpToPixels(context.resources.displayMetrics.density)
                )
            } finally {
                recycle()
            }
        }
    }

    /**
     * Adds this Scalebar to the given MapView. Used in Workflow 1 (see {@link Scalebar} above).
     *
     * @param mapView the MapView
     * @throws IllegalArgumentException if mapView is null
     * @throws IllegalStateException    if this Scalebar is already added to or bound to a MapView
     * @since 100.2.1
     */
    fun addToMapView(mapView: MapView) {
        this.mapView?.let {
            throw IllegalStateException("Scalebar already has a GeoView")
        }
        setupMapView(mapView)
        mapView.addView(
            this, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        drawInMapView = true
    }

    /**
     * Binds this Scalebar to the given MapView, or unbinds it. Used in Workflow 2 (see [Scalebar] above).
     *
     * @param mapView the MapView to bind to, or null to unbind it
     * @throws IllegalStateException if this Scalebar is currently added to a MapView
     * @since 100.2.1
     */
    fun bindTo(mapView: MapView?) {
        if (drawInMapView) {
            throw IllegalStateException("Scalebar is currently added to a MapView")
        }
        if (mapView == null) {
            if (this.mapView != null) {
                removeListenersFromMapView()
            }
        } else {
            setupMapView(mapView)
        }
    }

    override fun onDraw(canvas: Canvas) {
        mapView?.let {
            // Calculate width and height of visible part of MapView
            val mapViewVisibleWidth = it.width - (it.viewInsetLeft + it.viewInsetRight).dpToPixels(displayDensity)
            val mapViewVisibleHeight = it.height - (it.viewInsetTop + it.viewInsetBottom).dpToPixels(displayDensity)

            // Calculate maximum length of scalebar in pixels
            val baseUnits = if (unitSystem == UnitSystem.METRIC) LINEAR_UNIT_METERS else LINEAR_UNIT_FEET
            var maxScaleBarLengthPixels: Float
            if (drawInMapView) {
                // When scalebar is a child of the MapView, its length is based on the size of the visible part of the MapView
                maxScaleBarLengthPixels =
                    (if (mapViewVisibleWidth > mapViewVisibleHeight) mapViewVisibleWidth / 4 else mapViewVisibleWidth / 3).toFloat()
            } else {
                // When scalebar is a separate view, its length is based on the view's width; note we allow padding of
                // lineWidthDp at each end of the scalebar to ensure the lines at the ends fit within the view
                maxScaleBarLengthPixels = width.toFloat() - renderer.calculateExtraSpaceForUnits(null) -
                        (2 * lineWidthDp.dpToPixels(displayDensity)).toFloat()
                // But don't allow the scalebar length to be greater than the MapView width
                maxScaleBarLengthPixels = Math.min(maxScaleBarLengthPixels, mapViewVisibleWidth.toFloat())
            }

            // Calculate geodetic length of scalebar based on its maximum length in pixels
            val centerX = it.viewInsetLeft.dpToPixels(displayDensity) + mapViewVisibleWidth / 2
            val centerY = it.viewInsetTop.dpToPixels(displayDensity) + mapViewVisibleHeight / 2
            graphicsPoint.set((centerX - maxScaleBarLengthPixels / 2).toInt(), centerY)
            val p1 = it.screenToLocation(graphicsPoint)
            graphicsPoint.set((centerX + maxScaleBarLengthPixels / 2).toInt(), centerY)
            val p2 = it.screenToLocation(graphicsPoint)
            val visibleArea = it.visibleArea
            if (p1 == null || p2 == null || visibleArea == null) {
                return
            }
            val centerPoint = visibleArea.extent.center
            val builder = PolylineBuilder(it.spatialReference)
            builder.addPoint(p1)
            builder.addPoint(centerPoint) // include center point to ensure it goes the correct way round the globe
            builder.addPoint(p2)
            val maxLengthGeodetic =
                GeometryEngine.lengthGeodetic(builder.toGeometry(), baseUnits, GeodeticCurveType.GEODESIC)

            // Reduce length to make its geodetic length a nice number
            var scalebarLengthGeodetic =
                ScalebarUtil.calculateBestScalebarLength(maxLengthGeodetic, baseUnits, renderer.isSegmented)
            val scalebarLengthPixels = (maxScaleBarLengthPixels * scalebarLengthGeodetic / maxLengthGeodetic).toFloat()

            // Change units if the geodetic length is too big a number in the base units
            val displayUnits = ScalebarUtil.selectLinearUnit(scalebarLengthGeodetic, unitSystem)
            if (displayUnits != baseUnits) {
                scalebarLengthGeodetic = baseUnits.convertTo(displayUnits, scalebarLengthGeodetic)
            }

            // Calculate screen coordinates of left, right, top and bottom of the scalebar
            val left = calculateLeftPos(alignment, scalebarLengthPixels, displayUnits)
            val right = left + scalebarLengthPixels
            val bottom: Float
            val maxPixelsBelowBaseline = textPaint?.fontMetrics?.bottom
            bottom = if (drawInMapView) {
                it.height.toFloat().apply {
                    this - attributionTextHeight.toFloat()
                }.apply {
                    maxPixelsBelowBaseline?.let { maxPixelsBelowBaseline ->
                        this - maxPixelsBelowBaseline
                    }
                }.apply {
                    (it.viewInsetBottom + SCALEBAR_Y_PAD_DP.toDouble() + textSizeDp.toDouble()).dpToPixels(
                        displayDensity
                    ).toFloat()
                }
            } else {
                height.toFloat().apply {
                    this - textSizeDp.dpToPixels(displayDensity)
                }.apply {
                    maxPixelsBelowBaseline?.let { maxPixelsBelowBaseline ->
                        this - maxPixelsBelowBaseline
                    }
                }
            }
            val top = bottom - barHeightDp.dpToPixels(displayDensity)

            // Draw the scalebar
            renderer.drawScalebar(canvas, left, top, right, bottom, scalebarLengthGeodetic, displayUnits)
        }
    }

    private fun setupMapView(mapView: MapView) {
        // Remove listeners from old MapView
        this.mapView?.let {
            removeListenersFromMapView()
        }

        createTextPaint()

        // Add listeners to new MapView
        this.mapView = mapView
        mapView.addViewpointChangedListener(viewPointChangedListener)
        mapView.addAttributionViewLayoutChangeListener(attributionViewLayoutChangeListener)
    }

    /**
     * Creates the Paint used for drawing text.
     *
     * @since 100.2.1
     */
    private fun createTextPaint() {
        textPaint = Paint().apply {
            color = textColor
            setShadowLayer(2f, SHADOW_OFFSET_PIXELS, SHADOW_OFFSET_PIXELS, textShadowColor)
            typeface = this.typeface
            textSize = textSizeDp.dpToPixels(displayDensity).toFloat()
        }
    }

    private fun removeListenersFromMapView() {
        mapView?.removeViewpointChangedListener(viewPointChangedListener)
        mapView?.removeAttributionViewLayoutChangeListener(attributionViewLayoutChangeListener)
    }

    /**
     * Calculates the x-coordinate of the left hand end of the scalebar.
     *
     * @param alignment      the alignment of the scalebar
     * @param scalebarLength the length of the scalebar in pixels
     * @param displayUnits   the units to be displayed
     * @return the x-coordinate of the left hand end of the scalebar
     * @since 100.2.1
     */
    private fun calculateLeftPos(alignment: Alignment, scalebarLength: Float, displayUnits: LinearUnit): Float {
        var left = 0
        var right = width
        var padding = lineWidthDp.toDouble() // padding to ensure the lines at the ends fit within the view
        if (drawInMapView) {
            mapView?.let {
                left = it.viewInsetLeft.dpToPixels(displayDensity)
                right.minus(it.viewInsetRight.dpToPixels(displayDensity))
                padding = SCALEBAR_X_PAD_DP.dpToPixels(displayDensity).toDouble()
            }
        }
        return when (alignment) {
            Alignment.LEFT ->
                // Position start of scalebar at left hand edge of the view, plus padding
                (left + padding).toFloat()
            Alignment.RIGHT ->
                // Position end of scalebar at right hand edge of the view, less padding and the width of the units string (if
                // required)
                right.toFloat() - padding.toFloat() - lineWidthDp.dpToPixels(displayDensity).toFloat() - scalebarLength -
                        renderer.calculateExtraSpaceForUnits(displayUnits)
            Alignment.CENTER ->
                // Position center of scalebar (plus units string if required) at center of the view
                ((right + left).toFloat() - scalebarLength - renderer.calculateExtraSpaceForUnits(displayUnits)) / 2
            else -> (left + padding).toFloat()
        }
    }

    /**
     * Represents the style of scalebar to be displayed.
     *
     * @since 100.2.1
     */
    enum class Style(value: Int) {
        /**
         * A simple, non-segmented bar. A single label is displayed showing the distance represented by the length of the
         * whole bar.
         *
         * @since 100.2.1
         */
        BAR(0),

        /**
         * A bar split up into equal-length segments, with the colors of the segments alternating between the fill color and
         * the alternate fill color. A label is displayed at the end of each segment, showing the distance represented by
         * the length of the bar up to that point.
         *
         * @since 100.2.1
         */
        ALTERNATING_BAR(1),

        /**
         * A simple, non-segmented line. A single label is displayed showing the distance represented by the length of the
         * whole line.
         *
         * @since 100.2.1
         */
        LINE(2),

        /**
         * A line split up into equal-length segments. A tick and a label are displayed at the end of each segment, showing
         * the distance represented by the length of the line up to that point.
         *
         * @since 100.2.1
         */
        GRADUATED_LINE(3),

        /**
         * A line showing distance in dual unit systems - metric and imperial. The primary unit system, as set by
         * [.setUnitSystem], is used to determine the length of the line. A label above the line shows the
         * distance represented by the length of the whole line, in the primary unit system. A tick and another label are
         * displayed below the line, showing distance in the other unit system.
         *
         * @since 100.2.1
         */
        DUAL_UNIT_LINE(4)
    }

    /**
     * Represents the alignment of scalebar to be displayed.
     *
     * @since 100.2.1
     */
    enum class Alignment(value: Int) {
        /**
         * The scalebar is left-aligned, meaning that the left hand end of the scalebar is fixed and it shrinks and grows at
         * the right hand end. If the scalebar is added to a MapView using [.addToMapView], it will be
         * positioned near the bottom-left corner of the MapView.
         *
         * @since 100.2.1
         */
        LEFT(0),

        /**
         * The scalebar is right-aligned, meaning that the right hand end of the scalebar is fixed and it shrinks and grows
         * at the left hand end. If the scalebar is added to a MapView using [.addToMapView], it will be
         * positioned near the bottom-right corner of the MapView.
         *
         * @since 100.2.1
         */
        RIGHT(1),

        /**
         * The scalebar is center-aligned, meaning that the center point of the scalebar is fixed and it shrinks and grows
         * at both ends. If the scalebar is added to a MapView using [.addToMapView], it will be
         * positioned near the bottom the MapView, centered between the left and right edges.
         *
         * @since 100.2.1
         */
        CENTER(2)
    }

    /**
     * Renders an ALTERNATING_BAR style scalebar.
     *
     * @see Style.ALTERNATING_BAR
     *
     * @since 100.2.1
     *//*
    private inner class AlternatingBarRenderer : ScalebarRenderer() {

        override fun isSegmented(): Boolean {
            return true
        }

        override fun calculateExtraSpaceForUnits(displayUnits: LinearUnit): Float {
            return calculateWidthOfUnitsString(displayUnits)
        }

        override fun drawScalebar(
            canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, distance: Double,
            displayUnits: LinearUnit
        ) {

            // Calculate the number of segments in the bar
            val barDisplayLength = right - left
            val numSegments = calculateNumberOfSegments(distance, barDisplayLength.toDouble())
            val segmentDisplayLength = barDisplayLength / numSegments

            // Draw a solid bar, using mAlternateFillColor, and its shadow
            drawBarAndShadow(canvas, left, top, right, bottom, mAlternateFillColor)

            // Now draw every second segment on top of it using mFillColor
            mPaint.reset()
            mPaint.setStyle(Paint.Style.FILL)
            mPaint.setColor(mFillColor)
            var xPos = left + segmentDisplayLength
            var i = 1
            while (i < numSegments) {
                mRectF.set(xPos, top, xPos + segmentDisplayLength, bottom)
                canvas.drawRect(mRectF, mPaint)
                xPos += 2 * segmentDisplayLength
                i += 2
            }

            // Draw a line round the outside of the complete bar
            mRectF.set(left, top, right, bottom)
            mPaint.reset()
            mPaint.setColor(mLineColor)
            mPaint.setStyle(Paint.Style.STROKE)
            mPaint.setStrokeWidth(dpToPixels(mLineWidthDp.toDouble()).toFloat())
            canvas.drawRoundRect(
                mRectF,
                dpToPixels(mCornerRadiusDp.toDouble()).toFloat(),
                dpToPixels(mCornerRadiusDp.toDouble()).toFloat(),
                mPaint
            )

            // Draw a label at the start of the bar
            val yPosText = bottom + dpToPixels(mTextSizeDp.toDouble())
            mTextPaint.setTextAlign(Paint.Align.LEFT)
            canvas.drawText("0", left, yPosText, mTextPaint)

            // Draw a label at the end of the bar
            mTextPaint.setTextAlign(Paint.Align.RIGHT)
            canvas.drawText(ScalebarUtil.labelString(distance), right, yPosText, mTextPaint)
            mTextPaint.setTextAlign(Paint.Align.LEFT)
            canvas.drawText(' ' + displayUnits.abbreviation, right, yPosText, mTextPaint)

            // Draw a vertical line and a label at each segment boundary
            xPos = left + segmentDisplayLength
            val segmentDistance = distance / numSegments
            mTextPaint.setTextAlign(Paint.Align.CENTER)
            for (segNo in 1 until numSegments) {
                canvas.drawLine(xPos, top, xPos, bottom, mPaint)
                canvas.drawText(ScalebarUtil.labelString(segmentDistance * segNo), xPos, yPosText, mTextPaint)
                xPos += segmentDisplayLength
            }
        }
    }

    */
    /**
     * Renders a LINE style scalebar.
     *
     * @see Style.LINE
     *
     * @since 100.2.1
     *//*
    private inner class LineRenderer : ScalebarRenderer() {

        override fun isSegmented(): Boolean {
            return false
        }

        override fun calculateExtraSpaceForUnits(displayUnits: LinearUnit): Float {
            return 0f
        }

        override fun drawScalebar(
            canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, distance: Double,
            displayUnits: LinearUnit
        ) {

            // Draw the line and its shadow, including the ticks at each end
            drawLineAndShadow(canvas, left, top, right, bottom)

            // Draw the label, centered on the center of the line
            val label = ScalebarUtil.labelString(distance) + " " + displayUnits.abbreviation
            mTextPaint.setTextAlign(Paint.Align.CENTER)
            canvas.drawText(label, left + (right - left) / 2, bottom + dpToPixels(mTextSizeDp.toDouble()), mTextPaint)
        }
    }

    */
    /**
     * Renders a GRADUATED_LINE style scalebar.
     *
     * @see Style.GRADUATED_LINE
     *
     * @since 100.2.1
     *//*
    private inner class GraduatedLineRenderer : ScalebarRenderer() {

        override fun isSegmented(): Boolean {
            return true
        }

        override fun calculateExtraSpaceForUnits(displayUnits: LinearUnit): Float {
            return calculateWidthOfUnitsString(displayUnits)
        }

        override fun drawScalebar(
            canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, distance: Double,
            displayUnits: LinearUnit
        ) {

            // Calculate the number of segments in the line
            val lineDisplayLength = right - left
            val numSegments = calculateNumberOfSegments(distance, lineDisplayLength.toDouble())
            val segmentDisplayLength = lineDisplayLength / numSegments

            // Create Paint for drawing the ticks
            mPaint.reset()
            mPaint.setStyle(Paint.Style.STROKE)
            mPaint.setStrokeWidth(dpToPixels(mLineWidthDp.toDouble()).toFloat())
            mPaint.setStrokeCap(Paint.Cap.ROUND)

            // Draw a tick, its shadow and a label at each segment boundary
            var xPos = left + segmentDisplayLength
            val yPos = top + (bottom - top) / 4 // segment ticks are 3/4 the height of the ticks at the start and end
            val segmentDistance = distance / numSegments
            val yPosText = bottom + dpToPixels(mTextSizeDp.toDouble())
            mTextPaint.setTextAlign(Paint.Align.CENTER)
            for (segNo in 1 until numSegments) {
                // Draw the shadow, offset slightly from where the tick is drawn below
                mPaint.setColor(mShadowColor)
                canvas.drawLine(
                    xPos + SHADOW_OFFSET_PIXELS, yPos + SHADOW_OFFSET_PIXELS,
                    xPos + SHADOW_OFFSET_PIXELS, bottom + SHADOW_OFFSET_PIXELS, mPaint
                )

                // Draw the line on top of the shadow
                mPaint.setColor(mLineColor)
                canvas.drawLine(xPos, yPos, xPos, bottom, mPaint)

                // Draw the label
                canvas.drawText(ScalebarUtil.labelString(segmentDistance * segNo), xPos, yPosText, mTextPaint)
                xPos += segmentDisplayLength
            }

            // Draw the line and its shadow, including the ticks at each end
            drawLineAndShadow(canvas, left, top, right, bottom)

            // Draw a label at the start of the line
            mTextPaint.setTextAlign(Paint.Align.LEFT)
            canvas.drawText("0", left, yPosText, mTextPaint)

            // Draw a label at the end of the line
            mTextPaint.setTextAlign(Paint.Align.RIGHT)
            canvas.drawText(ScalebarUtil.labelString(distance), right, yPosText, mTextPaint)
            mTextPaint.setTextAlign(Paint.Align.LEFT)
            canvas.drawText(' ' + displayUnits.abbreviation, right, yPosText, mTextPaint)
        }
    }

    */
    /**
     * Renders a DUAL_UNIT_LINE style scalebar.
     *
     * @see Style.DUAL_UNIT_LINE
     *
     * @since 100.2.1
     *//*
    private inner class DualUnitLineRenderer : ScalebarRenderer() {

        override fun isSegmented(): Boolean {
            return false
        }

        override fun calculateExtraSpaceForUnits(displayUnits: LinearUnit): Float {
            return calculateWidthOfUnitsString(displayUnits)
        }

        override fun drawScalebar(
            canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, distance: Double,
            displayUnits: LinearUnit
        ) {

            // Calculate scalebar length in the secondary units
            val secondaryBaseUnits = if (mUnitSystem == UnitSystem.METRIC) LINEAR_UNIT_FEET else LINEAR_UNIT_METERS
            val fullLengthInSecondaryUnits = displayUnits.convertTo(secondaryBaseUnits, distance)

            // Reduce the secondary units length to make it a nice number
            var secondaryUnitsLength =
                ScalebarUtil.calculateBestScalebarLength(fullLengthInSecondaryUnits, secondaryBaseUnits, false)
            val lineDisplayLength = right - left
            val xPosSecondaryTick =
                left + (lineDisplayLength * secondaryUnitsLength / fullLengthInSecondaryUnits).toFloat()

            // Change units if secondaryUnitsLength is too big a number in the base units
            val secondaryUnitSystem = if (mUnitSystem == UnitSystem.METRIC) UnitSystem.IMPERIAL else UnitSystem.METRIC
            val secondaryDisplayUnits = ScalebarUtil.selectLinearUnit(secondaryUnitsLength, secondaryUnitSystem)
            if (secondaryDisplayUnits != secondaryBaseUnits) {
                secondaryUnitsLength = secondaryBaseUnits.convertTo(secondaryDisplayUnits, secondaryUnitsLength)
            }

            // Create Paint for drawing the lines
            mPaint.reset()
            mPaint.setStyle(Paint.Style.STROKE)
            mPaint.setStrokeWidth(dpToPixels(mLineWidthDp.toDouble()).toFloat())
            mPaint.setStrokeCap(Paint.Cap.ROUND)
            mPaint.setStrokeJoin(Paint.Join.ROUND)

            // Create a path to draw the line and the ticks
            val yPosLine = (top + bottom) / 2
            mLinePath.reset()
            mLinePath.moveTo(left, top)
            mLinePath.lineTo(left, bottom) // draw big tick at left
            mLinePath.moveTo(xPosSecondaryTick, yPosLine) // move to top of secondary tick
            mLinePath.lineTo(xPosSecondaryTick, bottom) // draw secondary tick
            mLinePath.moveTo(left, yPosLine) // move to start of horizontal line
            mLinePath.lineTo(right, yPosLine) // draw the line
            mLinePath.lineTo(right, top) // draw tick at right
            mLinePath.setLastPoint(right, top)

            // Create a copy of the line path to be the path of its shadow, offset slightly from the line path
            val shadowPath = Path(mLinePath)
            shadowPath.offset(SHADOW_OFFSET_PIXELS, SHADOW_OFFSET_PIXELS)

            // Draw the shadow
            mPaint.setColor(mShadowColor)
            canvas.drawPath(shadowPath, mPaint)

            // Draw the line and the ticks
            mPaint.setColor(mLineColor)
            canvas.drawPath(mLinePath, mPaint)

            // Draw the primary units label above the tick at the right hand end
            val maxPixelsBelowBaseline = mTextPaint.getFontMetrics().bottom
            var yPosText = top - maxPixelsBelowBaseline
            mTextPaint.setTextAlign(Paint.Align.RIGHT)
            canvas.drawText(ScalebarUtil.labelString(distance), right, yPosText, mTextPaint)
            mTextPaint.setTextAlign(Paint.Align.LEFT)
            canvas.drawText(' ' + displayUnits.abbreviation, right, yPosText, mTextPaint)

            // Draw the secondary units label below its tick
            yPosText = bottom + dpToPixels(mTextSizeDp.toDouble())
            mTextPaint.setTextAlign(Paint.Align.RIGHT)
            canvas.drawText(ScalebarUtil.labelString(secondaryUnitsLength), xPosSecondaryTick, yPosText, mTextPaint)
            mTextPaint.setTextAlign(Paint.Align.LEFT)
            canvas.drawText(' ' + secondaryDisplayUnits.abbreviation, xPosSecondaryTick, yPosText, mTextPaint)
        }
    }*/
}