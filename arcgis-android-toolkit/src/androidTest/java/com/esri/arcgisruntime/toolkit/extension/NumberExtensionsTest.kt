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

package com.esri.arcgisruntime.toolkit.extension

import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NumberExtensionsTest {

    @Test
    fun intThrowIfNotPositivePositiveOneDoesNotThrow() {
        1.throwIfNotPositive()
    }

    @Test
    fun intThrowIfNotPositivePositiveLargeNumberDoesNotThrow() {
        99999.throwIfNotPositive()
    }

    @Test
    fun intThrowIfNotPositivePositiveZeroThrows() {
        try {
            0.throwIfNotPositive()
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // success
        }
    }

    @Test
    fun intThrowIfNotPositivePositiveMinusOneThrows() {
        try {
            (-1).throwIfNotPositive()
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // success
        }
    }

    @Test
    fun intThrowIfNotPositivePositiveMinusLargeNumberThrows() {
        try {
            (-99999).throwIfNotPositive()
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // success
        }
    }

}