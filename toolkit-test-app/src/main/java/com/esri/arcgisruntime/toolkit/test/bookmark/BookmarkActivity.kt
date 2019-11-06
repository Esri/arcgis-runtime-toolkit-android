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

package com.esri.arcgisruntime.toolkit.test.bookmark

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Bookmark
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.security.UserCredential
import com.esri.arcgisruntime.toolkit.bookmark.BookmarkView
import com.esri.arcgisruntime.toolkit.test.R
import com.esri.arcgisruntime.toolkit.test.bookmark.map.MapViewModel
import kotlinx.android.synthetic.main.activity_bookmark.*

class BookmarkActivity : AppCompatActivity(), BookmarkView.OnItemClickListener<Bookmark> {

    private val map: ArcGISMap by lazy {
        val portal = Portal("https://arcgisruntime.maps.arcgis.com/")
        val portalItem = PortalItem(portal, "e1aa3973d50a456f998406a7c4dfd804")
        portal.credential = UserCredential("ArcGISRuntimeSDK", "agsRT3dk")
        ArcGISMap(portalItem)
    }

    private val mapViewModel: MapViewModel by lazy {
        ViewModelProviders.of(this, MapViewModel.Factory(map))
            .get(MapViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)

        bookmarkView.onItemClickListener = this

        mapViewModel.mapData.observe(this, Observer {
            it.let {
                mapView.map = mapViewModel.mapData.value
            }
        })

        mapViewModel.bookmarks.observe(this, Observer {
            it?.let { bookmarkView.bookmarksAdapter?.submitList(it) }
        })
    }

    override fun onItemClick(item: Bookmark) {
        mapView.setViewpointAsync(item.viewpoint)
    }
}
