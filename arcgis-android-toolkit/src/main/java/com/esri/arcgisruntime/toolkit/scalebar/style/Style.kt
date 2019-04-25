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

package com.esri.arcgisruntime.toolkit.scalebar.style

import com.esri.arcgisruntime.toolkit.scalebar.style.renderer.AlternatingBarRenderer
import com.esri.arcgisruntime.toolkit.scalebar.style.renderer.BarRenderer
import com.esri.arcgisruntime.toolkit.scalebar.style.renderer.DualUnitLineRenderer
import com.esri.arcgisruntime.toolkit.scalebar.style.renderer.GraduatedLineRenderer
import com.esri.arcgisruntime.toolkit.scalebar.style.renderer.LineRenderer
import com.esri.arcgisruntime.toolkit.scalebar.style.renderer.ScalebarRenderer

/**
 * Represents the style of scalebar to be displayed.
 *
 * @since 100.2.1
 */
enum class Style(val value: Int) {

    /**
     * A simple, non-segmented bar. A single label is displayed showing the distance represented by the length of the
     * whole bar.
     *
     * @since 100.2.1
     */
    BAR(0) {
        override val renderer: ScalebarRenderer = BarRenderer()
    },

    /**
     * A bar split up into equal-length segments, with the colors of the segments alternating between the fill color and
     * the alternate fill color. A label is displayed at the end of each segment, showing the distance represented by
     * the length of the bar up to that point.
     *
     * @since 100.2.1
     */
    ALTERNATING_BAR(1) {
        override val renderer: ScalebarRenderer = AlternatingBarRenderer()
    },

    /**
     * A simple, non-segmented line. A single label is displayed showing the distance represented by the length of the
     * whole line.
     *
     * @since 100.2.1
     */
    LINE(2) {
        override val renderer: ScalebarRenderer = LineRenderer()
    },

    /**
     * A line split up into equal-length segments. A tick and a label are displayed at the end of each segment, showing
     * the distance represented by the length of the line up to that point.
     *
     * @since 100.2.1
     */
    GRADUATED_LINE(3) {
        override val renderer: ScalebarRenderer = GraduatedLineRenderer()
    },

    /**
     * A line showing distance in dual unit systems - metric and imperial. The primary unit system, as set by
     * [.setUnitSystem], is used to determine the length of the line. A label above the line shows the
     * distance represented by the length of the whole line, in the primary unit system. A tick and another label are
     * displayed below the line, showing distance in the other unit system.
     *
     * @since 100.2.1
     */
    DUAL_UNIT_LINE(4) {
        override val renderer: ScalebarRenderer = DualUnitLineRenderer()
    };

    companion object {
        private val map = values().associateBy(Style::value)
        fun fromInt(type: Int) = map[type]
    }

    abstract val renderer: ScalebarRenderer
}