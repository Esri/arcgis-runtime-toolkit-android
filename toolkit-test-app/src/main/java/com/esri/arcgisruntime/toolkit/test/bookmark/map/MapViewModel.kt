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

package com.esri.arcgisruntime.toolkit.test.bookmark.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BookmarkList
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem

class MapViewModel : ViewModel() {

    val map: ArcGISMap by lazy {
        val portal = Portal("https://arcgisruntime.maps.arcgis.com/")
        val portalItem = PortalItem(portal, "16f1b8ba37b44dc3884afc8d5f454dd2")
        val map = ArcGISMap(portalItem)
        map.addDoneLoadingListener{
            _bookmarks.postValue(map.bookmarks)
        }
        map
    }

    private val _bookmarks: MutableLiveData<BookmarkList> = MutableLiveData()
    val bookmarks: LiveData<BookmarkList>
        get() {
            return _bookmarks
        }
}
