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
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esri.arcgisruntime.mapping.floor.FloorFacility
import com.esri.arcgisruntime.mapping.floor.FloorSite
import com.esri.arcgisruntime.toolkit.R
import com.esri.arcgisruntime.toolkit.extension.logTag

/**
 * A popup dialog that allows the user to choose a [FloorSite] and [FloorFacility] to display.
 *
 * @since 100.13.0
 */
internal class SiteFacilityView: LinearLayout {

    // This will be set to the FloorFilterView's floorFilterManager once created.
    private var floorFilterManager: FloorFilterManager? = null

    // This will be updated to the FloorFilterView's uiParameters once created.
    private var uiParameters: UiParameters = UiParameters()

    /**
     * The list of [FloorSite]s from the [FloorFilterManager].
     *
     * @since 100.13.0
     */
    private val sites: List<FloorSite>
        get() {
            return floorFilterManager?.sites ?: emptyList()
        }

    /**
     * The list of [FloorFacility]s from the [FloorFilterManager]. If a [FloorSite] has been
     * selected, this list will be filtered by the selected site ID.
     *
     * @since 100.13.0
     */
    private val facilities: List<FloorFacility>
        get() {
            val selectedSiteId = floorFilterManager?.selectedSiteId
            return if (selectedSiteId.isNullOrEmpty()) {
                floorFilterManager?.facilities
            } else {
                floorFilterManager?.facilities?.filter { it.site?.siteId == selectedSiteId }
            } ?: emptyList()
        }

    private var isShowingFacilities: Boolean = false
    private var dialog: Dialog? = null
    private val siteFacilityAdapter by lazy { SiteFacilityAdapter() }

    private var siteFacilityBackButton: ImageView? = null
    private var backToSitesButtonSeparator: View? = null
    private var siteFacilityTitle: TextView? = null
    private var siteFacilitySubtitle: TextView? = null
    private var siteFacilityCloseButton: ImageView? = null
    private var siteFacilityTitleSeparator: View? = null
    private var siteFacilitySearchLayout: LinearLayout? = null
    private var siteFacilitySearchButton: ImageView? = null
    private var siteFacilitySearchEditText: EditText? = null
    private var siteFacilitySearchClearButton: ImageView? = null
    private var siteFacilityRecyclerView: RecyclerView? = null
    private var siteFacilityEmptyView: TextView? = null

    /**
     * Called when the sites and facilities dialog closes.
     *
     * @since 100.13.0
     */
    var onDismissListener: (() -> Unit)? = null

    /**
     * Constructor used when instantiating this View directly to attach it to another view programmatically.
     *
     * @since 100.13.0
     */
    constructor(context: Context) : super(context) {
        init(context)
    }

    /**
     * Constructor used when defining this view in an XML layout.
     *
     * @since 100.13.0
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    /**
     * Attaches the [FloorFilterManager] and [UiParameters] from the [FloorFilterView].
     *
     * @since 100.13.0
     */
    internal fun setup(floorFilterManager: FloorFilterManager, uiParameters: UiParameters) {
        this.floorFilterManager = floorFilterManager
        this.uiParameters = uiParameters
    }

    /**
     * Shows the popup dialog to choose a [FloorSite] and [FloorFacility].
     *
     * @since 100.13.0
     */
    internal fun show() {
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

    /**
     * Closes the site/facility popup dialog.
     *
     * @since 100.13.0
     */
    internal fun close() {
        val dialog = this.dialog ?: return
        dialog.dismiss()
        (parent as? ViewGroup)?.removeView(this)
        this.dialog = null
    }

    /**
     * Shows or hides the search bar based on [UiParameters.hideFacilitySearch]
     * and [UiParameters.hideSiteSearch].
     *
     * @since 100.13.0
     */
    internal fun showHideSearch() {
        val hideSearch = if (isShowingFacilities) {
            uiParameters.hideFacilitySearch
        } else {
            uiParameters.hideSiteSearch
        }
        siteFacilitySearchLayout?.visibility = if (hideSearch) View.GONE else View.VISIBLE
        siteFacilityTitleSeparator?.visibility = if (hideSearch) View.VISIBLE else View.GONE
    }

    /**
     * Handles changes to the configurable UI parameters.
     *
     * @since 100.13.0
     */
    internal fun processUiParamUpdate() {
        setBackgroundColor(uiParameters.buttonBackgroundColor)
        siteFacilitySearchLayout?.setBackgroundColor(uiParameters.searchBackgroundColor)
        siteFacilitySearchEditText?.setBackgroundColor(uiParameters.searchBackgroundColor)

        uiParameters.setButtonTintColors(siteFacilityBackButton)
        backToSitesButtonSeparator?.setBackgroundColor(uiParameters.closeButtonBackgroundColor)
        siteFacilityTitleSeparator?.setBackgroundColor(uiParameters.closeButtonBackgroundColor)
        uiParameters.setButtonTintColors(siteFacilityCloseButton)
        uiParameters.setButtonTintColors(siteFacilitySearchClearButton)
        uiParameters.setButtonTintColors(siteFacilitySearchButton)

        siteFacilityTitle?.setTextColor(uiParameters.textColor)
        siteFacilitySubtitle?.setTextColor(uiParameters.textColor)
        siteFacilitySearchEditText?.setHintTextColor(uiParameters.textColor)
        siteFacilitySearchEditText?.setTextColor(uiParameters.textColor)
        siteFacilityEmptyView?.setTextColor(uiParameters.textColor)

        uiParameters.setScrollbarColor(siteFacilityRecyclerView)

        siteFacilityAdapter.notifyDataSetChanged()
    }

    /**
     * Clears the site and facility search text.
     *
     * @since 100.13.0
     */
    private fun clearSearch() {
        siteFacilitySearchEditText?.setText("")
    }

    /**
     * Returns the selected [FloorSite].
     *
     * @since 100.13.0
     */
    private fun getSelectedSite(): FloorSite? {
        return floorFilterManager?.getSelectedSite()
    }

    /**
     * Returns true if the [site] is selected.
     *
     * @since 100.13.0
     */
    private fun isSelectedSite(site: FloorSite): Boolean {
        return floorFilterManager?.isSiteSelected(site) ?: false
    }

    /**
     * Returns the selected [FloorFacility].
     *
     * @since 100.13.0
     */
    private fun getSelectedFacility(): FloorFacility? {
        return floorFilterManager?.getSelectedFacility()
    }

    /**
     * Returns true if the [facility] is selected.
     *
     * @since 100.13.0
     */
    private fun isSelectedFacility(facility: FloorFacility): Boolean {
        return floorFilterManager?.isFacilitySelected(facility) ?: false
    }

    /**
     * Initializes this SiteFacilityView by inflating the layout and setting the UI listeners.
     *
     * @since 100.13.0
     */
    private fun init(context: Context) {
        orientation = VERTICAL
        inflate(context, R.layout.layout_sitefacilityview, this)

        siteFacilityBackButton = findViewById(R.id.siteFacilityBackButton)
        backToSitesButtonSeparator = findViewById(R.id.backToSitesButtonSeparator)
        siteFacilityTitle = findViewById(R.id.siteFacilityTitle)
        siteFacilitySubtitle = findViewById(R.id.siteFacilitySubtitle)
        siteFacilityCloseButton = findViewById(R.id.siteFacilityCloseButton)
        siteFacilityTitleSeparator = findViewById(R.id.siteFacilityTitleSeparator)
        siteFacilitySearchLayout = findViewById(R.id.siteFacilitySearchLayout)
        siteFacilitySearchButton = findViewById(R.id.siteFacilitySearchButton)
        siteFacilitySearchEditText = findViewById(R.id.siteFacilitySearchEditText)
        siteFacilitySearchClearButton = findViewById(R.id.siteFacilitySearchClearButton)
        siteFacilityRecyclerView = findViewById(R.id.siteFacilityRecyclerView)
        siteFacilityEmptyView = findViewById(R.id.siteFacilityEmptyView)

        siteFacilityRecyclerView?.layoutManager = LinearLayoutManager(context)
        siteFacilityRecyclerView?.adapter = siteFacilityAdapter

        siteFacilityCloseButton?.setOnClickListener {
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
                floorFilterManager?.selectedSiteId = clickedItem.siteId
                isShowingFacilities = true
                clearSearch()
            } else if (clickedItem is FloorFacility) {
                if (!isSelectedFacility(clickedItem)) {
                    floorFilterManager?.selectedFacilityId = clickedItem.facilityId
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

    /**
     * Updates the title to the selected [FloorSite] or [FloorFacility] or default text if nothing
     * is selected.
     *
     * @since 100.13.0
     */
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
                showSiteFacilityBackButton(false)
            } else {
                siteFacilitySubtitle?.text = selectedSite.name
                siteFacilitySubtitle?.visibility = View.VISIBLE
                showSiteFacilityBackButton(true)
            }
            siteFacilityAdapter.updateData(facilities.map { SiteFacilityWrapper(null, it, isSelectedFacility(it)) })
        } else {
            siteFacilityTitle?.text = selectedSite?.name ?: context?.getString(R.string.floor_filter_select_site) ?: ""
            siteFacilitySubtitle?.visibility = View.GONE
            showSiteFacilityBackButton(false)
            siteFacilityAdapter.updateData(sites.map { SiteFacilityWrapper(it, null, isSelectedSite(it)) })
        }

        showHideSearch()
    }

    /**
     * Sets the visibility of the back chevron button.
     *
     * @since 100.13.0
     */
    private fun showSiteFacilityBackButton(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        siteFacilityBackButton?.visibility = visibility
        backToSitesButtonSeparator?.visibility = visibility
    }

    /**
     * Closes the site/facility popup dialog.
     *
     * @since 100.13.0
     */
    private fun closeSiteFacilityView() {
        close()
        onDismissListener?.invoke()
    }

    /**
     * Shows the site/facility popup dialog.
     *
     * @since 100.13.0
     */
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

    /**
     * Returns the [Activity] associated with the [Context].
     *
     * @since 100.13.0
     */
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
     * @since 100.13.0
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
     * @since 100.13.0
     */
    private inner class SiteFacilityViewHolder(binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(itemWrapper: SiteFacilityWrapper?, onItemClickListener: (SiteFacilityWrapper) -> Unit) {
            val textView = itemView.findViewById<TextView>(R.id.siteFacilityTextView)
            val selectedIndicator = itemView.findViewById<TextView>(R.id.siteFacilitySelectedIndicator)
            val chevronButton = itemView.findViewById<ImageView>(R.id.siteFacilityChevronButton)

            // Set the text color
            textView.setTextColor(uiParameters.textColor)
            selectedIndicator.setTextColor(uiParameters.selectedTextColor)
            uiParameters.setButtonTintColors(chevronButton)

            // Set the site or facility name
            textView?.text = itemWrapper?.name ?: ""

            // Only show a chevron button for sites.
            chevronButton?.visibility = if (itemWrapper?.getItem() is FloorSite) View.VISIBLE else View.INVISIBLE

            // Add a circle indicator if the site or facility is selected
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

    /**
     * A wrapper to give [FloorSite] and [FloorFacility] a common API so that only one adapter is
     * needed for sites and facilities.
     *
     * @since 100.13.0
     */
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
