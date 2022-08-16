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

// https://github.com/unicode-org/icu/blob/3ef03a408714cf0be1f6be62e3fad57757403da3/icu4j/main/classes/core/src/com/ibm/icu/text/UTF16.java

// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html
/** ******************************************************************************
  * Copyright (C) 1996-2016, International Business Machines Corporation and
  * others. All Rights Reserved.
  * ******************************************************************************
  */

package com.comcast.ip4s.idna

private[idna] object UTF16 {

  /** The minimum value for Supplementary code points
    *
    * @stable ICU 2.1
    */
  private[this] final val SUPPLEMENTARY_MIN_VALUE = 0x10000

  /** Lead surrogate minimum value
    *
    * @stable ICU 2.1
    */
  private[this] final val LEAD_SURROGATE_MIN_VALUE = 0xd800

  /** Trail surrogate minimum value
    *
    * @stable ICU 2.1
    */
  private[this] final val TRAIL_SURROGATE_MIN_VALUE = 0xdc00

  /** Lead surrogate bitmask
    */
  private[this] final val LEAD_SURROGATE_BITMASK = 0xfffffc00

  /** Trail surrogate bitmask
    */
  private[this] final val TRAIL_SURROGATE_BITMASK = 0xfffffc00

  /** Surrogate bitmask
    */
  private[this] final val SURROGATE_BITMASK = 0xfffff800

  /** Lead surrogate bits
    */
  private[this] final val LEAD_SURROGATE_BITS = 0xd800

  /** Trail surrogate bits
    */
  private[this] final val TRAIL_SURROGATE_BITS = 0xdc00

  /** Surrogate bits
    */
  private[this] final val SURROGATE_BITS = 0xd800

  /** Determines whether the code point is a surrogate.
    *
    * @param codePoint The input character.
    *        (In ICU 2.1-69 the type of this parameter was <code>char</code>.)
    * @return true If the input code point is a surrogate.
    * @stable ICU 70
    */
  def isSurrogate(codePoint: Int) = {
    (codePoint & SURROGATE_BITMASK) == SURROGATE_BITS
  }

  /** Determines whether the code point is a trail surrogate.
    *
    * @param codePoint The input character.
    *        (In ICU 2.1-69 the type of this parameter was <code>char</code>.)
    * @return true If the input code point is a trail surrogate.
    * @stable ICU 70
    */
  def isTrailSurrogate(codePoint: Int) = {
    (codePoint & TRAIL_SURROGATE_BITMASK) == TRAIL_SURROGATE_BITS
  }

  /** Determines whether the code point is a lead surrogate.
    *
    * @param codePoint The input character.
    *        (In ICU 2.1-69 the type of this parameter was <code>char</code>.)
    * @return true If the input code point is a lead surrogate
    * @stable ICU 70
    */
  def isLeadSurrogate(codePoint: Int) = {
    (codePoint & LEAD_SURROGATE_BITMASK) == LEAD_SURROGATE_BITS
  }

  /** Returns the lead surrogate. If a validity check is required, use
    * <code><a href="../lang/UCharacter.html#isLegal(char)">isLegal()</a></code> on char32
    * before calling.
    *
    * @param char32 The input character.
    * @return lead surrogate if the getCharCount(ch) is 2 <br>
    *         and 0 otherwise (note: 0 is not a valid lead surrogate).
    * @stable ICU 2.1
    */
  def getLeadSurrogate(char32: Int) = {
    if (char32 >= SUPPLEMENTARY_MIN_VALUE) {
      (LEAD_SURROGATE_OFFSET_ + (char32 >> LEAD_SURROGATE_SHIFT_)).toChar
    } else 0.toChar
  }

  /** Returns the trail surrogate. If a validity check is required, use
    * <code><a href="../lang/UCharacter.html#isLegal(char)">isLegal()</a></code> on char32
    * before calling.
    *
    * @param char32 The input character.
    * @return the trail surrogate if the getCharCount(ch) is 2 <br>
    *         otherwise the character itself
    * @stable ICU 2.1
    */
  def getTrailSurrogate(char32: Int) = {
    if (char32 >= SUPPLEMENTARY_MIN_VALUE) {
      (TRAIL_SURROGATE_MIN_VALUE + (char32 & TRAIL_SURROGATE_MASK_)).toChar
    } else char32.toChar
  }

  /** Shift value for lead surrogate to form a supplementary character.
    */
  private[this] final val LEAD_SURROGATE_SHIFT_ = 10

  /** Mask to retrieve the significant value from a trail surrogate.
    */
  private[this] final val TRAIL_SURROGATE_MASK_ = 0x3ff

  /** Value that all lead surrogate starts with
    */
  private[this] final val LEAD_SURROGATE_OFFSET_ =
    LEAD_SURROGATE_MIN_VALUE - (SUPPLEMENTARY_MIN_VALUE >> LEAD_SURROGATE_SHIFT_)

}
