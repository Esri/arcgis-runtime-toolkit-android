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
 *
 * @since 100.5.0
 */
@RunWith(AndroidJUnit4::class)
class NumberExtensionsTest {

    /**
     * Tests [Int.dpToPixels] to determine if the calculation performed produces the same results as the system. Ensuring
     * expected results are adhered to.
     *
     * @since 100.5.0
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
     *
     * @since 100.5.0
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
     *
     * @since 100.5.0
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
     *
     * @since 100.5.0
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
     *
     * @since 100.5.0
     */
    @Test
    fun intThrowIfNotPositivePositiveOneDoesNotThrow() {
        1.throwIfNotPositive("testParam")
    }

    /**
     * Tests [Int.throwIfNotPositive] with a "high" value to ensure that a positive [Int] does not throw an [Exception].
     *
     * @since 100.5.0
     */
    @Test
    fun intThrowIfNotPositivePositiveLargeNumberDoesNotThrow() {
        99999.throwIfNotPositive("testParam")
    }

    /**
     * Tests [Int.throwIfNotPositive] with a zero value to ensure that an instance of [IllegalArgumentException] is thrown
     * as per requirements.
     *
     * @since 100.5.0
     */
    @Test
    fun intThrowIfNotPositivePositiveZeroThrows() {
        try {
            0.throwIfNotPositive("testParam")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // success
        }
    }

    /**
     * Tests [Int.throwIfNotPositive] with a negative value to ensure that an instance of [IllegalArgumentException] is
     * thrown.
     *
     * @since 100.5.0
     */
    @Test
    fun intThrowIfNotPositivePositiveMinusOneThrows() {
        try {
            (-1).throwIfNotPositive("testParam")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // success
        }
    }

    /**
     * Tests [Int.throwIfNotPositive] with a "low" negative value to ensure that an instance of [IllegalArgumentException]
     * is thrown.
     *
     * @since 100.5.0
     */
    @Test
    fun intThrowIfNotPositivePositiveMinusLargeNumberThrows() {
        try {
            (-99999).throwIfNotPositive("testParam")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // success
        }
    }

    /**
     * Tests formatting Double as distance String with a negative number. Expected behaviour would be to return the String
     * without extra formatting.
     *
     * @since 100.5.0
     */
    @Test
    fun doubleAsDistanceStringMinusZeroPointOne() {
        assertEquals("-0.1", (-0.1).asDistanceString())
    }

    /**
     * Tests formatting Double as distance String with 0.0. Expected behaviour would be to return the String
     * without extra formatting.
     *
     * @since 100.5.0
     */
    @Test
    fun doubleAsDistanceStringZero() {
        assertEquals("0", 0.0.asDistanceString())
    }

    /**
     * Tests formatting Double as distance String with 0.1. Expected behaviour would be to return the String
     * without extra formatting.
     *
     * @since 100.5.0
     */
    @Test
    fun doubleAsDistanceStringZeroPointOne() {
        assertEquals("0.1", 0.1.asDistanceString())
    }

    /**
     * Tests formatting Double as distance String with 1.0. Expected behaviour would be to return the String
     * with the decimal place removed.
     *
     * @since 100.5.0
     */
    @Test
    fun doubleAsDistanceStringOne() {
        assertEquals("1", 1.0.asDistanceString())
    }

    /**
     * Tests formatting Double as distance String with 1.234567. Expected behaviour would be that the Double
     * would be rounded down to 1.23 and no other formatting occurring.
     *
     * @since 100.5.0
     */
    @Test
    fun doubleAsDistanceStringSixDecimalPlacesIsRounded() {
        assertEquals("1.23", 1.234567.asDistanceString())
    }

    /**
     * Tests formatting Double as distance String with 1.001. Expected behaviour would be that the Double
     * would be rounded down to 1.00, the trailing zeros are removed and no other formatting occurring.
     *
     * @since 100.5.0
     */
    @Test
    fun doubleAsDistanceStringRoundingToTwoDecimalPlacesAsZerosStripsDecimalZeros() {
        assertEquals("1", 1.001.asDistanceString())
    }

    /**
     * Tests formatting Double as distance String with 1.011. Expected behaviour would be that the Double
     * would be rounded down to 1.01, and no other formatting occurring.
     *
     * @since 100.5.0
     */
    @Test
    fun doubleAsDistanceStringRoundingToTwoDecimalPlacesAsNonZerosKeepsDecimals() {
        assertEquals("1.01", 1.011.asDistanceString())
    }

    /**
     * Tests formatting Double as distance String with 1.101. Expected behaviour would be that the Double
     * would be rounded down to 1.10 and the trailing zero is removed.
     *
     * @since 100.5.0
     */
    @Test
    fun doubleAsDistanceStringRoundingToTwoDecimalPlacesAsNonZeroFirstDecimalKeepsImportantDecimal() {
        assertEquals("1.1", 1.101.asDistanceString())
    }

    /**
     * Tests formatting Double as distance String with 99999.0.
     *
     * @since 100.5.0
     */
    @Test
    fun doubleAsDistanceLargeDouble() {
        assertEquals("99999", 99999.0.asDistanceString())
    }

    /**
     * Tests formatting Double as distance String with 12000.0
     *
     * @since 100.5.0
     */
    @Test
    fun doubleAsDistanceTwelveThousand() {
        assertEquals("12000", 12000.0.asDistanceString())
    }
}
