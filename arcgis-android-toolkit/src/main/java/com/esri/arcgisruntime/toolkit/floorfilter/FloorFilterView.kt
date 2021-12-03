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
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Dimension
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.widget.TextViewCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esri.arcgisruntime.mapping.GeoModel
import com.esri.arcgisruntime.mapping.floor.FloorFacility
import com.esri.arcgisruntime.mapping.floor.FloorLevel
import com.esri.arcgisruntime.mapping.floor.FloorSite
import com.esri.arcgisruntime.mapping.floor.FloorManager
import com.esri.arcgisruntime.mapping.view.GeoView
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.SceneView
import com.esri.arcgisruntime.toolkit.R
import com.esri.arcgisruntime.toolkit.extension.dpToPixels
import com.esri.arcgisruntime.toolkit.extension.pixelsToDp
import com.esri.arcgisruntime.toolkit.extension.pixelsToSp

class FloorFilterView: LinearLayout {

    /**
     * The siteId of the selected [FloorSite].
     *
     * @since 100.13.0
     */
    var selectedSiteId: String?
        get() { return floorFilterManager.selectedSiteId }
        set(value) { floorFilterManager.selectedSiteId = value }

    /**
     * The facilityId of the selected [FloorFacility].
     *
     * @since 100.13.0
     */
    var selectedFacilityId: String?
        get() { return floorFilterManager.selectedFacilityId }
        set(value) { floorFilterManager.selectedFacilityId = value }

    /**
     * The levelId of the selected [FloorLevel].
     *
     * @since 100.13.0
     */
    var selectedLevelId: String?
        get() { return floorFilterManager.selectedLevelId }
        set(value) { floorFilterManager.selectedLevelId = value }

    /**
     * The [FloorManager] of the attached [GeoModel].
     *
     * @since 100.13.0
     */
    val floorManager: FloorManager?
        get() { return floorFilterManager.floorManager }

    // Custom events

    /**
     * Called when the selected [FloorSite], [FloorFacility], or [FloorLevel] changes.
     *
     * @since 100.13.0
     */
    private var onSelectionChangeListener: (() -> Unit)? = null
    fun setOnSelectionChangeListener(onSelectionChangeListener: (() -> Unit)?) {
        this.onSelectionChangeListener = onSelectionChangeListener
    }

    // UI related parameters

    /**
     * The [Int] used to determine the height of the level, close, and site/facility buttons
     * in the [floorsRecyclerView].
     *
     * The default is 40dp.
     * Use [setButtonSize] to change the height of the buttons.
     *
     * @since 100.13.0
     */
    private var buttonHeightDp: Int
        set(value) { uiParameters.buttonHeightDp = value }
        get() { return uiParameters.buttonHeightDp }

    /**
     * The [Int] used to determine the width of the level, close, and site/facility buttons
     * in the [floorsRecyclerView].
     *
     * The default is 48 dp.
     * Use [setButtonSize] to change the width of the buttons.
     *
     * @since 100.13.0
     */
    private var buttonWidthDp: Int
        set(value) { uiParameters.buttonWidthDp = value }
        get() { return uiParameters.buttonWidthDp }

    /**
     * The [Int] used to determine the max amount of levels to show in the [floorsRecyclerView].
     *
     * The default is -1. Anything that is less than 1 will show all of the levels.
     * Use [setMaxDisplayLevels] to change the max amount of levels to display.
     *
     * @since 100.13.0
     */
    private var maxDisplayLevels: Int
        set(value) { uiParameters.maxDisplayLevels = value }
        get() { return uiParameters.maxDisplayLevels }

    /**
     * The [Int] used to determine the text size in [Dimension.SP].
     *
     * The default is 16sp.
     * Use [setTextSize] to change the text size.
     *
     * @since 100.13.0
     */
    private var textSizeSp: Int
        set(value) { uiParameters.textSizeSp = value }
        get() { return uiParameters.textSizeSp }

    /**
     * The color used for text.
     *
     * The default is #323236, a dark gray.
     *
     * @since 100.13.0
     */
    var textColor: Int
        set(value) {
            uiParameters.textColor = value
            processUiParamUpdate()
        }
        get() { return uiParameters.textColor }

    /**
     * The color used for selected text.
     *
     * The default is #004874, a dark blue
     *
     * @since 100.13.0
     */
    var selectedTextColor: Int
        set(value) {
            uiParameters.selectedTextColor = value
            processUiParamUpdate()
        }
        get() { return uiParameters.selectedTextColor }

    /**
     * The color used for button background.
     *
     * The default is white.
     *
     * @since 100.13.0
     */
    var buttonBackgroundColor: Int
        set(value) {
            uiParameters.buttonBackgroundColor = value
            processUiParamUpdate()
        }
        get() { return uiParameters.buttonBackgroundColor }

    /**
     * The color used for selected button background.
     *
     * The default is #004874, a dark blue.
     *
     * @since 100.13.0
     */
    var selectedButtonBackgroundColor: Int
        set(value) {
            uiParameters.selectedButtonBackgroundColor = value
            processUiParamUpdate()
        }
        get() { return uiParameters.selectedButtonBackgroundColor }

    /**
     * The color used for close button background.
     *
     * The default is #E7E7E7, a light gray.
     *
     * @since 100.13.0
     */
    var closeButtonBackgroundColor: Int
        set(value) {
            uiParameters.closeButtonBackgroundColor = value
            processUiParamUpdate()
        }
        get() { return uiParameters.closeButtonBackgroundColor }

    /**
     * The color used for search bar background.
     *
     * The default is #F3F3F3, a light gray.
     *
     * @since 100.13.0
     */
    var searchBackgroundColor: Int
        set(value) {
            uiParameters.searchBackgroundColor = value
            processUiParamUpdate()
        }
        get() { return uiParameters.searchBackgroundColor }

    /**
     * The [Typeface] used to draw text.
     *
     * The default typeface is Typeface.DEFAULT.
     *
     * @since 100.13.0
     */
    var typeface: Typeface
        set(value) {
            uiParameters.typeface = value
            processUiParamUpdate()
        }
        get() { return uiParameters.typeface }

    /**
     * The [Boolean] used to decide if the close button should be shown.
     *
     * The default is false.
     *
     * @since 100.13.0
     */
    var hideCloseButton: Boolean
        set(value) {
            uiParameters.hideCloseButton = value
            levelAdapter.updateCloseButtonVisibility()
        }
        get() { return uiParameters.hideCloseButton }

    /**
     * The [Boolean] used to decide if the site/facility button should be shown.
     *
     * The default is false.
     *
     * @since 100.13.0
     */
    var hideSiteFacilityButton: Boolean
        set(value) {
            uiParameters.hideSiteFacilityButton = value
            updateSiteFacilityButtonEnabled()
        }
        get() { return uiParameters.hideSiteFacilityButton }

    /**
     * The [Boolean] used to decide if search for site should be shown.
     *
     * The default is false.
     *
     * @since 100.13.0
     */
    var hideSiteSearch: Boolean
        set(value) {
            uiParameters.hideSiteSearch = value
            siteFacilityView.showHideSearch()
        }
        get() { return uiParameters.hideSiteSearch }

    /**
     * The [Boolean] used to decide if search for facility should be shown.
     *
     * The default is false.
     *
     * @since 100.13.0
     */
    var hideFacilitySearch: Boolean
        set(value) {
            uiParameters.hideFacilitySearch = value
            siteFacilityView.showHideSearch()
        }
        get() { return uiParameters.hideFacilitySearch }

    /**
     * The [ButtonPosition] used to place the close button at the top or bottom of
     * the [floorsRecyclerView].
     *
     * The default is [ButtonPosition.TOP].
     * The site/facility button will be located opposite of the close button. So if the close
     * button is TOP, the site/facility button will be BOTTOM.
     *
     * @since 100.13.0
     */
    var closeButtonPosition: ButtonPosition
        set(value) {
            if (uiParameters.closeButtonPosition != value) {
                uiParameters.closeButtonPosition = value
                setButtonPositions()
            }
        }
        get() { return uiParameters.closeButtonPosition }

    private var drawInGeoView: Boolean = false
    private var geoViewHolder: View? = null
    private val floorFilterManager: FloorFilterManager = FloorFilterManager()
    private val uiParameters: UiParameters = UiParameters()
    private val levelAdapter by lazy { LevelAdapter(floorFilterManager) }

    private val displayDensity: Float by lazy {
        resources.displayMetrics.density
    }

    private val scaledDensity: Float by lazy {
        resources.displayMetrics.scaledDensity
    }

    private val siteFacilityView by lazy {
        val view = SiteFacilityView(context)
        setupSiteFacilityView(view)
        view
    }

    /**
     * Constructor used when instantiating this View directly to attach it to another view
     * programmatically.
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
        context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.FloorFilterView,
                0, 0
        ).apply {
            try {
                // button display attributes
                maxDisplayLevels = getInt(R.styleable.FloorFilterView_maxDisplayLevels, DEFAULT_MAX_DISPLAY_LEVELS)
                hideCloseButton = getBoolean(R.styleable.FloorFilterView_hideCloseButton, DEFAULT_HIDE_CLOSE_BUTTON)
                hideSiteFacilityButton = getBoolean(R.styleable.FloorFilterView_hideSiteFacilityButton, DEFAULT_HIDE_SITE_FACILITY_BUTTON)
                hideSiteSearch = getBoolean(R.styleable.FloorFilterView_hideSiteSearch, DEFAULT_HIDE_SITE_SEARCH)
                hideFacilitySearch = getBoolean(R.styleable.FloorFilterView_hideFacilitySearch, DEFAULT_HIDE_FACILITY_SEARCH)
                ButtonPosition.fromInt(getInt(R.styleable.FloorFilterView_closeButtonPosition, DEFAULT_CLOSE_BUTTON_POSITION.value))?.let {
                    closeButtonPosition = it
                }

                // text size attribute
                textSizeSp = getDimensionPixelSize(R.styleable.FloorFilterView_textSize, 0).pixelsToSp(scaledDensity)
                if (textSizeSp <= 0) {
                    textSizeSp = DEFAULT_TEXT_SIZE_SP
                }

                // color attributes
                textColor = getColor(R.styleable.FloorFilterView_textColor, DEFAULT_TEXT_COLOR)
                selectedTextColor = getColor(R.styleable.FloorFilterView_selectedTextColor, DEFAULT_SELECTED_TEXT_COLOR)
                buttonBackgroundColor = getColor(R.styleable.FloorFilterView_buttonBackgroundColor, DEFAULT_BUTTON_BACKGROUND_COLOR)
                selectedButtonBackgroundColor = getColor(R.styleable.FloorFilterView_selectedButtonBackgroundColor, DEFAULT_SELECTED_BUTTON_BACKGROUND_COLOR)
                closeButtonBackgroundColor = getColor(R.styleable.FloorFilterView_closeButtonBackgroundColor, DEFAULT_CLOSE_BUTTON_BACKGROUND_COLOR)
                searchBackgroundColor = getColor(R.styleable.FloorFilterView_searchBackgroundColor, DEFAULT_SEARCH_BACKGROUND_COLOR)

                // size attributes
                buttonHeightDp = getDimensionPixelSize(R.styleable.FloorFilterView_buttonHeight, 0).pixelsToDp(displayDensity)
                buttonWidthDp = getDimensionPixelSize(R.styleable.FloorFilterView_buttonWidth, 0).pixelsToDp(displayDensity)
                if (buttonHeightDp <= 0) {
                    buttonHeightDp = DEFAULT_BUTTON_HEIGHT_DP
                }
                if (buttonWidthDp <= 0) {
                    buttonWidthDp = DEFAULT_BUTTON_WIDTH_DP
                }
            } finally {
                recycle()
            }
        }
        init(context)
    }

    /**
     * Initializes this FloorFilterView by inflating the layout and setting the [RecyclerView]
     * adapter.
     *
     * @since 100.13.0
     */
    private var floorsRecyclerView: RecyclerView? = null
    private var floorListCloseButton: ImageView? = null
    private var siteFacilityButton: ImageView? = null
    private var siteFacilityButtonSeparator: View? = null
    private fun init(context: Context) {
        orientation = VERTICAL
        inflate(context, R.layout.layout_floorfilterview, this)

        floorsRecyclerView = findViewById(R.id.floorsRecyclerView)
        floorListCloseButton = findViewById(R.id.floorListCloseButton)
        siteFacilityButton = findViewById(R.id.siteFacilityButton)
        siteFacilityButtonSeparator = findViewById(R.id.siteFacilityButtonSeparator)

        addDataChangeListeners()
        setupLevelsAdapter()
        setupSiteFacilityButton()
        setButtonPositions()
        processUiParamUpdate()
    }

    /**
     * Zooms to the selected [FloorFacility]. If no [FloorFacility] is selected, it will zoom to
     * the selected [FloorSite].
     *
     * @since 100.13.0
     */
    fun zoomToSelection() {
        floorFilterManager.zoomToSelection()
    }

    /**
     * Returns the [maxDisplayLevels] used to determine the max amount of levels to show in the [floorsRecyclerView].
     *
     * The default is -1. Anything that is less than 1 will show all of the levels.
     * Use [setMaxDisplayLevels] to change the max amount of levels to display.
     *
     * @since 100.13.0
     */
    fun getMaxDisplayLevels(): Int? {
        return if (maxDisplayLevels < 0) {
            null
        } else {
            maxDisplayLevels
        }
    }

    /**
     * The [Int] used to determine the max amount of levels to show in the list before scrolling.
     *
     * The default is -1. Anything that is less than 1 will show all of the levels.
     * Use [getMaxDisplayLevels] to get the current value.
     *
     * @since 100.13.0
     */
    fun setMaxDisplayLevels(maxDisplayLevels: Int?) {
        this.maxDisplayLevels = maxDisplayLevels ?: -1
        scrollToSelectedLevel()
    }

    /**
     * The size of the text in the levels list.
     *
     * The default is 16sp. Use [Dimension.SP] as the [unit] to pass [size] as sp.
     *
     * @since 100.13.0
     */
    fun setTextSize(unit: Int, size: Float) {
        textSizeSp = try {
            if (unit == Dimension.SP) {
                size.toInt()
            } else {
                TypedValue.applyDimension(unit, size, resources.displayMetrics).toInt().pixelsToSp(scaledDensity)
            }
        } catch (e: Exception) {
            DEFAULT_TEXT_SIZE_SP
        }
        processUiParamUpdate()
    }

    /**
     * The size of each level button in the levels list.
     *
     * The default is height is 40dp and width is 48dp.
     * Use [Dimension.DP] as the [unit] to pass [height] and [width] as dp.
     *
     * @since 100.13.0
     */
    fun setButtonSize(unit: Int, height: Float, width: Float) {
        val (buttonHeightDp, buttonWidthDp) = try {
            if (unit == Dimension.DP) {
                Pair(height.toInt(), width.toInt())
            } else {
                Pair(
                    TypedValue.applyDimension(unit, height, resources.displayMetrics).toInt().pixelsToDp(displayDensity),
                    TypedValue.applyDimension(unit, width, resources.displayMetrics).toInt().pixelsToDp(displayDensity)
                )
            }
        } catch (e: Exception) {
            Pair(DEFAULT_BUTTON_HEIGHT_DP, DEFAULT_BUTTON_WIDTH_DP)
        }

        this.buttonHeightDp = buttonHeightDp
        this.buttonWidthDp = buttonWidthDp
        processUiParamUpdate()
    }

    /**
     * Adds this [FloorFilterView] to the provided [geoView].
     *
     * @throws IllegalStateException if this FloorFilterView is already added to or bound to
     * a [GeoView]
     * @throws IllegalStateException if there is no [GeoModel] in the [GeoView]
     * @since 100.13.0
     */
    fun addToGeoView(geoView: GeoView, position: ListPosition = ListPosition.BOTTOM_START) {
        val map = getGeoModel(geoView) ?: throw IllegalStateException("There is no GeoModel in the GeoView")

        this.floorFilterManager.geoView?.let {
            throw IllegalStateException("FloorFilterView already has a GeoView")
        }
        drawInGeoView = true

        // Set up a constraint holder to hold the floor filter view
        val holder = ConstraintLayout(context)
        holder.isClickable = false
        holder.id = View.generateViewId()
        this.id = View.generateViewId()
        holder.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Add the floor filter view to the holder and the holder to the map
        this.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0)
        geoViewHolder = holder
        geoView.addView(holder)
        holder.addView(this)

        // Set the floor filter view elevation and rounded corners.
        this.elevation = ((4).dpToPixels(displayDensity)).toFloat()
        this.background = AppCompatResources.getDrawable(context, R.drawable.floor_filter_rounded_background)
        this.clipToOutline = true

        // Constrain the floor filter view to the correct spot in the holder
        val dp20 = (20).dpToPixels(displayDensity)
        val dp40 = (40).dpToPixels(displayDensity)
        val constraintSet = ConstraintSet()
        constraintSet.clone(holder)
        constraintSet.connect(this.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, dp20)
        constraintSet.connect(this.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, dp40)
        if (position.isStart()) {
            constraintSet.connect(this.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dp20)
        } else {
            constraintSet.connect(this.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dp20)
        }
        closeButtonPosition = if (position.isTop()) {
            constraintSet.setVerticalBias(this.id, 0f)
            ButtonPosition.BOTTOM
        } else {
            constraintSet.setVerticalBias(this.id, 1f)
            ButtonPosition.TOP
        }
        constraintSet.applyTo(holder)

        setupGeoView(geoView, map)
    }

    /**
     * Removes this FloorFilterView from the [GeoView] it was added to (if any).
     *
     * @throws IllegalStateException if this FloorFilterView is not currently added to a [GeoView]
     * @since 100.13.0
     */
    fun removeFromGeoView() {
        if (!drawInGeoView) {
            throw IllegalStateException("FloorFilterView is not currently added to a GeoView")
        }

        floorFilterManager.geoView?.removeView(geoViewHolder)
        geoViewHolder = null
        drawInGeoView = false
        floorFilterManager.clearGeoView()
    }

    /**
     * Binds this [FloorFilterView] to the provided [geoView], or unbinds it when passing in null.
     *
     * @throws IllegalStateException if this FloorFilterView is currently added to a [GeoView]
     * @since 100.13.0
     */
    fun bindTo(geoView: GeoView?) {
        if (drawInGeoView) {
            throw IllegalStateException("FloorFilterView is currently added to a GeoView")
        }
        if (geoView == null) {
            if (this.floorFilterManager.geoView != null) {
                floorFilterManager.clearGeoView()
            }
        } else {
            val map = getGeoModel(geoView) ?: throw IllegalStateException("There is no GeoModel in the GeoView")
            setupGeoView(geoView, map)
        }
    }

    /**
     * Scrolls the levels list to show the selected level.
     *
     * @since 100.13.0
     */
    fun scrollToSelectedLevel() {
        val selectedFloorPosition = levelAdapter.getSelectedLevelPosition()
        if (selectedFloorPosition > -1) {
            floorsRecyclerView?.scrollToPosition(selectedFloorPosition)
        }
    }

    /**
     * Opens the dialog to show the sites and facilities.
     *
     * @since 100.13.0
     */
    fun openSiteFacilityView() {
        siteFacilityView.show()
        siteFacilityButton?.isSelected = true
    }

    /**
     * Closes the sites and facilities dialog.
     *
     * @since 100.13.0
     */
    fun closeSiteFacilityView() {
        siteFacilityView.close()
        siteFacilityButton?.isSelected = false
    }

    // Setup functions

    /**
     * Sets up the [FloorFilterView] to work with the provided [geoView].
     *
     * @since 100.13.0
     */
    private fun setupGeoView(geoView: GeoView, map: GeoModel) {
        floorFilterManager.setupMap(geoView, map) {
            updateSiteFacilityButtonEnabled()
        }
    }

    /**
     * Disables the site/facility button if there are no sites or facilities in the map. Hides the
     * site/facility button if [hideSiteFacilityButton] is true.
     *
     * @since 100.13.0
     */
    private fun updateSiteFacilityButtonEnabled() {
        siteFacilityButton?.isEnabled = floorFilterManager.sites.isNotEmpty() || floorFilterManager.facilities.isNotEmpty()
        if (hideSiteFacilityButton) {
            siteFacilityButton?.visibility = View.GONE
        } else {
            siteFacilityButton?.visibility = View.VISIBLE
        }
        updateSeparatorVisible()
    }

    private fun updateSeparatorVisible() {
        siteFacilityButtonSeparator?.visibility = if (levelAdapter.itemCount > 0 && !hideSiteFacilityButton) View.VISIBLE else View.GONE
    }

    private fun addDataChangeListeners() {
        floorFilterManager.setOnLevelChangeListener {
            levelAdapter.updateData()
            onSelectionChangeListener?.invoke()
        }

        floorFilterManager.setOnFacilityChangeListener {
            levelAdapter.updateData()
            levelAdapter.onlyShowSelected = false
            scrollToSelectedLevel()
        }
    }

    private fun setupLevelsAdapter() {
        floorsRecyclerView?.layoutManager = LinearLayoutManager(context)
        floorsRecyclerView?.adapter = levelAdapter
        updateSeparatorVisible()
        levelAdapter.closeButton = floorListCloseButton

        levelAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                updateSeparatorVisible()
            }
        })
    }

    private fun setupSiteFacilityButton() {
        siteFacilityButton?.setOnClickListener {
            if (siteFacilityButton?.isSelected == true) {
                closeSiteFacilityView()
            } else {
                openSiteFacilityView()
            }
        }

        updateSiteFacilityButtonEnabled()
    }

    private fun setupSiteFacilityView(siteFacilityView: SiteFacilityView) {
        siteFacilityView.setup(floorFilterManager, uiParameters)
        siteFacilityView.processUiParamUpdate()

        siteFacilityView.setOnDismissListener {
            siteFacilityButton?.isSelected = false
        }
    }

    /**
     * Handles changes to the configurable UI parameters.
     *
     * @since 100.13.0
     */
    private fun processUiParamUpdate() {
        uiParameters.setButtonSizeForView(siteFacilityButton, displayDensity)
        uiParameters.setButtonBackgroundColors(siteFacilityButton)
        uiParameters.setButtonTintColors(siteFacilityButton)

        uiParameters.setButtonSizeForView(floorListCloseButton, displayDensity)
        floorListCloseButton?.setBackgroundColor(closeButtonBackgroundColor)
        uiParameters.setButtonTintColors(floorListCloseButton)

        uiParameters.setButtonSizeForView(siteFacilityButtonSeparator, displayDensity, ignoreHeight = true)
        siteFacilityButtonSeparator?.setBackgroundColor(closeButtonBackgroundColor)

        uiParameters.setScrollbarColor(floorsRecyclerView)

        levelAdapter.notifyDataSetChanged()
        siteFacilityView.processUiParamUpdate()
    }

    /**
     * Make sure the control is the correct height to display the number of levels that needs
     * to be shown. This can be affected by the number of levels in the selected facility, the
     * maxDisplayLevels set, and the max height the control can be.
     *
     * @since 100.13.0
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Get the height for the amount of levels we want to show.
        val levelsDisplayed = if (maxDisplayLevels >= 0) {
            minOf(maxDisplayLevels, levelAdapter.itemCount)
        } else {
            levelAdapter.itemCount
        }
        var heightDp = levelsDisplayed * buttonHeightDp
        when {
            levelsDisplayed == 1 -> heightDp += 1
            levelsDisplayed > 0 -> heightDp += (levelsDisplayed - 1)
            else -> heightDp = 0
        }

        // Include the height of the site/facility button and the separator if it is shown. The
        // separator is 1dp tall.
        if (siteFacilityButton?.visibility == View.VISIBLE) {
            heightDp += buttonHeightDp
        }
        if (siteFacilityButtonSeparator?.visibility == View.VISIBLE) {
            heightDp += 1
        }

        // Include the height of the close button if it is shown.
        if (floorListCloseButton?.visibility == View.VISIBLE) {
            heightDp += buttonHeightDp
        }

        // If the height we want is less than the max height for the control, we can use the height
        // we want. Otherwise use the max height for the control.
        val heightPixels = heightDp.dpToPixels(displayDensity)
        val originalHeightPixels = MeasureSpec.getSize(heightMeasureSpec)
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val newHeight = if (mode == MeasureSpec.UNSPECIFIED || heightPixels < originalHeightPixels) {
            heightPixels
        } else {
            originalHeightPixels
        }
        val newHeightSpec = MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, newHeightSpec)
    }

    /**
     * Sets the [floorListCloseButton] and [siteFacilityButton] to the top or bottom of the
     * [floorsRecyclerView] depending on the value of [closeButtonPosition].
     *
     * @since 100.13.0
     */
    private fun setButtonPositions() {
        val closeButtonPosition = this.closeButtonPosition
        val closeButton = floorListCloseButton
        val siteFacilityButton = siteFacilityButton
        val siteFacilityButtonSeparator = siteFacilityButtonSeparator
        val levelList = floorsRecyclerView

        if (closeButton != null && siteFacilityButton != null && siteFacilityButtonSeparator != null && levelList != null) {
            this.removeAllViews()
            if (closeButtonPosition == ButtonPosition.TOP) {
                this.addView(closeButton)
                this.addView(levelList)
                this.addView(siteFacilityButtonSeparator)
                this.addView(siteFacilityButton)
            } else {
                this.addView(siteFacilityButton)
                this.addView(siteFacilityButtonSeparator)
                this.addView(levelList)
                this.addView(closeButton)
            }
        }
    }

    /**
     * Returns the [GeoModel] from the [geoView].
     *
     * @since 100.13.0
     */
    private fun getGeoModel(geoView: GeoView): GeoModel? {
        return when (geoView) {
            is MapView -> geoView.map
            is SceneView -> geoView.scene
            else -> null
        }
    }

    /**
     * The adapter to be set on the [RecyclerView].
     *
     * @since 100.13.0
     */
    private inner class LevelAdapter(val floorFilterManager: FloorFilterManager) : RecyclerView.Adapter<LevelViewHolder>() {
        private var allLevels: List<FloorLevel> = listOf()
        private var visibleLevels: List<FloorLevel> = listOf()

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
                    scrollToSelectedLevel()
                } else {
                    selectedLevelId = clickedLevel.levelId
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
                // This shouldn't happen, but handling it just in case. There should always be a
                // selected level by the time this code runs.
                val verticalOrder0Floor = allLevels.lastOrNull { it.verticalOrder == 0 } ?: allLevels.lastOrNull()
                selectedLevelId = verticalOrder0Floor?.levelId
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
     * The [LevelAdapter] [RecyclerView.ViewHolder].
     *
     * @since 100.13.0
     */
    private inner class LevelViewHolder(binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(level: FloorLevel?, isSelected: Boolean, onItemClickListener: (FloorLevel) -> Unit) {
            val textView = itemView.findViewById<TextView>(R.id.levelTextView)
            if (textView != null) {
                // Set the button height and width
                uiParameters.setButtonSizeForView(textView, displayDensity)

                // Set the text color
                textView.setTextColor(if (isSelected) selectedTextColor else textColor)

                // Set the background color
                uiParameters.setButtonBackgroundColors(textView)

                // Set the typeface
                textView.typeface = typeface

                // Set the text size
                val minTextSizeSp = 10
                val maxTextSizeSp = textSizeSp
                if (minTextSizeSp < maxTextSizeSp) {
                    TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                        textView,
                        minTextSizeSp,
                        maxTextSizeSp,
                        1,
                        TypedValue.COMPLEX_UNIT_SP)
                } else {
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(textView, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE)
                    textView.setTextSize(Dimension.SP, maxTextSizeSp.toFloat())
                }

                // Set the text to show
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

    /**
     * Represents the position of the level list close button.
     *
     * @since 100.13.0
     */
    enum class ButtonPosition (
            val value: Int
    ) {
        /**
         * The button will be above the level list.
         *
         * @since 100.13.0
         */
        TOP(0),

        /**
         * The button will be below the level list.
         *
         * @since 100.13.0
         */
        BOTTOM(1);

        companion object {
            private val map = values().associateBy(ButtonPosition::value)
            fun fromInt(type: Int) = map[type]
        }
    }

    /**
     * Represents the position of the level list when using [addToGeoView].
     *
     * @since 100.13.0
     */
    enum class ListPosition (
        val value: Int
    ) {
        /**
         * The level list will be at the top start of the map.
         *
         * @since 100.13.0
         */
        TOP_START(0),

        /**
         * The level list will be at the top end of the map.
         *
         * @since 100.13.0
         */
        TOP_END(1),

        /**
         * The level list will be at the bottom start of the map.
         *
         * @since 100.13.0
         */
        BOTTOM_START(2),

        /**
         * The level list will be at the bottom end of the map.
         *
         * @since 100.13.0
         */
        BOTTOM_END(3),;

        companion object {
            private val map = values().associateBy(ListPosition::value)
            fun fromInt(type: Int) = map[type]
        }

        fun isTop(): Boolean {
            return this == TOP_END || this == TOP_START
        }

        fun isBottom(): Boolean {
            return this == BOTTOM_END || this == BOTTOM_START
        }

        fun isStart(): Boolean {
            return this == BOTTOM_START || this == TOP_START
        }

        fun isEnd(): Boolean {
            return this == BOTTOM_END || this == TOP_END
        }
    }
}
