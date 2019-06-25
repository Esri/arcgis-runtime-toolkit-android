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

import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.PointCloudLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.*
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler
import com.esri.arcgisruntime.toolkit.ar.ArcGISArView
import com.esri.arcgisruntime.toolkit.extension.logTag
import com.esri.arcgisruntime.toolkit.test.R
import com.esri.arcgisruntime.toolkit.test.dialog.EditTextDialogFragment
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem
import kotlinx.android.synthetic.main.activity_arcgissceneview.arcGisArView
import java.util.HashMap
import java.util.concurrent.ExecutionException

class ArcGISSceneViewTableTopActivity : AppCompatActivity(), ArcGISArView.OnStateChangedListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arcgissceneview_tabletop)


        with(arcGisArView.sceneView) {
            //scene = ArcGISScene(Basemap.createImagery())
            scene = ArcGISScene()
            val surface = Surface()
            val elevSource = ArcGISTiledElevationSource("http://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer")
            surface.elevationSources.add(elevSource)
            surface.navigationConstraint = NavigationConstraint.NONE
            surface.opacity = 0f
            scene.baseSurface = surface
            val pointCloud = PointCloudLayer("https://tiles.arcgis.com/tiles/OLiydejKCZTGhvWg/arcgis/rest/services/3D_Punktwolke_Dome_Köln/SceneServer/layers/0")
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
                arcGisArView.originCamera = Camera(50.94334724, 6.96472093, 44.412,  267.0, 90.0, 0.0)
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