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
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import com.esri.arcgisruntime.UnitSystem
import com.esri.arcgisruntime.geometry.GeodeticCurveType
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.LinearUnit
import com.esri.arcgisruntime.geometry.PolylineBuilder
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.ViewpointChangedListener
import com.esri.arcgisruntime.toolkit.R
import com.esri.arcgisruntime.toolkit.extension.dpToPixels
import com.esri.arcgisruntime.toolkit.extension.pixelsToSp
import com.esri.arcgisruntime.toolkit.extension.spToPixels
import com.esri.arcgisruntime.toolkit.java.scalebar.ScalebarUtil
import com.esri.arcgisruntime.toolkit.scalebar.style.Style

class Scalebar : View {

    var style: Style = DEFAULT_STYLE
        set(value) {
            field = value
            postInvalidate()
        }

    var alignment = DEFAULT_ALIGNMENT
        set(value) {
            field = value
            postInvalidate()
        }
    var fillColor: Int = DEFAULT_FILL_COLOR
        set(value) {
            field = value
            postInvalidate()
        }
    var alternateFillColor: Int = DEFAULT_ALTERNATE_FILL_COLOR
        set(value) {
            field = value
            postInvalidate()
        }
    var lineColor: Int = DEFAULT_LINE_COLOR
        set(value) {
            field = value
            postInvalidate()
        }
    var shadowColor: Int = DEFAULT_SHADOW_COLOR
        set(value) {
            field = value
            postInvalidate()
        }
    var textColor: Int = DEFAULT_TEXT_COLOR
        set(value) {
            field = value
            textPaint.color = value
            postInvalidate()
        }
    var textShadowColor = DEFAULT_TEXT_SHADOW_COLOR
        set(value) {
            field = value
            textPaint.setShadowLayer(2f, SHADOW_OFFSET_PIXELS, SHADOW_OFFSET_PIXELS, value)
            postInvalidate()
        }
    var textSizeSp = DEFAULT_TEXT_SIZE_SP
        set(value) {
            field = value
            textPaint.textSize = value.spToPixels(displayMetrics).toFloat()
            postInvalidate()
        }
    var typeface: Typeface = Typeface.DEFAULT
        set(value) {
            field = value
            textPaint.typeface = value
            postInvalidate()
        }
    var barHeightDp = DEFAULT_BAR_HEIGHT_DP
        set(value) {
            field = value
            postInvalidate()
        }
    var unitSystem: UnitSystem = UnitSystem.METRIC
        set(value) {
            field = value
            postInvalidate()
        }

    private var mapView: MapView? = null
    private var drawInMapView: Boolean = false
    private val displayDensity: Float by lazy {
        context.resources.displayMetrics.density
    }
    private val displayMetrics: DisplayMetrics by lazy {
        context.resources.displayMetrics
    }
    private var textPaint: Paint = Paint().apply {
        color = textColor
        setShadowLayer(2f, SHADOW_OFFSET_PIXELS, SHADOW_OFFSET_PIXELS, textShadowColor)
        typeface = this.typeface
        textSize = textSizeSp.spToPixels(displayMetrics).toFloat()
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
                Style.fromInt(getInt(R.styleable.Scalebar_style, DEFAULT_STYLE.value))?.let {
                    style = it
                }
                fillColor = getColor(R.styleable.Scalebar_fillColor, DEFAULT_FILL_COLOR)
                alternateFillColor = getColor(R.styleable.Scalebar_alternateFillColor, DEFAULT_ALTERNATE_FILL_COLOR)
                lineColor = getColor(R.styleable.Scalebar_lineColor, DEFAULT_LINE_COLOR)
                shadowColor = getColor(R.styleable.Scalebar_shadowColor, DEFAULT_SHADOW_COLOR)
                textColor = getColor(R.styleable.Scalebar_textColor, DEFAULT_TEXT_COLOR)
                textShadowColor = getColor(R.styleable.Scalebar_textShadowColor, DEFAULT_TEXT_SHADOW_COLOR)
                textSizeSp = getDimensionPixelSize(
                    R.styleable.Scalebar_textSize,
                    DEFAULT_TEXT_SIZE_SP
                ).pixelsToSp(displayMetrics)
                barHeightDp = getDimensionPixelSize(
                    R.styleable.Scalebar_barHeight,
                    DEFAULT_BAR_HEIGHT_DP.dpToPixels(displayDensity)
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
        mapView?.let { mapView ->
            // Calculate width and height of visible part of MapView
            val mapViewVisibleWidth =
                mapView.width - (mapView.viewInsetLeft + mapView.viewInsetRight).dpToPixels(displayDensity)
            val mapViewVisibleHeight =
                mapView.height - (mapView.viewInsetTop + mapView.viewInsetBottom).dpToPixels(displayDensity)

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
                maxScaleBarLengthPixels =
                    width.toFloat() - style.renderer.calculateExtraSpaceForUnits(null, textPaint) -
                            (2 * lineWidthDp.dpToPixels(displayDensity)).toFloat()
                // But don't allow the scalebar length to be greater than the MapView width
                maxScaleBarLengthPixels = Math.min(maxScaleBarLengthPixels, mapViewVisibleWidth.toFloat())
            }

            // Calculate geodetic length of scalebar based on its maximum length in pixels
            val centerX = mapView.viewInsetLeft.dpToPixels(displayDensity) + mapViewVisibleWidth / 2
            val centerY = mapView.viewInsetTop.dpToPixels(displayDensity) + mapViewVisibleHeight / 2
            graphicsPoint.set((centerX - maxScaleBarLengthPixels / 2).toInt(), centerY)
            val p1 = mapView.screenToLocation(graphicsPoint)
            graphicsPoint.set((centerX + maxScaleBarLengthPixels / 2).toInt(), centerY)
            val p2 = mapView.screenToLocation(graphicsPoint)
            val visibleArea = mapView.visibleArea
            if (p1 == null || p2 == null || visibleArea == null) {
                return
            }
            val centerPoint = visibleArea.extent.center
            val builder = PolylineBuilder(mapView.spatialReference)
            builder.addPoint(p1)
            builder.addPoint(centerPoint) // include center point to ensure it goes the correct way round the globe
            builder.addPoint(p2)
            val maxLengthGeodetic =
                GeometryEngine.lengthGeodetic(builder.toGeometry(), baseUnits, GeodeticCurveType.GEODESIC)

            // Reduce length to make its geodetic length a nice number
            var scalebarLengthGeodetic =
                ScalebarUtil.calculateBestScalebarLength(maxLengthGeodetic, baseUnits, style.renderer.isSegmented)
            val scalebarLengthPixels = (maxScaleBarLengthPixels * scalebarLengthGeodetic / maxLengthGeodetic).toFloat()

            // Change units if the geodetic length is too big a number in the base units
            val displayUnits = ScalebarUtil.selectLinearUnit(scalebarLengthGeodetic, unitSystem)
            if (displayUnits != baseUnits) {
                scalebarLengthGeodetic = baseUnits.convertTo(displayUnits, scalebarLengthGeodetic)
            }

            // Calculate screen coordinates of left, right, top and bottom of the scalebar
            val left = calculateLeftPos(alignment, scalebarLengthPixels, displayUnits)
            val right = left + scalebarLengthPixels
            val maxPixelsBelowBaseline: Float = textPaint.fontMetrics?.bottom ?: 0.0f
            val bottom = if (drawInMapView) {
                mapView.height.toFloat() - attributionTextHeight - (mapView.viewInsetBottom + SCALEBAR_Y_PAD_DP
                        + textSizeSp).dpToPixels(displayDensity) - maxPixelsBelowBaseline
            } else {
                height.toFloat() - (textSizeSp.spToPixels(displayMetrics)) - maxPixelsBelowBaseline
            }

            val top = bottom - barHeightDp.dpToPixels(displayDensity)

            // Draw the scalebar
            style.renderer.drawScalebar(
                canvas,
                left,
                top,
                right,
                bottom,
                scalebarLengthGeodetic,
                displayUnits,
                unitSystem,
                lineWidthDp.dpToPixels(displayDensity),
                cornerRadiusDp.dpToPixels(displayDensity),
                textSizeSp.spToPixels(displayMetrics),
                fillColor,
                alternateFillColor,
                shadowColor,
                lineColor,
                textPaint,
                displayDensity
            )
        }
    }

    private fun setupMapView(mapView: MapView) {
        // Remove listeners from old MapView
        this.mapView?.let {
            removeListenersFromMapView()
        }

        // Add listeners to new MapView
        this.mapView = mapView
        mapView.addViewpointChangedListener(viewPointChangedListener)
        mapView.addAttributionViewLayoutChangeListener(attributionViewLayoutChangeListener)
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
        // padding to ensure the lines at the ends fit within the view
        var padding = lineWidthDp.dpToPixels(displayDensity)
        if (drawInMapView) {
            mapView?.let { mapView ->
                left = mapView.viewInsetLeft.dpToPixels(displayDensity)
                right.minus(mapView.viewInsetRight.dpToPixels(displayDensity))
                padding = SCALEBAR_X_PAD_DP.dpToPixels(displayDensity)
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
                        style.renderer.calculateExtraSpaceForUnits(displayUnits, textPaint)
            Alignment.CENTER ->
                // Position center of scalebar (plus units string if required) at center of the view
                ((right + left).toFloat() - scalebarLength - style.renderer.calculateExtraSpaceForUnits(
                    displayUnits,
                    textPaint
                )) / 2
            else -> (left + padding).toFloat()
        }
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
}
