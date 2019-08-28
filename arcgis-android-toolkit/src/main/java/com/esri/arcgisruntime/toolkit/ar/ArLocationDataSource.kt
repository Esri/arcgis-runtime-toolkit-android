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

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Criteria
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Build
import android.os.Bundle
import android.os.Handler
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.location.LocationDataSource
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import java.util.ArrayList
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.PI

/**
 * A type of LocationDataSource that requests location updates from the Android platforms location API. To be used in
 * conjunction with [ArcGISArView] when ARCore is unavailable.
 *
 * The Android framework's `android.location.LocationManager` is used to select one or more
 * `android.location.LocationProvider`s based on the parameters passed to the constructor. If both the GPS and the
 * network location providers are disabled, then the ArLocationDataSource will fail to start and an
 * IllegalStateException will be returned from [LocationDataSource.getError].
 *
 * To use this class, the app must be granted Location permissions in the Android platform settings.
 *
 * @see ArcGISArView
 *
 * @since 100.6.0
 */
class ArLocationDataSource(private val context: Context) : LocationDataSource() {

    companion object {
        // The factor used to filter out the less accurate positions to reduce unnecessary updates
        private const val ACCURACY_THRESHOLD_FACTOR = 2.0
        private const val EXCEPTION_MSG = "No location provider found on the device"
        private const val NO_STARTED_MSG = "The location data source is not started yet"
        private const val NO_PROVIDER_MSG = "No provider found for the given name : %s"
        private const val PARAMETER_OUT_OF_BOUNDS_MSG = "Parameter %s is out of bounds"
    }

    // The minimum distance to change updates in meters
    private var minimumUpdateDistance = 0f // meters

    // The minimum time between updates in milliseconds
    private var minimumUpdateTime: Long = 100 // 0.1 second

    // The Android location manager
    private val locationManager: LocationManager? by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager?
    }

    // The sensor manager to detect the device orientation for compass mode
    private val sensorManager: SensorManager? by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager?
    }

    // The current selected location providers
    private val selectedLocationProviders = ArrayList<String>()

    // The internal android location listener implementation
    private var internalLocationListener: InternalLocationListener? = null

    // The internal listener to update the heading for compass mode
    private var internalHeadingListener: InternalHeadingListener? = null

    // The criteria for selecting the android location provider
    private var criteria: Criteria? = null

    // The user defined known provider
    private var provider: String? = null

    // The last updated location
    private var lastLocation: Location? = null

    /**
     * Creates a new instance of ArLocationDataSource using the provided [context] and based on the given provider
     * [criteria], with the given [minTime] update frequency in milliseconds and [minDistance] location distance change
     * in meters.
     *
     * Not all location providers can return all types of location information. Use the criteria parameter to specify
     * what kind of information or properties are required for this ArLocationDataSource; for example one that has low
     * power usage, avoids network usage, or that includes bearing or altitude information.
     *
     * @throws IllegalArgumentException if [minTime] or [minDistance] is negative
     * @since 100.6.0
     */
    constructor(context: Context, criteria: Criteria, minTime: Long, minDistance: Float) : this(
        context
    ) {
        this.criteria = criteria
        checkTimeDistanceParameters(minTime, minDistance)
    }

    /**
     * Creates a new instance of ArLocationDataSource using the provided [context] and based on the given [provider] name,
     * with the given [minTime] update frequency in milliseconds and [minDistance] location distance change in meters.
     *
     * Provider names are defined as constants of the `android.location.LocationManager` class.
     *
     * @throws IllegalArgumentException if minTime or minDistance is negative
     * @since 100.6.0
     */
    constructor(context: Context, provider: String, minTime: Long, minDistance: Float) : this(
        context
    ) {
        this.provider = provider
        checkTimeDistanceParameters(minTime, minDistance)
    }

    /**
     * Changes the location update parameters to use the given provider [criteria], [minTime] update frequency in
     * milliseconds and [minDistance] location distance change in meters. Applies only when [isStarted] is true.
     *
     * @throws IllegalArgumentException if [minTime] or [minDistance] is negative, or no location provider matches the
     * [criteria]
     * @throws IllegalStateException if no location provider is selected by the [criteria], or data source is not started
     * @since 100.6.0
     */
    fun requestLocationUpdates(criteria: Criteria, minTime: Long, minDistance: Float) {
        check(isStarted) { NO_STARTED_MSG }

        selectedLocationProviders.clear()

        checkTimeDistanceParameters(minTime, minDistance)
        selectProviderByCriteria(criteria)

        check(selectedLocationProviders.isNotEmpty()) { EXCEPTION_MSG }

        startLocationProviders()
    }

    /**
     * Changes the location update parameters to use the given [provider] name, [minTime] update frequency in
     * milliseconds and [minDistance] location distance change in meters. Applies only when [isStarted] is true.
     *
     * @throws IllegalArgumentException if [minTime] or [minDistance] is negative
     * @throws IllegalStateException the specified location [provider] is not found in the location manager, or data source
     * is not started
     * @since 100.6.0
     */
    fun requestLocationUpdates(provider: String, minTime: Long, minDistance: Float) {
        check(isStarted) { NO_STARTED_MSG }

        checkTimeDistanceParameters(minTime, minDistance)
        selectProviderByUserDefined(provider)

        require(selectedLocationProviders.isNotEmpty()) { String.format(NO_PROVIDER_MSG, provider) }

        startLocationProviders()
    }

    /**
     * Called when the LocationDataSource is started, by calling [LocationDataSource.startAsync], or indirectly
     * by calling [LocationDisplay.startAsync] on the LocationDisplay associated with this data source.
     *
     * This method requests that location updates are received from the underlying Android platform location providers;
     * if there is an error starting the location providers, it will be passed to [onStartCompleted].
     *
     * @since 100.6.0
     */
    override fun onStart() {
        // LocationManager.requestLocationUpdates() must be called from a looper thread
        // Use the main looper here to avoid creating new looper thread, thread management, and UI issue
        val handler = Handler(context.mainLooper)
        handler.post {
            var throwable: Throwable? = null
            try {
                // Selects the location provider
                when {
                    criteria != null -> selectProviderByCriteria(criteria!!)
                    provider != null -> selectProviderByUserDefined(provider!!)
                    else -> selectProvidersByDefault()
                }

                // If no location providers are available or enabled, the starting process fails
                check(selectedLocationProviders.isNotEmpty()) {
                    String.format(
                        NO_PROVIDER_MSG,
                        "selectedLocationProviders"
                    )
                }

                // The LocationListeners need the caller thread having a Looper
                registerListeners()
            } catch (exception: Exception) {
                throwable = exception
            }

            onStartCompleted(throwable)
        }
    }

    /**
     * To register the location listeners, the calling thread needs to associate with a Looper.
     * This is because LocationManager.requestLocationUpdates(), which is called by startLocationProviders(),
     * must be called from a Looper thread.
     *
     * @since 100.6.0
     */
    @SuppressLint("MissingPermission")
    private fun registerListeners() {
        val lastKnownLocation = locationManager?.getLastKnownLocation(selectedLocationProviders[0])
        if (lastKnownLocation != null) {
            // Reset the last known location, speed and bearing, original data has no meaning anymore
            lastKnownLocation.speed = 0f
            lastKnownLocation.bearing = 0f
            setLastKnownLocation(lastKnownLocation.toEsriLocation(true))
        }
        startLocationProviders()

        startUpdateHeading()
    }

    /**
     * Called when the location data source is stopped, by calling [LocationDataSource.stop], or indirectly by
     * calling [LocationDisplay.stop] on the LocationDisplay associated with this data source.
     *
     * This method requests that location updates stop being received from the underlying Android platform location
     * providers.
     *
     * @since 100.6.0
     */
    override fun onStop() {
        locationManager?.removeUpdates(internalLocationListener)
        internalLocationListener = null

        // Stop update heading if it is started
        stopUpdateHeading()
    }

    /**
     * Validates [minTime] and [minDistance].
     *
     * @since 100.6.0
     */
    private fun checkTimeDistanceParameters(minTime: Long, minDistance: Float) {
        require(minTime >= 0) { String.format(PARAMETER_OUT_OF_BOUNDS_MSG, "minTime") }

        minimumUpdateTime = minTime

        require(minDistance >= 0) {
            String.format(
                PARAMETER_OUT_OF_BOUNDS_MSG,
                "minDistance"
            )
        }

        minimumUpdateDistance = minDistance
    }

    /**
     * Select the default location providers, network will be used first if it is available.
     *
     * @since 100.6.0
     */
    private fun selectProvidersByDefault() {
        locationManager?.let {
            // Check if the network location service enabled
            if (it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                selectedLocationProviders.add(LocationManager.NETWORK_PROVIDER)
            }

            // Check if the GPS location service enabled, if true, use it too
            if (it.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                selectedLocationProviders.add(LocationManager.GPS_PROVIDER)
            }
        }
    }

    /**
     * Starts the location provider.
     *
     * @since 100.6.0
     */
    @SuppressLint("MissingPermission")
    private fun startLocationProviders() {
        if (internalLocationListener == null) {
            internalLocationListener = InternalLocationListener()
        }

        for (provider in selectedLocationProviders) {
            locationManager?.requestLocationUpdates(
                provider,
                minimumUpdateTime,
                minimumUpdateDistance,
                internalLocationListener
            )
        }
    }

    /**
     * Updates the LocationDataSource location with the provided [location], indicating if the
     * provided location is the [lastKnown] location.
     *
     * @since 100.6.0
     */
    private fun updateEsriLocation(location: android.location.Location?, lastKnown: Boolean) {
        if (location != null) {

            // If new location accuracy is two times less than previous one, it will be ignored
            if (lastLocation != null) {
                val accuracyThreshold =
                    lastLocation!!.horizontalAccuracy * ACCURACY_THRESHOLD_FACTOR
                if (location.accuracy > accuracyThreshold) {
                    return
                }
            }

            val currentLocation = location.toEsriLocation(lastKnown)
            updateLocation(currentLocation)
            lastLocation = currentLocation
        }
    }

    /**
     * Selects the [LocationProvider] based on the provided [criteria].
     *
     * @since 100.6.0
     */
    private fun selectProviderByCriteria(criteria: Criteria) {
        // Select the best provider based on the criteria
        locationManager?.getBestProvider(criteria, true /* only enabled returns */)?.let {
            selectedLocationProviders.add(it)
        }
    }

    /**
     * Selects the Location Provider based on the [userProvider].
     *
     * @since 100.6.0
     */
    private fun selectProviderByUserDefined(userProvider: String) {
        // Use the user supplied location provider if it is defined, otherwise ignore it
        if (locationManager != null && locationManager!!.allProviders.contains(userProvider)) {
            selectedLocationProviders.add(userProvider)
        }
    }

    /**
     * Start updating the heading direction.
     *
     * @since 100.6.0
     */
    private fun startUpdateHeading() {
        if (internalHeadingListener == null) {
            internalHeadingListener = InternalHeadingListener()
        }

        sensorManager?.let {
            // Most devices have one or both hardware-sensors
            it.registerListener(
                internalHeadingListener,
                it.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI
            )
            it.registerListener(
                internalHeadingListener,
                it.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    /**
     * Stops the sensor manager if it is started.
     *
     * @since 100.6.0
     */
    private fun stopUpdateHeading() {
        sensorManager?.unregisterListener(internalHeadingListener)
        if (internalHeadingListener != null) {
            internalHeadingListener = null
        }
        // Reset the heading to NaN when the heading is not available
        updateHeading(java.lang.Double.NaN)
    }

    /**
     * The internal implementation [LocationListener] to listen for the changes of Location.
     *
     * @since 100.6.0
     */
    private inner class InternalLocationListener : LocationListener {

        private var innerAndroidLocation: android.location.Location? = null

        override fun onLocationChanged(location: android.location.Location) {
            // Update the core location
            updateEsriLocation(location, false)
            innerAndroidLocation = location
        }

        override fun onProviderEnabled(provider: String) {
            // Re-register the enabled provider
            if (selectedLocationProviders.contains(provider)) {
                startLocationProviders()
            }
        }

        override fun onProviderDisabled(provider: String) {
            // If only one provider is is selected and that provider is disabled then the last known location is used as
            // the current location
            if (selectedLocationProviders.contains(provider) && selectedLocationProviders.size == 1) {
                updateEsriLocation(innerAndroidLocation, true)
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            if (selectedLocationProviders.contains(provider)) {
                if (status == LocationProvider.AVAILABLE) {
                    startLocationProviders()
                } else {
                    // Out of service or temporarily unavailable
                    updateEsriLocation(innerAndroidLocation, true)
                }
            }
        }
    }

    /**
     * Internal implementation for [SensorEventListener] to listen to changes in orientation of the device.
     *
     * @since 100.6.0
     */
    private inner class InternalHeadingListener : SensorEventListener {
        private var gravity = FloatArray(3)

        private var geomagnetic = FloatArray(3)

        private val rotationMatrixR = FloatArray(9)

        private val rotationMatrixI = FloatArray(9)

        private val orientation = FloatArray(3)

        private var heading: Float = 0.toFloat()

        override fun onSensorChanged(event: SensorEvent) {
            val type = event.sensor.type

            if (type == Sensor.TYPE_ACCELEROMETER) {
                gravity = lowPassFilter(event.values.clone(), gravity)
            } else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = lowPassFilter(event.values.clone(), geomagnetic)
            }

            val success = SensorManager.getRotationMatrix(
                rotationMatrixR,
                rotationMatrixI,
                gravity,
                geomagnetic
            )
            if (success) {
                SensorManager.getOrientation(rotationMatrixR, orientation)
                this.heading = orientation[0].toDegrees()
                if (this.heading < 0)
                    heading += 360f

                // Update the heading value
                updateHeading(heading.toDouble())
            }
        }

        /**
         * Function to apply low pass filter to smooth out sensor readings. Based upon implementation here:
         * https://www.built.io/blog/applying-low-pass-filter-to-android-sensor-s-readings
         *
         * @since 100.6.0
         */
        private fun lowPassFilter(input: FloatArray, output: FloatArray?): FloatArray {
            if (output == null) {
                return input
            }
            for (i in input.indices) {
                output[i] = output[i] + 0.1f * (input[i] - output[i])
            }
            return output
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    }
}

/**
 * Creates a Calendar with standard TimeZone and Locale from a [timeInMillis].
 *
 * @since 100.6.0
 */
private fun createCalendarFromTimeInMillis(timeInMillis: Long): Calendar {
    val ret = GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ENGLISH)
    ret.timeInMillis = timeInMillis
    return ret
}

/**
 * Creates a Location from android.location.Location object.
 *
 * @param lastKnown true if the location is last one, otherwise it should be false
 * @since 100.6.0
 */
private fun android.location.Location.toEsriLocation(lastKnown: Boolean): LocationDataSource.Location {
    val position = Point(longitude, latitude, SpatialReference.create(4326))
    var verticalAccuracy = java.lang.Double.NaN
    val timeStamp = createCalendarFromTimeInMillis(time)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        verticalAccuracy = verticalAccuracyMeters.toDouble()
    }

    return LocationDataSource.Location(
        position,
        accuracy.toDouble(),
        verticalAccuracy,
        speed.toDouble(),
        bearing.toDouble(),
        lastKnown,
        timeStamp
    )
}

/**
 * Converts an angle measured in radians to an approximately
 * equivalent angle measured in degrees.  The conversion from
 * radians to degrees is generally inexact.
 *
 * @since 100.6.0
 */
private fun Float.toDegrees(): Float = this * 180.0f / PI.toFloat()