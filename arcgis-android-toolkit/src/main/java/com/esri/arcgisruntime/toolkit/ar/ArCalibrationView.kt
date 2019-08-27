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
import com.esri.arcgisruntime.toolkit.JoystickSeekBar
import com.esri.arcgisruntime.toolkit.R
import kotlinx.android.synthetic.main.view_ar_calibration.view.elevationControl
import kotlinx.android.synthetic.main.view_ar_calibration.view.headingControl

class ArCalibrationView : FrameLayout {

    private var arcGISArView: ArcGISArView? = null
    private val _elevationControl: JoystickSeekBar by lazy {
        elevationControl
    }

    private val _headingControl: JoystickSeekBar by lazy {
        headingControl
    }

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

        _elevationControl.addDeltaProgressUpdatedListener(object :
            JoystickSeekBar.DeltaProgressUpdatedListener {
            override fun onDeltaProgressUpdated(deltaProgress: Float) {
                arcGISArView?.let { arcGISArView ->
                    val camera = arcGISArView.cameraController.originCamera
                    arcGISArView.cameraController.originCamera =
                        camera.elevate(deltaProgress.toDouble())
                }
            }
        })

        _headingControl.addDeltaProgressUpdatedListener(object :
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

    fun bindArcGISArView(arcGISArView: ArcGISArView) {
        this.arcGISArView = arcGISArView
    }
}
