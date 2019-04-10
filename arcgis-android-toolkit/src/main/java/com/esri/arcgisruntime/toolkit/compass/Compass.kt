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
import com.esri.arcgisruntime.mapping.view.*
import com.esri.arcgisruntime.toolkit.R
import com.esri.arcgisruntime.toolkit.ToolkitUtil

private const val AUTO_HIDE_THRESHOLD = 0.00000000001
private const val FADE_ANIMATION_DELAY_MILLISECS = 300L
private const val FADE_ANIMATION_DURATION_MILLISECS = 500L
private const val DEFAULT_HEIGHT_AND_WIDTH_DP = 50

class Compass : View {

    private val compassBitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.ic_compass)
    }

    private val compassMatrix: Matrix = Matrix()
    private var compassIsShown = false

    var isAutoHide: Boolean = true
        set(value) {
            field = value
            showOrHide()
        }

    var heightDp: Int = DEFAULT_HEIGHT_AND_WIDTH_DP
        set(value) {
            ToolkitUtil.throwIfNotPositive(value, "heightDp")
            field = value
            updateSize()
        }

    var widthDp: Int = DEFAULT_HEIGHT_AND_WIDTH_DP
        set(value) {
            ToolkitUtil.throwIfNotPositive(value, "widthDp")
            field = value
            updateSize()
        }

    private var geoView: GeoView? = null
    private var compassRotation: Double = 0.0
    private var drawInGeoView: Boolean = false
    private val displayDensity: Float by lazy {
        resources.displayMetrics.density
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
        showOrHide()
        postInvalidate()
    }

    private val attributionViewLayoutChangeListener =
        OnLayoutChangeListener { _: View, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int ->
            postInvalidate()
        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        attrs?.let {
            isAutoHide = it.getAttributeBooleanValue(null, "compass.autoHide", true)
            heightDp = it.getAttributeIntValue(null, "compass.height", DEFAULT_HEIGHT_AND_WIDTH_DP)
            widthDp = it.getAttributeIntValue(null, "compass.width", DEFAULT_HEIGHT_AND_WIDTH_DP)
        }
    }

    init {
        compassIsShown = !isAutoHide
        alpha = if (compassIsShown) 1.0f else 0.0f
        showOrHide()
        setOnTouchListener { _, _ ->
            performClick()
            true
        }
    }

    fun addToGeoView(geoView: GeoView) {
        this.geoView?.let {
            throw IllegalStateException("Compass already has a GeoView")
        }
        drawInGeoView = true
        val sizeDp = Math.min(heightDp, widthDp).toDouble()
        geoView.addView(this, ViewGroup.LayoutParams(dpToPixels(sizeDp), dpToPixels(sizeDp)))
        setupGeoView(geoView)
    }

    fun removeFromGeoView() {
        if (!drawInGeoView) {
            throw IllegalStateException("Compass is not currently added to a GeoView")
        }
        geoView?.removeView(this)
        removeListenersFromGeoView()
        drawInGeoView = false
    }

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

    override fun onDraw(canvas: Canvas?) {
        // Set the position of the compass if it's being drawn within the GeoView (workflow 1)
        val sizeDp = Math.min(heightDp, widthDp)
        if (drawInGeoView) {
            geoView?.let {
                var xPos = (it.right - (0.02f * it.width)) - dpToPixels(sizeDp.toDouble())
                var yPos = it.top + (0.02f * it.height)
                // If the GeoView is a MapView, adjust the position to take account of any view insets that may be set
                (geoView as? MapView)?.let { mapView ->
                    xPos -= dpToPixels(mapView.viewInsetRight).toFloat()
                    yPos += dpToPixels(mapView.viewInsetTop).toFloat()
                }
                x = xPos
                y = yPos
            }
        }

        // Setup a matrix with the correct compassRotation
        compassMatrix.reset()
        compassMatrix.postRotate(-compassRotation.toFloat(), (compassBitmap.width / 2F), (compassBitmap.height / 2F))

        // Scale the matrix by the size of the bitmap to the size of the compass view
        val xScale = dpToPixels(sizeDp.toDouble()).toFloat() / compassBitmap.width
        val yScale = dpToPixels(sizeDp.toDouble()).toFloat() / compassBitmap.height
        compassMatrix.postScale(xScale, yScale)

        // Draw the bitmap
        canvas?.drawBitmap(compassBitmap, compassMatrix, null)
    }

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

    private fun removeListenersFromGeoView() {
        geoView = geoView?.let {
            it.removeViewpointChangedListener(viewpointChangedListener)
            it.removeAttributionViewLayoutChangeListener(attributionViewLayoutChangeListener)
        }.let { null }
    }

    private fun showOrHide() {
        // If auto-hide is enabled, hide if compassRotation is less than the threshold
        geoView?.let {
            with(compassRotation) {
                if (isAutoHide && (this < AUTO_HIDE_THRESHOLD || (360 - this) < AUTO_HIDE_THRESHOLD)) {
                    if (compassIsShown) {
                        showCompass(false)
                    }
                } else {
                    if (!compassIsShown) {
                        showCompass(true)
                    }
                }
            }
        }
    }

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

    private fun updateSize() {
        if (drawInGeoView) {
            Math.min(heightDp, widthDp).let {
                layoutParams.apply {
                    height = it
                    width = it
                }
            }
        }
        postInvalidate()
    }

    private fun dpToPixels(dp: Double): Int = Math.round((dp * displayDensity).toFloat())
}