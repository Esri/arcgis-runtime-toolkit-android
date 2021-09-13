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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.esri.arcgisruntime.mapping.Bookmark
import com.esri.arcgisruntime.toolkit.bookmark.BookmarkView
import com.esri.arcgisruntime.toolkit.test.R
import com.esri.arcgisruntime.toolkit.test.bookmark.map.MapViewModel
import com.esri.arcgisruntime.toolkit.test.databinding.ActivityBookmarkBinding
import kotlinx.android.synthetic.main.activity_bookmark.*

class BookmarkActivity : AppCompatActivity(), BookmarkView.OnItemClickListener<Bookmark> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityBookmarkBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_bookmark)
        val mapViewModel: MapViewModel by viewModels()

        binding.mapViewModel = mapViewModel
        binding.lifecycleOwner = this

        bookmarkView.onItemClickListener = this
    }

    override fun onItemClick(item: Bookmark) {
        mapView.setViewpointAsync(item.viewpoint)
    }
}
