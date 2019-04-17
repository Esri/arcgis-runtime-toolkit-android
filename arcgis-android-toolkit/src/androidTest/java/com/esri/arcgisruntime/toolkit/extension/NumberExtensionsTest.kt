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

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.esri.arcgisruntime.toolkit.R
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented unit tests for NumberExtensions
 */
@RunWith(AndroidJUnit4::class)
class NumberExtensionsTest {

    /**
     * Tests [Int.dpToPixels] to determine if the calculation performed produces the same results as the system. Ensuring
     * expected results are adhered to.
     */
    @Test
    fun intToPixelsEightyDpReturnsSameValue() {
        with(InstrumentationRegistry.getContext().resources) {
            val expected = this.getDimensionPixelSize(R.dimen.test_int_to_pixel_size_eighty)
            val actual = 80.dpToPixels(this.displayMetrics.density)
            assertEquals(expected, actual)
        }
    }

    /**
     * Tests [Int.dpToPixels] to determine if a calculation against a zero value performed produces the same results as
     * the system. Ensuring expected results are adhered to.
     */
    @Test
    fun intToPixelsZeroDpReturnsSameValue() {
        with(InstrumentationRegistry.getContext().resources)
        {
            val expected = this.getDimensionPixelSize(R.dimen.test_int_to_pixel_size_zero)
            val actual = 0.dpToPixels(this.displayMetrics.density)
            assertEquals(expected, actual)
        }
    }

    /**
     * Tests [Double.dpToPixels] to determine if the calculation performed produces the same results as the system. Ensuring
     * expected results are adhered to.
     */
    @Test
    fun doubleToPixelsEightyDpReturnsSameValue() {
        with(InstrumentationRegistry.getContext().resources)
        {
            val expected = this.getDimensionPixelSize(R.dimen.test_int_to_pixel_size_eighty)
            val actual = 80.0.dpToPixels(this.displayMetrics.density)
            assertEquals(expected, actual)
        }
    }

    /**
     * Tests [Double.dpToPixels] to determine if a calculation against a zero value performed produces the same results as
     * the system. Ensuring expected results are adhered to.
     */
    @Test
    fun doubleToPixelsZeroDpReturnsSameValue() {
        with(InstrumentationRegistry.getContext().resources)
        {
            val expected = this.getDimensionPixelSize(R.dimen.test_int_to_pixel_size_zero)
            val actual = 0.0.dpToPixels(this.displayMetrics.density)
            assertEquals(expected, actual)
        }
    }

    /**
     * Tests [Int.throwIfNotPositive] to ensure that a positive [Int] does not throw an [Exception].
     */
    @Test
    fun intThrowIfNotPositivePositiveOneDoesNotThrow() {
        1.throwIfNotPositive()
    }

    /**
     * Tests [Int.throwIfNotPositive] with a "high" value to ensure that a positive [Int] does not throw an [Exception].
     */
    @Test
    fun intThrowIfNotPositivePositiveLargeNumberDoesNotThrow() {
        99999.throwIfNotPositive()
    }

    /**
     * Tests [Int.throwIfNotPositive] with a zero value to ensure that an instance of [IllegalArgumentException] is thrown
     * as per requirements.
     */
    @Test
    fun intThrowIfNotPositivePositiveZeroThrows() {
        try {
            0.throwIfNotPositive()
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // success
        }
    }

    /**
     * Tests [Int.throwIfNotPositive] with a negative value to ensure that an instance of [IllegalArgumentException] is
     * thrown.
     */
    @Test
    fun intThrowIfNotPositivePositiveMinusOneThrows() {
        try {
            (-1).throwIfNotPositive()
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // success
        }
    }

    /**
     * Tests [Int.throwIfNotPositive] with a "low" negative value to ensure that an instance of [IllegalArgumentException]
     * is thrown.
     */
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
