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
import androidx.lifecycle.ViewModelProvider
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Bookmark
import com.esri.arcgisruntime.toolkit.test.bookmark.BookmarksRepository

class MapViewModel(map: ArcGISMap) : ViewModel() {

    private val _mapData: MutableLiveData<ArcGISMap> = MutableLiveData(map)
    val mapData: LiveData<ArcGISMap> = _mapData

    private val bookmarksRepository: BookmarksRepository = BookmarksRepository(map)
    val bookmarks: LiveData<List<Bookmark>> = bookmarksRepository.bookmarks

    class Factory(private val arcGISMap: ArcGISMap) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(ArcGISMap::class.java)
                .newInstance(arcGISMap)
        }

    }

}