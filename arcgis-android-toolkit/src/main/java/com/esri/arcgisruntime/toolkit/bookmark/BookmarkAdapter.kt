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
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.esri.arcgisruntime.mapping.Bookmark
import com.esri.arcgisruntime.toolkit.bookmark.databinding.DataBindingViewHolder

class BookmarkAdapter(
    @LayoutRes private val itemLayoutRes: Int,
    private val itemBindingId: Int,
    private val onItemClickListenerBindingId: Int,
    private val onItemClickListener: OnItemClickListener<Bookmark>,
    diffCallback: DiffUtil.ItemCallback<Bookmark> = DiffCallback()
) : ListAdapter<Bookmark, ViewHolder<Bookmark>>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<Bookmark> {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, viewType, parent, false)
        return ViewHolder(binding, itemBindingId, onItemClickListenerBindingId, onItemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder<Bookmark>, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<Bookmark>() {

        override fun areItemsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
            return oldItem.name == newItem.name && oldItem.viewpoint.toJson() == newItem.viewpoint.toJson()
        }
    }

    override fun getItemViewType(position: Int): Int = itemLayoutRes

    interface OnItemClickListener<T> {
        fun onItemClick(item: T)
    }
}

class ViewHolder<Bookmark>(
    private val binding: ViewDataBinding,
    private val itemId: Int,
    private val onItemClickListenerBindingId: Int,
    private val onItemClickListener: BookmarkAdapter.OnItemClickListener<Bookmark>
) :
    DataBindingViewHolder<Bookmark>(binding, itemId) {

    override fun bind(item: Bookmark) {
        binding.setVariable(itemId, item)
        binding.setVariable(onItemClickListenerBindingId, onItemClickListener)
        binding.executePendingBindings()
    }

}