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
import android.graphics.Point
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.esri.arcgisruntime.layers.IntegratedMeshLayer
import com.esri.arcgisruntime.layers.PointCloudLayer
import com.esri.arcgisruntime.location.LocationDataSource
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.NavigationConstraint
import com.esri.arcgisruntime.mapping.Surface
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.symbology.SceneSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSceneSymbol
import com.esri.arcgisruntime.toolkit.ar.ArLocationDataSource
import com.esri.arcgisruntime.toolkit.ar.ArcGISArView
import com.esri.arcgisruntime.toolkit.extension.logTag
import com.esri.arcgisruntime.toolkit.test.R
import com.esri.arcgisruntime.toolkit.test.databinding.ActivityArcgisarviewBinding

import kotlin.properties.Delegates
import kotlin.reflect.KProperty

/**
 * Activity to show usages of [ArcGISArView].
 *
 * @since 100.6.0
 */
class ArcGISArViewActivity : AppCompatActivity() {

    private val locationDataSource: LocationDataSource get() = ArLocationDataSource(this)
    private val sphereOverlay: GraphicsOverlay by lazy {
        GraphicsOverlay().apply {
            this.sceneProperties.surfacePlacement =
                LayerSceneProperties.SurfacePlacement.ABSOLUTE
            binding.arcGisArView.sceneView.graphicsOverlays.add(this)
        }
    }
    private var calibrating: Boolean by Delegates.observable(false) { _: KProperty<*>,
                                                                      _: Boolean,
                                                                      newValue: Boolean ->
        if (newValue) {
            binding.arCalibrationView.bindArcGISArView(binding.arcGisArView)
            binding.arCalibrationView.visibility = View.VISIBLE
        } else {
            binding.arCalibrationView.unbindArcGISArView(binding.arcGisArView)
            binding.arCalibrationView.visibility = View.GONE
        }

        invalidateOptionsMenu()
    }

    /**
     * AR Mode: Full-Scale AR
     * Scene that uses a Streets Basemap.
     *
     * @since 100.6.0
     */
    private fun streetsScene(): () -> ArcGISScene {
        return {
            ArcGISScene(Basemap.createStreets()).apply {
                addElevationSource(this)

                binding.arcGisArView.locationDataSource = locationDataSource
                binding.arcGisArView.translationFactor = 1.0
                binding.arCalibrationView.elevationControlVisibility = false
            }
        }
    }

    /**
     * AR Mode: Tabletop AR
     * Scene that shows a Point Cloud Layer.
     *
     * @since 100.6.0
     */
    private fun pointCloudScene(): () -> ArcGISScene {
        return {
            ArcGISScene().apply {
                val portal = Portal("http://www.arcgis.com")
                val portalItem = PortalItem(portal, "fc3f4a4919394808830cd11df4631a54")
                val layer = PointCloudLayer(portalItem)
                addElevationSource(this)
                this.operationalLayers.add(layer)

                layer.addDoneLoadingListener doneLoadingLayer@{
                    layer.loadError?.let {
                        it.message?.let { errorMessage ->
                            displayErrorMessage(errorMessage)
                        }
                        return@doneLoadingLayer
                    }

                    val extent = layer.fullExtent

                    if (extent != null) {
                        val center = extent.center
                        val camera = Camera(center, 0.0, 90.0, 0.0)
                        binding.arcGisArView.originCamera = camera
                        binding.arcGisArView.translationFactor = 2000.0
                        // Set the clipping distance to limit the data display around the originCamera.
                        binding.arcGisArView.clippingDistance = 750.0
                    }
                }

                binding.arcGisArView.locationDataSource = null
            }
        }
    }

    /**
     * AR Mode: Tabletop AR
     * Scene that is centered on Yosemite National Park.
     *
     * @since 100.6.0
     */
    private fun yosemiteScene(): () -> ArcGISScene {
        return {
            ArcGISScene().apply {
                addElevationSource(this)

                val layer =
                    IntegratedMeshLayer("https://tiles.arcgis.com/tiles/FQD0rKU8X5sAQfh8/arcgis/rest/services/VRICON_Yosemite_Sample_Integrated_Mesh_scene_layer/SceneServer")
                this.operationalLayers.add(layer)

                layer.addDoneLoadingListener doneLoadingLayer@{
                    layer.loadError?.let {
                        it.message?.let { errorMessage ->
                            displayErrorMessage(errorMessage)
                            return@doneLoadingLayer
                        }
                    }

                    val extent = layer.fullExtent
                    val center = extent.center

                    val elevationSource = baseSurface.elevationSources.first()

                    elevationSource.addDoneLoadingListener doneLoadingElevationSource@{
                        loadError?.let {
                            it.message?.let { errorMessage ->
                                displayErrorMessage(errorMessage)
                            }
                            return@doneLoadingElevationSource
                        }

                        val elevationFuture = this.baseSurface.getElevationAsync(center)

                        // when the elevation has loaded
                        elevationFuture.addDoneListener doneLoadingElevation@{
                            loadError?.let {
                                it.message?.let { errorMessage ->
                                    displayErrorMessage(errorMessage)
                                }
                                return@doneLoadingElevation
                            }

                            val elevation = elevationFuture.get()
                            val camera = Camera(
                                center.y,
                                center.x,
                                elevation,
                                0.0,
                                90.0,
                                0.0
                            )
                            binding.arcGisArView.originCamera = camera
                            binding.arcGisArView.translationFactor = 1000.0
                        }
                    }
                }
                binding.arcGisArView.locationDataSource = null
            }
        }
    }

    /**
     * AR Mode: Tabletop AR
     * Scene that is centered on the US-Mexico border.
     *
     * @since 100.6.0
     */
    private fun borderScene(): () -> ArcGISScene {
        return {
            ArcGISScene().apply {
                addElevationSource(this)

                val layer =
                    IntegratedMeshLayer("https://tiles.arcgis.com/tiles/FQD0rKU8X5sAQfh8/arcgis/rest/services/VRICON_SW_US_Sample_Integrated_Mesh_scene_layer/SceneServer")
                this.operationalLayers.add(layer)
                this.addDoneLoadingListener doneLoadingScene@{
                    this.loadError?.let {
                        it.message?.let { errorMessage ->
                            displayErrorMessage(errorMessage)
                            return@doneLoadingScene
                        }
                    }

                    val extent = layer.fullExtent
                    val center = extent.center

                    val elevationSource = baseSurface.elevationSources.first()
                    elevationSource.addDoneLoadingListener doneLoadingElevationSource@{
                        loadError?.let {
                            it.message?.let { errorMessage ->
                                displayErrorMessage(errorMessage)
                            }
                            return@doneLoadingElevationSource
                        }

                        val elevationFuture = this.baseSurface.getElevationAsync(center)
                        // when the elevation has loaded
                        elevationFuture.addDoneListener doneLoadingElevation@{
                            loadError?.let {
                                it.message?.let { errorMessage ->
                                    displayErrorMessage(errorMessage)
                                }
                                return@doneLoadingElevation
                            }

                            val elevation = elevationFuture.get()
                            val camera = Camera(
                                center.y,
                                center.x,
                                elevation,
                                0.0,
                                90.0,
                                0.0
                            )
                            binding.arcGisArView.originCamera = camera
                            binding.arcGisArView.translationFactor = 1000.0
                            // Set the clipping distance to limit the data display around the originCamera.
                            binding.arcGisArView.clippingDistance = 500.0
                        }
                    }
                }
                binding.arcGisArView.locationDataSource = null
            }
        }
    }

    /**
     * AR Mode: Full-Scale AR.
     * Scene that is empty with an elevation source.
     *
     * @since 100.6.0
     */
    private fun emptyScene(): () -> ArcGISScene {
        return {
            ArcGISScene().apply {
                addElevationSource(this)

                binding.arcGisArView.locationDataSource = null
                binding.arcGisArView.originCamera = Camera(0.0, 0.0, 0.0, 0.0, 90.0, 0.0)
                binding.arcGisArView.translationFactor = 1.0
                binding.arCalibrationView.elevationControlVisibility = true
            }
        }
    }

    private val scenes: Array<SceneInfo> by lazy {
        arrayOf(
            SceneInfo(
                streetsScene(), getString(R.string.arcgis_ar_view_scene_streets), false,
                ArcGISArView.ARLocationTrackingMode.CONTINUOUS
            ),
            SceneInfo(
                pointCloudScene(),
                getString(R.string.arcgis_ar_view_scene_point_cloud),
                true
            ),
            SceneInfo(yosemiteScene(), getString(R.string.arcgis_ar_view_scene_yosemite), true),
            SceneInfo(borderScene(), getString(R.string.arcgis_ar_view_scene_border), true),
            SceneInfo(
                emptyScene(), getString(R.string.arcgis_ar_view_scene_empty), false,
                ArcGISArView.ARLocationTrackingMode.INITIAL
            )
        )
    }

    private var currentScene: SceneInfo? = null
        set(value) {
            field = value
            value?.let {
                binding.arcGisArView.sceneView.scene = it.scene.invoke()
                title = it.name
                calibrating = calibrating.and(it.isTabletop.not())
                invalidateOptionsMenu()
                binding.arcGisArView.resetTracking()
                binding.arcGisArView.startTracking(it.locationTrackingMode)
            }
        }

    private lateinit var binding: ActivityArcgisarviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_arcgisarview)
        binding.lifecycleOwner = this
        currentScene = scenes[0]

        binding.arcGisArView.sceneView.setOnTouchListener(object :
            DefaultSceneViewOnTouchListener(binding.arcGisArView.sceneView) {
            override fun onSingleTapConfirmed(motionEvent: MotionEvent?): Boolean {
                motionEvent?.let {
                    with(Point(motionEvent.x.toInt(), motionEvent.y.toInt())) {
                        if (currentScene?.isTabletop == true) {
                            binding.arcGisArView.setInitialTransformationMatrix(this)
                        } else {
                            val sphere = SimpleMarkerSceneSymbol.createSphere(
                                Color.CYAN,
                                0.25,
                                SceneSymbol.AnchorPosition.BOTTOM
                            )
                            binding.arcGisArView.arScreenToLocation(this)?.let {
                                val graphic = Graphic(it, sphere)
                                sphereOverlay.graphics.add(graphic)
                            }
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun onResume() {
        super.onResume()
        binding.arcGisArView.startTracking(currentScene?.locationTrackingMode)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.arcgisarview_menu, menu)

        menu?.findItem(R.id.actionToggleCalibration)?.let {
            setMenuItemIsEnabled(it, currentScene?.isTabletop?.not() ?: false)
        }

        scenes.forEachIndexed { index, sceneInfo ->
            menu?.add(R.id.arcgisArViewMenuSceneGroup, index, Menu.NONE, sceneInfo.name)?.let {
                setMenuItemIsEnabled(it, !calibrating)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Sets the [isEnabled] property on the provided [menuItem] and adds a color filter to the icon
     * to indicate whether it's enabled or not.Currently defaults to Color.WHITE for enabled and
     * Color.GRAY for disabled.
     *
     * @since 100.6.0
     */
    private fun setMenuItemIsEnabled(menuItem: MenuItem, isEnabled: Boolean) {
        menuItem.isEnabled = isEnabled

        val icon = menuItem.icon?.mutate()

        icon?.let {
            it.setColorFilter(
                if (isEnabled) Color.WHITE else Color.GRAY,
                PorterDuff.Mode.SRC_IN
            )
            menuItem.icon = it
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.groupId) {
            R.id.arcgisArViewMenuActionGroup -> {
                handleMenuAction(item.itemId)
                return true
            }
            R.id.arcgisArViewMenuSceneGroup -> {
                selectScene(item.itemId)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Handle an action defined in the menu using the [itemId] as an identifier.
     *
     * @since 100.6.0
     */
    private fun handleMenuAction(itemId: Int) {
        if (itemId == R.id.actionToggleCalibration) {
            toggleCalibration()
        }
    }

    /**
     * Select a scene using the [itemId] from the menuItem as an index.
     *
     * @since 100.6.0
     */
    private fun selectScene(itemId: Int) {
        currentScene = scenes[itemId]
    }

    /**
     * Toggle [calibrating] property.
     *
     * @since 100.6.0
     */
    private fun toggleCalibration() {
        calibrating = calibrating.not()
    }

    /**
     * Displays an error message in LogCat and as a Toast.
     *
     * @since 100.6.0
     */
    private fun displayErrorMessage(error: String) {
        Log.e(logTag, error)
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        binding.arcGisArView.stopTracking()
        super.onPause()
    }

    override fun onDestroy() {
        binding.arCalibrationView.unbindArcGISArView(binding.arcGisArView)
        super.onDestroy()
    }
}

/**
 * Adds an elevation source to the provided [scene].
 *
 * @since 100.6.0
 */
private fun addElevationSource(scene: ArcGISScene) {
    val elevationSource =
        ArcGISTiledElevationSource("https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer")
    val surface = Surface()
    surface.elevationSources.add(elevationSource)
    surface.name = "baseSurface"
    surface.isEnabled = true
    surface.backgroundGrid.color = Color.TRANSPARENT
    surface.backgroundGrid.gridLineColor = Color.TRANSPARENT
    surface.navigationConstraint = NavigationConstraint.NONE
    scene.baseSurface = surface
}

private data class SceneInfo(
    val scene: () -> ArcGISScene,
    val name: String,
    val isTabletop: Boolean,
    val locationTrackingMode: ArcGISArView.ARLocationTrackingMode = ArcGISArView.ARLocationTrackingMode.IGNORE
)
