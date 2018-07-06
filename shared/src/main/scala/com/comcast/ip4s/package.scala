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

import scala.language.experimental.macros

package object ip4s {
  final implicit class IpLiteralSyntax(val sc: StringContext) extends AnyVal {
    def ip(args: Any*): IpAddress = macro LiteralSyntaxMacros.ipInterpolator
    def ipv4(args: Any*): Ipv4Address =
      macro LiteralSyntaxMacros.ipv4Interpolator
    def ipv6(args: Any*): Ipv6Address =
      macro LiteralSyntaxMacros.ipv6Interpolator

    def mip(args: Any*): Multicast[IpAddress] =
      macro LiteralSyntaxMacros.mipInterpolator
    def mipv4(args: Any*): Multicast[Ipv4Address] =
      macro LiteralSyntaxMacros.mipv4Interpolator
    def mipv6(args: Any*): Multicast[Ipv6Address] =
      macro LiteralSyntaxMacros.mipv6Interpolator

    def ssmip(args: Any*): SourceSpecificMulticast[IpAddress] =
      macro LiteralSyntaxMacros.ssmipInterpolator
    def ssmipv4(args: Any*): SourceSpecificMulticast[Ipv4Address] =
      macro LiteralSyntaxMacros.ssmipv4Interpolator
    def ssmipv6(args: Any*): SourceSpecificMulticast[Ipv6Address] =
      macro LiteralSyntaxMacros.ssmipv6Interpolator

    def port(args: Any*): Port =
      macro LiteralSyntaxMacros.portInterpolator
    def host(args: Any*): Hostname =
      macro LiteralSyntaxMacros.hostnameInterpolator
    def idn(args: Any*): IDN =
      macro LiteralSyntaxMacros.idnInterpolator
  }
}
