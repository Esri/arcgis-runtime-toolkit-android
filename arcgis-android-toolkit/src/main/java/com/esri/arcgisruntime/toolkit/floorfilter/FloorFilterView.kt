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

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.toolkit.R
import com.esri.arcgisruntime.toolkit.extension.dpToPixels
import kotlinx.android.synthetic.main.item_level_row.view.*
import kotlinx.android.synthetic.main.layout_floorfilterview.view.*

class FloorFilterView: LinearLayout {

    var selectedSiteId: String?
        get() { return floorFilterManager.selectedSiteId }
        set(value) { floorFilterManager.selectedSiteId = value }
    var selectedFacilityId: String?
        get() { return floorFilterManager.selectedFacilityId }
        set(value) { floorFilterManager.selectedFacilityId = value }
    var selectedLevelId: String?
        get() { return floorFilterManager.selectedLevelId }
        set(value) { floorFilterManager.selectedLevelId = value }

    // Custom events
    // TODO: implement this and allow it to set more than one listener
    private var onSelectionChangeListener: (() -> Unit)? = null
    fun setOnSelectionChangeListener(onSelectionChangeListener: (() -> Unit)?) {
        this.onSelectionChangeListener = onSelectionChangeListener
    }

    // UI related parameters
    private var levelsMax: Int = -1
    var hideCloseButton: Boolean = false
        set(value) {
            field = value
            levelAdapter.hideCloseButton = field
            levelAdapter.updateCloseButtonVisibility()
        }
    var hideSiteFacilityButton: Boolean = false
        set(value) {
            field = value
            siteFacilityButtonSeparator?.visibility = if (hideSiteFacilityButton) View.GONE else View.VISIBLE
            siteFacilityButton?.visibility = if (hideSiteFacilityButton) View.GONE else View.VISIBLE
        }
    private var drawInMapView: Boolean = false

    private val floorFilterManager: FloorFilterManager = FloorFilterManager()
    private val levelAdapter by lazy { LevelAdapter(floorFilterManager) }

    private val displayDensity: Float by lazy {
        resources.displayMetrics.density
    }

    private val siteFacilityView by lazy {
        val view = SiteFacilityView(context)
        setupSiteFacilityView(view)
        view
    }

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
        context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.FloorFilterView,
                0, 0
        ).apply {
            try {
                // TODO: Need to support changing font size, font type, siteFacility icon, button size,
                // TODO: hide close button, hide siteFacility button, top/bottom location of close and siteFacility buttons
                levelsMax = getInt(R.styleable.FloorFilterView_levelsMax, -1)
                hideCloseButton = getBoolean(R.styleable.FloorFilterView_hideCloseButton, false)
                hideSiteFacilityButton = getBoolean(R.styleable.FloorFilterView_hideSiteFacilityButton, false)
            } finally {
                recycle()
            }
        }
        init(context)
    }

    /**
     * Initializes this FloorFilterView by inflating the layout and setting the [RecyclerView] adapter.
     *
     * @since 100.12.0
     */
    private fun init(context: Context) {
        orientation = VERTICAL
        inflate(context, R.layout.layout_floorfilterview, this)

        addDataChangeListeners()
        setupLevelsAdapter()
        setupSiteFacilityButton()

        // Set the color of the scroll bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val drawable = floorsRecyclerView?.verticalScrollbarThumbDrawable
            drawable?.setTint(ContextCompat.getColor(context, R.color.floor_filter_text))
        }
    }

    fun getLevelsMax(): Int? {
        return if (levelsMax < 0) {
            null
        } else {
            levelsMax
        }
    }

    fun setLevelsMax(levelsMax: Int?) {
        this.levelsMax = levelsMax ?: -1
    }

    /**
     * Adds this [FloorFilterView] to the provided [mapView].
     *
     * @throws IllegalStateException    if this FloorFilterView is already added to or bound to a MapView
     * @since 100.12.0
     */
    fun addToMapView(mapView: MapView) {
        this.floorFilterManager.mapView?.let {
            throw IllegalStateException("FloorFilterView already has a MapView")
        }
        drawInMapView = true
        levelsMax = 3
        setupMapView(mapView)
        // TODO: figure this out so it is pretty
        // TODO: add options to set it on the top/start/bottom/end
        ViewCompat.setElevation(this, 4.dpToPixels(displayDensity).toFloat())
        mapView.addView(
                this, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    /**
     * Removes this FloorFilterView from the MapView it was added to (if any).
     *
     * @throws IllegalStateException if this FloorFilterView is not currently added to a MapView
     * @since 100.12.0
     */
    fun removeFromMapView() {
        if (!drawInMapView) {
            throw IllegalStateException("FloorFilterView is not currently added to a MapView")
        }
        // TODO: make sure this works
        floorFilterManager.mapView?.removeView(this)
        drawInMapView = false
        floorFilterManager.clearMapView()
    }

    /**
     * Binds this [FloorFilterView] to the provided [mapView], or unbinds it when passing in null.
     *
     * @throws IllegalStateException if this FloorFilterView is currently added to a MapView
     * @since 100.12.0
     */
    fun bindTo(mapView: MapView?) {
        if (drawInMapView) {
            throw IllegalStateException("FloorFilterView is currently added to a MapView")
        }
        if (mapView == null) {
            if (this.floorFilterManager.mapView != null) {
                floorFilterManager.clearMapView()
            }
        } else {
            setupMapView(mapView)
        }
    }

    /**
     * Sets up the [FloorFilterView] to work with the provided [mapView].
     *
     * @since 100.12.0
     */
    private fun setupMapView(mapView: MapView) {
        // TODO: figure out how to get the map if it is not on the map view initially
        floorFilterManager.setupMap(mapView, mapView.map) {
            updateSiteFacilityButtonEnabled()
        }
    }

    private fun updateSiteFacilityButtonEnabled() {
        siteFacilityButton?.isEnabled = floorFilterManager.sites.isNotEmpty() || floorFilterManager.facilities.isNotEmpty()
    }

    private fun updateSeparatorVisible() {
        siteFacilityButtonSeparator?.visibility = if (levelAdapter.itemCount > 0 && !hideSiteFacilityButton) View.VISIBLE else View.GONE
    }

    private fun addDataChangeListeners() {
        floorFilterManager.setOnLevelChangeListener {
            levelAdapter.updateData()
        }

        floorFilterManager.setOnFacilityChangeListener {
            levelAdapter.updateData()
            levelAdapter.onlyShowSelected = false

            val selectedFloorPosition = levelAdapter.getSelectedLevelPosition()
            if (selectedFloorPosition > -1) {
                floorsRecyclerView?.scrollToPosition(selectedFloorPosition)
            }
        }
    }

    private fun setupLevelsAdapter() {
        floorsRecyclerView.layoutManager = LinearLayoutManager(context)
        floorsRecyclerView.adapter = levelAdapter
        updateSeparatorVisible()
        levelAdapter.closeButton = floorListCloseButton

        levelAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                updateSeparatorVisible()
            }
        })
    }

    private fun setupSiteFacilityButton() {
        siteFacilityButton.setOnClickListener {
            if (siteFacilityButton.isSelected) {
                closeSiteFacilityView()
            } else {
                openSiteFacilityView()
            }
        }

        updateSiteFacilityButtonEnabled()
    }

    private fun setupSiteFacilityView(siteFacilityView: SiteFacilityView) {
        siteFacilityView.floorFilterManager = floorFilterManager

        siteFacilityView.setOnDismissListener {
            siteFacilityButton.isSelected = false
        }
    }

    private fun openSiteFacilityView() {
        siteFacilityView.show()
        siteFacilityButton.isSelected = true
    }

    private fun closeSiteFacilityView() {
        siteFacilityView.close()
        siteFacilityButton.isSelected = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newHeightSpec = if (levelsMax >= 0) {
            // TODO: Make this work with button size configured differently
            // TODO: Make sure this does not get taller than the parent wants it to be
            // Each button is 40dp height. We need to take into account the visible levels, the close
            // button and the siteFacility button. There is also a 1dp divider.
            var heightDp = levelsMax * 40
            if (!hideSiteFacilityButton) {
                heightDp += 41
            }
            if (!hideCloseButton) {
                heightDp += 40
            }
            MeasureSpec.makeMeasureSpec(heightDp.dpToPixels(displayDensity), MeasureSpec.AT_MOST)
        } else {
            heightMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, newHeightSpec)
    }

    /**
     * Implements the adapter to be set on the [RecyclerView].
     *
     * @since 100.12.0
     */
    private inner class LevelAdapter(val floorFilterManager: FloorFilterManager) : RecyclerView.Adapter<LevelViewHolder>() {
        private var allLevels: List<FloorLevel> = listOf()
        private var visibleLevels: List<FloorLevel> = listOf()

        var hideCloseButton: Boolean = false
        var closeButton: ImageView? = null
            set(value) {
                field = value
                value?.setOnClickListener {
                    onlyShowSelected = true
                }
            }

        var onlyShowSelected: Boolean = false
            set(value) {
                field = value
                updateData()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<ViewDataBinding>(
                    inflater,
                    R.layout.item_level_row,
                    parent,
                    false
            )
            return LevelViewHolder(binding)
        }

        override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
            val level = getItem(position)
            holder.bind(level, isSelectedLevel(level)) { clickedLevel ->
                if (isSelectedLevel(clickedLevel)) {
                    onlyShowSelected = false

                    val selectedLevelPosition = levelAdapter.getSelectedLevelPosition()
                    if (selectedLevelPosition > -1) {
                        floorsRecyclerView?.scrollToPosition(selectedLevelPosition)
                    }
                } else {
                    selectedLevelId = clickedLevel.id
                }
            }
        }

        override fun getItemCount(): Int {
            return visibleLevels.size
        }

        fun updateCloseButtonVisibility() {
            if (hideCloseButton) {
                closeButton?.visibility = View.GONE
            } else {
                closeButton?.visibility = if (visibleLevels.size < 2) View.GONE else View.VISIBLE
            }
        }

        fun updateData() {
            val unsortedLevels = floorFilterManager.getSelectedFacility()?.levels ?: emptyList()
            allLevels = unsortedLevels.sortedByDescending { it.verticalOrder }

            if (allLevels.isNotEmpty() && !allLevels.any { isSelectedLevel(it) }) {
                Log.w("summer", "Floor selected in adapter. This probably shouldn't happen.")
                val verticalOrder0Floor = allLevels.lastOrNull { it.verticalOrder == 0 } ?: allLevels.lastOrNull()
                selectedLevelId = verticalOrder0Floor?.id
            }

            visibleLevels = if (onlyShowSelected) {
                allLevels.filter { isSelectedLevel(it) }
            } else {
                allLevels
            }

            updateCloseButtonVisibility()

            notifyDataSetChanged()
        }

        fun getSelectedLevelPosition(): Int {
            return visibleLevels.indexOfFirst { isSelectedLevel(it) }
        }

        private fun isSelectedLevel(level: FloorLevel?): Boolean {
            return floorFilterManager.isSelectedLevel(level)
        }

        private fun getItem(position: Int): FloorLevel? {
            return visibleLevels.getOrNull(position)
        }
    }

    /**
     * The LevelAdapter ViewHolder.
     *
     * @since 100.12.0
     */
    private class LevelViewHolder(binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(level: FloorLevel?, isSelected: Boolean, onItemClickListener: (FloorLevel) -> Unit) {
            val textView = itemView.levelTextView
            if (textView != null) {
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                        textView,
                        10,
                        16,
                        1,
                        TypedValue.COMPLEX_UNIT_SP)

                if (level == null) {
                    textView.text = ""
                    textView.isSelected = false
                    textView.isEnabled = false
                    itemView.setOnClickListener(null)
                } else {
                    textView.text = level.shortName
                    textView.isSelected = isSelected
                    itemView.setOnClickListener { onItemClickListener.invoke(level) }
                }
            }
        }
    }
}
