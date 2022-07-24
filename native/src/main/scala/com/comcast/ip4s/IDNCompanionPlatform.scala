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

package com.comcast.ip4s

import java.nio.charset.StandardCharsets
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.util.Try

private[ip4s] trait IDNCompanionPlatform {
  private[ip4s] def toAscii(value: String): Option[String] = Zone { implicit z =>
    val src = toCWideStringUTF16LE(value)
    val dest = stackalloc[uidna.UChar](MaxLength)
    val status = stackalloc[uidna.UErrorCode]()
    val destLength = uidna.uidna_IDNToASCII(
      src,
      -1,
      dest,
      MaxLength,
      0,
      null,
      status
    )
    if (!status == 0) {
      !(dest + destLength) = 0.toUShort
      Some(fromCWideString(dest, StandardCharsets.UTF_16LE))
    } else None
  }

  private[ip4s] def toUnicode(value: String): String = Zone { implicit z =>
    val src = toCWideStringUTF16LE(value)
    val dest = stackalloc[uidna.UChar](MaxLength)
    val destLength = uidna.uidna_IDNToUnicode(
      src,
      -1,
      dest,
      MaxLength,
      0,
      null,
      null
    )
    !(dest + destLength) = 0.toUShort
    fromCWideString(dest, StandardCharsets.UTF_16LE)
  }

  private final val MaxLength = 256
}

@link("icuuc")
@extern
private object uidna {

  type UChar = CChar16
  type UParseError = Ptr[Byte]
  type UErrorCode = CInt

  @name("ip4s_uidna_IDNToUnicode")
  def uidna_IDNToASCII(
      src: Ptr[UChar],
      srcLength: CInt,
      dest: Ptr[UChar],
      destCapacity: CInt,
      options: CInt,
      parseError: Ptr[UParseError],
      status: Ptr[UErrorCode]
  ): CInt = extern

  @name("ip4s_uidna_IDNToUnicode")
  def uidna_IDNToUnicode(
      src: Ptr[UChar],
      srcLength: CInt,
      dest: Ptr[UChar],
      destCapacity: CInt,
      options: CInt,
      parseError: Ptr[UParseError],
      status: Ptr[UErrorCode]
  ): CInt = extern

}
