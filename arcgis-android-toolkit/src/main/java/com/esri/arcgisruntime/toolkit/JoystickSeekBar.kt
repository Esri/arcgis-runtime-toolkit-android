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

package com.esri.arcgisruntime.toolkit

import android.content.Context
import android.support.v7.widget.AppCompatSeekBar
import android.util.AttributeSet
import android.widget.SeekBar
import java.util.Timer
import java.util.TimerTask
import kotlin.math.pow
import kotlin.math.roundToInt

private const val DEFAULT_MIN = 0.0f
private const val DEFAULT_MAX = 100.0f

class JoystickSeekBar : AppCompatSeekBar, SeekBar.OnSeekBarChangeListener {

    private var _min: Float = DEFAULT_MIN
    private var _max: Float = DEFAULT_MAX
    private val offsetProgress: Float
        get() {
            return if ((_min != DEFAULT_MIN).or(_max != DEFAULT_MAX)) {
                _min + ((_max - _min) * (progress * 0.01f))
            } else {
                max * (progress * 0.01f)
            }
        }
    private var deltaProgress: Float = 0.0f
    private val deltaProgressUpdatedListeners = ArrayList<DeltaProgressUpdatedListener>()
    private val deltaTimer: Timer = Timer()
    private lateinit var deltaTimerTask: TimerTask

    init {
        setOnSeekBarChangeListener(this)
    }

    /**
     * Constructor used when instantiating this View directly to attach it to another view programmatically.
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
                _min = getFloat(R.styleable.JoystickSeekBar_jsb_min, DEFAULT_MIN)
                _max = getFloat(R.styleable.JoystickSeekBar_jsb_max, DEFAULT_MAX)
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
        deltaTimer.schedule(deltaTimerTask, 0, 250)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        deltaTimerTask.cancel()
        deltaProgress = 0.0f
        progress = (max * 0.5).roundToInt()
    }

    fun addDeltaProgressUpdatedListener(listener: DeltaProgressUpdatedListener): Boolean {
        return this.deltaProgressUpdatedListeners.add(listener)
    }

    fun removeDeltaProgressUpdatedListener(listener: DeltaProgressUpdatedListener): Boolean {
        return this.removeDeltaProgressUpdatedListener(listener)
    }

    interface DeltaProgressUpdatedListener {
        fun onDeltaProgressUpdated(deltaProgress: Float)
    }
}
