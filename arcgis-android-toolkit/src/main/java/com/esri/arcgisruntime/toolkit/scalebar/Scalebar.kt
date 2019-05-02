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
import com.esri.arcgisruntime.toolkit.extension.calculateBestLength
import com.esri.arcgisruntime.toolkit.extension.dpToPixels
import com.esri.arcgisruntime.toolkit.extension.selectLinearUnit
import com.esri.arcgisruntime.toolkit.extension.unitSystemFromInt
import com.esri.arcgisruntime.toolkit.java.scalebar.Scalebar
import com.esri.arcgisruntime.toolkit.scalebar.style.Style

/**
 * Displays a bar or line indicating the current scale of a [MapView]. Two workflows are supported:
 *
 * _Workflow 1:_
 *
 * The simplest workflow is for the app to instantiate a Scalebar using using an instance of [Context] and call
 * [addToMapView] to display it within the MapView. Optionally, setter methods may be called to override
 * some of the default settings. The app has limited control over the position of the scalebar (bottom-left,
 * bottom-right or bottom-centered) and no control over the size (it is sized automatically to fit comfortably within
 * the MapView).
 *
 * For example:
 * ```
 * val scalebar = Scalebar(mapView.context)
 * scalebar.alignment = Scalebar.Alignment.CENTER // optionally override default settings
 * scalebar.addToMapView(mapView);
 * ```
 *
 * _Workflow 2:_
 *
 * Alternatively, the app could define a Scalebar anywhere it likes in its view hierarchy, because Scalebar extends the
 * Android View class. The system will instantiate the Scalebar. The app then calls [bindTo] to make it come to life as a
 * scalebar for the given MapView. This workflow gives the app complete control over where the scalebar is displayed -
 * it could be positioned on top of any part of the MapView, or placed somewhere outside the bounds of the MapView.
 * It also gives the app complete control over the size of the scalebar.
 *
 * Here's example XML code to define a Scalebar:
 * ```
 * <com.esri.arcgisruntime.toolkit.scalebar.Scalebar
 * android:id="@+id/scalebar"
 * android:layout_width="300dp"
 * android:layout_height="45dp"
 * android:layout_margin="5dp"
 * app:alternateFillColor="@android:color/holo_orange_light"
 * app:fillColor="@android:color/holo_orange_dark"
 * app:layout_constraintLeft_toLeftOf="@+id/mapview"
 * app:layout_constraintTop_toTopOf="@+id/mapview"
 * app:lineColor="#FFC0C0C0"
 * app:style="graduatedLine" />
 * ```
 *
 * Here's example Java code to bind the Scalebar to the MapView:
 * ```
 * val scalebar = findViewById(R.id.scalebar);
 * scalebar.bindTo(mapView);
 * ```
 *
 * _Mutually Exclusive Workflows:_
 *
 * The methods to connect and disconnect a Scalebar to a MapView are mutually exclusive between the two workflows. In
 * Workflow 1, use [addToMapView] to connect it to a MapView and [removeFromMapView] to
 * disconnect it. In Workflow 2, use [bindTo] to connect it to a MapView and [bindTo], passing **_null_** as an argument
 * to disconnect it.
 *
 * @since 100.5.0
 */
class Scalebar : View {

    /**
     * The [Style] of Scalebar that will be rendered. One of:
     * - [Style.BAR]
     * - [Style.ALTERNATING_BAR]
     * - [Style.LINE]
     * - [Style.GRADUATED_LINE]
     * - [Style.DUAL_UNIT_LINE]
     *
     * @since 100.5.0
     */
    var style: Style = DEFAULT_STYLE
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * The alignment of the Scalebar when using Workflow 1 (see [Scalebar] and [addToMapView]). One of:
     * - [Alignment.LEFT]
     * - [Alignment.RIGHT]
     * - [Alignment.CENTER]
     *
     * @since 100.5.0
     */
    var alignment = DEFAULT_ALIGNMENT
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * The resolved color used to fill bar based Scalebars. Used in [Style.BAR] and [Style.ALTERNATING_BAR].
     *
     * @since 100.5.0
     */
    var fillColor: Int = DEFAULT_FILL_COLOR
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * The resolved color used to fill the alternate bars in bar based Scalebars. Used in [Style.ALTERNATING_BAR].
     *
     * @since 100.5.0
     */
    var alternateFillColor: Int = DEFAULT_ALTERNATE_FILL_COLOR
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * The resolved color used to draw the lines in line based Scalebars. Used in [Style.LINE], [Style.GRADUATED_LINE] and
     * [Style.DUAL_UNIT_LINE].
     *
     * @since 100.5.0
     */
    var lineColor: Int = DEFAULT_LINE_COLOR
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * The resolved color used to draw shadows around bars and lines.
     *
     * @since 100.5.0
     */
    var shadowColor: Int = DEFAULT_SHADOW_COLOR
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * The resolved color used to draw text.
     *
     * @since 100.5.0
     */
    var textColor: Int = DEFAULT_TEXT_COLOR
        set(value) {
            field = value
            textPaint.color = value
            postInvalidate()
        }

    /**
     * The resolved color used to draw text shadows.
     *
     * @since 100.5.0
     */
    var textShadowColor = DEFAULT_TEXT_SHADOW_COLOR
        set(value) {
            field = value
            textPaint.setShadowLayer(2f, SHADOW_OFFSET_PIXELS, SHADOW_OFFSET_PIXELS, value)
            postInvalidate()
        }

    /**
     * The size of the text displayed in a [Scalebar] in pixels.
     *
     * @since 100.5.0
     */
    var textSize: Int = resources.getDimensionPixelSize(R.dimen.scalebar_default_text_size)
        set(value) {
            field = value
            textPaint.textSize = value.toFloat()
            postInvalidate()
        }

    /**
     * The [Typeface] used to draw text in a Scalebar.
     *
     * @since 100.5.0
     */
    var typeface: Typeface = DEFAULT_TYPEFACE
        set(value) {
            field = value
            textPaint.typeface = value
            postInvalidate()
        }

    /**
     * The [UnitSystem] used that the Scalebar is representing. One of:
     * - [UnitSystem.IMPERIAL]
     * - [UnitSystem.METRIC]
     *
     * @since 100.5.0
     */
    var unitSystem: UnitSystem = DEFAULT_UNIT_SYSTEM
        set(value) {
            field = value
            postInvalidate()
        }

    private var mapView: MapView? = null
    private var drawInMapView: Boolean = false

    private val displayDensity: Float by lazy {
        context.resources.displayMetrics.density
    }

    private var textPaint: Paint = Paint().apply {
        color = textColor
        setShadowLayer(2f, SHADOW_OFFSET_PIXELS, SHADOW_OFFSET_PIXELS, textShadowColor)
        typeface = this@Scalebar.typeface
        textSize = this@Scalebar.textSize.toFloat()
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
     * Constructs a Scalebar programmatically using the provided [context]. Called by the app when Workflow 1 is
     * used (see [Scalebar] above).
     *
     * @since 100.5.0
     */
    constructor(context: Context) : super(context) {
        style = DEFAULT_STYLE
    }

    /**
     * Constructor that's called when inflating a Scalebar from XML using the provided [context] and [attrs]. Called by
     * the system when Workflow 2 is used (see [Scalebar] above).
     *
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
                unitSystemFromInt(getInt(R.styleable.Scalebar_unitSystem, 1)).let {
                    unitSystem = it
                }
                Alignment.fromInt(getInt(R.styleable.Scalebar_alignment, DEFAULT_ALIGNMENT.value))?.let {
                    alignment = it
                }
                fillColor = getColor(R.styleable.Scalebar_fillColor, DEFAULT_FILL_COLOR)
                alternateFillColor = getColor(R.styleable.Scalebar_alternateFillColor, DEFAULT_ALTERNATE_FILL_COLOR)
                lineColor = getColor(R.styleable.Scalebar_lineColor, DEFAULT_LINE_COLOR)
                shadowColor = getColor(R.styleable.Scalebar_shadowColor, DEFAULT_SHADOW_COLOR)
                textColor = getColor(R.styleable.Scalebar_textColor, DEFAULT_TEXT_COLOR)
                textShadowColor = getColor(R.styleable.Scalebar_textShadowColor, DEFAULT_TEXT_SHADOW_COLOR)
                textSize = getDimensionPixelSize(
                    R.styleable.Scalebar_textSize,
                    resources.getDimensionPixelSize(R.dimen.scalebar_default_text_size)
                )
            } finally {
                recycle()
            }
        }
    }

    /**
     * Adds this [Scalebar] to the provided [mapView]. Used in Workflow 1 (see [Scalebar] above).
     *
     * @throws IllegalStateException    if this Scalebar is already added to or bound to a MapView
     * @since 100.5.0
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
     * Removes this Scalebar from the MapView it was added to (if any). For use in Workflow 1 only (see [Scalebar] above).
     *
     * @throws IllegalStateException if this Scalebar is not currently added to a MapView
     * @since 100.5.0
     */
    fun removeFromMapView() {
        if (!drawInMapView) {
            throw IllegalStateException("Scalebar is not currently added to a MapView")
        }
        mapView?.removeView(this)
        removeListenersFromMapView()
        drawInMapView = false
        mapView = null
    }

    /**
     * Binds this [Scalebar] to the provided [mapView], or unbinds it. Used in Workflow 2 (see [Scalebar] above).
     *
     * @throws IllegalStateException if this Scalebar is currently added to a MapView
     * @since 100.5.0
     */
    fun bindTo(mapView: MapView?) {
        if (drawInMapView) {
            throw IllegalStateException("Scalebar is currently added to a MapView")
        }
        if (mapView == null) {
            if (this.mapView != null) {
                removeListenersFromMapView()
                this.mapView = null
            }
        } else {
            setupMapView(mapView)
        }
    }

    /**
     * Draws the [Scalebar] onto the provided [canvas] with the current scale.
     *
     * @since 100.5.0
     */
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
            // We shouldn't be allocating an object here but the Polyline class appears to be immutable and the
            // PolylineBuilder class doesn't allow us to clear points we've added
            val builder = PolylineBuilder(mapView.spatialReference)
            builder.addPoint(p1)
            builder.addPoint(centerPoint) // include center point to ensure it goes the correct way round the globe
            builder.addPoint(p2)
            val maxLengthGeodetic =
                GeometryEngine.lengthGeodetic(builder.toGeometry(), baseUnits, GeodeticCurveType.GEODESIC)

            // Reduce length to make its geodetic length a nice number
            var scalebarLengthGeodetic = style.renderer.calculateBestLength(maxLengthGeodetic, baseUnits)
            val scalebarLengthPixels = (maxScaleBarLengthPixels * scalebarLengthGeodetic / maxLengthGeodetic).toFloat()

            // Change units if the geodetic length is too big a number in the base units
            val displayUnits = style.renderer.selectLinearUnit(scalebarLengthGeodetic, unitSystem)
            if (displayUnits != baseUnits) {
                scalebarLengthGeodetic = baseUnits.convertTo(displayUnits, scalebarLengthGeodetic)
            }

            // Calculate screen coordinates of left, right, top and bottom of the scalebar
            val left = calculateLeftPos(alignment, scalebarLengthPixels, displayUnits)
            val right = left + scalebarLengthPixels
            val maxPixelsBelowBaseline: Float = textPaint.fontMetrics?.bottom ?: 0.0f
            val bottom = if (drawInMapView) {
                mapView.height.toFloat() - attributionTextHeight - (mapView.viewInsetBottom + SCALEBAR_Y_PAD_DP).dpToPixels(
                    displayDensity
                ) - textSize - maxPixelsBelowBaseline
            } else {
                height.toFloat() - textSize - maxPixelsBelowBaseline
            }

            val top: Float =
                bottom - if (drawInMapView) DEFAULT_BAR_HEIGHT_DP.dpToPixels(displayDensity).toFloat() else top.toFloat()

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
                textSize,
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
     * Returns the x-coordinate of the left hand end of the scalebar using the provided [alignment], [scalebarLength]
     * and [displayUnits] as a Float.
     *
     * @since 100.5.0
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
        }
    }

    /**
     * Represents the alignment of [Scalebar] to be displayed.
     *
     * @since 100.5.0
     */
    enum class Alignment(
        /**
         * @suppress
         */
        val value: Int
    ) {
        /**
         * The scalebar is left-aligned, meaning that the left hand end of the scalebar is fixed and it shrinks and grows at
         * the right hand end. If the scalebar is added to a MapView using [addToMapView], it will be
         * positioned near the bottom-left corner of the MapView.
         *
         * @since 100.5.0
         */
        LEFT(0),

        /**
         * The scalebar is right-aligned, meaning that the right hand end of the scalebar is fixed and it shrinks and grows
         * at the left hand end. If the scalebar is added to a MapView using [addToMapView], it will be
         * positioned near the bottom-right corner of the MapView.
         *
         * @since 100.5.0
         */
        RIGHT(1),

        /**
         * The scalebar is center-aligned, meaning that the center point of the scalebar is fixed and it shrinks and grows
         * at both ends. If the scalebar is added to a MapView using [addToMapView], it will be
         * positioned near the bottom the MapView, centered between the left and right edges.
         *
         * @since 100.5.0
         */
        CENTER(2);

        companion object {
            private val map = Alignment.values().associateBy(Alignment::value)
            /**
             * @suppress
             */
            fun fromInt(type: Int) = map[type]
        }
    }
}
