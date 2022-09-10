/*
 * Copyright 2018 Comcast Cable Communications Management, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// https://github.com/unicode-org/icu/blob/3ef03a408714cf0be1f6be62e3fad57757403da3/icu4j/main/classes/core/src/com/ibm/icu/lang/UCharacter.java

// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html
/** ******************************************************************************
  * Copyright (C) 1996-2016, International Business Machines Corporation and
  * others. All Rights Reserved.
  * ******************************************************************************
  */

package com.comcast.ip4s.idna

private[idna] object UCharacter {

  /** Lead surrogate bitmask
    */
  private[this] final val LEAD_SURROGATE_BITMASK = 0xfffffc00

  /** Trail surrogate bitmask
    */
  private[this] final val TRAIL_SURROGATE_BITMASK = 0xfffffc00

  /** Lead surrogate bits
    */
  private[this] final val LEAD_SURROGATE_BITS = 0xd800

  /** Trail surrogate bits
    */
  private[this] final val TRAIL_SURROGATE_BITS = 0xdc00

  private[this] final val U16_SURROGATE_OFFSET = ((0xd800 << 10) + 0xdc00 - 0x10000)

  /** {@icu} Returns a code point corresponding to the two surrogate code units.
    *
    * @param lead the lead unit
    *        (In ICU 2.1-69 the type of both parameters was <code>char</code>.)
    * @param trail the trail unit
    * @return code point if lead and trail form a valid surrogate pair.
    * @exception IllegalArgumentException thrown when the code units do
    *            not form a valid surrogate pair
    * @stable ICU 70
    * @see #toCodePoint(int, int)
    */
  def getCodePoint(lead: Int, trail: Int): Int = {
    if (isHighSurrogate(lead) && isLowSurrogate(trail)) {
      return toCodePoint(lead, trail)
    }
    throw new IllegalArgumentException("Not a valid surrogate pair")
  }

  /** Same as {@link Character#toCodePoint},
    * except that the ICU version accepts <code>int</code> for code points.
    * Returns the code point represented by the two surrogate code units.
    * This does not check the surrogate pair for validity.
    *
    * @param high the high (lead) surrogate
    *        (In ICU 3.0-69 the type of both parameters was <code>char</code>.)
    * @param low the low (trail) surrogate
    * @return the code point formed by the surrogate pair
    * @stable ICU 70
    * @see #getCodePoint(int, int)
    */
  def toCodePoint(high: Int, low: Int) = {
    // see ICU4C U16_GET_SUPPLEMENTARY()
    (high << 10) + low - U16_SURROGATE_OFFSET
  }

  /** Same as {@link Character#isHighSurrogate},
    * except that the ICU version accepts <code>int</code> for code points.
    *
    * @param codePoint the code point to check
    *        (In ICU 3.0-69 the type of this parameter was <code>char</code>.)
    * @return true if codePoint is a high (lead) surrogate
    * @stable ICU 70
    */
  def isHighSurrogate(codePoint: Int) = {
    (codePoint & LEAD_SURROGATE_BITMASK) == LEAD_SURROGATE_BITS
  }

  /** Same as {@link Character#isLowSurrogate},
    * except that the ICU version accepts <code>int</code> for code points.
    *
    * @param codePoint the code point to check
    *        (In ICU 3.0-69 the type of this parameter was <code>char</code>.)
    * @return true if codePoint is a low (trail) surrogate
    * @stable ICU 70
    */
  def isLowSurrogate(codePoint: Int) = {
    (codePoint & TRAIL_SURROGATE_BITMASK) == TRAIL_SURROGATE_BITS
  }
}
