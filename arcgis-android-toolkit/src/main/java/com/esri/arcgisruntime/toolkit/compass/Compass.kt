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

package com.esri.arcgisruntime.toolkit.compass

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.GeoView
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.SceneView
import com.esri.arcgisruntime.mapping.view.ViewpointChangedListener
import com.esri.arcgisruntime.toolkit.R
import com.esri.arcgisruntime.toolkit.extension.dpToPixels
import com.esri.arcgisruntime.toolkit.extension.pixelsToDp
import com.esri.arcgisruntime.toolkit.extension.throwIfNotPositive
import kotlin.reflect.KProperty


private const val AUTO_HIDE_THRESHOLD_DEGREES = 0.25
private const val ANIMATION_DURATION_MILLISECS = 500L

/**
 * Shows the current orientation of a map or scene by displaying a compass icon that points towards North. The icon can
 * be tapped to reset the map/scene to 0 degrees orientation. By default the icon is hidden any time the map/scene is
 * orientated to 0 degrees. The auto hide behavior can be disabled using the property [isAutoHide].
 *
 * Two workflows are supported:
 *
 * _Workflow 1:_
 *
 * The simplest workflow is for the app to instantiate a Compass using an instance of [Context] and call
 * [addToGeoView] to display it within the GeoView. Optionally, properties may be set to override
 * some of the default settings. This workflow gives the app no control over the position of the compass - it's always
 * placed at the top-right corner of the GeoView.
 *
 * For example:
 * ```
 * val compass: Compass = Compass(geoView.context);
 * compass.isAutoHide = false // optionally disable the auto hide behavior
 * compass.addToGeoView(geoView)
 * ```
 *
 * _Workflow 2:_
 *
 * Alternatively, the app could define a Compass anywhere it likes in its view hierarchy, because Compass extends the
 * Android View class. The system will instantiate the Compass. The app then calls [bindTo] to make it
 * come to life as a compass for the given GeoView. This workflow gives the app complete control over where the compass
 * is displayed - it could be positioned on top of any part of the GeoView, or placed somewhere outside the bounds of the GeoView.
 *
 * Here's example XML code to define a Compass:
 * ```
 * <com.esri.arcgisruntime.toolkit.compass.Compass
 * android:id="@+id/compass"
 * android:layout_width="100dp"
 * android:layout_height="100dp"
 * android:layout_margin="5dp"
 * app:autoHide="false"/>
 * ```
 *
 * And here's example Kotlin code to bind the Compass to the GeoView:
 * ```
 * val compass = findViewById(R.id.compass)
 * compass.bindTo(geoView)
 * ```
 *
 * _Mutually Exclusive Workflows:_
 *
 * The functions to connect and disconnect a Compass to a GeoView are mutually exclusive between the two workflows. In
 * Workflow 1, use [addToGeoView] to connect it to a GeoView and [removeFromGeoView] to disconnect it. In Workflow 2,
 * use [bindTo], passing a non-null instance of GeoView as an argument to connect it to a GeoView and [bindTo],
 * passing **_null_** as an argument to disconnect it.
 *
 * @since 100.5.0
 */
class Compass : View {

    companion object {
        /**
         * Default height and width of [Compass] in DP
         * @suppress
         */
        const val DEFAULT_HEIGHT_AND_WIDTH_DP = 50
    }

    private val compassBitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.ic_compass)
    }

    private val compassMatrix: Matrix = Matrix()
    private var geoView: GeoView? = null

    private var compassRotation: Double = 0.0
        set(value) {
            field = value
            showOrHide()
        }

    private var drawInGeoView: Boolean = false

    private val displayDensity: Float by lazy {
        resources.displayMetrics.density
    }

    private val defaultLayoutParams by lazy {
        ViewGroup.LayoutParams(
            DEFAULT_HEIGHT_AND_WIDTH_DP.dpToPixels(displayDensity),
            DEFAULT_HEIGHT_AND_WIDTH_DP.dpToPixels(displayDensity)
        )
    }

    private val viewpointChangedListener = ViewpointChangedListener {
        geoView?.let { geoView ->
            (geoView as? MapView)?.let {
                compassRotation = it.mapRotation
            }
            (geoView as? SceneView)?.currentViewpointCamera?.let {
                compassRotation = it.heading
            }
        }

        // Invalidate the Compass view to update it
        postInvalidate()
    }

    private val attributionViewLayoutChangeListener = OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
        // Invalidate the Compass view when the bounds of the attribution view change; this happens when view insets are
        // set, which may affect where the Compass is drawn
        postInvalidate()
    }

    /**
     * A [ViewPropertyAnimator] that animates the [Compass]. Using a [AnimatorDelegate] to ensure that an animation cannot
     * be run before the previous animation is completed by nulling the backing property and returning null until the
     * animation is complete.
     */
    private val animator: ViewPropertyAnimator? by AnimatorDelegate()

    private class AnimatorDelegate {
        private var viewPropertyAnimator: ViewPropertyAnimator? = null

        operator fun getValue(compass: Compass, property: KProperty<*>): ViewPropertyAnimator? {
            viewPropertyAnimator?.let {
                return null
            }
            viewPropertyAnimator = compass.animate()
                .setDuration(ANIMATION_DURATION_MILLISECS)
                .withEndAction {
                    viewPropertyAnimator = null
                }
            return viewPropertyAnimator
        }
    }

    /**
     * Whether this Compass is automatically hidden when the map/scene rotation is 0 degrees.
     *
     * @since 100.5.0
     */
    var isAutoHide: Boolean = true
        set(value) {
            field = value
            showOrHide()
        }

    /**
     * Constructs a Compass programmatically using the [context] provided. Called by the app when Workflow 1 is used (see [Compass] above).
     *
     * @since 100.5.0
     */
    constructor(context: Context) : super(context) {
        layoutParams = defaultLayoutParams
        initializeCompass()
    }

    /**
     * Constructor that's called when inflating a Compass from XML using the [context] and [attrs] provided by the system.
     * Called by the system when Workflow 2 is used (see [Compass] above).
     *
     * @since 100.5.0
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.Compass,
            0, 0
        ).apply {
            try {
                isAutoHide = getBoolean(R.styleable.Compass_autoHide, true)
            } finally {
                recycle()
            }
        }
        initializeCompass()
    }

    /**
     * Called during construction to set the initial alpha value for the [Compass] and set the [View.OnTouchListener] to listen
     * for touches from the user.
     *
     * @since 100.5.0
     */
    private fun initializeCompass() {
        alpha = if (isAutoHide) 0.0f else 1.0f
        setOnTouchListener { _, _ ->
            performClick()
            true
        }
    }

    /**
     * Adds this Compass to the provided [geoView]. Used in Workflow 1 (see [Compass] above).
     *
     * @throws IllegalStateException    if this [Compass] is already added to or bound to a GeoView
     * @since 100.5.0
     */
    fun addToGeoView(geoView: GeoView) {
        this.geoView?.let {
            throw IllegalStateException("Compass already has a GeoView")
        }
        drawInGeoView = true
        layoutParams.let {
            Math.min(it.height, it.width)
        }.let {
            geoView.addView(this@Compass, ViewGroup.LayoutParams(it, it))
        }
        setupGeoView(geoView)
    }

    /**
     * Removes this Compass from the GeoView it was added to (if any). For use in Workflow 1 only (see [Compass]
     * above).
     *
     * @throws IllegalStateException if this Compass is not currently added to a GeoView
     * @since 100.5.0
     */
    fun removeFromGeoView() {
        if (!drawInGeoView) {
            throw IllegalStateException("Compass is not currently added to a GeoView")
        }
        geoView?.removeView(this)
        removeListenersFromGeoView()
        drawInGeoView = false
    }

    /**
     * Binds this [Compass] to the provided [geoView], or unbinds it. Used in Workflow 2 (see [Compass] above).
     *
     * @throws IllegalStateException if this [Compass] is currently added to a GeoView
     * @since 100.5.0
     */
    fun bindTo(geoView: GeoView?) {
        if (drawInGeoView) {
            throw IllegalStateException("Compass is currently added to a GeoView")
        }
        if (geoView == null) {
            if (this.geoView != null) {
                removeListenersFromGeoView()
            }
        } else {
            setupGeoView(geoView)
        }
    }

    /**
     * Provide a [height] DP value to set the height of the [Compass]. Must be positive.
     *
     * @throws [IllegalArgumentException] if [height] isn't positive
     * @since 100.5.0
     */
    fun setHeightDp(height: Int) {
        height.throwIfNotPositive("height")
        layoutParams.height = height.dpToPixels(displayDensity)
        if (!isInLayout) {
            requestLayout()
        }
    }

    /**
     * Get the DP height of the [Compass].
     *
     * @since 100.5.0
     */
    fun getHeightDp(): Int {
        return layoutParams.height.pixelsToDp(displayDensity)
    }

    /**
     * Provide a [width] DP value to set the width of the [Compass]. Must be positive.
     *
     * @throws [IllegalArgumentException] if [width] isn't positive
     * @since 100.5.0
     */
    fun setWidthDp(width: Int) {
        width.throwIfNotPositive("width")
        layoutParams.width = width.dpToPixels(displayDensity)
        if (!isInLayout) {
            requestLayout()
        }
    }

    /**
     * Get the DP width of the [Compass].
     *
     * @since 100.5.0
     */
    fun getWidthDp(): Int {
        return layoutParams.width.pixelsToDp(displayDensity)
    }

    /**
     * Resets the GeoView to be oriented toward 0 degrees when the [Compass] is clicked. Returns true if there was an
     * assigned [View.OnClickListener] that was called, false otherwise.
     *
     * @since 100.5.0
     */
    override fun performClick(): Boolean {
        geoView?.let {
            (it as? MapView)?.apply {
                setViewpointRotationAsync(0.0)
            }
            (it as? SceneView)?.apply {
                this.currentViewpointCamera?.let { camera ->
                    this.setViewpointCameraAsync(Camera(camera.location, 0.0, camera.pitch, camera.roll))
                }
            }
        }
        return super.performClick()
    }

    /**
     * Measure the view using the provided [widthMeasureSpec] and [heightMeasureSpec] and its content to determine the
     * measured width and the measured height.
     * Overridden to force a "square" View, using the lowest dimension applied to width and height.
     *
     * @since 100.5.0
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Math.min(measuredWidth - paddingLeft - paddingRight, measuredHeight - paddingTop - paddingBottom).let {
            setMeasuredDimension(it + paddingLeft + paddingRight, it + paddingTop + paddingBottom)
        }
    }

    /**
     * Draws the [Compass] onto the provided [canvas] with the current rotation to the screen.
     *
     * @since 100.5.0
     */
    override fun onDraw(canvas: Canvas?) {
        val preferredSizePx =
            Math.min(measuredHeight - paddingTop - paddingBottom, measuredWidth - paddingLeft - paddingRight)
        // Set the position of the compass if it's being drawn within the GeoView (workflow 1)
        if (drawInGeoView) {
            geoView?.let {
                var xPos = (0.98f * it.width) - preferredSizePx
                var yPos = (0.02f * it.height)
                // If the GeoView is a MapView, adjust the position to take account of any view insets that may be set
                (geoView as? MapView)?.let { mapView ->
                    xPos -= mapView.viewInsetRight.dpToPixels(displayDensity).toFloat()
                    yPos += mapView.viewInsetTop.dpToPixels(displayDensity).toFloat()
                }
                x = xPos
                y = yPos
            }
        }

        // Setup a matrix with the correct compassRotation
        compassMatrix.reset()
        compassMatrix.postRotate(-compassRotation.toFloat(), (compassBitmap.width / 2F), (compassBitmap.height / 2F))

        // Scale the matrix by the size of the bitmap to the size of the compass view
        val xScale = preferredSizePx.toFloat() / compassBitmap.width
        val yScale = preferredSizePx.toFloat() / compassBitmap.height
        compassMatrix.postScale(xScale, yScale)

        // Translate matrix to obey padding
        compassMatrix.postTranslate(paddingLeft.toFloat(), paddingTop.toFloat())

        // Draw the bitmap
        canvas?.drawBitmap(compassBitmap, compassMatrix, null)
    }

    /**
     * Sets up the [Compass] to work with the provided [geoView].
     *
     * @since 100.5.0
     */
    private fun setupGeoView(geoView: GeoView) {
        // Remove listeners from old GeoView
        this.geoView?.let {
            removeListenersFromGeoView()
        }

        // Add listeners to new GeoView
        this.geoView = geoView
        geoView.addViewpointChangedListener(viewpointChangedListener)
        geoView.addAttributionViewLayoutChangeListener(attributionViewLayoutChangeListener)
    }

    /**
     * Removes the listeners from [geoView].
     *
     * @since 100.5.0
     */
    private fun removeListenersFromGeoView() {
        geoView = geoView?.let {
            it.removeViewpointChangedListener(viewpointChangedListener)
            it.removeAttributionViewLayoutChangeListener(attributionViewLayoutChangeListener)
        }.let { null }
    }

    /**
     * Set the alpha value of the [Compass], depending on whether auto-hide is enabled, and if so whether the current rotation is less
     * than the threshold. Handle 0 and 360 degrees.
     *
     * @since 100.5.0
     */
    private fun showOrHide() {
        // Using the animator property, set the View's alpha to 1.0 (opaque) if we are showing or 0.0 (transparent)
        // if we are hiding
        if (isAutoHide) {
            if (alpha == 1.0f && (compassRotation < AUTO_HIDE_THRESHOLD_DEGREES || (360 - compassRotation) < AUTO_HIDE_THRESHOLD_DEGREES)) {
                animator?.alpha(0.0f)
            } else if (alpha == 0.0f && (compassRotation > AUTO_HIDE_THRESHOLD_DEGREES && ((360 - compassRotation) > AUTO_HIDE_THRESHOLD_DEGREES))) {
                animator?.alpha(1.0f)
            }
        } else {
            animator?.alpha(1.0f)
        }
    }
}
