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
import com.esri.arcgisruntime.ArcGISRuntimeException
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.FeatureQueryResult
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.loadable.LoadStatusChangedListener
import com.esri.arcgisruntime.loadable.Loadable
import com.esri.arcgisruntime.mapping.ArcGISMap
import org.json.JSONArray

/**
 * The FloorManager object. This will be replaced with the FloorManager object in runtime.
 */
internal class FloorManager(val map: ArcGISMap): Loadable {
    var facilities: List<FloorFacility> = emptyList()
    var facilityLayer: FeatureLayer? = null
    var levelLayer: FeatureLayer? = null
    var levels: List<FloorLevel> = emptyList()
    var siteLayer: FeatureLayer? = null
    var sites: List<FloorSite> = emptyList()

    private var mLoadStatus: LoadStatus = LoadStatus.NOT_LOADED
    private var mLoadError: ArcGISRuntimeException? = null
    private var mDoneLoadingListener: Runnable? = null

    override fun getLoadStatus(): LoadStatus {
        return mLoadStatus
    }

    override fun getLoadError(): ArcGISRuntimeException? {
        return mLoadError
    }

    override fun cancelLoad() {
        // This will be available in the runtime object
    }

    override fun loadAsync() {
        mLoadStatus = LoadStatus.LOADING
        loadFromMap()
    }

    override fun retryLoadAsync() {
        if (mLoadStatus != LoadStatus.LOADED && mLoadStatus != LoadStatus.LOADING) {
            loadAsync()
        }
    }

    override fun addDoneLoadingListener(p0: Runnable?) {
        mDoneLoadingListener = p0
        if (mLoadStatus == LoadStatus.LOADED) {
            mDoneLoadingListener?.run()
        }
    }

    override fun removeDoneLoadingListener(p0: Runnable?): Boolean {
        if (mDoneLoadingListener == p0) {
            mDoneLoadingListener = null
            return true
        }
        return false
    }

    override fun addLoadStatusChangedListener(p0: LoadStatusChangedListener?) {
        // This will be available in the runtime object
    }

    override fun removeLoadStatusChangedListener(p0: LoadStatusChangedListener?): Boolean {
        // This will be available in the runtime object
        return true
    }

    private fun loadFromMap() {
        Log.d("summer", "load data from map")
        siteLayer = getLayerByName("sites")
        facilityLayer = getLayerByName("facilities")
        levelLayer = getLayerByName("levels")

        if (facilityLayer == null || levelLayer == null) {
            mLoadError = ArcGISRuntimeException(-1, ArcGISRuntimeException.ErrorDomain.ARCGIS_RUNTIME, "No facility or level layer in the map.", null, null)
            finishDataLoad(LoadStatus.FAILED_TO_LOAD)
        } else {
            queryAllFeatures(siteLayer) { sitesQueryResult ->
                createSitesFromQueryResult(sitesQueryResult)
                queryAllFeatures(facilityLayer) { facilitiesQueryResult ->
                    createFacilitiesFromQueryResult(facilitiesQueryResult)
                    queryAllFeatures(levelLayer) { levelsQueryResult ->
                        createLevelsFromQueryResult(levelsQueryResult)
                        finishDataLoad()
                    }
                }
            }
        }
    }

    private fun createLevelsFromQueryResult(queryResult: FeatureQueryResult?) {
        val nameField = getFieldByName(levelLayer, "name_short")
        val idField = getFieldByName(levelLayer, "level_id")
        val facilityIdField = getFieldByName(levelLayer, "facility_id")
        val verticalOrderField = getFieldByName(levelLayer, "vertical_order")

        if (nameField == null || idField == null || verticalOrderField == null) {
            Log.e("summer", "No Name, ID, or Vertical Order field for the levels layer.")
            return
        }

        levels = queryResult?.mapNotNull {
            var level: FloorLevel? = null
            if (it != null) {
                val levelName = it.attributes?.get(nameField.name) as? String ?: ""
                val levelId = it.attributes?.get(idField.name) as? String ?: ""
                val facilityId = it.attributes?.get(facilityIdField?.name) as? String ?: ""
                val verticalOrder = it.attributes?.get(verticalOrderField.name) as? Int ?: Int.MAX_VALUE
                val facility = facilities.find { facility -> facility.id == facilityId }
                level = FloorLevel(levelId, levelName, verticalOrder, facility, it.geometry)
            }
            level
        } ?: emptyList()

        facilities.forEach { facility ->
            facility.levels = levels.filter { it.facility?.id == facility.id }
        }
    }

    private fun createFacilitiesFromQueryResult(queryResult: FeatureQueryResult?) {
        val nameField = getFieldByName(facilityLayer, "name")
        val idField = getFieldByName(facilityLayer, "facility_id")
        val siteIdField = getFieldByName(facilityLayer, "site_id")

        if (nameField == null || idField == null) {
            Log.e("summer", "No Name or ID field for the facilities layer.")
            return
        }

        facilities = queryResult?.mapNotNull {
            var facility: FloorFacility? = null
            if (it != null) {
                val facilityName = it.attributes?.get(nameField.name) as? String ?: ""
                val facilityId = it.attributes?.get(idField.name) as? String ?: ""
                val siteId = it.attributes?.get(siteIdField?.name) as? String ?: ""
                val site = sites.find { site -> site.id == siteId }
                facility = FloorFacility(facilityId, facilityName, site, emptyList(), it.geometry)
            }
            facility
        } ?: emptyList()

        sites.forEach { site ->
            site.facilities = facilities.filter { it.site?.id == site.id }
        }
    }

    private fun createSitesFromQueryResult(queryResult: FeatureQueryResult?) {
        val nameField = getFieldByName(siteLayer, "name")
        val idField = getFieldByName(siteLayer, "site_id")

        if (nameField == null || idField == null) {
            Log.e("summer", "No Name or ID field for the sites layer.")
            return
        }

        sites = queryResult?.mapNotNull {
            var site: FloorSite? = null
            if (it != null) {
                val siteName = it.attributes?.get(nameField.name) as? String ?: ""
                val siteId = it.attributes?.get(idField.name) as? String ?: ""
                site = FloorSite(siteId, siteName, emptyList(), it.geometry)
            }
            site
        } ?: emptyList()
    }

    private fun getLayerByName(layerName: String): FeatureLayer? {
        return map.operationalLayers?.find {
            it?.name.equals(layerName, true) && it is FeatureLayer
        } as? FeatureLayer
    }

    private fun getFieldByName(layer: FeatureLayer?, fieldName: String): Field? {
        return layer?.featureTable?.fields?.find {
            it?.name.equals(fieldName, true)
        }
    }

    private fun queryAllFeatures(layer: FeatureLayer?, callback: (result: FeatureQueryResult?) -> Unit) {
        val table = layer?.featureTable
        val queryParams = QueryParameters()
        queryParams.whereClause = "1=1"
        queryParams.isReturnGeometry = true
        queryParams.outSpatialReference = map.spatialReference

        val listenableFuture: ListenableFuture<FeatureQueryResult>? =
                if (table is ServiceFeatureTable) {
                    table.queryFeaturesAsync(queryParams, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
                } else {
                    table?.queryFeaturesAsync(queryParams)
                }

        if (listenableFuture != null) {
            listenableFuture.addDoneListener {
                try {
                    if (listenableFuture.isDone) {
                        val featureQueryResults = listenableFuture.get()
                        callback.invoke(featureQueryResults)
                    } else {
                        Log.w("summer", "${layer?.name} query failed: Query result not done.")
                        callback.invoke(null)
                    }
                } catch (e: Exception) {
                    Log.w("summer", "${layer?.name} query failed: ${e.message}")
                    callback.invoke(null)
                }
            }
        } else {
            callback.invoke(null)
        }
    }

    private fun finishDataLoad(loadStatus: LoadStatus = LoadStatus.LOADED) {
        summer()
        mLoadStatus = loadStatus
        mDoneLoadingListener?.run()
    }

    private fun summer() {
        val sitesJson = JSONArray()
        if (sites.isEmpty()) {
            facilities.forEach { facility ->
                sitesJson.put(facility.toJson())
            }
        } else {
            sites.forEach { site ->
                sitesJson.put(site.toJson())
            }
        }
        Log.d("summer", "$sitesJson")

        if (sites.isEmpty()) {
            facilities.forEach { facility ->
                Log.d("summer", "\t\t${facility.name}")
                facility.levels.forEach { level ->
                    Log.d("summer", "\t\t\t${level.shortName} : ${level.verticalOrder}")
                }
            }
        } else {
            sites.forEach { site ->
                Log.d("summer", "\t${site.name}")
                site.facilities.forEach { facility ->
                    Log.d("summer", "\t\t${facility.name}")
                    facility.levels.forEach { level ->
                        Log.d("summer", "\t\t\t${level.shortName} : ${level.verticalOrder}")
                    }
                }
            }
        }
    }
}
