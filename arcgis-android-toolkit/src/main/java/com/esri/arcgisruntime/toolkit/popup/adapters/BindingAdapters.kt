/*
 * Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.toolkit.popup.adapters

import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.esri.arcgisruntime.mapping.popup.Popup

/**
 * Sets the popup symbol of the identified feature popup in the bottom sheet when in the collapsed
 * state.
 */
@BindingAdapter("app:imageBitmap")
fun setImageBitmap(imageView: ImageView, identifiedPopup: LiveData<Popup>) {

    identifiedPopup.value?.let {
        val symbolFuture =
            it.symbol.createSwatchAsync(
                14, 14,
                imageView.context.resources.displayMetrics.density,
                0x00000000
            )
        symbolFuture.addDoneListener {
            try {
                val symbol = symbolFuture.get()
                imageView.setImageBitmap(symbol)
            } catch (e: Exception) {
                Log.i("PopupView", "Error creating swatch for the popup symbol ${e.message}")
            }
        }
    }
}
