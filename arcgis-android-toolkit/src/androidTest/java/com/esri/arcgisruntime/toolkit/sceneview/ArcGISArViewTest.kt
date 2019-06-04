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

import android.os.Build
import android.support.test.InstrumentationRegistry
import android.support.test.filters.SdkSuppress
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.esri.arcgisruntime.toolkit.TestActivity
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
        with(testActivityRule.activity) {
            this.runOnUiThread {
                ArcGISArView(InstrumentationRegistry.getContext(), true).let {
                    assertNotNull(it)
                    assertNotNull(it.sceneView)
                    assertNotNull(it.arSceneView)
                    assertNotNull(it.originCamera)
                }
            }
        }
    }

    /**
     * Tests the constructor used when declaring an [ArcGISArView] in XML.
     */
    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
    fun testXmlConstructor() {
        with(arcGisArViewTestActivityRule.activity) {
            this.arcGISArView.let {
                assertNotNull(it)
                assertNotNull(it?.sceneView)
                assertNotNull(it?.arSceneView)
                assertNotNull(it?.originCamera)
            }
        }
    }

}