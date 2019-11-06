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

import java.net.{InetAddress, Inet4Address, Inet6Address}

private[ip4s] trait IpAddressPlatform {
  /** Converts this address to a `java.net.InetAddress`. Note this method only exists on the JVM. */
  def toInetAddress: InetAddress
}

private[ip4s] trait Ipv4AddressPlatform extends IpAddressPlatform {
  protected val bytes: Array[Byte]

  override def toInetAddress: Inet4Address =
    InetAddress.getByAddress(bytes).asInstanceOf[Inet4Address]
}

private[ip4s] trait Ipv6AddressPlatform extends IpAddressPlatform {
  protected val bytes: Array[Byte]

  override def toInetAddress: Inet6Address =
    InetAddress.getByAddress(bytes).asInstanceOf[Inet6Address]
}
