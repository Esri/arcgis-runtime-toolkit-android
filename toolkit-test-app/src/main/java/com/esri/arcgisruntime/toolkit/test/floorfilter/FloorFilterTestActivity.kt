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
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.toolkit.test.R
import com.esri.arcgisruntime.toolkit.test.databinding.ActivityFloorfilterBinding
import kotlinx.android.synthetic.main.activity_floorfilter.*


class FloorFilterTestActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    val map: ArcGISMap by lazy {
        val portal = Portal("https://arcgisruntime.maps.arcgis.com/")
        val portalItem = PortalItem(portal, "16f1b8ba37b44dc3884afc8d5f454dd2")
        val map = ArcGISMap(portalItem)
        map
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityFloorfilterBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_floorfilter)

        binding.lifecycleOwner = this

        mapView = binding.mapView
        mapView.map = map

        mapView.map.addDoneLoadingListener {
            if (mapView.map.loadStatus == LoadStatus.LOADED) {
                floorFilterView.bindTo(mapView)

//                val floorFilterView = FloorFilterView(this)
//                floorFilterView.addToMapView(mapView)
            } else {
                val e = mapView.map?.loadError
                Toast.makeText(this, "The map didn't load.\n${e?.cause?.message}", Toast.LENGTH_LONG).show()
                Log.e("summer", "The map didn't load: ${e?.message}: ${e?.additionalMessage}: ${e?.cause?.message}")
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

}
