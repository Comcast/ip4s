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

// https://github.com/unicode-org/icu/blob/3ef03a408714cf0be1f6be62e3fad57757403da3/icu4j/main/classes/core/src/com/ibm/icu/impl/Punycode.java

// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html
/*
 *******************************************************************************
 * Copyright (C) 2003-2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.comcast.ip4s.idna

import java.lang.StringBuilder

/** Ported code from ICU punycode.c
  * @author ram
  */
private[idna] object Punycode {

  /* Punycode parameters for Bootstring */
  private[this] final val BASE = 36
  private[this] final val TMIN = 1
  private[this] final val TMAX = 26
  private[this] final val SKEW = 38
  private[this] final val DAMP = 700
  private[this] final val INITIAL_BIAS = 72
  private[this] final val INITIAL_N = 0x80

  /* "Basic" Unicode/ASCII code points */
  private[this] final val HYPHEN = 0x2d
  private[this] final val DELIMITER = HYPHEN.toChar

  private[this] final val ZERO = 0x30
  // private[this] final val NINE           = 0x39

  private[this] final val SMALL_A = 0x61
  private[this] final val SMALL_Z = 0x7a

  private[this] final val CAPITAL_A = 0x41
  private[this] final val CAPITAL_Z = 0x5a

  private[this] def adaptBias(_delta: Int, length: Int, firstTime: Boolean) = {
    var delta = _delta
    if (firstTime) {
      delta /= DAMP
    } else {
      delta /= 2
    }
    delta += delta / length

    var count = 0
    while (delta > ((BASE - TMIN) * TMAX) / 2) {
      delta /= (BASE - TMIN)
      count += BASE
    }

    count + (((BASE - TMIN + 1) * delta) / (delta + SKEW))
  }

  /** @return the numeric value of a basic code point (for use in representing integers)
    *         in the range 0 to BASE-1, or a negative value if cp is invalid.
    */
  private[this] def decodeDigit(cp: Int) = {
    if (cp <= 'Z') {
      if (cp <= '9') {
        if (cp < '0') {
          -1
        } else {
          cp - '0' + 26 // 0..9 -> 26..35
        }
      } else {
        cp - 'A' // A-Z -> 0..25
      }
    } else if (cp <= 'z') {
      cp - 'a' // a..z -> 0..25
    } else {
      -1
    }
  }

  private[this] def asciiCaseMap(_b: Char, uppercase: Boolean) = {
    var b = _b
    if (uppercase) {
      if (SMALL_A <= b && b <= SMALL_Z) {
        b = (b - (SMALL_A - CAPITAL_A)).toChar
      }
    } else {
      if (CAPITAL_A <= b && b <= CAPITAL_Z) {
        b = (b + (SMALL_A - CAPITAL_A)).toChar
      }
    }
    b
  }

  /** digitToBasic() returns the basic code point whose value
    * (when used for representing integers) is d, which must be in the
    * range 0 to BASE-1. The lowercase form is used unless the uppercase flag is
    * nonzero, in which case the uppercase form is used.
    */
  private[this] def digitToBasic(digit: Int, uppercase: Boolean) = {
    /*  0..25 map to ASCII a..z or A..Z */
    /* 26..35 map to ASCII 0..9         */
    if (digit < 26) {
      if (uppercase) {
        (CAPITAL_A + digit).toChar
      } else {
        (SMALL_A + digit).toChar
      }
    } else {
      ((ZERO - 26) + digit).toChar
    }
  }

  // ICU-13727: Limit input length for n^2 algorithm
  // where well-formed strings are at most 59 characters long.
  private[this] final val ENCODE_MAX_CODE_UNITS = 1000
  private[this] final val DECODE_MAX_CHARS = 2000

  /** Converts Unicode to Punycode.
    * The input string must not contain single, unpaired surrogates.
    * The output will be represented as an array of ASCII code points.
    *
    * @param src The source of the String Buffer passed.
    * @param caseFlags The boolean array of case flags.
    * @return An array of ASCII code points.
    */
  def encode(src: CharSequence, caseFlags: Array[Boolean]): StringBuilder = {
    var n, delta, handledCPCount, basicLength, bias, j, m, q, k, t, srcCPCount = 0
    var c, c2 = '\u0000'
    val srcLength = src.length()
    if (srcLength > ENCODE_MAX_CODE_UNITS) {
      throw new IllegalArgumentException("input too long: " + srcLength + " UTF-16 code units")
    }
    val cpBuffer = new Array[Int](srcLength)
    val dest = new StringBuilder(srcLength)
    /*
     * Handle the basic code points and
     * convert extended ones to UTF-32 in cpBuffer (caseFlag in sign bit):
     */
    srcCPCount = 0

    j = 0
    while (j < srcLength) {
      c = src.charAt(j)
      if (isBasic(c.toInt)) {
        cpBuffer(srcCPCount) = 0
        srcCPCount += 1
        dest.append(if (caseFlags != null) asciiCaseMap(c, caseFlags(j)) else c)
      } else {
        n = (if (caseFlags != null && caseFlags(j)) 1 else 0) << 31
        if (!UTF16.isSurrogate(c.toInt)) {
          n |= c
        } else if (
          UTF16.isLeadSurrogate(c.toInt) && (j + 1) < srcLength && UTF16
            .isTrailSurrogate({ c2 = src.charAt(j + 1); c2.toInt })
        ) {
          j += 1

          n |= UCharacter.getCodePoint(c.toInt, c2.toInt)
        } else {
          /* error: unmatched surrogate */
          throw new IllegalArgumentException("Illegal char found")
        }
        cpBuffer(srcCPCount) = n
        srcCPCount += 1
      }
      j += 1
    }

    /* Finish the basic string - if it is not empty - with a delimiter. */
    basicLength = dest.length()
    if (basicLength > 0) {
      dest.append(DELIMITER)
    }

    /*
     * handledCPCount is the number of code points that have been handled
     * basicLength is the number of basic code points
     * destLength is the number of chars that have been output
     */

    /* Initialize the state: */
    n = INITIAL_N
    delta = 0
    bias = INITIAL_BIAS

    /* Main encoding loop: */
    handledCPCount = basicLength
    while (handledCPCount < srcCPCount) {
      /*
       * All non-basic code points < n have been handled already.
       * Find the next larger one:
       */
      m = 0x7fffffff
      j = 0
      while (j < srcCPCount) {
        q = cpBuffer(j) & 0x7fffffff /* remove case flag from the sign bit */
        if (n <= q && q < m) {
          m = q
        }
        j += 1
      }

      /*
       * Increase delta enough to advance the decoder's
       * <n,i> state to <m,0>, but guard against overflow:
       */
      if (m - n > (0x7fffffff - handledCPCount - delta) / (handledCPCount + 1)) {
        throw new IllegalStateException("Internal program error")
      }
      delta += (m - n) * (handledCPCount + 1)
      n = m

      /* Encode a sequence of same code points n */
      j = 0
      while (j < srcCPCount) {
        q = cpBuffer(j) & 0x7fffffff /* remove case flag from the sign bit */
        if (q < n) {
          delta += 1
        } else if (q == n) {
          /* Represent delta as a generalized variable-length integer: */
          q = delta
          k = BASE
          var continue = true
          while (continue) {

            /** RAM: comment out the old code for conformance with draft-ietf-idn-punycode-03.txt
              *
              *                        t=k-bias
              *                        if(t<TMIN) {
              *                            t=TMIN
              *                        } else if(t>TMAX) {
              *                            t=TMAX
              *                        }
              */

            t = k - bias
            if (t < TMIN) {
              t = TMIN
            } else if (k >= (bias + TMAX)) {
              t = TMAX
            }

            if (q < t) {
              continue = false
            } else {
              dest.append(digitToBasic(t + (q - t) % (BASE - t), false))
              q = (q - t) / (BASE - t)

              k = k + BASE
            }
          }

          dest.append(digitToBasic(q, (cpBuffer(j) < 0)))
          bias = adaptBias(delta, handledCPCount + 1, (handledCPCount == basicLength))
          delta = 0
          handledCPCount += 1
        }
        j += 1
      }

      delta += 1
      n += 1
    }

    return dest
  }

  private[this] def isBasic(ch: Int) = {
    (ch < INITIAL_N)
  }

  private[this] def isBasicUpperCase(ch: Int) = {
    (CAPITAL_A <= ch && ch >= CAPITAL_Z)
  }

  private[this] def isSurrogate(ch: Int) = {
    (((ch) & 0xfffff800) == 0xd800)
  }

  /** Converts Punycode to Unicode.
    * The Unicode string will be at most as long as the Punycode string.
    *
    * @param src The source of the string buffer being passed.
    * @param caseFlags The array of boolean case flags.
    * @return StringBuilder string.
    */
  def decode(src: CharSequence, caseFlags: Array[Boolean]): StringBuilder = {
    val srcLength = src.length()
    if (srcLength > DECODE_MAX_CHARS) {
      throw new IllegalArgumentException("input too long: " + srcLength + " characters")
    }
    val dest = new StringBuilder(src.length())
    var n, i, bias, basicLength, j, in, oldi, w, k, digit, t, destCPCount, firstSupplementaryIndex, cpLength = 0
    var b = '\u0000'

    /*
     * Handle the basic code points:
     * Let basicLength be the number of input code points
     * before the last delimiter, or 0 if there is none,
     * then copy the first basicLength code points to the output.
     *
     * The following loop iterates backward.
     */
    j = srcLength
    var continue = true
    while (continue && j > 0) {
      j -= 1
      if (src.charAt(j) == DELIMITER) {
        continue = false
      }
    }
    basicLength = j
    destCPCount = j

    j = 0
    while (j < basicLength) {
      b = src.charAt(j)
      if (!isBasic(b.toInt)) {
        throw new IllegalArgumentException("Illegal char found")
      }
      dest.append(b)

      if (caseFlags != null && j < caseFlags.length) {
        caseFlags(j) = isBasicUpperCase(b.toInt)
      }
      j += 1
    }

    /* Initialize the state: */
    n = INITIAL_N
    i = 0
    bias = INITIAL_BIAS
    firstSupplementaryIndex = 1000000000

    /*
     * Main decoding loop:
     * Start just after the last delimiter if any
     * basic code points were copied start at the beginning otherwise.
     */
    in = if (basicLength > 0) basicLength + 1 else 0
    while (in < srcLength) {
      /*
       * in is the index of the next character to be consumed, and
       * destCPCount is the number of code points in the output array.
       *
       * Decode a generalized variable-length integer into delta,
       * which gets added to i.  The overflow checking is easier
       * if we increase i as we go, then subtract off its starting
       * value at the end to obtain delta.
       */
      oldi = i
      w = 1
      k = BASE
      continue = true
      while (continue) {
        if (in >= srcLength) {
          throw new IllegalArgumentException("Illegal char found")
        }

        digit = decodeDigit(src.charAt(in).toInt)
        in += 1
        if (digit < 0) {
          throw new IllegalArgumentException("Invalid char found")
        }
        if (digit > (0x7fffffff - i) / w) {
          /* integer overflow */
          throw new IllegalArgumentException("Illegal char found")
        }

        i += digit * w
        t = k - bias
        if (t < TMIN) {
          t = TMIN
        } else if (k >= (bias + TMAX)) {
          t = TMAX
        }
        if (digit < t) {
          continue = false
        } else {
          if (w > 0x7fffffff / (BASE - t)) {
            /* integer overflow */
            throw new IllegalArgumentException("Illegal char found")
          }
          w *= BASE - t

          k += BASE
        }
      }

      /*
       * Modification from sample code:
       * Increments destCPCount here,
       * where needed instead of in for() loop tail.
       */
      destCPCount += 1
      bias = adaptBias(i - oldi, destCPCount, (oldi == 0))

      /*
       * i was supposed to wrap around from (incremented) destCPCount to 0,
       * incrementing n each time, so we'll fix that now:
       */
      if (i / destCPCount > (0x7fffffff - n)) {
        /* integer overflow */
        throw new IllegalArgumentException("Illegal char found")
      }

      n += i / destCPCount
      i %= destCPCount
      /* not needed for Punycode: */
      /* if (decode_digit(n) <= BASE) return punycode_invalid_input */

      if (n > 0x10ffff || isSurrogate(n)) {
        /* Unicode code point overflow */
        throw new IllegalArgumentException("Illegal char found")
      }

      /* Insert n at position i of the output: */
      cpLength = Character.charCount(n)
      var codeUnitIndex = 0

      /*
       * Handle indexes when supplementary code points are present.
       *
       * In almost all cases, there will be only BMP code points before i
       * and even in the entire string.
       * This is handled with the same efficiency as with UTF-32.
       *
       * Only the rare cases with supplementary code points are handled
       * more slowly - but not too bad since this is an insertion anyway.
       */
      if (i <= firstSupplementaryIndex) {
        codeUnitIndex = i
        if (cpLength > 1) {
          firstSupplementaryIndex = codeUnitIndex
        } else {
          firstSupplementaryIndex += 1
        }
      } else {
        codeUnitIndex = dest.offsetByCodePoints(firstSupplementaryIndex, i - firstSupplementaryIndex)
      }

      /* use the UChar index codeUnitIndex instead of the code point index i */
      if (caseFlags != null && (dest.length() + cpLength) <= caseFlags.length) {
        if (codeUnitIndex < dest.length()) {
          System.arraycopy(caseFlags, codeUnitIndex, caseFlags, codeUnitIndex + cpLength, dest.length() - codeUnitIndex)
        }
        /* Case of last character determines uppercase flag: */
        caseFlags(codeUnitIndex) = isBasicUpperCase(src.charAt(in - 1).toInt)
        if (cpLength == 2) {
          caseFlags(codeUnitIndex + 1) = false
        }
      }
      if (cpLength == 1) {
        /* BMP, insert one code unit */
        dest.insert(codeUnitIndex, n.toChar)
      } else {
        /* supplementary character, insert two code units */
        dest.insert(codeUnitIndex, UTF16.getLeadSurrogate(n))
        dest.insert(codeUnitIndex + 1, UTF16.getTrailSurrogate(n))
      }
      i += 1
    }
    dest
  }
}
