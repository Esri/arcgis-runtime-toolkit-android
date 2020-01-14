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
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BookmarkList
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.security.UserCredential

class MapViewModel : ViewModel() {

    val map: ArcGISMap by lazy {
        val portal = Portal("https://arcgisruntime.maps.arcgis.com/")
        val portalItem = PortalItem(portal, "e1aa3973d50a456f998406a7c4dfd804")
        portal.credential = UserCredential("ArcGISRuntimeSDK", "agsRT3dk")
        ArcGISMap(portalItem)
    }

    private val _bookmarks: MutableLiveData<BookmarkList> = MutableLiveData()
    val bookmarks: LiveData<BookmarkList>
        get() {
            loadBookmarks()
            return _bookmarks
        }

    private fun loadBookmarks() {
        if (map.loadStatus == LoadStatus.LOADED) {
            _bookmarks.postValue(map.bookmarks)
        } else {
            map.addDoneLoadingListener {
                _bookmarks.postValue(map.bookmarks)
            }
        }
    }

}