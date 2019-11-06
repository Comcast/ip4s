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

/**
  * Represents a join of a multicast group.
  *
  * This is represented as an ADT consisting of two constructors, [[AnySourceMulticastJoin]] and
  * [[SourceSpecificMulticastJoin]]. These constructors are provided as top level types to allow
  * domain modeling where a specific join type is required. The address type is parameterized for
  * a similar reason -- to allow domain modeling where a specific address type is required.
  */
sealed abstract class MulticastJoin[+A <: IpAddress] extends Product with Serializable {
  /** Converts this join to a value of type `A` using the supplied functions. */
  def fold[B](asm: AnySourceMulticastJoin[A] => B, ssm: SourceSpecificMulticastJoin[A] => B): B =
    this match {
      case a: AnySourceMulticastJoin[A]      => asm(a)
      case a: SourceSpecificMulticastJoin[A] => ssm(a)
    }

  /** Narrows to an `AnySourceMulticastJoin`. */
  def asAsm: Option[AnySourceMulticastJoin[A]] = fold(Some(_), _ => None)

  /** Narrows to a `SourceSpecificMulticastJoin`. */
  def asSsm: Option[SourceSpecificMulticastJoin[A]] = fold(_ => None, Some(_))

  /**
    * Returns the source address and group address. If this join is an any-source join, `None` is
    * returned for the source. Otherwise, this join is a source specific join and `Some(src)` is
    * returned for the source.
    */
  def sourceAndGroup: (Option[A], Multicast[A]) =
    fold(asm => (None, asm.group), ssm => (Some(ssm.source), ssm.group))

  override def toString: String =
    fold(asm => asm.group.toString, ssm => s"${ssm.source}@${ssm.group}")
}

object MulticastJoin {
  /** Constructs an `AnySourceMulticastJoin[A]`. */
  def asm[A <: IpAddress](group: Multicast[A]): MulticastJoin[A] =
    AnySourceMulticastJoin(group)

  /** Constructs a `SourceSpecificMulticastJoin[A]`. */
  def ssm[A <: IpAddress](source: A, group: SourceSpecificMulticast[A]): MulticastJoin[A] =
    SourceSpecificMulticastJoin(source, group)

  def fromString(value: String): Option[MulticastJoin[IpAddress]] =
    fromStringGeneric(value, IpAddress(_))

  def fromString4(value: String): Option[MulticastJoin[Ipv4Address]] =
    fromStringGeneric(value, Ipv4Address(_))

  def fromString6(value: String): Option[MulticastJoin[Ipv6Address]] =
    fromStringGeneric(value, Ipv6Address(_))

  private val Pattern = """(?:([^@]+)@)?(.+)""".r
  private[ip4s] def fromStringGeneric[A <: IpAddress](
      value: String,
      parse: String => Option[A]
  ): Option[MulticastJoin[A]] =
    value match {
      case Pattern(sourceStr, groupStr) =>
        Option(sourceStr) match {
          case Some(sourceStr) =>
            for {
              source <- parse(sourceStr)
              group <- parse(groupStr).flatMap(_.asSourceSpecificMulticast)
            } yield ssm(source, group)
          case None =>
            for {
              group <- parse(groupStr).flatMap(_.asSourceSpecificMulticast)
            } yield asm(group)
        }
      case _ => None
    }

  implicit def ordering[J[x <: IpAddress] <: MulticastJoin[x], A <: IpAddress]: Ordering[J[A]] =
    Ordering.by(_.sourceAndGroup)
}

/** Multicast join to a group without a source filter. */
final case class AnySourceMulticastJoin[+A <: IpAddress](group: Multicast[A]) extends MulticastJoin[A]

object AnySourceMulticastJoin {
  def fromString(value: String): Option[AnySourceMulticastJoin[IpAddress]] =
    MulticastJoin.fromStringGeneric(value, IpAddress(_)).flatMap(_.asAsm)

  def fromString4(value: String): Option[AnySourceMulticastJoin[Ipv4Address]] =
    MulticastJoin.fromStringGeneric(value, Ipv4Address(_)).flatMap(_.asAsm)

  def fromString6(value: String): Option[AnySourceMulticastJoin[Ipv6Address]] =
    MulticastJoin.fromStringGeneric(value, Ipv6Address(_)).flatMap(_.asAsm)
}

/** Multicast join to a group from the specified source. */
final case class SourceSpecificMulticastJoin[+A <: IpAddress](source: A, group: SourceSpecificMulticast[A])
    extends MulticastJoin[A]

object SourceSpecificMulticastJoin {
  def fromString(value: String): Option[SourceSpecificMulticastJoin[IpAddress]] =
    MulticastJoin.fromStringGeneric(value, IpAddress(_)).flatMap(_.asSsm)

  def fromString4(value: String): Option[SourceSpecificMulticastJoin[Ipv4Address]] =
    MulticastJoin.fromStringGeneric(value, Ipv4Address(_)).flatMap(_.asSsm)

  def fromString6(value: String): Option[SourceSpecificMulticastJoin[Ipv6Address]] =
    MulticastJoin.fromStringGeneric(value, Ipv6Address(_)).flatMap(_.asSsm)
}
