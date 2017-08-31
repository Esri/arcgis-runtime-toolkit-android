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
package com.esri.arcgisruntime.toolkit;

/**
 * Provides utility methods used by toolkit components.
 *
 * @since 100.1.0
 */
public class ToolkitUtil {

  private static final String PARAMETER_NULL_EXCEPTION_MSG = "Parameter %s must not be null";

  /**
   * Throws IllegalArgumentException if the value of a given argument is null.
   *
   * @param argValue the argument value to check
   * @param argName  the name of the argument
   * @throws IllegalArgumentException if argValue is null
   * @since 100.1.0
   */
  public static void throwIfNull(Object argValue, String argName) {
    if (argValue == null) {
      throw new IllegalArgumentException(String.format(PARAMETER_NULL_EXCEPTION_MSG, argName));
    }
  }

}
