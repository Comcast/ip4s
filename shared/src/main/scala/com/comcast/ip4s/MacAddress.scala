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

import scala.util.control.NonFatal
import cats.{Order, Show}

/** 6-byte MAC address. */
final class MacAddress private (private val bytes: Array[Byte]) extends Ordered[MacAddress] with Serializable {
  def toBytes: Array[Byte] = bytes.clone

  def toLong: Long = {
    val bs = bytes
    var result = 0L
    for (i <- 0 until bs.size) {
      result = (result << 8) | (0x0ff & bs(i))
    }
    result
  }

  override def compare(that: MacAddress): Int = {
    var i, result = 0
    val tb = that.bytes
    val sz = bytes.length
    while (i < sz && result == 0) {
      result = Integer.compare(bytes(i) & 0xff, tb(i) & 0xff)
      i += 1
    }
    result
  }

  override def equals(other: Any): Boolean = other match {
    case that: MacAddress => java.util.Arrays.equals(bytes, that.bytes)
    case _                => false
  }

  override def hashCode: Int = java.util.Arrays.hashCode(bytes)

  override def toString: String =
    bytes.map(b => f"${0xff & b}%02x").mkString("", ":", "")
}

object MacAddress {

  /** Constructs a `MacAddress` from a 6-element byte array. Returns `Some` when array is exactly 6-bytes and `None`
    * otherwise.
    */
  def fromBytes(bytes: Array[Byte]): Option[MacAddress] = {
    if (bytes.length == 6) Some(new MacAddress(bytes))
    else None
  }

  /** Constructs a `MacAddress` from the specified 6 bytes.
    *
    * Each byte is represented as an `Int` to avoid having to manually call `.toByte` on each value -- the `toByte` call
    * is done inside this function.
    */
  def fromBytes(
      b0: Int,
      b1: Int,
      b2: Int,
      b3: Int,
      b4: Int,
      b5: Int
  ): MacAddress = {
    val bytes = new Array[Byte](6)
    bytes(0) = b0.toByte
    bytes(1) = b1.toByte
    bytes(2) = b2.toByte
    bytes(3) = b3.toByte
    bytes(4) = b4.toByte
    bytes(5) = b5.toByte
    new MacAddress(bytes)
  }

  /** Constructs a `MacAddress` from a `Long`, using the lower 48-bits. */
  def fromLong(value: Long): MacAddress = {
    val bytes = new Array[Byte](6)
    var rem = value
    for (i <- 5 to 0 by -1) {
      bytes(i) = (rem & 0x0ff).toByte
      rem = rem >> 8
    }
    new MacAddress(bytes)
  }

  /** Parses a `MacAddress` from a string, returning `None` if the string is not a valid mac. */
  def fromString(value: String): Option[MacAddress] = {
    val trimmed = value.trim
    val fields = trimmed.split(':')
    if (fields.length == 6) {
      val result = new Array[Byte](6)
      var i = 0
      while (i < result.length) {
        val field = fields(i)
        if (field.size == 2) {
          try {
            result(i) = (0xff & Integer.parseInt(field, 16)).toByte
            i += 1
          } catch {
            case NonFatal(_) => return None
          }
        } else return None
      }
      Some(new MacAddress(result))
    } else None
  }

  implicit val order: Order[MacAddress] = Order.fromComparable[MacAddress]
  implicit val show: Show[MacAddress] = Show.fromToString[MacAddress]
}
