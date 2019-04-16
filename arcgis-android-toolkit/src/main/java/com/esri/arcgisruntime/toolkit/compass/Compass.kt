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

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.GeoView
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.SceneView
import com.esri.arcgisruntime.mapping.view.ViewpointChangedListener
import com.esri.arcgisruntime.toolkit.R
import com.esri.arcgisruntime.toolkit.extension.throwIfNotPositive
import com.esri.arcgisruntime.toolkit.extension.toDp
import com.esri.arcgisruntime.toolkit.extension.toPixels


private const val AUTO_HIDE_THRESHOLD = 0.00000000001
private const val FADE_ANIMATION_DELAY_MILLISECS = 300L
private const val FADE_ANIMATION_DURATION_MILLISECS = 500L

/**
 * Shows the current orientation of a map or scene by displaying a compass icon that points towards North. The icon can
 * be tapped to reset the map/scene to 0 degrees orientation. By default the icon is hidden any time the map/scene is
 * orientated to 0 degrees. The auto hide behavior can be disabled using the property [isAutoHidden].
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
 * compass.isAutoHidden = false // optionally disable the auto hide behavior
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
 * android:layout_height="50dp"
 * android:layout_margin="5dp"
 * app:autoHidden="false"/>
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
 * The methods to connect and disconnect a Compass to a GeoView are mutually exclusive between the two workflows. In
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
         */
        const val DEFAULT_HEIGHT_AND_WIDTH_DP = 50
    }

    private val compassBitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.ic_compass)
    }

    private val compassMatrix: Matrix = Matrix()
    private var compassIsShown = false

    /**
     * Whether this Compass is automatically hidden when the map/scene rotation is 0 degrees.
     *
     * @since 100.5.0
     */
    var isAutoHidden: Boolean = true
        set(value) {
            field = value
            showOrHide()
        }

    private var geoView: GeoView? = null
    private var compassRotation: Double = 0.0
    private var drawInGeoView: Boolean = false
    private val displayDensity: Float by lazy {
        resources.displayMetrics.density
    }
    private val defaultLayoutParams = ViewGroup.LayoutParams(
        Companion.DEFAULT_HEIGHT_AND_WIDTH_DP.toPixels(displayDensity),
        Companion.DEFAULT_HEIGHT_AND_WIDTH_DP.toPixels(displayDensity)
    )

    private val viewpointChangedListener = ViewpointChangedListener {
        geoView?.let { geoView ->
            (geoView as? MapView)?.let {
                compassRotation = it.mapRotation
            }
            (geoView as? SceneView)?.currentViewpointCamera?.let {
                compassRotation = it.heading
            }
        }

        // Show or hide, depending on whether auto-hide is enabled, and if so depending on current rotation
        showOrHide()

        // Invalidate the Compass view to update it
        postInvalidate()
    }

    private val attributionViewLayoutChangeListener =
        OnLayoutChangeListener { _: View, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int ->
            // Invalidate the Compass view when the bounds of the attribution view change; this happens when view insets are
            // set, which may affect where the Compass is drawn
            postInvalidate()
        }

    /**
     * Constructs a Compass programmatically. Called by the app when Workflow 1 is used (see [Compass] above).
     *
     * @param context the execution [Context]
     * @since 100.5.0
     */
    constructor(context: Context) : super(context)

    /**
     * Constructor that's called when inflating a Compass from XML. Called by the system when Workflow 2 is used (see
     * [Compass] above).
     *
     * @param context the execution [Context]
     * @param attrs   the attributes of the XML tag that is inflating the view
     * @since 100.5.0
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.Compass,
            0, 0
        ).apply {
            try {
                isAutoHidden = getBoolean(R.styleable.Compass_autoHidden, true)
            } finally {
                recycle()
            }
        }
    }

    init {
        compassIsShown = !isAutoHidden
        alpha = if (compassIsShown) 1.0f else 0.0f
        showOrHide()
        setOnTouchListener { _, _ ->
            performClick()
            true
        }
    }

    /**
     * Adds this Compass to the given GeoView. Used in Workflow 1 (see [Compass] above).
     *
     * @param geoView the GeoView
     * @throws IllegalStateException    if this [Compass] is already added to or bound to a GeoView
     * @since 100.5.0
     */
    fun addToGeoView(geoView: GeoView) {
        this.geoView?.let {
            throw IllegalStateException("Compass already has a GeoView")
        }
        drawInGeoView = true
        Math.min(height, width).let {
            geoView.addView(this, ViewGroup.LayoutParams(it, it))
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
     * Binds this [Compass] to the given GeoView, or unbinds it. Used in Workflow 2 (see [Compass] above).
     *
     * @param geoView the GeoView to bind to, or null to unbind it
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
     * Provide a DP value to set the height of the [Compass]. Must be called after the [View] has been measured as
     * otherwise the [ViewGroup.LayoutParams] are null.
     *
     * @throws [IllegalStateException] if [View] hasn't been measured yet
     * @param height DP height to apply to the [Compass]
     * @since 100.5.0
     */
    fun setHeightDp(height: Int) {
        height.throwIfNotPositive()

        if (layoutParams == null) {
            throw IllegalStateException("View hasn't been measured yet")
        }
        layoutParams.height = height.toPixels(displayDensity)
        if (!isInLayout) {
            requestLayout()
        }
    }

    /**
     * Get the DP height of the [Compass]. Must be called after the [View] has been measured as
     * otherwise the [ViewGroup.LayoutParams] are null.
     *
     * @throws [IllegalStateException] if [View] hasn't been measured yet
     * @since 100.5.0
     */
    fun getHeightDp(): Int {
        layoutParams?.height?.toDp(displayDensity)?.let {
            return it
        }
        throw IllegalStateException("View hasn't been measured yet")
    }

    /**
     * Provide a DP value to set the width of the [Compass]. Must be called after the [View] has been measured as
     * otherwise the [ViewGroup.LayoutParams] are null.
     *
     * @throws [IllegalStateException] if [View] hasn't been measured yet
     * @param width DP width to apply to the [Compass]
     * @since 100.5.0
     */
    fun setWidthDp(width: Int) {
        width.throwIfNotPositive()

        if (layoutParams == null) {
            throw IllegalStateException("View hasn't been measured yet")
        }
        layoutParams.width = width.toPixels(displayDensity)
        if (!isInLayout) {
            requestLayout()
        }
    }

    /**
     * Get the DP width of the [Compass]. Must be called after the [View] has been measured as
     * otherwise the [ViewGroup.LayoutParams] are null.
     *
     * @throws [IllegalStateException] if [View] hasn't been measured yet
     * @since 100.5.0
     */
    fun getWidthDp(): Int {
        layoutParams?.width?.toDp(displayDensity)?.let {
            return it
        }
        throw IllegalStateException("View hasn't been measured yet")
    }

    /**
     * Resets the GeoView to be oriented toward 0 degrees when the [Compass] is clicked.
     *
     * @return true if there was an assigned [View.OnClickListener] that was called, false otherwise
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
     * Measure the view and its content to determine the measured width and the
     * measured height.
     * Overridden to determine if user has used Method 1 or Method 2 (see [Compass] above]. If user has used Method 1,
     * no [ViewGroup.LayoutParams] have been provided and we fallback to using defaultLayoutParams.
     *
     * @param widthMeasureSpec horizontal space requirements as imposed by the parent.
     *                         The requirements are encoded with
     *                         {@link android.view.View.MeasureSpec}.
     * @param heightMeasureSpec vertical space requirements as imposed by the parent.
     *                         The requirements are encoded with
     *                         {@link android.view.View.MeasureSpec}.
     * @since 100.5.0
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        this.setMeasuredDimension(
            View.MeasureSpec.getSize(widthMeasureSpec),
            View.MeasureSpec.getSize(heightMeasureSpec)
        )
        if (measuredWidth == 0 || measuredHeight == 0) {
            layoutParams = defaultLayoutParams
            if (!isInLayout) {
                requestLayout()
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * Draws the [Compass] with the current rotation to the screen.
     *
     * @param canvas the [Canvas] to draw on
     * @since 100.5.0
     */
    override fun onDraw(canvas: Canvas?) {
        // Set the position of the compass if it's being drawn within the GeoView (workflow 1)
        val sizeDp = Math.min(measuredHeight, measuredWidth)
        if (drawInGeoView) {
            geoView?.let {
                var xPos = (it.right - (0.02f * it.width)) - sizeDp
                var yPos = it.top + (0.02f * it.height)
                // If the GeoView is a MapView, adjust the position to take account of any view insets that may be set
                (geoView as? MapView)?.let { mapView ->
                    xPos -= mapView.viewInsetRight.toPixels(displayDensity).toFloat()
                    yPos += mapView.viewInsetTop.toPixels(displayDensity).toFloat()
                }
                x = xPos
                y = yPos
            }
        }

        // Setup a matrix with the correct compassRotation
        compassMatrix.reset()
        compassMatrix.postRotate(-compassRotation.toFloat(), (compassBitmap.width / 2F), (compassBitmap.height / 2F))

        // Scale the matrix by the size of the bitmap to the size of the compass view
        val xScale = sizeDp.toFloat() / compassBitmap.width
        val yScale = sizeDp.toFloat() / compassBitmap.height
        compassMatrix.postScale(xScale, yScale)

        // Draw the bitmap
        canvas?.drawBitmap(compassBitmap, compassMatrix, null)
    }

    /**
     * Sets up the [Compass] to work with the given GeoView.
     *
     * @param geoView the GeoView
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
     * Show or hide the [Compass], depending on whether auto-hide is enabled, and if so whether the current rotation is less
     * than the threshold. Handle 0 and 360 degrees.
     *
     * @since 100.5.0
     */
    private fun showOrHide() {
        geoView?.let {
            with(compassRotation) {
                // If auto-hide is enabled, hide if compassRotation is less than the threshold
                if (isAutoHidden && (this < AUTO_HIDE_THRESHOLD || (360 - this) < AUTO_HIDE_THRESHOLD)) {
                    if (compassIsShown) {
                        showCompass(false)
                    }
                } else {
                    // Otherwise show the compass
                    if (!compassIsShown) {
                        showCompass(true)
                    }
                }
            }
        }
    }

    /**
     * Shows or hides the [Compass], using an animator to make it fade in and out.
     *
     * @param show true to show the [Compass], false to hide it
     * @since 100.5.0
     */
    private fun showCompass(show: Boolean) {
        // Set the desired state in mIsShown
        compassIsShown = show

        // Post a Runnable to the main UI thread, to run after a short delay; the delay prevents the Compass from starting
        // to fade as it momentarily passes through north
        with(Handler(Looper.getMainLooper())) {
            this.postDelayed({
                // Check if the conditions for showing/hiding still hold now the delay has happened
                if (show == compassIsShown) {
                    // Create an animator that changes the View's alpha to 1.0 (opaque) if we are showing or 0.0 (transparent) if
                    // we are hiding
                    ObjectAnimator.ofFloat(this@Compass, "alpha", if (show) 1.0f else 0.0f)
                        .setDuration(FADE_ANIMATION_DURATION_MILLISECS).start()
                }
            }, FADE_ANIMATION_DELAY_MILLISECS)
        }
    }
}
