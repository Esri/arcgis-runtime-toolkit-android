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

import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.mapping.GeoModel
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.floor.FloorFacility
import com.esri.arcgisruntime.mapping.floor.FloorLevel
import com.esri.arcgisruntime.mapping.floor.FloorManager
import com.esri.arcgisruntime.mapping.floor.FloorSite
import com.esri.arcgisruntime.mapping.view.GeoView

internal class FloorFilterManager {

    /**
     * The [FloorManager] from the attached [GeoModel].
     *
     * @since 100.13.0
     */
    var floorManager: FloorManager? = null

    // Custom events

    /**
     * Invoked when the selected facility changes
     *
     * @since 100.13.0
     */
    private var onFacilityChangeListener: ((FloorFacility?) -> Unit)? = null
    fun setOnFacilityChangeListener(onFacilityChangeListener: ((FloorFacility?) -> Unit)?) {
        this.onFacilityChangeListener = onFacilityChangeListener
    }

    /**
     * Invoked when the selected level changes
     *
     * @since 100.13.0
     */
    private var onLevelChangeListener: ((FloorLevel?) -> Unit)? = null
    fun setOnLevelChangeListener(onLevelChangeListener: ((FloorLevel?) -> Unit)?) {
        this.onLevelChangeListener = onLevelChangeListener
    }

    /**
     * The selected [FloorSite]'s site ID.
     *
     * @since 100.13.0
     */
    private var mSelectedSiteId: String? = null
    var selectedSiteId: String?
        get() { return mSelectedSiteId }
        set(value) {
            mSelectedSiteId = value
            selectedFacilityId = null
            zoomToSite()
        }

    /**
     * The selected [FloorFacility]'s facility ID.
     *
     * @since 100.13.0
     */
    private var mSelectedFacilityId: String? = null
    var selectedFacilityId: String?
        get() { return mSelectedFacilityId }
        set(value) {
            mSelectedFacilityId = value
            if (mSelectedFacilityId != null) {
                mSelectedSiteId = getSelectedFacility()?.site?.siteId
            }
            selectedLevelId = getDefaultLevelIdForFacility(value)
            zoomToFacility()
            onFacilityChangeListener?.invoke(getSelectedFacility())
        }

    /**
     * The selected [FloorLevel]'s level ID.
     *
     * @since 100.13.0
     */
    private var mSelectedLevelId: String? = null
    var selectedLevelId: String?
        get() { return mSelectedLevelId }
        set(value) {
            if (mSelectedLevelId != value) {
                mSelectedLevelId = value
                if (mSelectedLevelId != null) {
                    val selectedLevelsFacility = getSelectedLevel()?.facility
                    mSelectedFacilityId = selectedLevelsFacility?.facilityId
                    mSelectedSiteId = selectedLevelsFacility?.site?.siteId
                }
                filterMap()
            }
            onLevelChangeListener?.invoke(getSelectedLevel())
        }

    /**
     * The [GeoView] attached to the [FloorFilterView].
     *
     * @since 100.13.0
     */
    var geoView: GeoView? = null
        private set

    /**
     * The list of [FloorSite]s from the [FloorManager].
     *
     * @since 100.13.0
     */
    val sites: List<FloorSite>
        get() { return floorManager?.sites ?: emptyList() }
    /**
     * The list of [FloorFacility]s from the [FloorManager].
     *
     * @since 100.13.0
     */
    val facilities: List<FloorFacility>
        get() { return floorManager?.facilities ?: emptyList() }
    /**
     * The list of [FloorLevel]s from the [FloorManager].
     *
     * @since 100.13.0
     */
    val levels: List<FloorLevel>
        get() { return floorManager?.levels ?: emptyList() }

    /**
     * Returns true if the [level] is selected.
     *
     * @since 100.13.0
     */
    fun isSelectedLevel(level: FloorLevel?): Boolean {
        return level != null && selectedLevelId == level.levelId
    }

    /**
     * Returns true if the [facility] is selected.
     *
     * @since 100.13.0
     */
    fun isSelectedFacility(facility: FloorFacility?): Boolean {
        return facility != null && selectedFacilityId == facility.facilityId
    }

    /**
     * Returns true if the [site] is selected.
     *
     * @since 100.13.0
     */
    fun isSelectedSite(site: FloorSite?): Boolean {
        return site != null && selectedSiteId == site.siteId
    }

    /**
     * Returns the selected [FloorLevel] or null if no [FloorLevel] is selected.
     *
     * @since 100.13.0
     */
    fun getSelectedLevel(): FloorLevel? {
        return levels.firstOrNull { isSelectedLevel(it) }
    }

    /**
     * Returns the selected [FloorFacility] or null if no [FloorFacility] is selected.
     *
     * @since 100.13.0
     */
    fun getSelectedFacility(): FloorFacility? {
        return facilities.firstOrNull { isSelectedFacility(it) }
    }

    /**
     * Returns the selected [FloorSite] or null if no [FloorSite] is selected.
     *
     * @since 100.13.0
     */
    fun getSelectedSite(): FloorSite? {
        return sites.firstOrNull { isSelectedSite(it) }
    }

    /**
     * Loads the [FloorManager] of the [GeoView] attached to the [FloorFilterView]
     *
     * @since 100.13.0
     */
    fun setupMap(geoView: GeoView, map: GeoModel, setupDone: (() -> Unit)? = null) {
        this.geoView = geoView

        val floorManager = map.floorManager

        if (floorManager == null) {
            setupDone?.invoke()
            return
        }

        this.floorManager = floorManager
        val doneLoadingListener: Runnable = object: Runnable {
            override fun run() {
                floorManager.removeDoneLoadingListener(this)

                // Do this to make sure the UI gets set correctly if the selected level id was set
                // before the floor manager loaded.
                val temp = mSelectedLevelId
                mSelectedLevelId = null
                selectedLevelId = temp
                filterMap()

                setupDone?.invoke()
            }
        }
        floorManager.addDoneLoadingListener(doneLoadingListener)
        floorManager.loadAsync()
    }

    /**
     * Removes the attached [GeoView].
     *
     * @since 100.13.0
     */
    fun clearGeoView() {
        clearMapFilter()
        this.geoView = null
        this.floorManager = null
    }

    /**
     * Zooms to the selected [FloorFacility]. If no [FloorFacility] is selected, it will zoom to
     * the selected [FloorSite].
     *
     * @since 100.13.0
     */
    internal fun zoomToSelection() {
        if (!selectedFacilityId.isNullOrBlank()) {
            zoomToFacility()
        } else if (!selectedSiteId.isNullOrBlank()) {
            zoomToSite()
        }
    }

    /**
     * Zooms to the selected [FloorSite].
     *
     * @since 100.13.0
     */
    private fun zoomToSite() {
        zoomToExtent(geoView, getSelectedSite()?.geometry?.extent)
    }

    /**
     * Zooms to the selected [FloorFacility].
     *
     * @since 100.13.0
     */
    private fun zoomToFacility() {
        zoomToExtent(geoView, getSelectedFacility()?.geometry?.extent)
    }

    /**
     * Zooms the [geoView] to the [envelope].
     *
     * @since 100.13.0
     */
    private fun zoomToExtent(geoView: GeoView?, envelope: Envelope?, bufferFactor:Double = 1.25) {
        if (geoView != null && envelope != null && !envelope.isEmpty) {
            try {
                val envelopeWithBuffer = Envelope(envelope.center, envelope.width * bufferFactor,envelope.height * bufferFactor)
                if (!envelopeWithBuffer.isEmpty) {
                    geoView.setViewpointAsync(Viewpoint(envelopeWithBuffer), 0.5f)
                }
            } catch (e: Exception) {
                // do nothing
            }
        }
    }

    /**
     * Filters the attached [GeoModel] to the selected [FloorLevel]. If no [FloorLevel] is
     * selected, clears the floor filter from the selected [GeoModel].
     *
     * @since 100.13.0
     */
    private fun filterMap() {
        // Set levels that match the selected level's vertical order to visible
        val selectedLevel = getSelectedLevel()
        if (selectedLevel == null) {
            clearMapFilter()
        } else {
            floorManager?.levels?.forEach {
                it?.isVisible = it?.verticalOrder == selectedLevel.verticalOrder
            }
        }
    }

    /**
     * Clears the floor filter from the attached [GeoModel].
     *
     * @since 100.13.0
     */
    private fun clearMapFilter() {
        floorManager?.levels?.forEach {
            it?.isVisible = true
        }
    }

    /**
     * Returns the level ID of the [FloorLevel] with [FloorLevel.getVerticalOrder] of 0. If no
     * [FloorLevel] has [FloorLevel.getVerticalOrder] of 0, it will return the level ID of the
     * [FloorLevel] with the lowest [FloorLevel.getVerticalOrder].
     *
     * @since 100.13.0
     */
    private fun getDefaultLevelIdForFacility(facilityId: String?): String? {
        val candidateLevels = levels.filter { it.facility?.facilityId == facilityId }
        return (candidateLevels.firstOrNull{ it.verticalOrder == 0 } ?: getLowestLevel(candidateLevels))?.levelId
    }

    /**
     * Returns the [FloorLevel] with the lowest[FloorLevel.getVerticalOrder].
     *
     * @since 100.13.0
     */
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
