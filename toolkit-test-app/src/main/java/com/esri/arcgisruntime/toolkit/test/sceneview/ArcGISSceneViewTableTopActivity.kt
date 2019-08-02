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

import android.graphics.Point
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Toast
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.layers.PointCloudLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.NavigationConstraint
import com.esri.arcgisruntime.mapping.Surface
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener
import com.esri.arcgisruntime.toolkit.ar.ArcGISArView
import com.esri.arcgisruntime.toolkit.extension.logTag
import com.esri.arcgisruntime.toolkit.test.R
import kotlinx.android.synthetic.main.activity_arcgissceneview.arcGisArView

class ArcGISSceneViewTableTopActivity : AppCompatActivity(), ArcGISArView.OnStateChangedListener {

    private var _scene: ArcGISScene? = null
    private var pointCloudLayer: Layer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arcgissceneview_tabletop)
        arcGisArView.registerLifecycle(lifecycle)
        arcGisArView.addOnStateChangedListener(this)

        arcGisArView.sceneView.setOnTouchListener(object : DefaultSceneViewOnTouchListener(arcGisArView.sceneView) {
            override fun onSingleTapConfirmed(motionEvent: MotionEvent?): Boolean {
                motionEvent?.let {
                    with(Point(motionEvent.x.toInt(), motionEvent.y.toInt())) {
                        if (arcGisArView.setInitialTransformationMatrix(this)) {
                            if (_scene != null && pointCloudLayer != null && !_scene!!.operationalLayers.contains(
                                    pointCloudLayer
                                )
                            ) {
                                _scene!!.operationalLayers.add(pointCloudLayer)
                            }
                        }
                    }
                }
                return false
            }
        })
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
                arcGisArView.originCamera = Camera(50.94334724, 6.96472093, 44.412, 0.0, 0.0, 0.0)
                arcGisArView.translationTransformationFactor = 4000.0

                with(arcGisArView.sceneView) {
                    _scene = ArcGISScene()
                    scene = _scene
                    val surface = Surface()
                    ArcGISTiledElevationSource("http://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer").let {
                        surface.elevationSources.add(it)
                    }
                    surface.navigationConstraint = NavigationConstraint.NONE
                    surface.opacity = 0f
                    scene.baseSurface = surface
                    pointCloudLayer =
                        PointCloudLayer("https://tiles.arcgis.com/tiles/OLiydejKCZTGhvWg/arcgis/rest/services/3D_Punktwolke_Dome_Köln/SceneServer/layers/0").also {
                            it.addLoadStatusChangedListener { loadStatusChangedEvent ->
                                if (loadStatusChangedEvent.newLoadStatus == LoadStatus.LOADED) {
                                    showSnackbar(R.string.arcgis_sceneview_tabletop_activity_loading_point_cloud_layer_complete)
                                } else if (loadStatusChangedEvent.newLoadStatus == LoadStatus.FAILED_TO_LOAD) {
                                    Toast.makeText(
                                        this@ArcGISSceneViewTableTopActivity,
                                        getString(
                                            R.string.arcgis_sceneview_tabletop_activity_loading_point_cloud_layer_failed,
                                            "${it.loadError.message} - ${it.loadError.cause}"
                                        ),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            it.loadAsync()
                            showSnackbar(R.string.arcgis_sceneview_tabletop_activity_loading_point_cloud_layer)
                        }
                }
            }
            ArcGISArView.ArcGISArViewState.INITIALIZATION_FAILURE -> {
                with(getString(R.string.arcgisarview_error, arcGisArView.error?.message)) {
                    Log.e(logTag, this)
                    Toast.makeText(this@ArcGISSceneViewTableTopActivity, this, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showSnackbar(@StringRes stringRes: Int) {
        Snackbar.make(arcGisArView, stringRes, Snackbar.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.arcgissceneview_tabletop_activity, menu)
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
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        arcGisArView.removeOnStateChangedListener(this)
        super.onDestroy()
    }

}
