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

package com.esri.arcgisruntime.toolkit.ar

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.esri.arcgisruntime.toolkit.R
import com.esri.arcgisruntime.toolkit.ar.ArCalibrationView.Companion.SCENEVIEW_CALIBRATING_OPACITY
import com.esri.arcgisruntime.toolkit.control.JoystickSeekBar
import kotlinx.android.synthetic.main.view_ar_calibration.view.elevationControl
import kotlinx.android.synthetic.main.view_ar_calibration.view.headingControl

/**
 * The ArCalibrationView allows the modification of the following properties of the SceneView within
 * an [ArcGISArView]:
 * - Elevation
 * - Heading
 *
 * Modification of these properties can be achieved by binding an ArcGISArView to the ArCalibrationView
 * using the [bindArcGISArView] function and subsequently using the controls provided by the
 * ArCalibrationView.
 *
 * Upon binding, the opacity of the base layer in the SceneView is set to the
 * [SCENEVIEW_CALIBRATING_OPACITY] value to aide in calibration.
 *
 * When calibration is completed, use [unbindArcGISArView] to restore the [previousBaseSurfaceOpacity]
 * and release the ArcGISArView.
 *
 * _Example usage_:
 * ```
 * <com.esri.arcgisruntime.toolkit.ar.ArCalibrationView
 * android:layout_width="match_parent"
 * android:layout_height="match_parent" />
 * ```
 *
 * ```
 * arCalibrationView.bindArcGISArView(arcGisArView)
 * ```
 *
 * @since 100.6.0
 */
class ArCalibrationView : FrameLayout {

    companion object {
        /**
         * Opacity value used when ArcGISArView is bound to ArCalibrationView.
         *
         * @since 100.6.0
         */
        private const val SCENEVIEW_CALIBRATING_OPACITY = 0.65f
    }

    /**
     * The ArcGISArView that is being calibrated. This property is set during [bindArcGISArView].
     *
     * @since 100.6.0
     */
    private var arcGISArView: ArcGISArView? = null

    /**
     * The value of the base surface opacity of the SceneView before it is changed during binding of
     * the ArcGISArView. Used to restore the opacity of the SceneView during [unbindArcGISArView].
     *
     * @since 100.6.0
     */
    private var previousBaseSurfaceOpacity: Float? = null

    /**
     * Constructor used when instantiating this View directly to attach it to another view programmatically.
     *
     * @since 100.6.0
     */
    constructor(context: Context) : super(context) {
        initialize()
    }

    /**
     * Constructor used when defining this view in an XML layout.
     *
     * @since 100.6.0
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    private fun initialize() {
        inflate(context, R.layout.view_ar_calibration, this)

        elevationControl.addDeltaProgressUpdatedListener(object :
            JoystickSeekBar.DeltaProgressUpdatedListener {
            override fun onDeltaProgressUpdated(deltaProgress: Float) {
                arcGISArView?.let { arcGISArView ->
                    val camera = arcGISArView.cameraController.originCamera
                    arcGISArView.cameraController.originCamera =
                        camera.elevate(deltaProgress.toDouble())
                }
            }
        })

        headingControl.addDeltaProgressUpdatedListener(object :
            JoystickSeekBar.DeltaProgressUpdatedListener {
            override fun onDeltaProgressUpdated(deltaProgress: Float) {
                arcGISArView?.let { arcGISArView ->
                    val camera = arcGISArView.cameraController.originCamera
                    val newHeading = camera.heading + deltaProgress
                    arcGISArView.cameraController.originCamera =
                        camera.rotateTo(newHeading, camera.pitch, camera.roll)
                }
            }
        })
    }

    /**
     * Binds an ArcGISArView to an ArCalibrationView. During this, the base surface opacity is set
     * to [SCENEVIEW_CALIBRATING_OPACITY] to aid calibration for the user.
     *
     * @since 100.6.0
     */
    fun bindArcGISArView(arcGISArView: ArcGISArView) {
        this.arcGISArView = arcGISArView
        this.arcGISArView?.sceneView?.scene?.let {
            previousBaseSurfaceOpacity = it.baseSurface?.opacity
            it.baseSurface?.opacity = SCENEVIEW_CALIBRATING_OPACITY
        }
    }

    /**
     * Unbinds an ArcGISArView from an ArCalibrationView. During this, the base surface opacity is
     * set to the value used before binding.
     *
     * @since 100.6.0
     */
    fun unbindArcGISArView(arcGISArView: ArcGISArView) {
        if (this.arcGISArView == arcGISArView) {
            this.arcGISArView?.sceneView?.scene?.let {
                it.baseSurface?.opacity = previousBaseSurfaceOpacity ?: 1.0f
            }
            this.arcGISArView = null
        }
    }
}
