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

import android.location.LocationManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.esri.arcgisruntime.location.AndroidLocationDataSource
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Surface
import com.esri.arcgisruntime.toolkit.test.R
import kotlinx.android.synthetic.main.activity_ar_arcgissceneview.arcGisArView

class ArcGISSceneViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_arcgissceneview)
        arcGisArView.registerLifecycle(lifecycle)
        arcGisArView.sceneView.scene = streetsScene
    }

    private val streetsScene: ArcGISScene
        get() {
            val scene = ArcGISScene(Basemap.createStreets())
            addElevationSource(scene)

            arcGisArView.locationDataSource =
                AndroidLocationDataSource(this, LocationManager.NETWORK_PROVIDER, 100, 0.0f)
            return scene
        }
}

private fun addElevationSource(scene: ArcGISScene) {
    val elevationSource =
        ArcGISTiledElevationSource("https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer")
    val surface = Surface()
    surface.elevationSources.add(elevationSource)
    surface.name = "baseSurface"
    surface.isEnabled = true
    scene.baseSurface = surface
}