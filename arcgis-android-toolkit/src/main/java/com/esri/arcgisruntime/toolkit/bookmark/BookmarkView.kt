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

package com.esri.arcgisruntime.toolkit.bookmark

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.esri.arcgisruntime.mapping.Bookmark
import com.esri.arcgisruntime.toolkit.BR
import com.esri.arcgisruntime.toolkit.R
import kotlinx.android.synthetic.main.layout_bookmarkview.view.*

class BookmarkView : FrameLayout {

    private lateinit var rootview : View

    var recyclerView = _bookmarkRecyclerView

    var bookmarksAdapter: BookmarkAdapter? = null

    var onItemClickListener: OnItemClickListener<Bookmark>? = null

    interface OnItemClickListener<T> {
        fun onItemClick(item: T)
    }


    /**
     * Constructor used when instantiating this View directly to attach it to another view programmatically.
     *
     * @since 100.7.0
     */
    constructor(context: Context) : super(context) {
        init(context)
    }

    /**
     * Constructor used when defining this view in an XML layout.
     *
     * @since 100.7.0
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        rootview = inflate(context, R.layout.layout_bookmarkview, this)
        recyclerView = rootview.findViewById(R.id._bookmarkRecyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        if (bookmarksAdapter == null) {
            bookmarksAdapter = BookmarkAdapter(
                R.layout.item_bookmark, BR.bookmarkItem, BR.onItemClickListener,
                object : BookmarkAdapter.OnItemClickListener<Bookmark> {
                    override fun onItemClick(item: Bookmark) {
//                        bookmarksViewModel.onBookmarkClicked(item)
                        onItemClickListener?.onItemClick(item)
                    }
                })
        }
        recyclerView?.adapter = bookmarksAdapter

    }

}