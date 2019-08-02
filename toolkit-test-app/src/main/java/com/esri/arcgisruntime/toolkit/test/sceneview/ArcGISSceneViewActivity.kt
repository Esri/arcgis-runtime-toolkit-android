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

package com.esri.arcgisruntime.toolkit.test.sceneview

import android.location.LocationManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.esri.arcgisruntime.location.AndroidLocationDataSource
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.toolkit.ar.ArcGISArView
import com.esri.arcgisruntime.toolkit.extension.logTag
import com.esri.arcgisruntime.toolkit.test.R
import kotlinx.android.synthetic.main.activity_arcgissceneview.arcGisArView

/**
 * Example Activity to show usage of [ArcGISArView].
 *
 * @since 100.6.0
 */
class ArcGISSceneViewActivity : AppCompatActivity(), ArcGISArView.OnStateChangedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arcgissceneview)

        with(ArcGISScene(Basemap.createImagery())) {
            arcGisArView.sceneView.scene = this
        }

        arcGisArView.registerLifecycle(lifecycle)
        arcGisArView.addOnStateChangedListener(this)
        arcGisArView.locationDataSource = AndroidLocationDataSource(this, LocationManager.NETWORK_PROVIDER, 100, 0.0f)
    }

    override fun onStateChanged(state: ArcGISArView.ArcGISArViewState) {
        when (state) {
            ArcGISArView.ArcGISArViewState.NOT_INITIALIZED -> {
                //no-op
            }
            ArcGISArView.ArcGISArViewState.INITIALIZING -> {
                // no-op
            }
            ArcGISArView.ArcGISArViewState.INITIALIZED -> {
                /*arcGisArView.originCamera = Camera(20.0, 30.0, 25000000.0, 0.0, 0.0, 0.0)
                arcGisArView.translationTransformationFactor = 25000000.0*/
            }
            ArcGISArView.ArcGISArViewState.INITIALIZATION_FAILURE -> {
                with(getString(R.string.arcgisarview_error, arcGisArView.error?.message)) {
                    Log.e(logTag, this)
                    Toast.makeText(this@ArcGISSceneViewActivity, this, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.arcgissceneview_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_start_tracking -> {
                arcGisArView.startTracking()
                return true
            }
            R.id.action_stop_tracking -> {
                arcGisArView.stopTracking()
                return true
            }
            R.id.action_toggle_manual_rendering -> {
                toggleManualRendering()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Toggle manual rendering of SceneView.
     *
     * @since 100.6.0
     */
    private fun toggleManualRendering() {
        arcGisArView.sceneView.isManualRenderingEnabled = !arcGisArView.sceneView.isManualRenderingEnabled
        Toast.makeText(
            this,
            getString(
                R.string.arcgis_ar_view_activity_manual_rendering_enabled,
                arcGisArView.sceneView.isManualRenderingEnabled
            ),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroy() {
        arcGisArView.removeOnStateChangedListener(this)
        super.onDestroy()
    }
}
