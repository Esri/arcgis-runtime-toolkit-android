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

package com.esri.arcgisruntime.toolkit.floorfilter

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Typeface
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esri.arcgisruntime.toolkit.R
import com.esri.arcgisruntime.toolkit.extension.logTag
import kotlinx.android.synthetic.main.item_site_facility_row.view.*
import kotlinx.android.synthetic.main.layout_sitefacilityview.view.*

internal class SiteFacilityView: LinearLayout {

    var floorFilterManager: FloorFilterManager? = null

    // Custom events
    private var onDismissListener: (() -> Unit)? = null
    fun setOnDismissListener(onDismissListener: (() -> Unit)?) {
        this.onDismissListener = onDismissListener
    }

    private val sites: List<FloorSite>
        get() {
            return floorFilterManager?.sites ?: emptyList()
        }
    private val facilities: List<FloorFacility>
        get() {
            val selectedSiteId = floorFilterManager?.selectedSiteId
            return if (selectedSiteId.isNullOrEmpty()) {
                floorFilterManager?.facilities
            } else {
                floorFilterManager?.facilities?.filter { it.site?.id == selectedSiteId }
            } ?: emptyList()
        }

    private var isShowingFacilities: Boolean = false
    private var dialog: Dialog? = null
    private val siteFacilityAdapter by lazy { SiteFacilityAdapter() }

    /**
     * Constructor used when instantiating this View directly to attach it to another view programmatically.
     *
     * @since 100.12.0
     */
    constructor(context: Context) : super(context) {
        init(context)
    }

    /**
     * Constructor used when defining this view in an XML layout.
     *
     * @since 100.12.0
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    fun clearSearch() {
        siteFacilitySearchEditText?.setText("")
    }

    fun show() {
        val dialog = showSiteFacilityDialog()
        if (dialog != null) {
            clearSearch()
            updateSiteFacilityTitle()

            dialog.setOnDismissListener {
                closeSiteFacilityView()
            }
            this.dialog = dialog
        }
    }

    fun close() {
        val dialog = this.dialog ?: return
        dialog.dismiss()
        (parent as? ViewGroup)?.removeView(this)
        this.dialog = null
    }

    private fun getSelectedSite(): FloorSite? {
        return floorFilterManager?.getSelectedSite()
    }

    private fun isSelectedSite(site: FloorSite): Boolean {
        return floorFilterManager?.isSelectedSite(site) ?: false
    }

    private fun getSelectedFacility(): FloorFacility? {
        return floorFilterManager?.getSelectedFacility()
    }

    private fun isSelectedFacility(facility: FloorFacility): Boolean {
        return floorFilterManager?.isSelectedFacility(facility) ?: false
    }

    private fun init(context: Context) {
        orientation = VERTICAL
        inflate(context, R.layout.layout_sitefacilityview, this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val drawable = siteFacilityRecyclerView?.verticalScrollbarThumbDrawable
            drawable?.setTint(ContextCompat.getColor(context, R.color.floor_filter_text))
        }

        siteFacilityRecyclerView.layoutManager = LinearLayoutManager(context)
        siteFacilityRecyclerView.adapter = siteFacilityAdapter

        siteFacilityCloseButton.setOnClickListener {
            closeSiteFacilityView()
        }

        siteFacilityBackButton?.setOnClickListener {
            isShowingFacilities = false
            clearSearch()
            updateSiteFacilityTitle()
        }

        siteFacilitySearchEditText?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s?.toString()
                siteFacilityAdapter.filterData(searchText)
                siteFacilitySearchClearButton?.visibility = if (searchText.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun afterTextChanged(s: Editable?) { }
        })

        siteFacilitySearchClearButton?.setOnClickListener {
            clearSearch()
        }

        siteFacilityAdapter.onItemClick = { clickedItemWrapper ->
            val clickedItem = clickedItemWrapper.getItem()
            if (clickedItem is FloorSite) {
                if (!isSelectedSite(clickedItem)) {
                    floorFilterManager?.selectedFacilityId = null
                }
                floorFilterManager?.selectedSiteId = clickedItem.id
                isShowingFacilities = true
                clearSearch()
            } else if (clickedItem is FloorFacility) {
                if (!isSelectedFacility(clickedItem)) {
                    floorFilterManager?.selectedFacilityId = clickedItem.id
                }
                closeSiteFacilityView()
            }

            updateSiteFacilityTitle()
        }

        siteFacilityAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                siteFacilityEmptyView?.visibility = if (siteFacilityAdapter.itemCount > 0) View.GONE else View.VISIBLE
            }
        })
    }

    private fun updateSiteFacilityTitle() {
        if (sites.isEmpty()) {
            isShowingFacilities = true
        }

        val selectedSite = getSelectedSite()
        if (isShowingFacilities) {
            val selectedFacility = getSelectedFacility()
            siteFacilityTitle?.text = selectedFacility?.name ?: context?.getString(R.string.floor_filter_select_facility) ?: ""
            if (selectedSite == null) {
                siteFacilitySubtitle?.visibility = View.GONE
                siteFacilityBackButton?.visibility = View.GONE
            } else {
                siteFacilitySubtitle?.text = selectedSite.name
                siteFacilitySubtitle?.visibility = View.VISIBLE
                siteFacilityBackButton?.visibility = View.VISIBLE
            }
            siteFacilityAdapter.updateData(facilities.map { SiteFacilityWrapper(null, it, isSelectedFacility(it)) })
        } else {
            siteFacilityTitle?.text = selectedSite?.name ?: context?.getString(R.string.floor_filter_select_site) ?: ""
            siteFacilitySubtitle?.visibility = View.GONE
            siteFacilityBackButton?.visibility = View.GONE
            siteFacilityAdapter.updateData(sites.map { SiteFacilityWrapper(it, null, isSelectedSite(it)) })
        }
    }

    private fun closeSiteFacilityView() {
        close()
        onDismissListener?.invoke()
    }

    private fun showSiteFacilityDialog(): Dialog? {
        val activity = getActivity(context) ?: return null

        fun showDialog(activity: Activity, dialog: Dialog): Boolean {
            var dialogShown = false
            try {
                activity.runOnUiThread {
                    if (!activity.isFinishing && !activity.isDestroyed) {
                        dialog.show()
                        dialogShown = true
                    }
                }
            } catch (e: Exception) {
                Log.e(logTag, "Unable to show dialog: ${e.message}")
            }
            return dialogShown
        }

        val dialogBuilder = AlertDialog.Builder(activity, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setView(this)
        val dialog = dialogBuilder.create()
        val dialogShown = showDialog(activity, dialog)

        if (dialog == null || !dialogShown) return null
        return dialog
    }

    private fun getActivity(context: Context?): Activity? {
        if (context == null) {
            return null
        } else if (context is ContextWrapper) {
            return if (context is Activity) {
                context
            } else {
                getActivity(context.baseContext)
            }
        }
        return null
    }

    /**
     * Implements the adapter to be set on the [RecyclerView].
     *
     * @since 100.12.0
     */
    private inner class SiteFacilityAdapter : RecyclerView.Adapter<SiteFacilityViewHolder>() {

        var onItemClick: ((SiteFacilityWrapper) -> Unit)? = null

        private var searchString: String? = null
        private var allSitesFacilities: List<SiteFacilityWrapper> = listOf()
        private var filteredSitesFacilities: List<SiteFacilityWrapper> = listOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteFacilityViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<ViewDataBinding>(
                    inflater,
                    R.layout.item_site_facility_row,
                    parent,
                    false
            )
            return SiteFacilityViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SiteFacilityViewHolder, position: Int) =
                holder.bind(filteredSitesFacilities.getOrNull(position)) { clickedItem ->
                    onItemClick?.invoke(clickedItem)
                }

        override fun getItemCount(): Int {
            return filteredSitesFacilities.size
        }

        fun updateData(newSitesFacilities: List<SiteFacilityWrapper>) {
            allSitesFacilities = newSitesFacilities.sortedBy { it.name }
            filterData(searchString)
        }

        fun filterData(filterBy: String?) {
            searchString = filterBy
            filteredSitesFacilities = if (filterBy.isNullOrEmpty()) {
                allSitesFacilities
            } else {
                allSitesFacilities.filter {
                    it.name.contains(filterBy, true)
                }
            }

            notifyDataSetChanged()
        }
    }

    /**
     * The SiteFacilityAdapter ViewHolder.
     *
     * @since 100.12.0
     */
    private class SiteFacilityViewHolder(binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(itemWrapper: SiteFacilityWrapper?, onItemClickListener: (SiteFacilityWrapper) -> Unit) {
            val textView = itemView.siteFacilityTextView
            val selectedIndicator = itemView.siteFacilitySelectedIndicator
            val chevronButton = itemView.siteFacilityChevronButton

            textView?.text = itemWrapper?.name ?: ""
            chevronButton?.visibility = if (itemWrapper?.getItem() is FloorSite) View.VISIBLE else View.INVISIBLE

            if (itemWrapper?.isSelected == true) {
                selectedIndicator?.visibility = View.VISIBLE
                textView?.setTypeface(textView.typeface, Typeface.BOLD)
            } else {
                selectedIndicator?.visibility = View.INVISIBLE
                textView?.setTypeface(Typeface.create(textView.typeface, Typeface.NORMAL), Typeface.NORMAL)
            }

            if (itemWrapper == null) {
                itemView.setOnClickListener(null)
            } else {
                itemView.setOnClickListener { onItemClickListener.invoke(itemWrapper) }
            }
        }
    }

    private data class SiteFacilityWrapper(
            val site: FloorSite?,
            val facility: FloorFacility?,
            var isSelected: Boolean = false) {
        val name: String
            get() {
                return when {
                    site != null -> site.name
                    facility != null -> facility.name
                    else -> ""
                }
            }

        fun getItem(): Any? {
            return when {
                site != null -> site
                facility != null -> facility
                else -> null
            }
        }
    }
}
