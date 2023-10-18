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

import scala.util.Try
import scala.util.hashing.MurmurHash3

import cats.{Order, Show}
import com.comcast.ip4s.Cidr.Strict

/** Classless Inter-Domain Routing address, which represents an IP address and its routing prefix.
  *
  * @param address
  *   IP address for which this CIDR refers to
  * @param prefixBits
  *   number of leading 1s in the routing mask
  */
sealed class Cidr[+A <: IpAddress] protected (val address: A, val prefixBits: Int) extends Product with Serializable {
  def copy[AA >: A <: IpAddress](address: AA = this.address, prefixBits: Int = this.prefixBits): Cidr[AA] =
    Cidr[AA](address, prefixBits)

  /** Returns a normalized cidr range, where the address is truncated to the prefix, so that the returned range
    * is (and prints as) a spec-valid cidr range, with no bits outside the routing mask set.
    *
    * @return a normalized cidr range
    *
    * @example {{{
    * scala> Cidr(ipv4"10.11.12.13", 8).normalized
    * res0: Cidr.Strict[Ipv4Address] = 10.0.0.0/8
    * }}}
    */
  def normalized: Strict[A] = Cidr.Strict(this)

  /** Returns the routing mask.
    *
    * @example {{{
    * scala> Cidr(ipv4"10.11.12.13", 8).mask
    * res0: Ipv4Address = 255.0.0.0
    * scala> Cidr(ipv6"2001:db8:abcd:12::", 96).mask
    * res1: Ipv6Address = ffff:ffff:ffff:ffff:ffff:ffff::
    * }}}
    */
  def mask: A = address.transform(_ => Ipv4Address.mask(prefixBits), _ => Ipv6Address.mask(prefixBits))

  /** Returns the routing prefix.
    *
    * Note: the routing prefix also serves as the first address in the range described by this CIDR.
    *
    * @example {{{
    * scala> Cidr(ipv4"10.11.12.13", 8).prefix
    * res0: Ipv4Address = 10.0.0.0
    * scala> Cidr(ipv6"2001:db8:abcd:12::", 96).prefix
    * res1: Ipv6Address = 2001:db8:abcd:12::
    * scala> Cidr(ipv6"2001:db8:abcd:12::", 32).prefix
    * res2: Ipv6Address = 2001:db8::
    * }}}
    */
  def prefix: A =
    address.transform(_.masked(Ipv4Address.mask(prefixBits)), _.masked(Ipv6Address.mask(prefixBits)))

  /** Returns the last address in the range described by this CIDR.
    *
    * @example {{{
    * scala> Cidr(ipv4"10.11.12.13", 8).last
    * res0: Ipv4Address = 10.255.255.255
    * scala> Cidr(ipv6"2001:db8:abcd:12::", 96).last
    * res1: Ipv6Address = 2001:db8:abcd:12::ffff:ffff
    * scala> Cidr(ipv6"2001:db8:abcd:12::", 32).last
    * res2: Ipv6Address = 2001:db8:ffff:ffff:ffff:ffff:ffff:ffff
    * }}}
    */
  def last: A =
    address.transform(_.maskedLast(Ipv4Address.mask(prefixBits)), _.maskedLast(Ipv6Address.mask(prefixBits)))

  /** Returns the number of addresses in the range described by this CIDR.
    */
  def totalIps: BigInt = {
    BigInt(1) << (address.fold(_ => 32, _ => 128) - prefixBits)
  }

  /** Returns a predicate which tests if the supplied address is in the range described by this CIDR.
    *
    * @example {{{
    * scala> Cidr(ipv4"10.11.12.13", 8).contains(ipv4"10.100.100.100")
    * res0: Boolean = true
    * scala> Cidr(ipv4"10.11.12.13", 8).contains(ipv4"11.100.100.100")
    * res1: Boolean = false
    * scala> val x = Cidr(ipv6"2001:db8:abcd:12::", 96).contains
    * scala> x(ipv6"2001:db8:abcd:12::5")
    * res2: Boolean = true
    * scala> x(ipv6"2001:db8::")
    * res3: Boolean = false
    * }}}
    */
  def contains[AA >: A <: IpAddress]: AA => Boolean = {
    val start = prefix
    val end = last
    a => a >= start && a <= end
  }

  override def toString: String = s"$address/$prefixBits"
  override def hashCode: Int = MurmurHash3.productHash(this, productPrefix.hashCode)
  override def equals(other: Any): Boolean =
    other match {
      case that: Cidr[_] => address == that.address && prefixBits == that.prefixBits
      case _             => false
    }
  override def canEqual(other: Any): Boolean = other.isInstanceOf[Cidr[_]]
  override def productArity: Int = 2
  override def productElement(n: Int): Any =
    n match {
      case 0 => address
      case 1 => prefixBits
      case _ => throw new IndexOutOfBoundsException
    }
}

object Cidr {

  /** A normalized cidr range, of which the address is identical to the prefix.
    *
    * This means the address will never have any bits set outside the prefix. For example, a range
    * such as 192.168.0.1/31 is not allowed.
    */
  final class Strict[+A <: IpAddress] private (override val prefix: A, override val prefixBits: Int)
      extends Cidr[A](prefix, prefixBits) {
    override def normalized: this.type = this
  }

  object Strict {
    def apply[A <: IpAddress](cidr: Cidr[A]): Cidr.Strict[A] = cidr match {
      case already: Strict[_] => already.asInstanceOf[Strict[A]]
      case _                  => new Cidr.Strict(cidr.prefix, cidr.prefixBits)
    }
  }

  /** Constructs a CIDR from the supplied IP address and prefix bit count. Note if `prefixBits` is less than 0, the
    * built `Cidr` will have `prefixBits` set to 0. Similarly, if `prefixBits` is greater than the bit length of the
    * address, it will be set to the bit length of the address.
    */
  def apply[A <: IpAddress](address: A, prefixBits: Int): Cidr[A] = {
    val maxPrefixBits = address.fold(_ => 32, _ => 128)
    val b = if (prefixBits < 0) 0 else if (prefixBits > maxPrefixBits) maxPrefixBits else prefixBits
    new Cidr(address, b)
  }

  /** Constructs a CIDR from the supplied IP address and netmask. The number of leading 1 bits in the netmask are used
    * as the prefix bits for the CIDR.
    */
  def fromIpAndMask[A <: IpAddress](address: A, mask: A): Cidr[A] =
    address / mask.prefixBits

  /** Constructs a CIDR from a string of the form `ip/prefixBits`. */
  def fromString(value: String): Option[Cidr[IpAddress]] = fromStringGeneral(value, IpAddress.fromString)

  /** Constructs a CIDR from a string of the form `ipv4/prefixBits`. */
  def fromString4(value: String): Option[Cidr[Ipv4Address]] = fromStringGeneral(value, Ipv4Address.fromString)

  /** Constructs a CIDR from a string of the form `ipv6/prefixBits`. */
  def fromString6(value: String): Option[Cidr[Ipv6Address]] = fromStringGeneral(value, Ipv6Address.fromString)

  private val CidrPattern = """([^/]+)/(\d+)""".r
  private def fromStringGeneral[A <: IpAddress](value: String, parseAddress: String => Option[A]): Option[Cidr[A]] =
    value match {
      case CidrPattern(addrStr, prefixBitsStr) =>
        for {
          addr <- parseAddress(addrStr)
          maxPrefixBits = addr.fold(_ => 32, _ => 128)
          prefixBits <- Try(prefixBitsStr.toInt).toOption if prefixBits >= 0 && prefixBits <= maxPrefixBits
        } yield Cidr(addr, prefixBits)
      case _ => None
    }

  implicit def order[A <: IpAddress]: Order[Cidr[A]] = Order.fromOrdering(Cidr.ordering[A])
  implicit def ordering[A <: IpAddress]: Ordering[Cidr[A]] = Ordering.by(x => (x.address, x.prefixBits))
  implicit def show[A <: IpAddress]: Show[Cidr[A]] = Show.fromToString[Cidr[A]]

  def unapply[A <: IpAddress](c: Cidr[A]): Option[(A, Int)] = Some((c.address, c.prefixBits))
}
