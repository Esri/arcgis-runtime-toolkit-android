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

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.esri.arcgisruntime.mapping.GeoModel
import com.esri.arcgisruntime.mapping.floor.FloorFacility
import com.esri.arcgisruntime.mapping.floor.FloorLevel
import com.esri.arcgisruntime.mapping.floor.FloorManager
import com.esri.arcgisruntime.mapping.floor.FloorSite
import com.esri.arcgisruntime.toolkit.extension.dpToPixels
import com.esri.arcgisruntime.toolkit.floorfilter.FloorFilterView.ButtonPosition

/**
 * Stores the customizable options for the [FloorFilterView] and [SiteFacilityView]. Also contains
 * some utility functions to apply the options to the UI components.
 *
 * @since 100.13.0
 */
internal data class UiParameters(
    var buttonHeightDp: Int = DEFAULT_BUTTON_HEIGHT_DP,
    var buttonWidthDp: Int = DEFAULT_BUTTON_WIDTH_DP,
    var maxDisplayLevels: Int = DEFAULT_MAX_DISPLAY_LEVELS,
    var textSizeSp: Int = DEFAULT_TEXT_SIZE_SP,
    var textColor: Int = DEFAULT_TEXT_COLOR,
    var selectedTextColor: Int = DEFAULT_SELECTED_TEXT_COLOR,
    var buttonBackgroundColor: Int = DEFAULT_BUTTON_BACKGROUND_COLOR,
    var selectedButtonBackgroundColor: Int = DEFAULT_SELECTED_BUTTON_BACKGROUND_COLOR,
    var closeButtonBackgroundColor: Int = DEFAULT_CLOSE_BUTTON_BACKGROUND_COLOR,
    var searchBackgroundColor: Int = DEFAULT_SEARCH_BACKGROUND_COLOR,
    var typeface: Typeface = DEFAULT_TYPEFACE,
    var hideCloseButton: Boolean = DEFAULT_HIDE_CLOSE_BUTTON,
    var hideSiteFacilityButton: Boolean = DEFAULT_HIDE_SITE_FACILITY_BUTTON,
    var hideSiteSearch: Boolean = DEFAULT_HIDE_SITE_SEARCH,
    var hideFacilitySearch: Boolean = DEFAULT_HIDE_FACILITY_SEARCH,
    var closeButtonPosition: ButtonPosition = DEFAULT_CLOSE_BUTTON_POSITION
) {

    /**
     * Sets the imageTintList of the [view] to the disabled [textColor], [selectedTextColor],
     * and [textColor].
     *
     * @since 100.13.0
     */
    fun setButtonTintColors(view: ImageView?) {
        val states = arrayOf(
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_selected),
            intArrayOf()
        )
        val colors = intArrayOf(getDisabledColor(textColor), selectedTextColor, textColor)
        view?.imageTintList = ColorStateList(states, colors)
    }

    /**
     * Sets the backgroundTintList of the [view] to the disabled [buttonBackgroundColor],
     * [selectedButtonBackgroundColor], and [buttonBackgroundColor].
     *
     * @since 100.13.0
     */
    fun setButtonBackgroundColors(view: View?) {
        val states = arrayOf(
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_selected),
            intArrayOf()
        )
        val colors = intArrayOf(getDisabledColor(buttonBackgroundColor), selectedButtonBackgroundColor, buttonBackgroundColor)
        view?.backgroundTintList = ColorStateList(states, colors)
    }

    /**
     * Sets the width of the [view] to [buttonWidthDp] unless [ignoreWidth] is true.
     * Sets the height of the [view] to [buttonHeightDp] unless [ignoreHeight] is true.
     *
     * @since 100.13.0
     */
    fun setButtonSizeForView(view: View?, displayDensity: Float, ignoreHeight: Boolean = false, ignoreWidth: Boolean = false) {
        val layoutParams = view?.layoutParams
        if (!ignoreHeight) {
            layoutParams?.height = buttonHeightDp.dpToPixels(displayDensity)
        }
        if (!ignoreWidth) {
            layoutParams?.width = buttonWidthDp.dpToPixels(displayDensity)
        }
        view?.layoutParams = layoutParams
    }

    /**
     * Sets the color of the [recyclerView] scrollbar to [textColor].
     *
     * @since 100.13.0
     */
    fun setScrollbarColor(recyclerView: RecyclerView?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val drawable = recyclerView?.verticalScrollbarThumbDrawable
            drawable?.setTint(textColor)
        }
    }

    /**
     * Returns the [color] with 38% opacity as recommended by material design for disabled controls.
     *
     * @since 100.13.0
     */
    private fun getDisabledColor(color: Int): Int {
        return ColorUtils.setAlphaComponent(color, 100)
    }
}
