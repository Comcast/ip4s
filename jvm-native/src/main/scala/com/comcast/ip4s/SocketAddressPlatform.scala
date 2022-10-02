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

import cats.Applicative

import java.net.InetSocketAddress

private[ip4s] trait SocketAddressPlatform[+A <: Host] {
  val host: A
  val port: Port

  def toInetSocketAddress(implicit ev: A <:< IpAddress): InetSocketAddress =
    new InetSocketAddress(host.toInetAddress, port.value)
}

private[ip4s] trait SocketAddressCompanionPlatform {
  @deprecated("resolve is now defined on SocketAddress", "3.2.1")
  implicit class ResolveOps(private val self: SocketAddress[Host]) {

    /** Resolves this `SocketAddress[Hostname]` to a `SocketAddress[IpAddress]`. */
    def resolve[F[_]: Dns: Applicative]: F[SocketAddress[IpAddress]] = self.resolve
  }

  /** Converts an `InetSocketAddress` to a `SocketAddress[IpAddress]`. */
  def fromInetSocketAddress(address: InetSocketAddress): SocketAddress[IpAddress] =
    SocketAddress(IpAddress.fromInetAddress(address.getAddress), Port.fromInt(address.getPort).get)
}
