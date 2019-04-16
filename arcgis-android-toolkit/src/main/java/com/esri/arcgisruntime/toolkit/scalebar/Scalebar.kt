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
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.esri.arcgisruntime.UnitSystem
import com.esri.arcgisruntime.geometry.GeodeticCurveType
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.LinearUnit
import com.esri.arcgisruntime.geometry.LinearUnitId
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.toolkit.R
import com.esri.arcgisruntime.toolkit.extension.toPixels
import com.esri.arcgisruntime.toolkit.java.scalebar.ScalebarUtil

private const val ALPHA_50_PC = -0x80000000
private const val SHADOW_OFFSET_PIXELS = 2f
private val LINEAR_UNIT_METERS = LinearUnit(LinearUnitId.METERS)
private val LINEAR_UNIT_FEET = LinearUnit(LinearUnitId.FEET)
private val DEFAULT_STYLE = Scalebar.Style.ALTERNATING_BAR
private const val DEFAULT_FILL_COLOR = Color.LTGRAY.toByte() + ALPHA_50_PC.toByte()
private const val DEFAULT_ALTERNATE_FILL_COLOR = Color.BLACK
private const val DEFAULT_LINE_COLOR = Color.WHITE
private const val DEFAULT_SHADOW_COLOR = Color.BLACK.toByte() + ALPHA_50_PC.toByte()
private const val DEFAULT_TEXT_COLOR = Color.BLACK
private const val DEFAULT_TEXT_SHADOW_COLOR = Color.WHITE
private const val DEFAULT_TEXT_SIZE_DP = 15
private const val DEFAULT_BAR_HEIGHT_DP = 10

class Scalebar : View {

    private var style: Style = DEFAULT_STYLE
    private var fillColor: Int = DEFAULT_FILL_COLOR
    private var alternateFillColor: Int = DEFAULT_ALTERNATE_FILL_COLOR
    private var lineColor: Int = DEFAULT_LINE_COLOR
    private var shadowColor: Int = DEFAULT_SHADOW_COLOR
    private var textColor: Int = DEFAULT_TEXT_COLOR
    private var textShadowColor = DEFAULT_TEXT_SHADOW_COLOR
    private var textSizeDp = DEFAULT_TEXT_SIZE_DP
    private var barHeightDp = DEFAULT_BAR_HEIGHT_DP

    private var mapView: MapView? = null
    private var drawInMapView: Boolean = false
    private val displayDensity: Float by lazy {
        context.resources.displayMetrics.density
    }
    private var textPaint: Paint? = null
    private var unitSystem: UnitSystem? = UnitSystem.METRIC

    /**
     * Constructs a Scalebar programmatically. Called by the app when Workflow 1 is used (see [Scalebar] above).
     *
     * @param context the execution [Context]
     * @since 100.5.0
     */
    constructor(context: Context) : super(context)

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
                    DEFAULT_TEXT_SIZE_DP.toPixels(context.resources.displayMetrics.density)
                )
                barHeightDp = getDimensionPixelSize(
                    R.styleable.Scalebar_barHeight,
                    DEFAULT_BAR_HEIGHT_DP.toPixels(context.resources.displayMetrics.density)
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
            throw IllegalStateException("Compass already has a GeoView")
        }
        drawInMapView = true
        Math.min(height, width).let {
            mapView.addView(this, ViewGroup.LayoutParams(it, it))
        }
        setupMapView(mapView)
    }

    override fun onDraw(canvas: Canvas) {
        mapView?.let {
            // Calculate width and height of visible part of MapView
            val mapViewVisibleWidth = it.width - (it.viewInsetLeft + it.viewInsetRight).toPixels(displayDensity)
            val mapViewVisibleHeight = it.height - (it.viewInsetTop + it.viewInsetBottom).toPixels(displayDensity)

            // Calculate maximum length of scalebar in pixels
            val baseUnits = if (unitSystem == UnitSystem.METRIC) LINEAR_UNIT_METERS else LINEAR_UNIT_FEET
            var maxScaleBarLengthPixels: Float
            if (drawInMapView) {
                // When scalebar is a child of the MapView, its length is based on the size of the visible part of the MapView
                maxScaleBarLengthPixels =
                    (if (mapViewVisibleWidth > mapViewVisibleHeight) mapViewVisibleWidth / 4 else mapViewVisibleWidth / 3).toFloat()
            } else {
                // When scalebar is a separate view, its length is based on the view's width; note we allow padding of
                // mLineWidthDp at each end of the scalebar to ensure the lines at the ends fit within the view
                maxScaleBarLengthPixels = width.toFloat() - mRenderer.calculateExtraSpaceForUnits(null) -
                        (2 * dpToPixels(mLineWidthDp.toDouble())).toFloat()
                // But don't allow the scalebar length to be greater than the MapView width
                maxScaleBarLengthPixels = Math.min(maxScaleBarLengthPixels, mapViewVisibleWidth.toFloat())
            }

            // Calculate geodetic length of scalebar based on its maximum length in pixels
            val centerX = dpToPixels(mMapView.getViewInsetLeft()) + mapViewVisibleWidth / 2
            val centerY = dpToPixels(mMapView.getViewInsetTop()) + mapViewVisibleHeight / 2
            mGraphicsPoint.set((centerX - maxScaleBarLengthPixels / 2).toInt(), centerY)
            val p1 = mMapView.screenToLocation(mGraphicsPoint)
            mGraphicsPoint.set((centerX + maxScaleBarLengthPixels / 2).toInt(), centerY)
            val p2 = mMapView.screenToLocation(mGraphicsPoint)
            val visibleArea = mMapView.getVisibleArea()
            if (p1 == null || p2 == null || visibleArea == null) {
                return
            }
            val centerPoint = visibleArea!!.getExtent().getCenter()
            val builder = PolylineBuilder(mMapView.getSpatialReference())
            builder.addPoint(p1!!)
            builder.addPoint(centerPoint) // include center point to ensure it goes the correct way round the globe
            builder.addPoint(p2!!)
            val maxLengthGeodetic =
                GeometryEngine.lengthGeodetic(builder.toGeometry(), baseUnits, GeodeticCurveType.GEODESIC)

            // Reduce length to make its geodetic length a nice number
            var scalebarLengthGeodetic =
                ScalebarUtil.calculateBestScalebarLength(maxLengthGeodetic, baseUnits, mRenderer.isSegmented())
            val scalebarLengthPixels = (maxScaleBarLengthPixels * scalebarLengthGeodetic / maxLengthGeodetic).toFloat()

            // Change units if the geodetic length is too big a number in the base units
            val displayUnits = ScalebarUtil.selectLinearUnit(scalebarLengthGeodetic, mUnitSystem)
            if (displayUnits != baseUnits) {
                scalebarLengthGeodetic = baseUnits.convertTo(displayUnits, scalebarLengthGeodetic)
            }

            // Calculate screen coordinates of left, right, top and bottom of the scalebar
            val left = calculateLeftPos(mAlignment, scalebarLengthPixels, displayUnits)
            val right = left + scalebarLengthPixels
            val bottom: Float
            val maxPixelsBelowBaseline = mTextPaint.getFontMetrics().bottom
            if (mScalebarIsChildOfMapView) {
                bottom = mMapView.getHeight().toFloat() - mAttributionTextHeight.toFloat() - maxPixelsBelowBaseline -
                        dpToPixels(mMapView.getViewInsetBottom() + SCALEBAR_Y_PAD_DP.toDouble() + mTextSizeDp.toDouble()).toFloat()
            } else {
                bottom = height.toFloat() - dpToPixels(mTextSizeDp.toDouble()).toFloat() - maxPixelsBelowBaseline
            }
            val top = bottom - dpToPixels(mBarHeightDp.toDouble())

            // Draw the scalebar
            mRenderer.drawScalebar(canvas, left, top, right, bottom, scalebarLengthGeodetic, displayUnits)
        }
    }

    private fun setupMapView(mapView: MapView) {
        // Remove listeners from old MapView
        this.mapView?.let {
            removeListenersFromMapView()
        }

        // Add listeners to new MapView
        this.mapView = mapView
        //TODO add listeners
        createTextPaint()
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
            textSize = textSizeDp.toPixels(displayDensity).toFloat()
        }
    }

    private fun removeListenersFromMapView() {
        // TODO
    }

    /**
     * Represents the style of scalebar to be displayed.
     *
     * @since 100.2.1
     */
    enum class Style {
        /**
         * A simple, non-segmented bar. A single label is displayed showing the distance represented by the length of the
         * whole bar.
         *
         * @since 100.2.1
         */
        BAR,

        /**
         * A bar split up into equal-length segments, with the colors of the segments alternating between the fill color and
         * the alternate fill color. A label is displayed at the end of each segment, showing the distance represented by
         * the length of the bar up to that point.
         *
         * @since 100.2.1
         */
        ALTERNATING_BAR,

        /**
         * A simple, non-segmented line. A single label is displayed showing the distance represented by the length of the
         * whole line.
         *
         * @since 100.2.1
         */
        LINE,

        /**
         * A line split up into equal-length segments. A tick and a label are displayed at the end of each segment, showing
         * the distance represented by the length of the line up to that point.
         *
         * @since 100.2.1
         */
        GRADUATED_LINE,

        /**
         * A line showing distance in dual unit systems - metric and imperial. The primary unit system, as set by
         * [.setUnitSystem], is used to determine the length of the line. A label above the line shows the
         * distance represented by the length of the whole line, in the primary unit system. A tick and another label are
         * displayed below the line, showing distance in the other unit system.
         *
         * @since 100.2.1
         */
        DUAL_UNIT_LINE
    }
}