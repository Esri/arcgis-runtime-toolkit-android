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
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler
import com.esri.arcgisruntime.toolkit.extension.logTag
import com.esri.arcgisruntime.toolkit.sceneview.ArcGISArView
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

class ArcGISSceneViewCollectorActivity : AppCompatActivity(), ArcGISArView.OnStateChangedListener,
    EditTextDialogFragment.OnButtonClickedListener {

    private lateinit var serviceFeatureTable: ServiceFeatureTable
    private var viewRenderable: ViewRenderable? = null
    private var lastTapPoint: Point? = null
    private var lastTapAnchor: Anchor? = null

    private val onPointResolvedListener = object : ArcGISArView.OnPointResolvedListener {
        override fun onPointResolved(point: Point, tapAnchor: Anchor) {
            lastTapPoint = point
            lastTapAnchor = tapAnchor
            Log.d(logTag, "Lat=${point.y} Lon=${point.x}")
            EditTextDialogFragment.newInstance("Feature Name", "Save", "Cancel").show(supportFragmentManager, "dialog")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arcgissceneview_collector)

        AuthenticationManager.setAuthenticationChallengeHandler(DefaultAuthenticationChallengeHandler(this))

        with(arcGisArView.sceneView) {
            scene = ArcGISScene(Basemap.createImagery())
            // create service feature table from URL
            serviceFeatureTable =
                ServiceFeatureTable("https://services1.arcgis.com/6677msI40mnLuuLr/arcgis/rest/services/LukesARLayer/FeatureServer/0")

            // create a feature layer from table
            val featureLayer = FeatureLayer(serviceFeatureTable)

            featureLayer.addLoadStatusChangedListener {
                if (it.newLoadStatus == LoadStatus.FAILED_TO_LOAD) {
                    Toast.makeText(
                        this@ArcGISSceneViewCollectorActivity,
                        featureLayer.loadError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Toast.makeText(
                    this@ArcGISSceneViewCollectorActivity,
                    "Feature Layer Status: ${it.newLoadStatus}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // add the layer to the map
            scene.operationalLayers.add(featureLayer)
        }

        arcGisArView.registerLifecycle(lifecycle)
        arcGisArView.addOnStateChangedListener(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            initializeViewRenderable()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeViewRenderable() {
        val imageView = ImageView(this)
        imageView.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                com.esri.arcgisruntime.toolkit.R.drawable.ic_location_on,
                null
            )
        )
        ViewRenderable.builder()
            .setView(this, imageView)
            .build()
            .thenAccept { renderable ->
                viewRenderable = renderable
            }
            .exceptionally {
                val toast = Toast.makeText(this, "Unable to load view renderable", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
                return@exceptionally null
            }
    }

    override fun onStateChanged(state: ArcGISArView.ArcGISArViewState) {
        when (state) {
            is ArcGISArView.ArcGISArViewState.Initialized -> {
                arcGisArView.originCamera = Camera(55.953251, -3.188267, 1.0, 0.0, 90.0, 0.0)
                arcGisArView.onPointResolvedListener = onPointResolvedListener
            }
            is ArcGISArView.ArcGISArViewState.InitializationFailure -> {
                with(getString(R.string.arcgisarview_error, state.exception.message)) {
                    Log.e(logTag, this)
                    Toast.makeText(this@ArcGISSceneViewCollectorActivity, this, Toast.LENGTH_SHORT).show()
                }
            }
            is ArcGISArView.ArcGISArViewState.PermissionRequired -> {
                with(getString(R.string.arcgisarview_permission_required, state.permission)) {
                    Log.d(logTag, this)
                    Toast.makeText(this@ArcGISSceneViewCollectorActivity, this, Toast.LENGTH_SHORT).show()
                }
            }
            is ArcGISArView.ArcGISArViewState.ArCoreInstallationRequired -> {
                with(getString(R.string.arcgisarview_arcore_install_required)) {
                    Log.d(logTag, this)
                    Toast.makeText(this@ArcGISSceneViewCollectorActivity, this, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onPositiveClicked(editTextValue: String, extraData: Bundle?) {
        lastTapPoint?.let { addFeature(it, serviceFeatureTable, editTextValue) }
        lastTapAnchor?.let { createIcon(it) }
        lastTapPoint = null
        lastTapAnchor = null
    }

    override fun onCancelClicked() {
        lastTapPoint = null
        lastTapAnchor = null
    }

    /**
     * Adds a new Feature to a ServiceFeatureTable and applies the changes to the
     * server.
     *
     * @param mapPoint     location to add feature
     * @param featureTable service feature table to add feature
     */
    private fun addFeature(
        mapPoint: Point,
        featureTable: ServiceFeatureTable,
        pointName: String
    ) {
        // create default attributes for the feature
        val attributes = HashMap<String, Any>()
        attributes["point_name"] = pointName

        // creates a new feature using default attributes and point
        val feature = featureTable.createFeature(attributes, mapPoint)

        // check if feature can be added to feature table
        if (featureTable.canAdd()) {
            // add the new feature to the feature table and to server
            featureTable.addFeatureAsync(feature).addDoneListener {
                applyEdits(featureTable)
            }
        } else {
            runOnUiThread {
                Toast.makeText(this, "Failed to add feature", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Sends any edits on the ServiceFeatureTable to the server.
     *
     * @param featureTable service feature table
     */
    private fun applyEdits(featureTable: ServiceFeatureTable) {
        // apply the changes to the server
        val editResult = featureTable.applyEditsAsync()
        editResult.addDoneListener {
            try {
                val editResults = editResult.get()
                // check if the server edit was successful
                if (editResults != null && editResults.isNotEmpty()) {
                    if (!editResults[0].hasCompletedWithErrors()) {
                        runOnUiThread {
                            Toast.makeText(this, "Feature added", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        throw editResults[0].error
                    }
                }
            } catch (e: InterruptedException) {
                runOnUiThread {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: ExecutionException) {
                runOnUiThread { Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun createIcon(anchor: Anchor) {
        if (viewRenderable == null) {
            return
        }

        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arcGisArView.arSceneView.scene)

        val selectionVisualizer = FootprintSelectionVisualizer()

        val transformationSystem = TransformationSystem(resources.displayMetrics, selectionVisualizer)

        // Create the transformable andy and add it to the anchor.
        val icon = TransformableNode(transformationSystem)
        icon.setParent(anchorNode)
        icon.renderable = viewRenderable
        icon.select()
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
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        arcGisArView.removeOnStateChangedListener(this)
        super.onDestroy()
    }

}