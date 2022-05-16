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

import org.typelevel.literally.Literally

extension (inline ctx: StringContext)
  inline def ip(inline args: Any*): IpAddress =
    ${ Literals.ip('ctx, 'args) }

  inline def ipv4(inline args: Any*): Ipv4Address =
    ${ Literals.ipv4('ctx, 'args) }

  inline def ipv6(inline args: Any*): Ipv6Address =
    ${ Literals.ipv6('ctx, 'args) }

  inline def mip(inline args: Any*): Multicast[IpAddress] =
    ${ Literals.mip('ctx, 'args) }

  inline def mipv4(inline args: Any*): Multicast[Ipv4Address] =
    ${ Literals.mipv4('ctx, 'args) }

  inline def mipv6(inline args: Any*): Multicast[Ipv6Address] =
    ${ Literals.mipv6('ctx, 'args) }

  inline def ssmip(inline args: Any*): SourceSpecificMulticast[IpAddress] =
    ${ Literals.ssmip('ctx, 'args) }

  inline def ssmipv4(inline args: Any*): SourceSpecificMulticast[Ipv4Address] =
    ${ Literals.ssmipv4('ctx, 'args) }

  inline def ssmipv6(inline args: Any*): SourceSpecificMulticast[Ipv6Address] =
    ${ Literals.ssmipv6('ctx, 'args) }

  inline def port(inline args: Any*): Port =
    ${ Literals.port('ctx, 'args) }

  inline def host(inline args: Any*): Hostname =
    ${ Literals.host('ctx, 'args) }

  inline def idn(inline args: Any*): IDN =
    ${ Literals.idn('ctx, 'args) }

object Literals:

  object ip extends Literally[IpAddress]:
    def validate(s: String)(using Quotes) =
      IpAddress.fromString(s) match
        case Some(_) => Right('{ _root_.com.comcast.ip4s.IpAddress.fromString(${ Expr(s) }).get })
        case None    => Left("Invalid IP address")

  object ipv4 extends Literally[Ipv4Address]:
    def validate(s: String)(using Quotes) =
      Ipv4Address.fromString(s) match
        case Some(_) => Right('{ _root_.com.comcast.ip4s.Ipv4Address.fromString(${ Expr(s) }).get })
        case None    => Left("Invalid IPv4 address")

  object ipv6 extends Literally[Ipv6Address]:
    def validate(s: String)(using Quotes) =
      Ipv6Address.fromString(s) match
        case Some(_) => Right('{ _root_.com.comcast.ip4s.Ipv6Address.fromString(${ Expr(s) }).get })
        case None    => Left("Invalid IPv6 address")

  object mip extends Literally[Multicast[IpAddress]]:
    def validate(s: String)(using Quotes) =
      IpAddress.fromString(s).flatMap(_.asMulticast) match
        case Some(_) => Right('{ _root_.com.comcast.ip4s.IpAddress.fromString(${ Expr(s) }).get.asMulticast.get })
        case None    => Left("Invalid IP multicast address")

  object mipv4 extends Literally[Multicast[Ipv4Address]]:
    def validate(s: String)(using Quotes) =
      Ipv4Address.fromString(s).flatMap(_.asMulticast) match
        case Some(_) => Right('{ _root_.com.comcast.ip4s.Ipv4Address.fromString(${ Expr(s) }).get.asMulticast.get })
        case None    => Left("Invalid IPv4 multicast address")

  object mipv6 extends Literally[Multicast[Ipv6Address]]:
    def validate(s: String)(using Quotes) =
      Ipv6Address.fromString(s).flatMap(_.asMulticast) match
        case Some(_) => Right('{ _root_.com.comcast.ip4s.Ipv6Address.fromString(${ Expr(s) }).get.asMulticast.get })
        case None    => Left("Invalid IPv6 multicast address")

  object ssmip extends Literally[SourceSpecificMulticast[IpAddress]]:
    def validate(s: String)(using Quotes) =
      IpAddress.fromString(s).flatMap(_.asSourceSpecificMulticast) match
        case Some(_) =>
          Right('{
            _root_.com.comcast.ip4s.IpAddress.fromString(${ Expr(s) }).get.asSourceSpecificMulticast.get
          })
        case None => Left("Invalid source specific IP multicast address")

  object ssmipv4 extends Literally[SourceSpecificMulticast[Ipv4Address]]:
    def validate(s: String)(using Quotes) =
      Ipv4Address.fromString(s).flatMap(_.asSourceSpecificMulticast) match
        case Some(_) =>
          Right('{
            _root_.com.comcast.ip4s.Ipv4Address.fromString(${ Expr(s) }).get.asSourceSpecificMulticast.get
          })
        case None => Left("Invalid source specific IPv4 multicast address")

  object ssmipv6 extends Literally[SourceSpecificMulticast[Ipv6Address]]:
    def validate(s: String)(using Quotes) =
      Ipv6Address.fromString(s).flatMap(_.asSourceSpecificMulticast) match
        case Some(_) =>
          Right('{
            _root_.com.comcast.ip4s.Ipv6Address.fromString(${ Expr(s) }).get.asSourceSpecificMulticast.get
          })
        case None => Left("Invalid source specific IPv6 multicast address")

  object port extends Literally[Port]:
    def validate(s: String)(using Quotes) =
      s.toIntOption.flatMap(Port.fromInt) match
        case Some(_) => Right('{ _root_.com.comcast.ip4s.Port.fromInt(${ Expr(s.toInt) }).get })
        case None    => Left("Invalid port")

  object host extends Literally[Hostname]:
    def validate(s: String)(using Quotes) =
      Hostname.fromString(s) match
        case Some(_) => Right('{ _root_.com.comcast.ip4s.Hostname.fromString(${ Expr(s) }).get })
        case None    => Left("Invalid hostname")

  object idn extends Literally[IDN]:
    def validate(s: String)(using Quotes) =
      IDN.fromString(s) match
        case Some(_) => Right('{ _root_.com.comcast.ip4s.IDN.fromString(${ Expr(s) }).get })
        case None    => Left("Invalid IDN")
