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

import cats.{Order, Show}

import scala.util.hashing.MurmurHash3

/** ADT representing either an `IpAddress`, `Hostname`, or `IDN`. */
sealed trait Host extends HostPlatform with Ordered[Host] {

  def compare(that: Host): Int =
    this match {
      case x: Ipv4Address =>
        that match {
          case y: Ipv4Address => IpAddress.compareBytes(x, y)
          case y: Ipv6Address => IpAddress.compareBytes(x.toCompatV6, y)
          case _              => -1
        }
      case x: Ipv6Address =>
        that match {
          case y: Ipv4Address => IpAddress.compareBytes(x, y.toCompatV6)
          case y: Ipv6Address => IpAddress.compareBytes(x, y)
          case _              => -1
        }
      case x: Hostname =>
        that match {
          case _: Ipv4Address => 1
          case _: Ipv6Address => 1
          case y: Hostname    => x.toString.compare(y.toString)
          case y: IDN         => x.toString.compare(y.hostname.toString)
        }
      case x: IDN =>
        that match {
          case _: Ipv4Address => 1
          case _: Ipv6Address => 1
          case y: Hostname    => x.hostname.toString.compare(y.toString)
          case y: IDN         => x.hostname.toString.compare(y.hostname.toString)
        }
    }
}

object Host {
  def fromString(string: String): Option[Host] =
    IpAddress.fromString(string) orElse Hostname.fromString(string) orElse IDN.fromString(string)

  implicit def show: Show[Host] = Show.fromToString[Host]
  implicit def order: Order[Host] = Order.fromComparable[Host]
  implicit def ordering: Ordering[Host] = _.compare(_)
}

/** RFC1123 compliant hostname.
  *
  * A hostname contains one or more labels, where each label consists of letters A-Z, a-z, digits 0-9, or a dash.
  * A label may not start or end in a dash and may not exceed 63 characters in length. Labels are separated by
  * periods and the overall hostname must not exceed 253 characters in length.
  */
final class Hostname private (val labels: List[Hostname.Label], override val toString: String) extends Host {

  /** Converts this hostname to lower case. */
  def normalized: Hostname =
    new Hostname(labels.map(l => new Hostname.Label(l.toString.toLowerCase)), toString.toLowerCase)

  override def hashCode: Int = MurmurHash3.stringHash(toString, "Hostname".hashCode)
  override def equals(other: Any): Boolean =
    other match {
      case that: Hostname => toString == that.toString
      case _              => false
    }
}

object Hostname {

  /** Label component of a hostname.
    *
    * A label consists of letters A-Z, a-z, digits 0-9, or a dash. A label may not start or end in a
    * dash and may not exceed 63 characters in length.
    */
  final class Label private[Hostname] (override val toString: String) extends Serializable with Ordered[Label] {
    def compare(that: Label): Int = toString.compare(that.toString)
    override def hashCode: Int = MurmurHash3.stringHash(toString, "Label".hashCode)
    override def equals(other: Any): Boolean =
      other match {
        case that: Label => toString == that.toString
        case _           => false
      }
  }

  private val Pattern =
    """[a-zA-Z0-9](?:[a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?)*""".r

  /** Constructs a `Hostname` from a string. */
  def fromString(value: String): Option[Hostname] =
    value.size match {
      case 0            => None
      case i if i > 253 => None
      case _ =>
        value match {
          case Pattern(_*) =>
            val labels = value
              .split('.')
              .iterator
              .map(new Label(_))
              .toList
            if (labels.isEmpty) None else Option(new Hostname(labels, value))
          case _ => None
        }
    }
}

sealed trait IpVersion
object IpVersion {
  case object V4 extends IpVersion
  case object V6 extends IpVersion
}

/** Immutable and safe representation of an IP address, either V4 or V6.
  *
  * === Construction ===
  *
  * `IpAddress` instances are constructed in a few different ways:
  * - via `IpAddress("127.0.0.1")`, which parses a string representation of the IP and returns an `Option[IpAddress]`
  * - via `IpAddress.fromBytes(arr)`, which returns an IP address if the supplied array is either exactly 4 bytes or exactly 16 bytes
  * - via literal syntax like `ip"127.0.0.1"`, which returns an `IpAddress` and fails to *compile* if the IP is invalid.
  *
  * === Type Hierarchy ===
  *
  * There are two subtypes of `IpAddress` -- [[Ipv4Address]] and [[Ipv6Address]]. Each of these subtypes have a richer
  * API than `IpAddress` and it is often useful to use those types directly, for example if your use case requires a V6 address.
  * It is safe to pattern match on `IpAddress` to access `Ipv4Address` or `Ipv6Address` directly, or alternatively, you can use [[fold]].
  *
  * === JVM Specific API ===
  *
  * If using `IpAddress` on the JVM, you can call `toInetAddress` to convert the address to a `java.net.InetAddress`, for use
  * with networking libraries. This method does not exist on the Scala.js version.
  */
sealed abstract class IpAddress extends IpAddressPlatform with Host with Serializable {
  protected val bytes: Array[Byte]

  /** Converts this address to a network order byte array of either 4 or 16 bytes. */
  def toBytes: Array[Byte] = bytes.clone

  /** Converts this address to a value of type `A` using the supplied functions. */
  def fold[A](v4: Ipv4Address => A, v6: Ipv6Address => A): A

  /** Maps a type-preserving function across this IP address. */
  def transform(v4: Ipv4Address => Ipv4Address, v6: Ipv6Address => Ipv6Address): this.type

  /** Returns true if this address is in the multicast range. */
  def isMulticast: Boolean

  /** Converts this address to a multicast address, as long as it is in the multicast address range. */
  def asMulticast: Option[Multicast[this.type]] = Multicast.fromIpAddress(this)

  /** Returns true if this address is in the source specific multicast range. */
  def isSourceSpecificMulticast: Boolean

  /** Converts this address to a source specific multicast address, as long as it is in the source specific multicast address range. */
  def asSourceSpecificMulticast: Option[SourceSpecificMulticast[this.type]] =
    SourceSpecificMulticast.fromIpAddress(this)

  /** Narrows this address to an Ipv4Address if that is the underlying type. */
  def asIpv4: Option[Ipv4Address] = collapseMappedV4.fold(Some(_), _ => None)

  /** Narrows this address to an Ipv6Address if that is the underlying type. */
  def asIpv6: Option[Ipv6Address] = fold(_ => None, Some(_))

  /** Returns the version of this address. */
  def version: IpVersion = fold(_ => IpVersion.V4, _ => IpVersion.V6)

  /** Returns true if this address is a V6 address containing a mapped V4 address. */
  def isMappedV4: Boolean = fold(_ => false, Ipv6Address.MappedV4Block.contains)

  /** If this address is an IPv4 mapped IPv6 address, converts to an IPv4 address, otherwise returns this. */
  def collapseMappedV4: IpAddress =
    fold(identity, v6 => if (v6.isMappedV4) IpAddress.fromBytes(v6.toBytes.takeRight(4)).get else v6)

  /** Constructs a [[Cidr]] address from this address. */
  def /(prefixBits: Int): Cidr[this.type] = Cidr(this, prefixBits)

  /** Gets the IP address after this address, with overflow from the maximum value to the minimum value. */
  def next: IpAddress

  /** Gets the IP address before this address, with underflow from minimum value to the maximum value. */
  def previous: IpAddress

  /** Converts this address to a string form that is compatible for use in a URI per RFC3986
    * (namely, IPv6 addresses are rendered in condensed form and surrounded by brackets).
    */
  def toUriString: String

  override def equals(other: Any): Boolean =
    other match {
      case that: IpAddress => java.util.Arrays.equals(bytes, that.bytes)
      case _               => false
    }

  override def hashCode: Int = java.util.Arrays.hashCode(bytes)
}

object IpAddress extends IpAddressCompanionPlatform {

  /** Parses an IP address from a string, either in dotted decimal notation or in RFC4291 notation. */
  def fromString(value: String): Option[IpAddress] =
    Ipv4Address.fromString(value) orElse Ipv6Address.fromString(value)

  /** Constructs an IP address from either a 4-element byte array or a 16-element byte array. Any other size array results in a `None`. */
  def fromBytes(bytes: Array[Byte]): Option[IpAddress] =
    Ipv4Address.fromBytes(bytes) orElse Ipv6Address.fromBytes(bytes)

  private[ip4s] def compareBytes(x: IpAddress, y: IpAddress): Int = {
    var i, result = 0
    val xb = x.bytes
    val yb = y.bytes
    val sz = xb.length
    while (i < sz && result == 0) {
      result = Integer.compare(xb(i) & 0xff, yb(i) & 0xff)
      i += 1
    }
    result
  }

  implicit def order[A <: IpAddress]: Order[A] = Order.fromOrdering(IpAddress.ordering[A])
  implicit def ordering[A <: IpAddress]: Ordering[A] = _.compare(_)
}

/** Representation of an IPv4 address that works on both the JVM and Scala.js. */
final class Ipv4Address private (protected val bytes: Array[Byte]) extends IpAddress with Ipv4AddressPlatform {
  override def fold[A](v4: Ipv4Address => A, v6: Ipv6Address => A): A = v4(this)

  override def transform(v4: Ipv4Address => Ipv4Address, v6: Ipv6Address => Ipv6Address): this.type =
    v4(this).asInstanceOf[this.type]

  /** Returns the dotted decimal representation of this address. */
  override def toString: String =
    s"${bytes(0) & 0xff}.${bytes(1) & 0xff}.${bytes(2) & 0xff}.${bytes(3) & 0xff}"

  override def toUriString: String = toString

  /** Gets the IPv4 address after this address, with overflow from `255.255.255.255` to `0.0.0.0`. */
  override def next: Ipv4Address = Ipv4Address.fromLong(toLong + 1)

  /** Gets the IPv4 address before this address, with underflow from `0.0.0.0` to `255.255.255.255`. */
  override def previous: Ipv4Address = Ipv4Address.fromLong(toLong - 1)

  /** Converts this address to a 32-bit unsigned integer. */
  def toLong: Long = {
    val bs = bytes
    var result = 0L
    for (i <- 0 until bs.size) {
      result = (result << 8) | (0x0ff & bs(i))
    }
    result
  }

  override def isMulticast: Boolean =
    this >= Ipv4Address.MulticastRangeStart && this <= Ipv4Address.MulticastRangeEnd

  override def isSourceSpecificMulticast: Boolean =
    this >= Ipv4Address.SourceSpecificMulticastRangeStart && this <= Ipv4Address.SourceSpecificMulticastRangeEnd

  /** Converts this V4 address to a compat V6 address, where the first 12 bytes are all zero
    * and the last 4 bytes contain the bytes of this V4 address.
    */
  def toCompatV6: Ipv6Address = {
    val compat = new Array[Byte](16)
    compat(12) = bytes(0)
    compat(13) = bytes(1)
    compat(14) = bytes(2)
    compat(15) = bytes(3)
    Ipv6Address.fromBytes(compat).get
  }

  /** Converts this V4 address to a mapped V6 address, where the first 10 bytes are all zero,
    * the next two bytes are `ff`, and the last 4 bytes contain the bytes of this V4 address.
    */
  def toMappedV6: Ipv6Address = {
    val mapped = new Array[Byte](16)
    mapped(10) = 255.toByte
    mapped(11) = 255.toByte
    mapped(12) = bytes(0)
    mapped(13) = bytes(1)
    mapped(14) = bytes(2)
    mapped(15) = bytes(3)
    Ipv6Address.fromBytes(mapped).get
  }

  /** Applies the supplied mask to this address.
    *
    * @example {{{
    * scala> ipv4"192.168.29.1".masked(ipv4"255.255.0.0")
    * res0: Ipv4Address = 192.168.0.0
    * }}}
    */
  def masked(mask: Ipv4Address): Ipv4Address =
    Ipv4Address.fromLong(toLong & mask.toLong)

  /** Computes the last address in the network identified by applying the supplied mask to this address.
    *
    * @example {{{
    * scala> ipv4"192.168.29.1".maskedLast(ipv4"255.255.0.0")
    * res0: Ipv4Address = 192.168.255.255
    * }}}
    */
  def maskedLast(mask: Ipv4Address): Ipv4Address =
    Ipv4Address.fromLong(toLong & mask.toLong | ~mask.toLong)
}

object Ipv4Address extends Ipv4AddressCompanionPlatform {

  /** First IP address in the IPv4 multicast range. */
  val MulticastRangeStart: Ipv4Address = fromBytes(224, 0, 0, 0)

  /** Last IP address in the IPv4 multicast range. */
  val MulticastRangeEnd: Ipv4Address = fromBytes(239, 255, 255, 255)

  /** First IP address in the IPv4 source specific multicast range. */
  val SourceSpecificMulticastRangeStart: Ipv4Address = fromBytes(232, 0, 0, 0)

  /** Last IP address in the IPv4 source specific multicast range. */
  val SourceSpecificMulticastRangeEnd: Ipv4Address =
    fromBytes(232, 255, 255, 255)

  /** Parses an IPv4 address from a dotted-decimal string, returning `None` if the string is not a valid IPv4 address. */
  def fromString(value: String): Option[Ipv4Address] = {
    val trimmed = value.trim
    val fields = trimmed.split('.')
    if (fields.length == 4) {
      val bytes = new Array[Byte](4)
      var idx = 0
      var result: Option[Ipv4Address] = null
      while (idx < bytes.length && (result eq null)) {
        try {
          val value = fields(idx).toInt
          if (value >= 0 && value < 256) bytes(idx) = value.toByte
          else result = None
          idx += 1
        } catch {
          case _: NumberFormatException =>
            result = None
        }
      }
      if (result eq null) Some(unsafeFromBytes(bytes)) else result
    } else None
  }

  /** Constructs an IPv4 address from a 4-element byte array.
    * Returns `Some` when array is exactly 4-bytes and `None` otherwise.
    */
  def fromBytes(bytes: Array[Byte]): Option[Ipv4Address] =
    if (bytes.size == 4) Some(unsafeFromBytes(bytes.clone))
    else None

  private def unsafeFromBytes(bytes: Array[Byte]): Ipv4Address =
    new Ipv4Address(bytes)

  /** Constructs an address from the specified 4 bytes.
    *
    * Each byte is represented as an `Int` to avoid having to manually call `.toByte` on each value --
    * the `toByte` call is done inside this function.
    */
  def fromBytes(a: Int, b: Int, c: Int, d: Int): Ipv4Address = {
    val bytes = new Array[Byte](4)
    bytes(0) = a.toByte
    bytes(1) = b.toByte
    bytes(2) = c.toByte
    bytes(3) = d.toByte
    unsafeFromBytes(bytes)
  }

  /** Constructs an IPv4 address from a `Long`, using the lower 32-bits. */
  def fromLong(value: Long): Ipv4Address = {
    val bytes = new Array[Byte](4)
    var rem = value
    for (i <- 3 to 0 by -1) {
      bytes(i) = (rem & 0x0ff).toByte
      rem = rem >> 8
    }
    unsafeFromBytes(bytes)
  }

  /** Computes a mask by setting the first / left-most `n` bits high.
    *
    * @example {{{
    * scala> Ipv4Address.mask(16)
    * res0: Ipv4Address = 255.255.0.0
    * }}}
    */
  def mask(bits: Int): Ipv4Address = {
    val b = if (bits < 0) 0 else if (bits > 32) 32 else bits
    Ipv4Address.fromLong(if (b == 32) -1L else ~(-1 >>> b).toLong)
  }
}

/** Representation of an IPv6 address that works on both the JVM and Scala.js. */
final class Ipv6Address private (protected val bytes: Array[Byte]) extends IpAddress with Ipv6AddressPlatform {
  override def fold[A](v4: Ipv4Address => A, v6: Ipv6Address => A): A = v6(this)
  override def transform(v4: Ipv4Address => Ipv4Address, v6: Ipv6Address => Ipv6Address): this.type =
    v6(this).asInstanceOf[this.type]

  /** Returns the condensed string representation of the array per RFC5952. */
  override def toString: String = {
    val fields: Array[Int] = new Array[Int](8)
    var condensing = false
    var condensedStart, maxCondensedStart = -1
    var condensedLength, maxCondensedLength = 0
    var idx = 0
    while (idx < 8) {
      val j = 2 * idx
      val field = ((0x0ff & bytes(j)) << 8) | (0x0ff & bytes(j + 1))
      fields(idx) = field
      if (field == 0) {
        if (!condensing) {
          condensing = true
          condensedStart = idx
          condensedLength = 0
        }
        condensedLength += 1
      } else {
        condensing = false
      }
      if (condensedLength > maxCondensedLength) {
        maxCondensedLength = condensedLength
        maxCondensedStart = condensedStart
      }
      idx += 1
    }
    if (maxCondensedLength == 1) maxCondensedStart = -1
    val str = new StringBuilder
    idx = 0
    while (idx < 8) {
      if (idx == maxCondensedStart) {
        str.append("::")
        idx += maxCondensedLength
      } else {
        val hextet = Integer.toString(fields(idx), 16)
        str.append(hextet)
        idx += 1
        if (idx < 8 && idx != maxCondensedStart) str.append(":")
      }
    }
    str.toString
  }

  /** Returns an uncondensed string representation of the array. */
  def toUncondensedString: String = {
    val str = new StringBuilder
    var idx = 0
    val bytes = toBytes
    while (idx < 16) {
      val field = ((bytes(idx) & 0xff) << 8) | (bytes(idx + 1) & 0xff)
      val hextet = f"$field%04x"
      str.append(hextet)
      idx += 2
      if (idx < 15) str.append(":")
    }
    str.toString
  }

  /** Converts this address to a string of form `x:x:x:x:x:x:a.b.c.d` where
    * each `x` represents 16-bits and `a.b.c.d` is IPv4 dotted decimal notation.
    * Consecutive 0 `x` fields are condensed with `::`.
    *
    * For example, the IPv4 address `127.0.0.1` can be converted to a compatible
    * IPv6 address via [[Ipv4Address#toCompatV6]], which is represented as the string
    * `::7f00:1` and the mixed string `::127.0.0.1`.
    *
    * Similarly, `127.0.0.1` can be converted to a mapped V6 address via [[Ipv4Address#toMappedV6]],
    * resulting in `::ffff:7f00:1` and the mixed string `::ffff:127.0.0.1`.
    *
    * This format is described in RFC4291 section 2.2.3.
    *
    * @example {{{
    * scala> ipv6"::7f00:1".toMixedString
    * res0: String = ::127.0.0.1
    * scala> ipv6"ff3b:1234::ffab:7f00:1".toMixedString
    * res0: String = ff3b:1234::ffab:127.0.0.1
    * }}}
    */
  def toMixedString: String = {
    val bytes = toBytes
    val v4 = Ipv4Address.fromBytes(bytes.slice(12, 16)).get
    bytes(12) = 0
    bytes(13) = 1
    bytes(14) = 0
    bytes(15) = 1
    val s = Ipv6Address.unsafeFromBytes(bytes).toString
    val prefix = s.slice(0, s.size - 3)
    prefix + v4.toString
  }

  override def toUriString: String = s"[$toString]"

  /** Gets the IPv6 address after this address, with overflow from `ffff:ffff:....:ffff` to `::`. */
  override def next: Ipv6Address = Ipv6Address.fromBigInt(toBigInt + 1)

  /** Gets the IPv6 address before this address, with underflow from `::` to `ffff:ffff:....:ffff`. */
  override def previous: Ipv6Address = Ipv6Address.fromBigInt(toBigInt - 1)

  /** Converts this address to a 128-bit unsigned integer. */
  def toBigInt: BigInt = {
    val bs = bytes
    var result = BigInt(0)
    for (i <- 0 until bs.size) {
      result = (result << 8) | (0x0ff & bs(i))
    }
    result
  }

  override def isMulticast: Boolean =
    this >= Ipv6Address.MulticastRangeStart && this <= Ipv6Address.MulticastRangeEnd

  override def isSourceSpecificMulticast: Boolean =
    this >= Ipv6Address.SourceSpecificMulticastRangeStart && this <= Ipv6Address.SourceSpecificMulticastRangeEnd

  /** Applies the supplied mask to this address.
    *
    * @example {{{
    * scala> ipv6"ff3b::1".masked(ipv6"fff0::")
    * res0: Ipv6Address = ff30::
    * }}}
    */
  def masked(mask: Ipv6Address): Ipv6Address =
    Ipv6Address.fromBigInt(toBigInt & mask.toBigInt)

  /** Computes the last address in the network identified by applying the supplied mask to this address.
    *
    * @example {{{
    * scala> ipv6"ff3b::1".maskedLast(ipv6"fff0::")
    * res0: Ipv6Address = ff3f:ffff:ffff:ffff:ffff:ffff:ffff:ffff
    * }}}
    */
  def maskedLast(mask: Ipv6Address): Ipv6Address =
    Ipv6Address.fromBigInt(toBigInt & mask.toBigInt | ~mask.toBigInt)
}

object Ipv6Address extends Ipv6AddressCompanionPlatform {

  /** First IP address in the IPv6 multicast range. */
  val MulticastRangeStart: Ipv6Address =
    fromBytes(255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

  /** Last IP address in the IPv6 multicast range. */
  val MulticastRangeEnd: Ipv6Address =
    fromBytes(255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255)

  /** First IP address in the IPv6 source specific multicast range. */
  val SourceSpecificMulticastRangeStart: Ipv6Address =
    fromBytes(255, 48, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

  /** Last IP address in the IPv6 source specific multicast range. */
  val SourceSpecificMulticastRangeEnd: Ipv6Address =
    fromBytes(255, 63, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255)

  /** CIDR which defines the mapped IPv4 address block (https://datatracker.ietf.org/doc/html/rfc4291#section-2.5.5.2). */
  val MappedV4Block: Cidr[Ipv6Address] =
    Cidr(Ipv6Address.fromBytes(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 255, 0, 0, 0, 0), 96)

  /** Parses an IPv6 address from a string in RFC4291 notation, returning `None` if the string is not a valid IPv6 address. */
  def fromString(value: String): Option[Ipv6Address] =
    fromNonMixedString(value) orElse fromMixedString(value)

  private def fromNonMixedString(value: String): Option[Ipv6Address] = {
    var prefix: List[Int] = Nil
    var beforeCondenser = true
    var suffix: List[Int] = Nil
    val trimmed = value.trim()
    val fields =
      if (trimmed.nonEmpty) trimmed.split(":") else Array.empty[String]
    var idx = 0
    var result: Option[Ipv6Address] = null
    while (idx < fields.size && (result eq null)) {
      val field = fields(idx)
      if (field.isEmpty) {
        if (beforeCondenser) {
          beforeCondenser = false
          if (idx + 1 < fields.size && fields(idx + 1).isEmpty) idx += 1
        } else {
          result = None
        }
      } else {
        try {
          if (field.size > 4) {
            result = None
          } else {
            val fieldValue = Integer.parseInt(field, 16)
            if (beforeCondenser) prefix = fieldValue :: prefix
            else suffix = fieldValue :: suffix
          }
        } catch {
          case _: NumberFormatException =>
            result = None
        }
      }
      idx += 1
    }
    if (result ne null) {
      result
    } else if (fields.isEmpty && (trimmed.isEmpty || trimmed == ":")) {
      None
    } else {
      val bytes = new Array[Byte](16)
      idx = 0
      val prefixSize = prefix.size
      var prefixIdx = prefixSize - 1
      while (prefixIdx >= 0) {
        val value = prefix(prefixIdx)
        bytes(idx) = (value >> 8).toByte
        bytes(idx + 1) = value.toByte
        prefixIdx -= 1
        idx += 2
      }
      val suffixSize = suffix.size
      val numCondensedZeroes = bytes.size - idx - (suffixSize * 2)
      idx += numCondensedZeroes
      var suffixIdx = suffixSize - 1
      while (suffixIdx >= 0) {
        val value = suffix(suffixIdx)
        bytes(idx) = (value >> 8).toByte
        bytes(idx + 1) = value.toByte
        suffixIdx -= 1
        idx += 2
      }
      Some(unsafeFromBytes(bytes))
    }
  }

  private val MixedStringFormat = """([:a-fA-F0-9]+:)(\d+\.\d+\.\d+\.\d+)""".r
  private def fromMixedString(value: String): Option[Ipv6Address] =
    value match {
      case MixedStringFormat(prefix, v4Str) =>
        for {
          pfx <- fromNonMixedString(prefix + "0:0")
          v4 <- Ipv4Address.fromString(v4Str)
        } yield {
          val bytes = pfx.toBytes
          val v4bytes = v4.toBytes
          bytes(12) = v4bytes(0)
          bytes(13) = v4bytes(1)
          bytes(14) = v4bytes(2)
          bytes(15) = v4bytes(3)
          unsafeFromBytes(bytes)
        }
      case _ => None
    }

  /** Constructs an IPv6 address from a 16-element byte array.
    * Returns `Some` when array is exactly 16-bytes and `None` otherwise.
    */
  def fromBytes(bytes: Array[Byte]): Option[Ipv6Address] =
    if (bytes.size == 16) Some(unsafeFromBytes(bytes.clone))
    else None

  private def unsafeFromBytes(bytes: Array[Byte]): Ipv6Address =
    new Ipv6Address(bytes)

  /** Constructs an address from the specified 16 bytes.
    *
    * Each byte is represented as an `Int` to avoid having to manually call `.toByte` on each value -- the `toByte` call is done inside this function.
    */
  def fromBytes(
      b0: Int,
      b1: Int,
      b2: Int,
      b3: Int,
      b4: Int,
      b5: Int,
      b6: Int,
      b7: Int,
      b8: Int,
      b9: Int,
      b10: Int,
      b11: Int,
      b12: Int,
      b13: Int,
      b14: Int,
      b15: Int
  ): Ipv6Address = {
    val bytes = new Array[Byte](16)
    bytes(0) = b0.toByte
    bytes(1) = b1.toByte
    bytes(2) = b2.toByte
    bytes(3) = b3.toByte
    bytes(4) = b4.toByte
    bytes(5) = b5.toByte
    bytes(6) = b6.toByte
    bytes(7) = b7.toByte
    bytes(8) = b8.toByte
    bytes(9) = b9.toByte
    bytes(10) = b10.toByte
    bytes(11) = b11.toByte
    bytes(12) = b12.toByte
    bytes(13) = b13.toByte
    bytes(14) = b14.toByte
    bytes(15) = b15.toByte
    unsafeFromBytes(bytes)
  }

  /** Constructs an IPv6 address from a `BigInt`, using the lower 128-bits. */
  def fromBigInt(value: BigInt): Ipv6Address = {
    val bytes = new Array[Byte](16)
    var rem = value
    for (i <- 15 to 0 by -1) {
      bytes(i) = (rem & 0x0ff).toByte
      rem = rem >> 8
    }
    unsafeFromBytes(bytes)
  }

  /** Computes a mask by setting the first / left-most `n` bits high.
    *
    * @example {{{
    * scala> Ipv6Address.mask(32)
    * res0: Ipv6Address = ffff:ffff::
    * }}}
    */
  def mask(bits: Int): Ipv6Address = {
    val b = if (bits < 0) 0 else if (bits > 128) 128 else bits
    Ipv6Address
      .fromBigInt {
        if (b == 128) (BigInt(-1L) << 64) | BigInt(-1L)
        else if (b < 64) BigInt(~(-1L >>> b)) << 64
        else (BigInt(-1L) << 64) | BigInt(~(-1L >>> (b - 64)))
      }
  }
}

/** Internationalized domain name, as specified by RFC3490 and RFC5891.
  *
  * This type models internationalized hostnames. An IDN provides unicode labels, a unicode string form,
  * and an ASCII hostname form.
  *
  * A well formed IDN consists of one or more labels separated by dots. Each label may contain unicode characters
  * as long as the converted ASCII form meets the requirements of RFC1123 (e.g., 63 or fewer characters and no
  * leading or trailing dash). A dot is represented as an ASCII period or one of the unicode dots: full stop,
  * ideographic full stop, fullwidth full stop, halfwidth ideographic full stop.
  *
  * The `toString` method returns the IDN in the form in which it was constructed. Sometimes it is useful to
  * normalize the IDN -- converting all dots to an ASCII period and converting all labels to lowercase.
  *
  * Note: equality and comparison of IDNs is case-sensitive. Consider comparing normalized toString values
  * for a more lenient notion of equality.
  */
final class IDN private (val labels: List[IDN.Label], val hostname: Hostname, override val toString: String)
    extends Host {

  /** Converts this IDN to lower case and replaces dots with ASCII periods. */
  def normalized: IDN = {
    val newLabels = labels.map(l => new IDN.Label(l.toString.toLowerCase))
    new IDN(newLabels, hostname.normalized, newLabels.toList.mkString("."))
  }

  override def hashCode: Int = MurmurHash3.stringHash(toString, "IDN".hashCode)
  override def equals(other: Any): Boolean =
    other match {
      case that: IDN => toString == that.toString
      case _         => false
    }
}

object IDN extends IDNCompanionPlatform {

  /** Label component of an IDN. */
  final class Label private[IDN] (override val toString: String) extends Serializable with Ordered[Label] {
    def compare(that: Label): Int = toString.compare(that.toString)
    override def hashCode: Int = MurmurHash3.stringHash(toString, "Label".hashCode)
    override def equals(other: Any): Boolean =
      other match {
        case that: Label => toString == that.toString
        case _           => false
      }
  }

  private val DotPattern = "[\\.\u002e\u3002\uff0e\uff61]"

  /** Constructs a `IDN` from a string. */
  def fromString(value: String): Option[IDN] =
    value.size match {
      case 0 => None
      case _ =>
        val labels = value
          .split(DotPattern)
          .iterator
          .map(new Label(_))
          .toList
        Option(labels).filterNot(_.isEmpty).flatMap { ls =>
          val hostname = toAscii(value).flatMap(Hostname.fromString)
          hostname.map(h => new IDN(ls, h, value))
        }
    }

  /** Converts the supplied (ASCII) hostname in to an IDN. */
  def fromHostname(hostname: Hostname): IDN = {
    val labels =
      hostname.labels.map(l => new Label(toUnicode(l.toString)))
    new IDN(labels, hostname, labels.toList.mkString("."))
  }

  implicit val show: Show[IDN] = Show.fromToString[IDN]
}
