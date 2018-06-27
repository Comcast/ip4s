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

import scala.language.higherKinds

/**
  * A multicast join of the specified type and a port number. Used to describe UDP join of a multicast group.
  */
final case class MulticastSocketAddress[J[+x <: IpAddress] <: MulticastJoin[x], +A <: IpAddress](join: J[A],
                                                                                                 port: Port) {

  /** Narrows join to an `AnySourceMulticastJoin`. */
  def asAsm: Option[MulticastSocketAddress[AnySourceMulticastJoin, A]] =
    join.asAsm.map(j => MulticastSocketAddress(j, port))

  /** Narrows join to a `SourceSpecificMulticastJoin`. */
  def asSsm: Option[MulticastSocketAddress[SourceSpecificMulticastJoin, A]] =
    join.asSsm.map(j => MulticastSocketAddress(j, port))

  override def toString: String = {
    val (source, group) = join.sourceAndGroup
    group.address match {
      case _: Ipv4Address => s"$join:$port"
      case _: Ipv6Address =>
        source match {
          case None      => s"[${group.address}]:$port"
          case Some(src) => s"[$src]@[${group.address}]:$port"
        }
    }
  }
}

object MulticastSocketAddress {
  def fromString(value: String): Option[MulticastSocketAddress[MulticastJoin, IpAddress]] =
    fromString4(value) orElse fromString6(value)

  private val V4Pattern = """(?:([^@]+)@)?([^:]+):(\d+)""".r
  def fromString4(value: String): Option[MulticastSocketAddress[MulticastJoin, Ipv4Address]] =
    fromStringGeneric(value, V4Pattern, Ipv4Address(_))

  private val V6Pattern = """(?:\[([^\]]+)\]@)?\[([^\]]+)\]:(\d+)""".r
  def fromString6(value: String): Option[MulticastSocketAddress[MulticastJoin, Ipv6Address]] =
    fromStringGeneric(value, V6Pattern, Ipv6Address(_))

  def anySourceFromString(value: String): Option[MulticastSocketAddress[AnySourceMulticastJoin, IpAddress]] =
    anySourceFromString4(value) orElse anySourceFromString6(value)
  def anySourceFromString6(value: String): Option[MulticastSocketAddress[AnySourceMulticastJoin, Ipv6Address]] =
    fromString6(value).flatMap(_.asAsm)
  def anySourceFromString4(value: String): Option[MulticastSocketAddress[AnySourceMulticastJoin, Ipv4Address]] =
    fromString4(value).flatMap(_.asAsm)

  def sourceSpecificFromString(value: String): Option[MulticastSocketAddress[SourceSpecificMulticastJoin, IpAddress]] =
    sourceSpecificFromString4(value) orElse sourceSpecificFromString6(value)
  def sourceSpecificFromString6(
      value: String): Option[MulticastSocketAddress[SourceSpecificMulticastJoin, Ipv6Address]] =
    fromString6(value).flatMap(_.asSsm)
  def sourceSpecificFromString4(
      value: String): Option[MulticastSocketAddress[SourceSpecificMulticastJoin, Ipv4Address]] =
    fromString4(value).flatMap(_.asSsm)

  private def fromStringGeneric[A <: IpAddress](
      value: String,
      pattern: util.matching.Regex,
      parse: String => Option[A]): Option[MulticastSocketAddress[MulticastJoin, A]] = {
    val Pattern = pattern
    value match {
      case Pattern(sourceStr, groupStr, portStr) =>
        Option(sourceStr) match {
          case Some(sourceStr) =>
            for {
              source <- parse(sourceStr)
              group <- parse(groupStr).flatMap(_.asSourceSpecificMulticast)
              port <- Port.fromString(portStr)
            } yield MulticastSocketAddress(MulticastJoin.ssm(source, group), port)
          case None =>
            for {
              group <- parse(groupStr).flatMap(_.asMulticast)
              port <- Port.fromString(portStr)
            } yield MulticastSocketAddress(MulticastJoin.asm(group), port)
        }
      case _ => None
    }
  }

  implicit def ordering[J[+x <: IpAddress] <: MulticastJoin[x], A <: IpAddress]
    : Ordering[MulticastSocketAddress[J, A]] = Ordering.by(x => (x.join, x.port))
}
