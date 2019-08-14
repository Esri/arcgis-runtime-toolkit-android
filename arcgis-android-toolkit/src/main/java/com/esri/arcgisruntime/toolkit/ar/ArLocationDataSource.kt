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
import com.esri.arcgisruntime.internal.jni.CoreLocationDataSource
import com.esri.arcgisruntime.location.LocationDataSource
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.mapping.view.LocationDisplay.AutoPanMode
import com.esri.arcgisruntime.mapping.view.LocationDisplay.AutoPanModeChangedListener
import java.util.ArrayList
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

/**
 * A type of LocationDataSource that requests location updates from the Android platforms location API.
 *
 *
 * The Android framework's `android.location.LocationManager` is used to select one or more
 * `android.location.LocationProvider`s based on the parameters passed to the constructor. If both the GPS and the
 * network location providers are disabled, then the AndroidLocationDataSource will fail to start and an
 * IllegalStateException will be returned from [.getError].
 *
 *
 * To use this class, the app must be granted Location permissions in the Android platform settings.
 *
 *
 * This class also implements the [AutoPanModeChangedListener] to listen to AutoPanMode change events. When the
 * AutoPanMode is changed to [AutoPanMode.COMPASS_NAVIGATION] mode, the update of heading information is enabled. Heading
 * information is not retrieved by this data source if a different AutoPanMode is set.
 *
 * @see LocationDisplay
 *
 * @since 100.0.0
 */

// the factor used to filter out the less accuracy positions to reduce unnecessary update.
private const val ACCURACY_THRESHOLD_FACTOR = 2.0
private const val EXCEPTION_MSG = "No location provider found on the device" // NO I18N
private const val NO_STARTED_MSG = "The location data source is not started yet" // NO I18N
private const val NO_PROVIDER_MSG = "No provider found for the given name : %s" // NO I18N;

class ArLocationDataSource(private val context: Context) : LocationDataSource(), AutoPanModeChangedListener {

    // The minimum distance to change updates in meters
    private var minimumUpdateDistance = 0f // meters

    // The minimum time between updates in milliseconds
    private var minimumUpdateTime: Long = 100 // 0.1 second

    // android location manager
    private lateinit var locationManager: LocationManager

    // store the current selected location provider;
    private val selectedLocationProviders = ArrayList<String>()

    // internal android location listener implementation.
    private var internalLocationListener: InternalLocationListener? = null

    // Sensor manager to detect the device orientation for compass mode.
    private var sensorManager: SensorManager? = null

    // internal listener to update the heading for compass mode.
    private var internalHeadingListener: InternalHeadingListener? = null

    // The criteria for selecting the android location provider;
    private var criteria: Criteria? = null

    // The user defined known provider;
    private var provider: String? = null

    // last update location;
    private var lastLocation: Location? = null

    // The current AutoPanMode setting; volatile because it may be set and read by different threads
    @Volatile
    private var mAutoPanMode: AutoPanMode? = null

    /**
     * Creates a new instance of AndroidLocationDataSource based on the given provider Criteria, with the given minimum
     * update frequency and minimum location distance change.
     *
     * Not all location providers can return all types of location information. Use the
     * `criteria` parameter to specify what kind of information or properties are required
     * for this AndroidLocationDataSource; for example one that has low power usage, avoids network usage, or that
     * includes bearing or altitude information.
     *
     * @param context the Context that the MapView of the associated LocationDisplay is running in
     * @param criteria the set of requirements that the selected location provider must satisfy
     * @param minTime the minimum time interval at which location updates should be received, in milliseconds
     * @param minDistance the minimum distance between which location updates should be received, in meters
     * @throws IllegalArgumentException if minTime or minDistance is negative
     * @since 100.0.0
     */
    constructor(context: Context, criteria: Criteria, minTime: Long, minDistance: Float) : this(context) {
        this.criteria = criteria
        checkTimeDistanceParameters(minTime, minDistance)
    }

    /**
     * Creates a new instance of AndroidLocationDataSource based on the given provider name, with the given minimum update
     * frequency and minimum location distance change.
     *
     * Provider names are defined as constants of the `android.location.LocationManager` class.
     *
     * @param context the Context that the MapView of the associated LocationDisplay is running in
     * @param provider the name of the underlying Android platform location provider to use, for example 'gps'
     * @param minTime the minimum time interval at which location updates should be received, in milliseconds
     * @param minDistance the minimum distance between which location updates should be received, in meters
     * @throws IllegalArgumentException if minTime or minDistance is negative
     * @since 100.0.0
     */
    constructor(context: Context, provider: String, minTime: Long, minDistance: Float) : this(context) {
        this.provider = provider
        checkTimeDistanceParameters(minTime, minDistance)
    }

    /**
     * Changes the location update parameters to use the given provider Criteria, minimum update frequency, and minimum
     * location distance change. Applies only when [.isStarted] is true.
     *
     * @param criteria the set of requirements that the selected location provider must satisfy
     * @param minTime the minimum time interval at which location updates should be received, in milliseconds
     * @param minDistance the minimum distance between which location updates should be received, in meters
     * @throws IllegalArgumentException if minTime or minDistance is negative, or criteria is null,or no location provider
     * matches the criteria
     * @throws IllegalStateException if no location provider is selected by the criteria, or data source is not started
     * @since 100.0.0
     */
    fun requestLocationUpdates(criteria: Criteria, minTime: Long, minDistance: Float) {
        if (!isStarted) {
            throw IllegalStateException(NO_STARTED_MSG)
        }
        selectedLocationProviders.clear()

        checkTimeDistanceParameters(minTime, minDistance)
        selectProviderByCriteria(criteria)

        if (selectedLocationProviders.isEmpty()) {
            throw IllegalStateException(EXCEPTION_MSG)
        }

        startLocationProviders()
    }

    /**
     * Changes the location update parameters to use the given provider name, minimum update frequency, and minimum
     * location distance change. Applies only when [.isStarted] is true.
     *
     * @param provider the name of the underlying Android platform location provider to use, for example 'gps'
     * @param minTime the minimum time interval at which location updates should be received, in milliseconds
     * @param minDistance the minimum distance of location change at which location updates should be received, in meters
     * @throws IllegalArgumentException if minTime or minDistance is negative, or criteria is null
     * @throws IllegalStateException the specified location provider is not found in the location manager, or data source
     * is not started
     * @since 100.0.0
     */
    fun requestLocationUpdates(provider: String, minTime: Long, minDistance: Float) {
        if (!isStarted) {
            throw IllegalStateException(NO_STARTED_MSG)
        }
        checkTimeDistanceParameters(minTime, minDistance)
        selectProviderByUserDefined(provider)

        if (selectedLocationProviders.isEmpty()) {
            throw IllegalArgumentException(String.format(NO_PROVIDER_MSG, provider))
        }

        startLocationProviders()
    }

    /**
     * Called when the LocationDataSource is started, by calling [LocationDataSource.startAsync], or indirectly
     * by calling [LocationDisplay.startAsync] on the LocationDisplay associated with this data source.
     *
     * This method requests that location updates are received from the underlying Android platform location providers;
     * if there is an error starting the location providers, it will be passed to [.onStartCompleted].
     *
     * @since 100.0.0
     */
    override fun onStart() {
        //LocationManager.requestLocationUpdates() must be called from a looper thread.
        //Use the main looper here to avoid creating new looper thread,thread management, and UI issue.
        val handler = Handler(context.mainLooper)
        handler.post {
            var throwable: Throwable? = null
            try {
                // Initializes the location manager;
                initializeLocationManager()

                // Selects the location provider
                when {
                    criteria != null -> selectProviderByCriteria(criteria!!)
                    provider != null -> selectProviderByUserDefined(provider!!)
                    else -> selectProvidersByDefault()
                }

                // if no location providers are available or enabled, the starting process fails;
                if (selectedLocationProviders.isEmpty()) {
                    throw IllegalStateException(String.format(NO_PROVIDER_MSG, "selectedLocationProviders"))
                }

                //the LocationListeners need the caller thread having a Looper.
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
     * @since 100.2.0
     */
    @SuppressLint("MissingPermission")
    private fun registerListeners() {
        val lastKnownLocation = locationManager.getLastKnownLocation(selectedLocationProviders[0])
        if (lastKnownLocation != null) {
            // reset the last known location'speed and bearing, original data no meaning anymore.
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
     * @since 100.0.0
     */
    override fun onStop() {
        locationManager.removeUpdates(internalLocationListener)
        internalLocationListener = null

        // stop update heading if it is started.
        stopUpdateHeading()
    }

    /**
     * Initialize the AndroidLocationDataSource.
     *
     * @since 100.0.0
     */
    private fun initializeLocationManager() {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    /**
     * Validates the time and distance
     *
     * @param minTime no negative value
     * @param minDistance no negative value
     * @since 100.0.0
     */
    private fun checkTimeDistanceParameters(minTime: Long, minDistance: Float) {
        minimumUpdateTime = minTime
        minimumUpdateDistance = minDistance
    }

    /**
     * Select the default location providers, network will be used first if it is available.
     *
     * @since 100.0.0
     */
    private fun selectProvidersByDefault() {
        // Check if the network location service enabled
        if (locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            selectedLocationProviders.add(LocationManager.NETWORK_PROVIDER)
        }

        // check if the GPS location service enabled,if true, use it too.
        if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            selectedLocationProviders.add(LocationManager.GPS_PROVIDER)
        }
    }

    /**
     * Called when the AutoPanMode is changed on the associated LocationDisplay, this method populates the heading
     * ([LocationDisplay.getHeading]) on the associated LocationDisplay if the AutoPanMode is changed to
     * [AutoPanMode.COMPASS_NAVIGATION].
     *
     * @since 100.0.0
     */
    override fun onAutoPanModeChanged(autoPanModeEvent: LocationDisplay.AutoPanModeChangedEvent) {
        mAutoPanMode = autoPanModeEvent.autoPanMode
        if (!isStarted) {
            return
        }
    }

    /**
     * Starts the location provider.
     *
     * @since 100.0.0
     */
    @SuppressLint("MissingPermission")
    private fun startLocationProviders() {
        if (internalLocationListener == null) {
            internalLocationListener = InternalLocationListener()
        }

        for (provider in selectedLocationProviders) {
            locationManager!!.requestLocationUpdates(
                provider, minimumUpdateTime, minimumUpdateDistance,
                internalLocationListener
            )
        }
    }

    /**
     * this is to update the core [CoreLocationDataSource.updateLocation]
     *
     * @param location android Location object
     * @param lastKnown indicating if the location is last one
     * @since 100.0.0
     */
    private fun updateCoreLocation(location: android.location.Location?, lastKnown: Boolean) {

        if (location != null) {

            // if new location accuracy is two times less than previous one, it will be ignored.
            if (lastLocation != null) {
                val accuracyThreshold = lastLocation!!.horizontalAccuracy * ACCURACY_THRESHOLD_FACTOR
                if (location.accuracy > accuracyThreshold)
                    return
            }

            val currentLocation = location.toEsriLocation(lastKnown)
            updateLocation(currentLocation)
            lastLocation = currentLocation
        }
    }

    /**
     * Selects the [LocationProvider] based on the criteria
     *
     * @param criteria Android Criteria object
     * @since 100.0.0
     */
    private fun selectProviderByCriteria(criteria: Criteria) {
        // Select the best provider based on the criteria
        val provider = locationManager.getBestProvider(criteria, true /* only enabled returns */)
        selectedLocationProviders.add(provider)
    }

    /**
     * Selects the Location Provider based user defined.
     *
     * @param userProvider the service name of the location provider
     * @since 100.0.0
     */
    private fun selectProviderByUserDefined(userProvider: String) {
        // use the user supplied location provider if it is defined, otherwise ignore it.
        if (locationManager.allProviders.contains(userProvider)) {
            selectedLocationProviders.add(userProvider)
        }
    }

    /**
     * Update the heading direction, it calls [CoreLocationDataSource.updateHeading] method.
     *
     * @since 100.0.0
     */
    private fun startUpdateHeading() {
        if (sensorManager == null) {
            // initialize your android device sensor capabilities
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        }

        if (internalHeadingListener == null) {
            internalHeadingListener = InternalHeadingListener()
        }

        // most of device has one or both hardware-sensors.
        sensorManager!!.registerListener(
            internalHeadingListener,
            sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI
        )
        sensorManager!!.registerListener(
            internalHeadingListener,
            sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI
        )
    }

    /**
     * Stops the sensor manager if it is started.
     *
     * @since 100.0.0
     */
    private fun stopUpdateHeading() {
        if (sensorManager != null && internalHeadingListener != null) {
            sensorManager!!.unregisterListener(internalHeadingListener)
            internalHeadingListener = null
        }
        //reset the heading to NaN when the heading is not available.
        updateHeading(java.lang.Double.NaN)
    }

    /**
     * The internal implementation [LocationListener] to listen the changes of Location.
     *
     * @since 100.0.0
     */
    private inner class InternalLocationListener : LocationListener {

        private var mInnerAndroidLocation: android.location.Location? = null

        override fun onLocationChanged(location: android.location.Location) {
            // update the core location
            updateCoreLocation(location, false)
            mInnerAndroidLocation = location
        }

        override fun onProviderEnabled(provider: String) {
            // re-registered the enabled provider.
            if (selectedLocationProviders.contains(provider)) {
                startLocationProviders()
            }
        }

        override fun onProviderDisabled(provider: String) {
            // if only one provider is disabled, the last known location is display in the gray symbol.
            if (selectedLocationProviders.contains(provider) && selectedLocationProviders.size == 1) {
                updateCoreLocation(mInnerAndroidLocation, true)
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            if (selectedLocationProviders.contains(provider)) {
                if (status == LocationProvider.AVAILABLE) {
                    startLocationProviders()
                } else {
                    // out of service or temporarily unavailable.
                    updateCoreLocation(mInnerAndroidLocation, true)
                }
            }
        }
    }

    /**
     * Internal implementation for [SensorEventListener] to listen to changes in orientation of the device when in
     * compass mode.
     *
     * @since 100.0.0
     */
    private inner class InternalHeadingListener : SensorEventListener {
        private var gravity = FloatArray(3)

        private var geomagnetic = FloatArray(3)

        private val rotationMatrixR = FloatArray(9)

        private val rotationMatrixI = FloatArray(9)

        private val orientation = FloatArray(3)

        private var heading: Float = 0.toFloat()

        private val rad2Deg = (180.0f / Math.PI).toFloat()

        override fun onSensorChanged(event: SensorEvent) {
            val type = event.sensor.type

            if (type == Sensor.TYPE_ACCELEROMETER) {
                gravity = lowPassFilter(event.values.clone(), gravity)
            } else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = lowPassFilter(event.values.clone(), geomagnetic)
            }

            val success = SensorManager.getRotationMatrix(rotationMatrixR, rotationMatrixI, gravity, geomagnetic)
            if (success) {
                SensorManager.getOrientation(rotationMatrixR, orientation)
                this.heading = orientation[0] * rad2Deg
                if (this.heading < 0)
                    heading += 360f

                // update the core heading value.
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
            for (i in 0 until input.size) {
                output[i] = output[i] + 0.1f * (input[i] - output[i])
            }
            return output
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    }
}

fun createCalendarFromTimeInMillis(milliseconds: Long): Calendar {
    val ret = GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ENGLISH)
    ret.timeInMillis = milliseconds
    return ret
}

/**
 * Creates a Location from android.location.Location object.
 *
 * @param lastKnown true if the location is last one, otherwise it should be false
 * @since 100.6.0
 */
fun android.location.Location.toEsriLocation(lastKnown: Boolean): LocationDataSource.Location {
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