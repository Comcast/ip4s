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

/** Macros that support literal string interpolators. */
object Literals {

  object ip extends Literally[IpAddress] {
    def validate(c: Context)(s: String) = {
      import c.universe._
      IpAddress.fromString(s) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.IpAddress.fromString($s).get"))
        case None    => Left("invalid IP address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[IpAddress] = apply(c)(args: _*)
  }

  object ipv4 extends Literally[Ipv4Address] {
    def validate(c: Context)(s: String) = {
      import c.universe._
      Ipv4Address.fromString(s) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.Ipv4Address.fromString($s).get"))
        case None    => Left("invalid IPv4 address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Ipv4Address] = apply(c)(args: _*)
  }

  object ipv6 extends Literally[Ipv6Address] {
    def validate(c: Context)(s: String) = {
      import c.universe._
      Ipv6Address.fromString(s) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.Ipv6Address.fromString($s).get"))
        case None    => Left("invalid IPv6 address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Ipv6Address] = apply(c)(args: _*)
  }

  object mip extends Literally[Multicast[IpAddress]] {
    def validate(c: Context)(s: String) = {
      import c.universe._
      IpAddress.fromString(s).flatMap(_.asMulticast) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.IpAddress.fromString($s).get.asMulticast.get"))
        case None    => Left("invalid IP multicast address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Multicast[IpAddress]] = apply(c)(args: _*)
  }

  object mipv4 extends Literally[Multicast[Ipv4Address]] {
    def validate(c: Context)(s: String) = {
      import c.universe._
      Ipv4Address.fromString(s).flatMap(_.asMulticast) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.Ipv4Address.fromString($s).get.asMulticast.get"))
        case None    => Left("invalid IPv4 multicast address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Multicast[Ipv4Address]] = apply(c)(args: _*)
  }

  object mipv6 extends Literally[Multicast[Ipv6Address]] {
    def validate(c: Context)(s: String) = {
      import c.universe._
      Ipv6Address.fromString(s).flatMap(_.asMulticast) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.Ipv6Address.fromString($s).get.asMulticast.get"))
        case None    => Left("invalid IPv6 multicast address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Multicast[Ipv6Address]] = apply(c)(args: _*)
  }

  object ssmip extends Literally[SourceSpecificMulticast[IpAddress]] {
    def validate(c: Context)(s: String) = {
      import c.universe._
      IpAddress.fromString(s).flatMap(_.asSourceSpecificMulticastLenient) match {
        case Some(_) =>
          Right(c.Expr(q"_root_.com.comcast.ip4s.IpAddress.fromString($s).get.asSourceSpecificMulticastLenient.get"))
        case None => Left("invalid source specific IP multicast address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[SourceSpecificMulticast[IpAddress]] = apply(c)(args: _*)
  }

  object ssmipv4 extends Literally[SourceSpecificMulticast[Ipv4Address]] {
    def validate(c: Context)(s: String) = {
      import c.universe._
      Ipv4Address.fromString(s).flatMap(_.asSourceSpecificMulticastLenient) match {
        case Some(_) =>
          Right(c.Expr(q"_root_.com.comcast.ip4s.Ipv4Address.fromString($s).get.asSourceSpecificMulticastLenient.get"))
        case None => Left("invalid source specific IPv4 multicast address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[SourceSpecificMulticast[Ipv4Address]] = apply(c)(args: _*)
  }

  object ssmipv6 extends Literally[SourceSpecificMulticast[Ipv6Address]] {
    def validate(c: Context)(s: String) = {
      import c.universe._
      Ipv6Address.fromString(s).flatMap(_.asSourceSpecificMulticastLenient) match {
        case Some(_) =>
          Right(c.Expr(q"_root_.com.comcast.ip4s.Ipv6Address.fromString($s).get.asSourceSpecificMulticastLenient.get"))
        case None => Left("invalid source specific IPv6 multicast address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[SourceSpecificMulticast[Ipv6Address]] = apply(c)(args: _*)
  }

  object port extends Literally[Port] {
    def validate(c: Context)(s: String) = {
      import c.universe._
      scala.util.Try(s.toInt).toOption.flatMap(Port.fromInt) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.Port.fromInt($s.toInt).get"))
        case None    => Left("invalid port")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Port] = apply(c)(args: _*)
  }

  object hostname extends Literally[Hostname] {
    def validate(c: Context)(s: String) = {
      import c.universe._
      Hostname.fromString(s) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.Hostname.fromString($s).get"))
        case None    => Left("invalid hostname")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Hostname] = apply(c)(args: _*)
  }

  object idn extends Literally[IDN] {
    def validate(c: Context)(s: String) = {
      import c.universe._
      IDN.fromString(s) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.IDN.fromString($s).get"))
        case None    => Left("invalid IDN")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[IDN] = apply(c)(args: _*)
  }
}
