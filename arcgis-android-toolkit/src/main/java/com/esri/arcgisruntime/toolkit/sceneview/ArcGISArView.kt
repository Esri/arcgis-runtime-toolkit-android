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
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.SceneView
import com.esri.arcgisruntime.mapping.view.TransformationMatrix
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
private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
private const val DEFAULT_TRANSLATION_TRANSFORMATION_FACTOR = 1.0

/**
 * This view simplifies the task of configuring a [SceneView] to be used for AR experiences by calculating the optimal
 * [TransformationMatrix] to be set on the [Camera] of the [SceneView] by using the translation and quaternion factors
 * provided by the camera used in ArSceneView.
 *
 * @since 100.6.0
 */
final class ArcGISArView : FrameLayout, LifecycleObserver, Scene.OnUpdateListener {

    /**
     * A Boolean defining whether a request for ARCore has been made. Used when requesting installation of ARCore.
     *
     * @since 100.6.0
     */
    private var arCoreInstallRequested: Boolean = false

    /**
     * Initial [TransformationMatrix] obtained from the initial [Camera] used by [sceneView].
     *
     * @since 100.6.0
     */
    private var initialTransformationMatrix: TransformationMatrix? = null

    /**
     * A list of [OnStateChangedListener] used to notify when the sate of this view has changed.
     *
     * @since 100.6.0
     */
    private val onStateChangedListeners: MutableList<OnStateChangedListener> = ArrayList()

    /**
     * ArcGIS SceneView used to render the data from an [ArcGISScene].
     *
     * @since 100.6.0
     */
    val sceneView: SceneView get() = arcGisSceneView

    /**
     * A SurfaceView that integrates with ARCore and renders a scene.
     *
     * @since 100.6.0
     */
    val arSceneView: ArSceneView get() = _arSceneView

    /**
     * A Camera that defines the origin of the Camera used as the viewpoint for the [SceneView]. Setting this property
     * sets the current viewpoint of the [SceneView] and the initial [TransformationMatrix] used in this view.
     *
     * @since 100.6.0
     */
    var originCamera: Camera? = null
        set(value) {
            field = value
            sceneView.setViewpointCamera(value)
            initialTransformationMatrix = value?.transformationMatrix
        }

    /**
     * Defines whether the background of the [SceneView] is transparent or not. Enabling transparency allows for the
     * [ArSceneView] to be visible underneath the SceneView.
     *
     * @since 100.6.0
     */
    private var renderVideoFeed: Boolean = true

    /**
     * This allows the "flyover" and the "table top" experience by augmenting the translation inside the
     * TransformationMatrix. Meaning that if the user moves 1 meter in real life, they could be moving faster in the
     * digital model, dependant on the value used.
     *
     * @since 100.6.0
     */
    var translationTransformationFactor: Double = DEFAULT_TRANSLATION_TRANSFORMATION_FACTOR

    /**
     * Exposes an [Exception] should it occur when using this view.
     *
     * @since 100.6.0
     */
    var error: ArcGISArViewException? = null

    /**
     * Constructor used when instantiating this View directly to attach it to another view programmatically.
     *
     * @since 100.6.0
     */
    constructor(context: Context, renderVideoFeed: Boolean) : super(context) {
        this.renderVideoFeed = renderVideoFeed
        initialize()
    }

    /**
     * Constructor used when defining this view in an XML layout.
     *
     * @since 100.6.0
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ArcGISArView,
            0, 0
        ).apply {
            try {
                renderVideoFeed = getBoolean(R.styleable.ArcGISArView_renderVideoFeed, true)
            } finally {
                recycle()
            }
        }
        initialize()
    }

    /**
     * Initialize this View by inflating the layout containing the [SceneView] and [ArSceneView].
     *
     * @since 100.6.0
     */
    private fun initialize() {
        inflate(context, R.layout.layout_arcgisarview, this)
        originCamera = sceneView.currentViewpointCamera
        initialTransformationMatrix = sceneView.currentViewpointCamera.transformationMatrix
        sceneView.setIsBackgroundTransparent(renderVideoFeed)
    }

    /**
     * Begins AR session. Should not be used in conjunction with [registerLifecycle] as when using [registerLifecycle] the
     * lifecycle of this View is maintained by the LifecycleOwner.
     *
     * @since 100.6.0
     */
    fun startTracking() {
        beginSession()
    }

    /**
     * Pauses AR session. Should not be used in conjunction with [registerLifecycle] as when using [registerLifecycle] the
     * lifecycle of this View is maintained by the LifecycleOwner.
     *
     * @since 100.6.0
     */
    fun stopTracking() {
        arSceneView.pause()
    }

    /**
     * Add a [listener] to be notified of changes to the [ArcGISArViewState].
     *
     * @since 100.6.0
     */
    fun addOnStateChangedListener(listener: OnStateChangedListener) {
        onStateChangedListeners.add(listener)
    }

    /**
     * Remove a [listener] that was previously added.
     *
     * @since 100.6.0
     */
    fun removeOnStateChangedListener(listener: OnStateChangedListener) {
        onStateChangedListeners.remove(listener)
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
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    internal fun resume() {
        beginSession()
    }

    /**
     * Checks the following perquisites required for the use of ARCore:
     * - Checks for permissions required to use ARCore.
     * - Checks for an installation of ARCore.
     *
     * If perquisites are met, the ARCore session is created and started, provided there are no exceptions. If there are
     * any exceptions related to permissions, ARCore installation or the beginning of an ARCore session, an exception is
     * caught and listeners are notified. Otherwise, listeners are notified that this view has initialized.
     *
     * This function currently assumes that the [Context] of this view is an instance of [Activity] to ensure that we can
     * request permissions. This may not always be the case and the handling of permission are under review.
     *
     * @since 100.6.0
     */
    @SuppressLint("MissingPermission") // suppressed as function returns if permission hasn't been granted
    private fun beginSession() {
        try {
            (context as? Activity)?.let {
                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!hasPermission(CAMERA_PERMISSION)) {
                    onStateChangedListeners.forEach { listener ->
                        listener.onStateChanged(ArcGISArViewState.PermissionRequired(CAMERA_PERMISSION))
                    }
                    requestPermission(it, CAMERA_PERMISSION, CAMERA_PERMISSION_CODE)
                    return
                }

                if (ArCoreApk.getInstance().requestInstall(
                        it,
                        !arCoreInstallRequested
                    ) == ArCoreApk.InstallStatus.INSTALL_REQUESTED
                ) {
                    arCoreInstallRequested = true
                    onStateChangedListeners.forEach { listener ->
                        listener.onStateChanged(ArcGISArViewState.ArCoreInstallationRequired)
                    }
                    return
                }
            }
            // Create the session.
            Session(context).apply {
                val config = Config(this)
                config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                config.focusMode = Config.FocusMode.AUTO
                this.configure(config)
                arSceneView.setupSession(this)
            }

            arSceneView.scene.addOnUpdateListener(this)
        } catch (e: Exception) {
            error = when (e) {
                is UnavailableArcoreNotInstalledException, is UnavailableUserDeclinedInstallationException -> ArcGISArViewException(
                    resources.getString(R.string.arcgisarview_exception_install_ar_core)
                )
                is UnavailableApkTooOldException -> ArcGISArViewException(resources.getString(R.string.arcgisarview_exception_update_ar_core))
                is UnavailableSdkTooOldException -> ArcGISArViewException(resources.getString(R.string.arcgisarview_exception_update_app))
                is UnavailableDeviceNotCompatibleException -> ArcGISArViewException(resources.getString(R.string.arcgisarview_exception_device_support))
                else -> ArcGISArViewException(
                    resources.getString(
                        R.string.arcgisarview_exception_failed_to_create_ar_session,
                        e.message
                    )
                )
            }
        }

        error?.let { error ->
            Log.e(logTag, error.message)
            onStateChangedListeners.forEach {
                it.onStateChanged(ArcGISArViewState.InitializationFailure(error))
            }
            this.error = null
            return
        }

        arSceneView.resume()
        sceneView.resume()
        onStateChangedListeners.forEach {
            it.onStateChanged(ArcGISArViewState.Initialized)
        }
    }

    /**
     * Callback that is invoked once per frame immediately before the [Scene] is updated, with the provided [frameTime]
     * which provides time information for the current frame.
     *
     * @since 100.6.0
     */
    override fun onUpdate(frameTime: FrameTime?) {
        arSceneView.arFrame?.camera?.let { arCamera ->
            if (arCamera.trackingState == TrackingState.TRACKING) {
                // TODO: refactor to apply translationTransformationFactor when implemented
                TransformationMatrix(
                    arCamera.displayOrientedPose.rotationQuaternion.map {
                        it.toDouble()
                    }.toDoubleArray(),
                    arCamera.displayOrientedPose.translation.map {
                        it * translationTransformationFactor
                    }.toDoubleArray()
                ).let {
                    initialTransformationMatrix?.addTransformation(it)
                }?.let {
                    sceneView.setViewpointCamera(Camera(it))
                }
            }
        }
    }

    /**
     * The function invoked for the [Lifecycle.Event.ON_PAUSE] lifecycle event.
     *
     * @since 100.6.0
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    internal fun pause() {
        arSceneView.pause()
        sceneView.pause()
    }

    /**
     * The function invoked for the [Lifecycle.Event.ON_DESTROY] lifecycle event.
     * [ArSceneView] may have an issue with a leak and an error may appear in the LogCat:
     * https://github.com/google-ar/sceneform-android-sdk/issues/538
     *
     * @since 100.6.0
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    internal fun destroy() {
        arSceneView.destroy()
        // disposing of SceneView causes the render surface, which is shared with ArSceneView, to become invalid and
        // rendering of the camera fails.
        // sceneView.dispose()
    }

    /**
     * Check to see we have the necessary permissions for accessing the camera using the current [Context].
     *
     * @since 100.6.0
     */
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request a [permission] using the provided [activity] and [permissionCode].
     *
     * @since 100.6.0
     */
    private fun requestPermission(activity: Activity, permission: String, permissionCode: Int) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), permissionCode)
    }

    /**
     * A class that extends [Exception] to notify users when an error has occurred in [ArcGISArView] using the provided
     * [message] which should explain the exception.
     *
     * @since 100.6.0
     */
    class ArcGISArViewException(override val message: String) : Exception(message)

    /**
     * An interface that allows a user to receive updates of the state of [ArcGISArView].
     *
     * @since 100.6.0
     */
    interface OnStateChangedListener {
        /**
         * Should be called when the state of [ArcGISArView] changes using an appropriate [state] of type [ArcGISArViewState].
         *
         * @since 100.6.0
         */
        fun onStateChanged(state: ArcGISArViewState)
    }

    /**
     * A class representing the available states of [ArcGISArView].
     *
     * @since 100.6.0
     */
    sealed class ArcGISArViewState {
        /**
         * Should be used to indicate that the [ArcGISArView] has initialized correctly, an ARCore [Session] has begun
         * and the [SceneView] has resumed.
         *
         * @since 100.6.0
         */
        object Initialized : ArcGISArViewState()

        /**
         * Should be used to indicate that a permission is required in order to use [ArcGISArView].
         *
         * @since 100.6.0
         */
        data class PermissionRequired(val permission: String) : ArcGISArViewState()

        /**
         * Should be used to indicate that an installation of ARCore is required.
         *
         * @since 100.6.0
         */
        object ArCoreInstallationRequired : ArcGISArViewState()

        /**
         * Should be used to indicate that an [Exception] has occurred during initialization.
         *
         * @since 100.6.0
         */
        data class InitializationFailure(val exception: Exception) : ArcGISArViewState()
    }
}
