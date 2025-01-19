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
    def validate(c: Context)(s: String): Either[String, c.Expr[IpAddress]] = {
      import c.universe._
      IpAddress.fromString(s) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.IpAddress.fromString($s).get"))
        case None    => Left("invalid IP address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[IpAddress] = apply(c)(args*)
  }

  object ipv4 extends Literally[Ipv4Address] {
    def validate(c: Context)(s: String): Either[String, c.Expr[Ipv4Address]] = {
      import c.universe._
      Ipv4Address.fromString(s) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.Ipv4Address.fromString($s).get"))
        case None    => Left("invalid IPv4 address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Ipv4Address] = apply(c)(args*)
  }

  object ipv6 extends Literally[Ipv6Address] {
    def validate(c: Context)(s: String): Either[String, c.Expr[Ipv6Address]] = {
      import c.universe._
      Ipv6Address.fromString(s) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.Ipv6Address.fromString($s).get"))
        case None    => Left("invalid IPv6 address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Ipv6Address] = apply(c)(args*)
  }

  object mip extends Literally[Multicast[IpAddress]] {
    def validate(c: Context)(s: String): Either[String, c.Expr[Multicast[IpAddress]]] = {
      import c.universe._
      IpAddress.fromString(s).flatMap(_.asMulticast) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.IpAddress.fromString($s).get.asMulticast.get"))
        case None    => Left("invalid IP multicast address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Multicast[IpAddress]] = apply(c)(args*)
  }

  object mipv4 extends Literally[Multicast[Ipv4Address]] {
    def validate(c: Context)(s: String): Either[String, c.Expr[Multicast[Ipv4Address]]] = {
      import c.universe._
      Ipv4Address.fromString(s).flatMap(_.asMulticast) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.Ipv4Address.fromString($s).get.asMulticast.get"))
        case None    => Left("invalid IPv4 multicast address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Multicast[Ipv4Address]] = apply(c)(args*)
  }

  object mipv6 extends Literally[Multicast[Ipv6Address]] {
    def validate(c: Context)(s: String): Either[String, c.Expr[Multicast[Ipv6Address]]] = {
      import c.universe._
      Ipv6Address.fromString(s).flatMap(_.asMulticast) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.Ipv6Address.fromString($s).get.asMulticast.get"))
        case None    => Left("invalid IPv6 multicast address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Multicast[Ipv6Address]] = apply(c)(args*)
  }

  object ssmip extends Literally[SourceSpecificMulticast.Strict[IpAddress]] {
    def validate(c: Context)(s: String): Either[String, c.Expr[SourceSpecificMulticast.Strict[IpAddress]]] = {
      import c.universe._
      IpAddress.fromString(s).flatMap(_.asSourceSpecificMulticast) match {
        case Some(_) =>
          Right(c.Expr(q"_root_.com.comcast.ip4s.IpAddress.fromString($s).get.asSourceSpecificMulticast.get"))
        case None => Left("invalid source specific IP multicast address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[SourceSpecificMulticast.Strict[IpAddress]] = apply(c)(args*)
  }

  object ssmipv4 extends Literally[SourceSpecificMulticast.Strict[Ipv4Address]] {
    def validate(c: Context)(s: String): Either[String, c.Expr[SourceSpecificMulticast.Strict[Ipv4Address]]] = {
      import c.universe._
      Ipv4Address.fromString(s).flatMap(_.asSourceSpecificMulticast) match {
        case Some(_) =>
          Right(c.Expr(q"_root_.com.comcast.ip4s.Ipv4Address.fromString($s).get.asSourceSpecificMulticast.get"))
        case None => Left("invalid source specific IPv4 multicast address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[SourceSpecificMulticast.Strict[Ipv4Address]] = apply(c)(args*)
  }

  object ssmipv6 extends Literally[SourceSpecificMulticast.Strict[Ipv6Address]] {
    def validate(c: Context)(s: String): Either[String, c.Expr[SourceSpecificMulticast.Strict[Ipv6Address]]] = {
      import c.universe._
      Ipv6Address.fromString(s).flatMap(_.asSourceSpecificMulticast) match {
        case Some(_) =>
          Right(c.Expr(q"_root_.com.comcast.ip4s.Ipv6Address.fromString($s).get.asSourceSpecificMulticast.get"))
        case None => Left("invalid source specific IPv6 multicast address")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[SourceSpecificMulticast.Strict[Ipv6Address]] = apply(c)(args*)
  }

  object port extends Literally[Port] {
    def validate(c: Context)(s: String): Either[String, c.Expr[Port]] = {
      import c.universe._
      scala.util.Try(s.toInt).toOption.flatMap(Port.fromInt) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.Port.fromInt($s.toInt).get"))
        case None    => Left("invalid port")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Port] = apply(c)(args*)
  }

  object hostname extends Literally[Hostname] {
    def validate(c: Context)(s: String): Either[String, c.Expr[Hostname]] = {
      import c.universe._
      Hostname.fromString(s) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.Hostname.fromString($s).get"))
        case None    => Left("invalid hostname")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Hostname] = apply(c)(args*)
  }

  object idn extends Literally[IDN] {
    def validate(c: Context)(s: String): Either[String, c.Expr[IDN]] = {
      import c.universe._
      IDN.fromString(s) match {
        case Some(_) => Right(c.Expr(q"_root_.com.comcast.ip4s.IDN.fromString($s).get"))
        case None    => Left("invalid IDN")
      }
    }
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[IDN] = apply(c)(args*)
  }
}
