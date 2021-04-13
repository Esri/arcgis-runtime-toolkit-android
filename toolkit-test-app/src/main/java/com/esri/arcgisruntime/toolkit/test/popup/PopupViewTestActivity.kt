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
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.toolkit.extension.logTag
import com.esri.arcgisruntime.toolkit.popup.util.observeEvent
import com.esri.arcgisruntime.toolkit.popup.viewmodel.PopupViewModel
import com.esri.arcgisruntime.toolkit.test.R
import com.esri.arcgisruntime.toolkit.test.popup.viewmodel.PopupViewTestViewModel
import com.esri.arcgisruntime.toolkit.test.databinding.ActivityPopupBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.math.roundToInt

class PopupViewTestActivity : AppCompatActivity() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mapView: MapView
    private val popupViewTestViewModel: PopupViewTestViewModel by viewModels()
    private val popupViewModel: PopupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityPopupBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_popup)

        binding.popupViewTestViewModel = popupViewTestViewModel
        binding.lifecycleOwner = this

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetContainer)
        mapView = binding.mapView

        popupViewTestViewModel.bottomSheetState.observe(this, { bottomSheetState ->
            bottomSheetBehavior.state = bottomSheetState
        })

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
            popupViewTestViewModel.setCurrentBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
            // Clear the selected features from the feature layer
            resetIdentifyResult()
        }

        mapView.onTouchListener =
            object : DefaultMapViewOnTouchListener(this, mapView) {
                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {

                    // Only perform identify on the mapview if the Popup is not in edit mode
                    if (popupViewModel.isPopupInEditMode.value == false) {

                        popupViewTestViewModel.setCurrentBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)

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

        popupViewTestViewModel.identifiableLayer?.let {
            // Clear the selected features from the feature layer
            resetIdentifyResult()

            val identifyLayerResultsFuture = mapView
                .identifyLayerAsync(popupViewTestViewModel.identifiableLayer, screenPoint, 5.0, true)

            identifyLayerResultsFuture.addDoneListener {
                try {
                    val identifyLayerResult = identifyLayerResultsFuture.get()

                    if (identifyLayerResult.popups.size > 0) {
                        popupViewModel.setPopup(identifyLayerResult.popups[0])
                        val featureLayer: FeatureLayer? = identifyLayerResult.layerContent as? FeatureLayer
                        featureLayer?.selectFeature(identifyLayerResult.popups[0].geoElement as Feature)
                        popupViewTestViewModel.setCurrentBottomSheetState(BottomSheetBehavior.STATE_HALF_EXPANDED)
                    }
                } catch (e: Exception) {
                    displayErrorMessage("Error identifying results ${e.message}")
                }
            }
        }
    }

    private fun resetIdentifyResult() {
        popupViewTestViewModel.identifiableLayer?.clearSelection()
        popupViewModel.clearPopup()
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

}



