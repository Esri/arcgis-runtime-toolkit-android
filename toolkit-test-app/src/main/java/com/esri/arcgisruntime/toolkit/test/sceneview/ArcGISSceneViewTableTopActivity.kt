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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.esri.arcgisruntime.layers.PointCloudLayer
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.NavigationConstraint
import com.esri.arcgisruntime.mapping.Surface
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.toolkit.ar.ArcGISArView
import com.esri.arcgisruntime.toolkit.extension.logTag
import com.esri.arcgisruntime.toolkit.test.R
import kotlinx.android.synthetic.main.activity_arcgissceneview.arcGisArView
import java.net.URI

class ArcGISSceneViewTableTopActivity : AppCompatActivity(), ArcGISArView.OnStateChangedListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arcgissceneview_tabletop)


        with(arcGisArView.sceneView) {
            //scene = ArcGISScene(Basemap.createImagery())
            scene = ArcGISScene()
            val surface = Surface()
            val elevSource =
                ArcGISTiledElevationSource("http://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer")
            surface.elevationSources.add(elevSource)
            surface.navigationConstraint = NavigationConstraint.NONE
            surface.opacity = 0f
            scene.baseSurface = surface
            val encodedString =
                URI.create("https://tiles.arcgis.com/tiles/OLiydejKCZTGhvWg/arcgis/rest/services/3D_Punktwolke_Dome_Köln/SceneServer/layers/0")
            val pointCloud = PointCloudLayer(encodedString.toASCIIString())
            scene.operationalLayers.add(pointCloud)
        }


        arcGisArView.registerLifecycle(lifecycle)
        arcGisArView.addOnStateChangedListener(this)

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
                //https://www.arcgis.com/home/webscene/viewer.html?webscene=f55040b5d81f40e291c9739477fdb7b7&viewpoint=cam:6.96472093,50.94334724,44.412;267.371,0.434
                //arcGisArView.originCamera = Camera(34.05610, -117.18374, 412.44,  0.0, 90.0, 0.0)
                arcGisArView.originCamera = Camera(50.94334724, 6.96472093, 44.412, 267.0, 90.0, 0.0)
                arcGisArView.translationTransformationFactor = 4000.0
            }
            ArcGISArView.ArcGISArViewState.INITIALIZATION_FAILURE -> {
                with(getString(R.string.arcgisarview_error, arcGisArView.error?.message)) {
                    Log.e(logTag, this)
                    Toast.makeText(this@ArcGISSceneViewTableTopActivity, this, Toast.LENGTH_LONG).show()
                }
            }
        }
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