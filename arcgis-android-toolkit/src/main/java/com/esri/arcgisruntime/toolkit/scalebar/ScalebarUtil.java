/*
 * Copyright 2017 Esri
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
package com.esri.arcgisruntime.toolkit.scalebar;

import com.esri.arcgisruntime.UnitSystem;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;

/**
 * Utility methods used by Scalebar.
 *
 * @since 100.1.0
 */
public class ScalebarUtil {

  private static final LinearUnit LINEAR_UNIT_METERS = new LinearUnit(LinearUnitId.METERS);

  private static final LinearUnit LINEAR_UNIT_FEET = new LinearUnit(LinearUnitId.FEET);

  private static final LinearUnit LINEAR_UNIT_KILOMETERS = new LinearUnit(LinearUnitId.KILOMETERS);

  private static final LinearUnit LINEAR_UNIT_MILES = new LinearUnit(LinearUnitId.MILES);

  // Array containing the multipliers that may be used for a scalebar and arrays of segment options appropriate for each
  // multiplier
  private static final MultiplierData[] MULTIPLIER_DATA_ARRAY = {
      new MultiplierData(1.0, new int[] { 1, 2, 4, 5 }),
      new MultiplierData(1.2, new int[] { 1, 2, 3, 4 }),
      new MultiplierData(1.5, new int[] { 1, 2, 3, 5 }),
      new MultiplierData(1.6, new int[] { 1, 2, 4 }),
      new MultiplierData(2.0, new int[] { 1, 2, 4, 5 }),
      new MultiplierData(2.4, new int[] { 1, 2, 3, 4 }),
      new MultiplierData(3.0, new int[] { 1, 2, 3 }),
      new MultiplierData(3.6, new int[] { 1, 2, 3 }),
      new MultiplierData(4.0, new int[] { 1, 2, 4 }),
      new MultiplierData(5.0, new int[] { 1, 2, 5 }),
      new MultiplierData(6.0, new int[] { 1, 2, 3 }),
      new MultiplierData(8.0, new int[] { 1, 2, 4 }),
      new MultiplierData(9.0, new int[] { 1, 2, 3 }),
      new MultiplierData(10.0, new int[] { 1, 2, 5 })
  };

  /**
   * Calculates the best length for the scalebar to fit within a given maximum length.
   *
   * @param maxLength   the maximum length
   * @param unit        indicates the unit of length being used: meters or feet
   * @param isSegmented true if the scalebar is segmented
   * @return the "best length", the highest "nice" number less than or equal to maxLength
   * @since 100.1.0
   */
  public static double calculateBestScalebarLength(double maxLength, LinearUnit unit, boolean isSegmented) {
    double magnitude = calculateMagnitude(maxLength);
    double multiplier = selectMultiplierData(maxLength, magnitude).getMultiplier();

    // If the scalebar isn't segmented, force the multiplier to be an integer if it's > 2.0
    if (!isSegmented && multiplier > 2.0) {
      multiplier = Math.floor(multiplier);
    }
    double bestLength = multiplier * magnitude;

    // If using imperial units, check if the number of feet is greater than the threshold for using feet; note this
    // isn't necessary for metric units because bestLength calculated using meters will also be a nice number of km
    if (unit.getLinearUnitId() == LinearUnitId.FEET) {
      LinearUnit displayUnits = selectLinearUnit(bestLength, UnitSystem.IMPERIAL);
      if (unit.getLinearUnitId() != displayUnits.getLinearUnitId()) {
        // 'unit' is feet but we're going to display in miles, so recalculate bestLength to give a nice number of miles
        bestLength = calculateBestScalebarLength(unit.convertTo(displayUnits, maxLength), displayUnits, isSegmented);
        // but convert that back to feet because the caller is using feet
        return displayUnits.convertTo(unit, bestLength);
      }
    }
    return bestLength;
  }

  /**
   * Calculates the optimal number of segments in the scalebar when the distance represented by the whole scalebar has
   * a particular value. This is optimized so that the labels on the segments are all "nice" numbers.
   *
   * @param distance       the distance represented by the whole scalebar, that is the value to be displayed at the
   *                       end of the scalebar
   * @param maxNumSegments the maximum number of segments to avoid the labels of the segments overwriting each other
   *                       (this is passed in by the caller to allow this method to be platform independent)
   * @return the optimal number of segments in the scalebar
   * @since 100.1.0
   */
  public static int calculateOptimalNumberOfSegments(double distance, int maxNumSegments) {
    // Create an ordered array of options for the specified distance
    int[] options = segmentOptionsForDistance(distance);

    // Select the largest option that's <= maxNumSegments
    int ret = 1;
    for (int option : options) {
      if (option > maxNumSegments) {
        break;
      }
      ret = option;
    }
    return ret;
  }

  /**
   * Selects the appropriate LinearUnit to use when the distance represented by the whole scalebar has a particular
   * value.
   *
   * @param distance   the distance represented by the whole scalebar, that is the value to be displayed at the end
   *                   of the scalebar; in feet if unitSystem is IMPERIAL or meters if unitSystem is METRIC
   * @param unitSystem the UnitSystem being used
   * @return the LinearUnit
   * @since 100.1.0
   */
  public static LinearUnit selectLinearUnit(double distance, UnitSystem unitSystem) {
    switch (unitSystem) {
      case IMPERIAL:
        // use MILES if at least half a mile
        if (distance >= 2640) {
          return LINEAR_UNIT_MILES;
        }
        return LINEAR_UNIT_FEET;

      case METRIC:
      default:
        // use KILOMETERS if at least one kilometer
        if (distance >= 1000) {
          return LINEAR_UNIT_KILOMETERS;
        }
        return LINEAR_UNIT_METERS;
    }
  }

  /**
   * Creates a string to display as a scalebar label corresponding to a given distance.
   *
   * @param distance the distance
   * @return the label string
   * @since 100.1.0
   */
  public static String labelString(double distance) {
    // Format with 2 decimal places
    String label = String.format("%.2f", distance);

    // Strip off both decimal places if they're 0s
    if (label.endsWith(".00") || label.endsWith(",00")) {
      return label.substring(0, label.length() - 3);
    }

    // Otherwise, strip off last decimal place if it's 0
    if (label.endsWith("0")) {
      return label.substring(0, label.length() - 1);
    }
    return label;
  }

  /**
   * Calculates the "magnitude" used when calculating the length of a scalebar or the number of segments. This is the
   * largest power of 10 that's less than or equal to a given distance.
   *
   * @param distance the distance represented by the scalebar
   * @return the magnitude, a power of 10
   * @since 100.1.0
   */
  private static double calculateMagnitude(double distance) {
    return Math.pow(10, Math.floor(Math.log10(distance)));
  }

  /**
   * Selects the "multiplier" used when calculating the length of a scalebar or the number of segments in the
   * scalebar. This is chosen to give "nice" numbers for all the labels on the scalebar.
   *
   * @param distance  the distance represented by the scalebar
   * @param magnitude the "magnitude" used when calculating the length of a scalebar or the number of segments
   * @return a MultiplierData object containing the multiplier, which will give the scalebar length when multiplied by
   * the magnitude
   * @since 100.1.0
   */
  private static MultiplierData selectMultiplierData(double distance, double magnitude) {
    double residual = distance / magnitude;

    // Select the largest multiplier that's <= residual
    MultiplierData ret = MULTIPLIER_DATA_ARRAY[0];
    for (MultiplierData multiplierData : MULTIPLIER_DATA_ARRAY) {
      if (multiplierData.getMultiplier() > residual) {
        break;
      }
      ret = multiplierData;
    }
    return ret;
  }

  /**
   * Returns the segment options that are appropriate when a scalebar represents a given distance.
   *
   * @param distance the distance represented by the scalebar
   * @return the segment options; these are ints representing number of segments in the scalebar
   * @since 100.1.0
   */
  private static int[] segmentOptionsForDistance(double distance) {
    return selectMultiplierData(distance, calculateMagnitude(distance)).getSegmentOptions();
  }

  /**
   * Container for a "multiplier" and the array of segment options appropriate for that multiplier. The multiplier is
   * used when calculating the length of a scalebar or the number of segments in the scalebar.
   *
   * @since 100.1.0
   */
  private static class MultiplierData {
    private final double mMultiplier;

    private final int[] mSegmentOptions;

    /**
     * Constructs a MultiplierData.
     *
     * @param multiplier     the multiplier
     * @param segmentOptions the array of segment options appropriate for the multiplier; these are ints representing
     *                       number of segments in the scalebar; it's important that they are in ascending order
     * @since 100.1.0
     */
    public MultiplierData(double multiplier, int[] segmentOptions) {
      mMultiplier = multiplier;
      mSegmentOptions = segmentOptions;
    }

    /**
     * Gets the multiplier.
     *
     * @return the multiplier
     * @since 100.1.0
     */
    public double getMultiplier() {
      return mMultiplier;
    }

    /**
     * Gets the segment options.
     *
     * @return the segment options; these are ints representing number of segments in the scalebar
     * @since 100.1.0
     */
    public int[] getSegmentOptions() {
      return mSegmentOptions;
    }
  }

}
