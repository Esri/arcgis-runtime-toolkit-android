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

import android.app.Activity
import android.support.test.rule.ActivityTestRule

/**
 * Extension method to invoke finish on the Activity belonging to the ActivityTestRule.
 *
 * @since 100.6.0
 */
fun <T : Activity> ActivityTestRule<out T?>.finish() {
    this.activity?.finish()
}

/**
 * Extension method to launch the Activity belonging to the ActivityTestRule using the Intent that will be used to start
 * the Activity under test.
 *
 * @since 100.6.0
 */
fun <T : Activity> ActivityTestRule<out T?>.launchActivity() {
    this.launchActivity(activity?.intent)
}

/**
 * Extension method to relaunch the Activity belonging to the ActivityTestRule by finishing it and launching it again.
 *
 * @since 100.6.0
 */
fun <T : Activity> ActivityTestRule<out T?>.relaunchActivity() {
    this.finish()
    this.launchActivity()
}
