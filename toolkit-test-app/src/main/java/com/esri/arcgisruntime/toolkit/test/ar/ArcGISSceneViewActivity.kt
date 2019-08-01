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

package com.esri.arcgisruntime.toolkit.test.ar

import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.esri.arcgisruntime.layers.PointCloudLayer
import com.esri.arcgisruntime.location.AndroidLocationDataSource
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Surface
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.toolkit.test.R
import kotlinx.android.synthetic.main.activity_ar_arcgissceneview.arcGisArView

class ArcGISSceneViewActivity : AppCompatActivity() {

    private val streetsScene: ArcGISScene by lazy {
        ArcGISScene(Basemap.createStreets()).apply {
            addElevationSource(this)

            arcGisArView.locationDataSource =
                AndroidLocationDataSource(this@ArcGISSceneViewActivity, LocationManager.NETWORK_PROVIDER, 100, 0.0f)
        }
    }

    private val pointCloudScene: ArcGISScene by lazy {
        ArcGISScene().apply {
            val portal = Portal("http://www.arcgis.com")
            val portalItem = PortalItem(portal, "fc3f4a4919394808830cd11df4631a54")
            val layer = PointCloudLayer(portalItem)
            addElevationSource(this)
            this.operationalLayers.add(layer)

            layer.addDoneLoadingListener {
                layer.loadError?.let {
                    return@addDoneLoadingListener
                }

                layer.fullExtent?.let {
                    val center = it.center

                    val camera = Camera(center, 0.0, 0.0, 0.0)
                    arcGisArView.originCamera = camera
                    arcGisArView.translationTransformationFactor = 2000.0
                }
            }

            arcGisArView.locationDataSource = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_arcgissceneview)
        arcGisArView.registerLifecycle(lifecycle)
        arcGisArView.sceneView.scene = pointCloudScene
    }
}

private fun addElevationSource(scene: ArcGISScene) {
    val elevationSource =
        ArcGISTiledElevationSource("https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer")
    val surface = Surface()
    surface.elevationSources.add(elevationSource)
    surface.name = "baseSurface"
    surface.isEnabled = true
    surface.backgroundGrid.color = Color.TRANSPARENT
    surface.backgroundGrid.gridLineColor = Color.TRANSPARENT
    scene.baseSurface = surface
}