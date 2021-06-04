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
import org.json.JSONArray
import org.json.JSONObject

/**
 * The FloorSite object. This will be replaced with the FloorSite object in runtime.
 */

internal class FloorSite(
        val id: String,
        val name: String,
        var facilities: List<FloorFacility> = emptyList(),
        val geometry: Geometry? = null) {

    fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("name", name)

        val facilitiesJson = JSONArray()
        facilities.forEach { facility ->
            facilitiesJson.put(facility.toJson())
        }
        jsonObject.put("facilities", facilitiesJson)
        return jsonObject
    }

}
