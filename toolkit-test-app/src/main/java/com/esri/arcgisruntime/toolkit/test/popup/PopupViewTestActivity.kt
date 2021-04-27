/*
 * Copyright 2021 Esri
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

package com.esri.arcgisruntime.toolkit.test.popup

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.toolkit.extension.logTag
import com.esri.arcgisruntime.toolkit.popup.PopupView
import com.esri.arcgisruntime.toolkit.popup.PopupViewModel
import com.esri.arcgisruntime.toolkit.test.R
import com.esri.arcgisruntime.toolkit.test.databinding.ActivityPopupBinding
import com.esri.arcgisruntime.toolkit.util.observeEvent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.math.roundToInt

/**
 * Activity to show usages of [PopupView] and [PopupViewModel].
 *
 * - The activity's layout hosts the MapView and bottom sheet
 * - Creates and owns the PopupViewModel for showing the Popup of an identified feature
 * - It handles tap gestures on the MapView in order to identify a feature by overriding
 * onSingleTapConfirmed on DefaultMapViewOnTouchListener
 * - When a feature is identified, selects that feature in the MapView, initializes the
 * PopupViewModel and shows the bottom sheet populated with PopupFragment.
 * - The PopupFragment's layout comprises of edit layout which hosts the controls to activate/de-activate
 * the edit-mode on the PopupView, delete feature, save and cancel edits. It also has the [PopupView] which
 * shows the attributes of the identified feature in read-only mode upon initialization.
 */
class PopupViewTestActivity : AppCompatActivity() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mapView: MapView
    private val popupViewModel: PopupViewModel by viewModels()

    val map: ArcGISMap by lazy {
        val portal = Portal("https://arcgisruntime.maps.arcgis.com/")
        val portalItem = PortalItem(portal, "16f1b8ba37b44dc3884afc8d5f454dd2")
        val map = ArcGISMap(portalItem)
        map
    }

    private val identifiableLayer: FeatureLayer?
        get() {
            return map.operationalLayers?.filterIsInstance<FeatureLayer>()?.filter {
                (it.featureTable?.geometryType == GeometryType.POINT)
                    .and(it.isVisible)
                    .and(it.isPopupEnabled && it.popupDefinition != null)
            }?.get(0)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityPopupBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_popup)

        binding.lifecycleOwner = this

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetContainer)
        mapView = binding.mapView
        mapView.map = map
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    // Clear the selected features from the feature layer
                    resetIdentifyResult()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        popupViewModel.dismissPopupEvent.observeEvent(this) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            // Clear the selected features from the feature layer
            resetIdentifyResult()
        }

        mapView.onTouchListener =
            object : DefaultMapViewOnTouchListener(this, mapView) {
                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {

                    // Only perform identify on the mapview if the Popup is not in edit mode
                    if (popupViewModel.isPopupInEditMode.value == false) {

                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

                        e?.let {
                            val screenPoint = android.graphics.Point(
                                it.x.roundToInt(),
                                it.y.roundToInt()
                            )
                            identifyLayer(screenPoint)
                        }
                    }
                    return true
                }
            }
    }

    /**
     * Performs an identify on the identifiable layer at the given screen point.
     *
     * @param screenPoint in Android graphic coordinates.
     */
    private fun identifyLayer(screenPoint: android.graphics.Point) {

        identifiableLayer?.let {
            // Clear the selected features from the feature layer
            resetIdentifyResult()

            val identifyLayerResultsFuture = mapView
                .identifyLayerAsync(identifiableLayer, screenPoint, 5.0, true)

            identifyLayerResultsFuture.addDoneListener {
                try {
                    val identifyLayerResult = identifyLayerResultsFuture.get()

                    if (identifyLayerResult.popups.size > 0) {
                        popupViewModel.setPopup(identifyLayerResult.popups[0])
                        val featureLayer: FeatureLayer? = identifyLayerResult.layerContent as? FeatureLayer
                        featureLayer?.selectFeature(identifyLayerResult.popups[0].geoElement as Feature)
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                    }
                } catch (e: Exception) {
                    displayErrorMessage("Error identifying results ${e.message}")
                }
            }
        }
    }

    /**
     * Resets the Identify Result.
     */
    private fun resetIdentifyResult() {
        identifiableLayer?.clearSelection()
        popupViewModel.clearPopup()
    }

    /**
     * Displays an error message in LogCat and as a Toast.
     */
    private fun displayErrorMessage(error: String) {
        Log.e(logTag, error)
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

}



