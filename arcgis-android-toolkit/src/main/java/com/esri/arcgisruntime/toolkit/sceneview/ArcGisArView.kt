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

package com.esri.arcgisruntime.toolkit.sceneview

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.SceneView
import com.esri.arcgisruntime.toolkit.R
import com.esri.arcgisruntime.toolkit.extension.logTag
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import kotlinx.android.synthetic.main.layout_arcgisarview.view._arSceneView
import kotlinx.android.synthetic.main.layout_arcgisarview.view.arcGisSceneView


private const val CAMERA_PERMISSION_CODE = 0
private const val LOCATION_PERMISSION_CODE = 1
private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
private const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION

class ArcGisArView : FrameLayout, LifecycleObserver, Scene.OnUpdateListener {

    private var renderVideoFeed: Boolean = true
    private var installRequested: Boolean = false
    private var session: Session? = null
    private val locationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            location?.let {
                originCamera?.let { camera ->
                    originCamera = Camera(
                        it.latitude,
                        it.longitude,
                        it.altitude,
                        camera.heading,
                        camera.pitch,
                        camera.roll
                    )
                }
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            Log.d(logTag, "Location - onStatusChanged: provider: $provider, status: $status, extras: $extras")
        }

        override fun onProviderEnabled(provider: String?) {
            Log.d(logTag, "Location - onProviderEnabled: provider: $provider")
        }

        override fun onProviderDisabled(provider: String?) {
            Log.d(logTag, "Location - onProviderDisabled: provider: $provider")
        }

    }

    val sceneView: SceneView get() = arcGisSceneView
    val arSceneView: ArSceneView get() = _arSceneView

    var originCamera: Camera? = null
        set(value) {
            field = value
            arcGisSceneView.setViewpointCamera(value)
        }
    var translationTransformationFactor: Double = 0.0

    constructor(context: Context, renderVideoFeed: Boolean) : super(context) {
        this.renderVideoFeed = renderVideoFeed
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ArcGisArView,
            0, 0
        ).apply {
            try {
                renderVideoFeed = getBoolean(R.styleable.ArcGisArView_renderVideoFeed, true)
            } finally {
                recycle()
            }
        }
        initialize()
    }

    /**
     * Initialize this View.
     *
     * @since 100.6.0
     */
    private fun initialize() {
        inflate(context, R.layout.layout_arcgisarview, this)
        originCamera = sceneView.currentViewpointCamera
    }

    fun startTracking() {
        // no-op
    }

    fun stopTracking() {
        // no-op
    }

    /**
     * Register this View as a [LifecycleObserver] to the provided [lifecycle].
     *
     * @since 100.6.0
     */
    fun registerLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    /**
     * The function invoked for the [Lifecycle.Event.ON_RESUME] lifecycle event.
     *
     * @since 100.6.0
     */
    @SuppressLint("MissingPermission") // suppressed as function returns if permission hasn't been granted
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        (context as? Activity)?.let {
            // ARCore requires camera permissions to operate. If we did not yet obtain runtime
            // permission on Android M and above, now is a good time to ask the user for it.
            if (!hasPermission(CAMERA_PERMISSION)) {
                requestPermission(it, CAMERA_PERMISSION, CAMERA_PERMISSION_CODE)
                return
            }

            if (!hasPermission(LOCATION_PERMISSION)) {
                requestPermission(it, LOCATION_PERMISSION, LOCATION_PERMISSION_CODE)
                return
            }
        }

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)

        if (session == null) {
            var exception: Exception? = null
            var message: String? = null
            try {
                (context as? Activity)?.let {
                    if (ArCoreApk.getInstance().requestInstall(
                            it,
                            !installRequested
                        ) == ArCoreApk.InstallStatus.INSTALL_REQUESTED
                    ) {
                        installRequested = true
                        return
                    }
                }

                // Create the session.
                session = Session(context).apply {
                    val config = Config(this)
                    config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                    config.focusMode = Config.FocusMode.AUTO
                    this.configure(config)
                    arSceneView.setupSession(this)
                }

                arSceneView.scene.addOnUpdateListener(this)

            } catch (e: UnavailableArcoreNotInstalledException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableUserDeclinedInstallationException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableApkTooOldException) {
                message = "Please update ARCore"
                exception = e
            } catch (e: UnavailableSdkTooOldException) {
                message = "Please update this app"
                exception = e
            } catch (e: UnavailableDeviceNotCompatibleException) {
                message = "This device does not support AR"
                exception = e
            } catch (e: Exception) {
                message = "Failed to create AR session"
                exception = e
            }

            if (message != null) {
                Log.e(logTag, "Exception creating session", exception)
                return
            }
        }
        arSceneView.resume()
        arcGisSceneView.resume()
    }

    /**
     * Callback that is invoked once per frame immediately before the [Scene] is updated, with the provided [frameTime]
     * which provides time information for the current frame.
     *
     * @since 100.6.0
     */
    override fun onUpdate(frameTime: FrameTime?) {
        arSceneView.arFrame?.camera?.let {
            if (it.trackingState == TrackingState.TRACKING) {
                // TODO - combine cameras and transform
            }
        }
    }

    /**
     * The function invoked for the [Lifecycle.Event.ON_PAUSE] lifecycle event.
     *
     * @since 100.6.0
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pause() {
        arcGisSceneView.pause()
        arSceneView.pause()
    }

    /**
     * The function invoked for the [Lifecycle.Event.ON_DESTROY] lifecycle event.
     *
     * @since 100.6.0
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        arcGisSceneView.dispose()
        session = null
    }

    /**
     * Check to see we have the necessary permissions for accessing the camera using the instance of [Activity].
     *
     * @since 100.6.0
     */
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check to see we have the necessary permissions for the camera using the instance of [Context], and ask for them
     * if we don't.
     *
     * @since 100.6.0
     */
    private fun requestPermission(activity: Activity, permission: String, permissionCode: Int) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), permissionCode)
    }
}
