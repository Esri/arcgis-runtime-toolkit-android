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

import android.os.Build
import android.support.test.filters.SdkSuppress
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.toolkit.TestActivity
import com.esri.arcgisruntime.toolkit.finish
import com.esri.arcgisruntime.toolkit.launchActivity
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented tests for [ArcGISArView]
 *
 * @since 100.6.0
 */
@RunWith(AndroidJUnit4::class)
class ArcGISArViewTest {

    @get:Rule
    public val testActivityRule: ActivityTestRule<TestActivity> = ActivityTestRule(TestActivity::class.java)

    @get:Rule
    public val arcGisArViewTestActivityRule: ActivityTestRule<ArcGISArViewTestActivity> =
        ActivityTestRule(ArcGISArViewTestActivity::class.java)

    /**
     * Tests the constructor that takes just a Context and a Boolean.
     *
     * @since 100.6.0
     */
    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
    fun testSimpleConstructorDefaultValues() {
        with(testActivityRule) {
            this.runOnUiThread {
                ArcGISArView(this.activity, true).let {
                    assertNotNull(it)
                    assertNotNull(it.sceneView)
                    assertNotNull(it.arSceneView)
                }
            }
            this.finish()
        }
    }

    /**
     * Tests the constructor used when declaring an [ArcGISArView] in XML.
     *
     * @since 100.6.0
     */
    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
    fun testXmlConstructor() {
        with(arcGisArViewTestActivityRule) {
            this.activity.arcGISArView.let {
                assertNotNull(it)
                assertNotNull(it?.sceneView)
                assertNotNull(it?.arSceneView)
            }
            this.finish()
        }
    }

    /**
     * Tests setting the [ArcGISArView.originCamera] property.
     *
     * @since 100.6.0
     */
    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
    fun testSettingOriginCamera() {
        val cameraLat = 20.0
        val cameraLon = 30.0
        val cameraAlt = 2500.0
        val cameraHeading = 10.0
        val cameraPitch = 20.0
        val cameraRoll = 30.0

        val delta = 0.001

        val camera = Camera(cameraLat, cameraLon, cameraAlt, cameraHeading, cameraPitch, cameraRoll)
        with(arcGisArViewTestActivityRule) {
            this.launchActivity()
            this.activity.arcGISArView.let {
                it?.originCamera = camera

                val actualCamera = it?.originCamera

                assertEquals(
                    "Expected camera location latitude: $cameraLat was ${actualCamera?.location?.y}",
                    cameraLat,
                    actualCamera?.location?.y!!,
                    delta
                )
                assertEquals(
                    "Expected camera location longitude: $cameraLon was ${actualCamera.location?.x}",
                    cameraLon,
                    actualCamera.location?.x!!,
                    delta
                )
                assertEquals(
                    "Expected camera location altitude: $cameraAlt was ${actualCamera.location?.z}",
                    cameraAlt,
                    actualCamera.location?.z!!,
                    delta
                )
                assertEquals(
                    "Expected camera heading: $cameraHeading was ${actualCamera.heading}",
                    cameraHeading,
                    actualCamera.heading,
                    delta
                )
                assertEquals(
                    "Expected camera pitch: $cameraPitch was ${actualCamera.pitch}",
                    cameraPitch,
                    actualCamera.pitch,
                    delta
                )
                assertEquals(
                    "Expected camera roll: $cameraRoll was ${actualCamera.roll}",
                    cameraRoll,
                    actualCamera.roll,
                    delta
                )
            }
            this.finish()
        }
    }

    /**
     * Tests setting the [ArcGISArView.translationFactor] property.
     *
     * @since 100.6.0
     */
    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
    fun testTranslationTransformationFactor() {
        val translationTransformationFactor = 99.9
        with(arcGisArViewTestActivityRule) {
            this.launchActivity()
            this.activity.arcGISArView.let {
                it?.translationFactor = translationTransformationFactor

                assertEquals(
                    "Expected translation transformation factor $translationTransformationFactor",
                    translationTransformationFactor,
                    it?.translationFactor
                )
            }
            this.finish()
        }
    }

    /**
     * Tests setting the [ArcGISArView.clippingDistance] property.
     *
     * @since 100.6.0
     */
    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
    fun testClippingDistance() {
        val clippingDistance = 99.9
        with(arcGisArViewTestActivityRule) {
            this.launchActivity()
            this.activity.arcGISArView.let {
                it?.clippingDistance = clippingDistance

                assertEquals(
                        "Expected clipping distance $clippingDistance",
                        clippingDistance,
                        it?.clippingDistance
                )
            }
            this.finish()
        }
    }

}
