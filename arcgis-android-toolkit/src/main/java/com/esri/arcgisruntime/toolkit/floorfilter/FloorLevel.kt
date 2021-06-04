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

import com.esri.arcgisruntime.geometry.Geometry
import org.json.JSONObject

/**
 * The FloorLevel object. This will be replaced with the FloorLevel object in runtime.
 */

internal class FloorLevel(
        val id: String,
        val shortName: String,
        val verticalOrder: Int,
        var facility: FloorFacility? = null,
        val geometry: Geometry? = null) {

    fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("shortName", shortName)
        jsonObject.put("verticalOrder", verticalOrder)
        return jsonObject
    }

}
