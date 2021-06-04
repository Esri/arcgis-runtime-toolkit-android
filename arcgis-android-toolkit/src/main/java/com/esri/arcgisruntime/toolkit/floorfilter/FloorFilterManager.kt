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

package com.esri.arcgisruntime.toolkit.floorfilter

import android.util.Log
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView

internal class FloorFilterManager {

    var floorManager: FloorManager? = null

    // Custom events
    // TODO: allow these to set more than one listener?
    private var onSiteChangeListener: ((FloorSite?) -> Unit)? = null
    fun setOnSiteChangeListener(onSiteChangeListener: ((FloorSite?) -> Unit)?) {
        this.onSiteChangeListener = onSiteChangeListener
    }
    private var onFacilityChangeListener: ((FloorFacility?) -> Unit)? = null
    fun setOnFacilityChangeListener(onFacilityChangeListener: ((FloorFacility?) -> Unit)?) {
        this.onFacilityChangeListener = onFacilityChangeListener
    }
    private var onLevelChangeListener: ((FloorLevel?) -> Unit)? = null
    fun setOnLevelChangeListener(onLevelChangeListener: ((FloorLevel?) -> Unit)?) {
        this.onLevelChangeListener = onLevelChangeListener
    }

    private var mSelectedSiteId: String? = null
    var selectedSiteId: String?
        get() { return mSelectedSiteId }
        set(value) {
            if (mSelectedSiteId != value) {
                mSelectedSiteId = value
                selectedFacilityId = null
                zoomToSite()
                Log.d("summer", "site changed: ${getSelectedSite()?.name}")
                onSiteChangeListener?.invoke(getSelectedSite())
            }
        }
    private var mSelectedFacilityId: String? = null
    var selectedFacilityId: String?
        get() { return mSelectedFacilityId }
        set(value) {
            if (mSelectedFacilityId != value) {
                mSelectedFacilityId = value
                selectedLevelId = getDefaultLevelIdForFacility(value)
                zoomToFacility()
                Log.d("summer", "facility changed: ${getSelectedFacility()?.name}")
                onFacilityChangeListener?.invoke(getSelectedFacility())
            }
        }
    private var mSelectedLevelId: String? = null
    var selectedLevelId: String?
        get() { return mSelectedLevelId }
        set(value) {
            if (mSelectedLevelId != value) {
                mSelectedLevelId = value
                val selectedLevelsFacility = getSelectedLevel()?.facility
                mSelectedFacilityId = selectedLevelsFacility?.id
                mSelectedSiteId = selectedLevelsFacility?.site?.id
                Log.d("summer", "level changed: ${getSelectedLevel()?.shortName}")
                filterMap()
                onLevelChangeListener?.invoke(getSelectedLevel())
            }
        }

    var mapView: MapView? = null
        private set
    var map: ArcGISMap? = null
        private set

    val sites: List<FloorSite>
        get() { return floorManager?.sites ?: emptyList() }
    val facilities: List<FloorFacility>
        get() { return floorManager?.facilities ?: emptyList() }
    val levels: List<FloorLevel>
        get() { return floorManager?.levels ?: emptyList() }

    fun isSelectedLevel(level: FloorLevel?): Boolean {
        return level != null && selectedLevelId == level.id
    }

    fun isSelectedFacility(facility: FloorFacility?): Boolean {
        return facility != null && selectedFacilityId == facility.id
    }

    fun isSelectedSite(site: FloorSite?): Boolean {
        return site != null && selectedSiteId == site.id
    }

    fun getSelectedLevel(): FloorLevel? {
        return levels.firstOrNull { isSelectedLevel(it) }
    }

    fun getSelectedFacility(): FloorFacility? {
        return facilities.firstOrNull { isSelectedFacility(it) }
    }

    fun getSelectedSite(): FloorSite? {
        return sites.firstOrNull { isSelectedSite(it) }
    }

    fun setupMap(mapView: MapView, map: ArcGISMap, setupDone: (() -> Unit)? = null) {
        this.map = map
        this.mapView = mapView

        val floorManager = FloorManager(map)
        this.floorManager = floorManager
        val doneLoadingListener: Runnable = object: Runnable {
            override fun run() {
                floorManager.removeDoneLoadingListener(this)
                setupDone?.invoke()
            }
        }
        floorManager.addDoneLoadingListener(doneLoadingListener)
        floorManager.loadAsync()
    }

    fun clearMapView() {
        this.mapView = null
    }

    private fun zoomToSite() {
        zoomToExtent(mapView, getSelectedSite()?.geometry?.extent)
    }

    private fun zoomToFacility() {
        zoomToExtent(mapView, getSelectedFacility()?.geometry?.extent)
    }

    private fun zoomToExtent(mapView: MapView?, envelope: Envelope?, bufferFactor:Double = 1.25) {
        if (mapView != null && envelope != null) {
            try {
                val envelopeWithBuffer = Envelope(envelope.center, envelope.width * bufferFactor,envelope.height * bufferFactor)
                if (!envelopeWithBuffer.isEmpty) {
                    mapView.setViewpointAsync(Viewpoint(envelopeWithBuffer), 0.5f)
                }
            } catch (e: Exception) {
                Log.w("summer", "Unable to zoom to extent: ${e.message}")
            }
        }
    }

    private fun filterMap() {
        // TODO: This should use runtime api instead of layer definition expressions
        // TODO: This also needs to handle showing ground floor data for other facilities
        Log.d("summer", "----------- Filter Map ----------")
        val selectedLevelId = selectedLevelId

        fun getFieldByName(layer: FeatureLayer?, fieldName: String): Field? {
            return layer?.featureTable?.fields?.find {
                it?.name.equals(fieldName, true)
            }
        }
        map?.operationalLayers?.forEach {
            val layer = it as? FeatureLayer
            if (layer != null) {
                val levelIdField = getFieldByName(layer, "level_id")
                if (levelIdField != null) {
                    layer.definitionExpression = "${levelIdField.name} = '${selectedLevelId}' OR ${levelIdField.name} IS NULL"
                }
                Log.d("summer", "\t\t${layer.name}: ${layer.definitionExpression}")
            }
        }
        Log.d("summer", "---------------------------------")
    }

    private fun getDefaultLevelIdForFacility(facilityId: String?): String? {
        val candidateLevels = levels.filter { it.facility?.id == facilityId }
        return (candidateLevels.firstOrNull{ it.verticalOrder == 0 } ?: getLowestLevel(candidateLevels))?.id
    }

    private fun getLowestLevel(levels: List<FloorLevel>): FloorLevel? {
        var lowestLevel: FloorLevel? = null
        levels.forEach {
            if (it.verticalOrder != Int.MIN_VALUE && it.verticalOrder != Int.MAX_VALUE) {
                val lowestVerticalOrder = lowestLevel?.verticalOrder
                if (lowestVerticalOrder == null || lowestVerticalOrder > it.verticalOrder) {
                    lowestLevel = it
                }
            }
        }
        return lowestLevel
    }
}
