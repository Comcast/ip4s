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

package com.comcast

package object ip4s extends ip4splatform {
  final implicit class IpLiteralSyntax(val sc: StringContext) extends AnyVal {
    def ip(args: Any*): IpAddress = macro Literals.ip.make
    def ipv4(args: Any*): Ipv4Address =
      macro Literals.ipv4.make
    def ipv6(args: Any*): Ipv6Address =
      macro Literals.ipv6.make

    def mip(args: Any*): Multicast[IpAddress] =
      macro Literals.mip.make
    def mipv4(args: Any*): Multicast[Ipv4Address] =
      macro Literals.mipv4.make
    def mipv6(args: Any*): Multicast[Ipv6Address] =
      macro Literals.mipv6.make

    def ssmip(args: Any*): SourceSpecificMulticast[IpAddress] =
      macro Literals.ssmip.make
    def ssmipv4(args: Any*): SourceSpecificMulticast[Ipv4Address] =
      macro Literals.ssmipv4.make
    def ssmipv6(args: Any*): SourceSpecificMulticast[Ipv6Address] =
      macro Literals.ssmipv6.make

    def port(args: Any*): Port =
      macro Literals.port.make
    def host(args: Any*): Hostname =
      macro Literals.hostname.make
    def idn(args: Any*): IDN =
      macro Literals.idn.make
  }
}
