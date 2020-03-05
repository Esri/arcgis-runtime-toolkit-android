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

package com.esri.arcgisruntime.toolkit.control

import android.content.Context
import androidx.appcompat.widget.AppCompatSeekBar
import android.util.AttributeSet
import android.widget.SeekBar
import com.esri.arcgisruntime.toolkit.R
import java.util.Timer
import java.util.TimerTask
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * A subclass of AppCompatSeekBar with an initial progress of 50% of the max value.
 *
 * The distance from the center point of the slider determines the [deltaProgress] value applied
 * during each interval. For distances close to the center, the delta is small and used for fine-grained
 * adjustments; distances farther from the center are for larger-scale adjustments. When released,
 * the slider "thumb" will return to the center of the slider.
 *
 * _Example usage_:
 * ```
 * <com.esri.arcgisruntime.toolkit.control.JoystickSeekBar
 * android:layout_width="100dp"
 * android:layout_height="wrap_content"
 * app:jsb_max="50.0"
 * app:jsb_min="-50.0" />
 * ```
 *
 * _Attributes_:
 *
 * `jsb_max` - Sets the maximum value
 *
 * `jsb_min` - Sets the minimum value
 *
 * @since 100.6.0
 */
class JoystickSeekBar : AppCompatSeekBar, SeekBar.OnSeekBarChangeListener {

    companion object {
        /**
         * Default minimum value for the JoystickSeekBar.
         *
         * @since 100.6.0
         */
        private const val DEFAULT_MIN = 0.0f

        /**
         * Default maximum value for the JoystickSeekBar.
         *
         * @since 100.6.0
         */
        private const val DEFAULT_MAX = 100.0f

        /**
         * Default interval value in milliseconds between delta progress updates.
         *
         * @since 100.6.0
         */
        private const val DEFAULT_DELTA_INTERVAL_MILLIS = 250L
    }

    /**
     * Backing minimum property to not conflict with superclass's min value.
     *
     * @since 100.6.0
     */
    private var _min: Float = DEFAULT_MIN

    /**
     * Backing maximum property to not conflict with superclass's max value.
     *
     * @since 100.6.0
     */
    private var _max: Float = DEFAULT_MAX

    /**
     * Progress value that is offset based upon the [_min] & [_max] values if they are set. Otherwise
     * the default max value is used.
     *
     * @since 100.6.0
     */
    private val offsetProgress: Float
        get() {
            return if ((_min != DEFAULT_MIN).or(_max != DEFAULT_MAX)) {
                _min + ((_max - _min) * (progress * 0.01f))
            } else {
                max * (progress * 0.01f)
            }
        }

    /**
     * The property representing the delta of the "thumb" control from the center of the JoystickSeekBar.
     * Can be listened to by using [addDeltaProgressUpdatedListener].
     *
     * @since 100.6.0
     */
    private var deltaProgress: Float = 0.0f

    /**
     * A list of [DeltaProgressUpdatedListener] used to listen for delta progress updates.
     *
     * @since 100.6.0
     */
    private val deltaProgressUpdatedListeners = ArrayList<DeltaProgressUpdatedListener>()

    /**
     * Timer used to schedule [deltaTimerTask].
     *
     * @since 100.6.0
     */
    private val deltaTimer: Timer = Timer()

    /**
     * TimerTask that notifies registered [DeltaProgressUpdatedListener] of the current [deltaProgress].
     *
     * @since 100.6.0
     */
    private lateinit var deltaTimerTask: TimerTask

    init {
        // Set the initial progress to half of the max value
        progress = (max * 0.5).roundToInt()
        setOnSeekBarChangeListener(this)
    }

    /**
     * Constructor used when instantiating this View directly to attach it to another view
     * programmatically.
     *
     * @since 100.6.0
     */
    constructor(context: Context) : super(context)

    /**
     * Constructor used when defining this view in an XML layout.
     *
     * @since 100.6.0
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.JoystickSeekBar,
            0, 0
        ).apply {
            try {
                _min = getFloat(
                    R.styleable.JoystickSeekBar_jsb_min,
                    DEFAULT_MIN
                )
                _max = getFloat(
                    R.styleable.JoystickSeekBar_jsb_max,
                    DEFAULT_MAX
                )
            } finally {
                recycle()
            }
        }
        if (_min >= _max) {
            throw RuntimeException("Attribute jsb_min must be less than attribute jsb_max")
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        deltaProgress = offsetProgress.pow(2) / 10.0f * (if (offsetProgress < 0) -1.0f else 1.0f)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        deltaTimerTask = object : TimerTask() {
            override fun run() {
                post {
                    deltaProgressUpdatedListeners.forEach {
                        it.onDeltaProgressUpdated(deltaProgress)
                    }
                }
            }
        }
        deltaTimer.schedule(deltaTimerTask, 0, DEFAULT_DELTA_INTERVAL_MILLIS)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        deltaTimerTask.cancel()
        deltaProgress = 0.0f
        progress = (max * 0.5).roundToInt()
    }

    /**
     * Add a [listener] to be notified when a [deltaProgress] update is made. Returns true if the
     * listener was added, false otherwise.
     *
     * @since 100.6.0
     */
    fun addDeltaProgressUpdatedListener(listener: DeltaProgressUpdatedListener): Boolean {
        return this.deltaProgressUpdatedListeners.add(listener)
    }

    /**
     * Removes a previously added [listener]. Returns true if the listener was removed, false
     * otherwise.
     *
     * @since 100.6.0
     */
    fun removeDeltaProgressUpdatedListener(listener: DeltaProgressUpdatedListener): Boolean {
        return this.removeDeltaProgressUpdatedListener(listener)
    }

    /**
     * Listener interface to listen for delta progress updates.
     *
     * @since 100.6.0
     */
    interface DeltaProgressUpdatedListener {
        /**
         * The distance from the center point of the slider determines the [deltaProgress] value
         * applied during each interval. For distances close to the center, the delta is small and
         * used for fine-grained adjustments; distances farther from the center are for larger-scale
         * adjustments. When released, the slider "thumb" will return to the center of the slider.
         *
         * @since 100.6.0
         */
        fun onDeltaProgressUpdated(deltaProgress: Float)
    }
}
