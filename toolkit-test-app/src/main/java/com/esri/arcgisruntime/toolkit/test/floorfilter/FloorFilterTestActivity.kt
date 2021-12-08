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

package com.esri.arcgisruntime.toolkit.test.floorfilter

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.toolkit.floorfilter.FloorFilterView
import com.esri.arcgisruntime.toolkit.test.R
import com.esri.arcgisruntime.toolkit.test.databinding.ActivityFloorfilterBinding

class FloorFilterTestActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var floorFilterView: FloorFilterView? = null

    val map: ArcGISMap by lazy {
        val portal = Portal("https://arcgis.com/")
        val portalItem = PortalItem(portal, "f133a698536f44c8884ad81f80b6cfc7")
        val map = ArcGISMap(portalItem)
        map
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityFloorfilterBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_floorfilter)

        binding.lifecycleOwner = this
        floorFilterView = binding.floorFilterView

        floorFilterView?.clipToOutline = true

        mapView = binding.mapView
        mapView.map = map

        mapView.map.addDoneLoadingListener {
            if (mapView.map.loadStatus == LoadStatus.LOADED) {
                setupFloorFilterView()
            } else {
                val e = mapView.map?.loadError
                Toast.makeText(this, "The map didn't load.\n${e?.cause?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onPause() {
        mapView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onDestroy() {
        mapView.dispose()
        super.onDestroy()
    }

    private fun setupFloorFilterView() {
        floorFilterView?.bindTo(mapView)

        // You can alternatively initialize floorFilterView programmatically rather than in xml and
        // add it to GeoView using addToGeoView().
        // floorFilterView = FloorFilterView(this)
        // floorFilterView?.addToGeoView(mapView, FloorFilterView.ListPosition.TOP_END)

        floorFilterView?.floorManager?.addDoneLoadingListener {
            floorFilterView?.selectedLevel = floorFilterView?.floorManager?.levels?.find {
                it?.levelId == "ESRI.RED.MAIN.L.L1"
            }
        }
    }

}
