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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.esri.arcgisruntime.mapping.Bookmark
import com.esri.arcgisruntime.toolkit.BR
import com.esri.arcgisruntime.toolkit.R

class BookmarkAdapter(
    private val onItemClickListener: OnItemClickListener<Bookmark>,
    diffCallback: DiffUtil.ItemCallback<Bookmark> = DiffCallback()
) : ListAdapter<Bookmark, ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, R.layout.item_bookmark_row, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), onItemClickListener)

    class DiffCallback : DiffUtil.ItemCallback<Bookmark>() {

        override fun areItemsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
            return oldItem.name == newItem.name && oldItem.viewpoint.toJson() == newItem.viewpoint.toJson()
        }
    }

    interface OnItemClickListener<Bookmark> {
        fun onItemClick(item: Bookmark)
    }
}

class ViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        bookmark: Bookmark,
        onItemClickListener: BookmarkAdapter.OnItemClickListener<Bookmark>
    ) {
        binding.setVariable(BR.bookmarkItem, bookmark)
        itemView.setOnClickListener { onItemClickListener.onItemClick(bookmark) }
        binding.executePendingBindings()
    }
}

