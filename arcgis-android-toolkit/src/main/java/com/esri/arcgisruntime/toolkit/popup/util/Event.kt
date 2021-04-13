/*
 * Copyright 2021 Esri
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

package com.esri.arcgisruntime.toolkit.popup.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 *
 * <p>
 * This avoids a common problem with events: on configuration change (like rotation) an update
 * can be emitted if the observer is active. This LiveData only calls the observable if there's an
 * explicit call to postValue().
 * <p>
 * https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
 */
open class Event<out T>(private val content: T) {
    private var hasBeenHandled = false
    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}

/**
 * Adds the given event function to observer being added to the observers list within the lifespan
 * of the given owner. The events are dispatched on the main thread.
 *
 * @param owner    The LifecycleOwner which controls the observer
 * @param onEventRaised The observer function that will receive the events from the observer
*/
fun <T> LiveData<Event<T>>.observeEvent(owner: LifecycleOwner, onEventRaised: (T) -> Unit) {
    observe(owner, Observer<Event<T>> { event  ->
        event?.getContentIfNotHandled()?.let { onEventRaised(it) }
    })
}

/**
 * Raises an event with given argument by calling postValue() on the MutableLiveData object.
 *
 * @param arg The argument to pass to the observers
 */
fun <T> MutableLiveData<Event<T>>.raiseEvent(arg: T) {
    postValue(Event(arg))
}

/**
 * Raises an event by calling postValue() on the MutableLiveData object.
 */
fun MutableLiveData<Event<Unit>>.raiseEvent() {
    postValue(Event(Unit))
}
