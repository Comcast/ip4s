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

/**
  * An IP address of the specified type and a port number. Used to describe the source or destination of a socket.
  */
final case class SocketAddress[+A <: IpAddress](ip: A, port: Port) extends SocketAddressPlatform[A] {
  override def toString: String = ip match {
    case _: Ipv4Address => s"$ip:$port"
    case _: Ipv6Address => s"[$ip]:$port"
  }
}

object SocketAddress {
  def fromString(value: String): Option[SocketAddress[IpAddress]] = fromString4(value) orElse fromString6(value)

  private val V4Pattern = """([^:]+):(\d+)""".r
  def fromString4(value: String): Option[SocketAddress[Ipv4Address]] =
    value match {
      case V4Pattern(ip, port) =>
        for {
          addr <- Ipv4Address(ip)
          prt <- Port.fromString(port)
        } yield SocketAddress(addr, prt)
      case _ => None
    }

  private val V6Pattern = """\[(.+)\]:(\d+)""".r
  def fromString6(value: String): Option[SocketAddress[Ipv6Address]] =
    value match {
      case V6Pattern(ip, port) =>
        for {
          addr <- Ipv6Address(ip)
          prt <- Port.fromString(port)
        } yield SocketAddress(addr, prt)
      case _ => None
    }

  implicit def order[A <: IpAddress]: Order[SocketAddress[A]] =
    Order.fromOrdering(SocketAddress.ordering[A])
  implicit def ordering[A <: IpAddress]: Ordering[SocketAddress[A]] = Ordering.by(x => (x.ip, x.port))
  implicit def show[A <: IpAddress]: Show[SocketAddress[A]] = Show.fromToString[SocketAddress[A]]
}
