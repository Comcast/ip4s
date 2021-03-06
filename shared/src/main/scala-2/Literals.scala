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
import scala.util.Try

/** Macros that support literal string interpolators. */
object Literals {
  private[ip4s] abstract class SimpleLiterally[A](
      name: String,
      doValidate: String => Option[A]
  ) extends Literally[A] {
    def validate(s: String) = if (doValidate(s).isDefined) None else Some(s"invalid $name")
  }

  object ip extends SimpleLiterally[IpAddress]("IP address", IpAddress.fromString) {
    def build(c: Context)(s: c.Expr[String]) = c.universe.reify(IpAddress.fromString(s.splice).get)
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[IpAddress] = apply(c)(args: _*)
  }

  object ipv4 extends SimpleLiterally[Ipv4Address]("IPv4 address", Ipv4Address.fromString) {
    def build(c: Context)(s: c.Expr[String]) = c.universe.reify(Ipv4Address.fromString(s.splice).get)
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Ipv4Address] = apply(c)(args: _*)
  }

  object ipv6 extends SimpleLiterally[Ipv6Address]("IPv6 address", Ipv6Address.fromString) {
    def build(c: Context)(s: c.Expr[String]) = c.universe.reify(Ipv6Address.fromString(s.splice).get)
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Ipv6Address] = apply(c)(args: _*)
  }

  object mip
      extends SimpleLiterally[Multicast[IpAddress]](
        "IP multicast address",
        s => IpAddress.fromString(s).flatMap(_.asMulticast)
      ) {
    def build(c: Context)(s: c.Expr[String]) = c.universe.reify(IpAddress.fromString(s.splice).get.asMulticast.get)
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Multicast[IpAddress]] = apply(c)(args: _*)
  }

  object mipv4
      extends SimpleLiterally[Multicast[Ipv4Address]](
        "IPv4 multicast address",
        s => Ipv4Address.fromString(s).flatMap(_.asMulticast)
      ) {
    def build(c: Context)(s: c.Expr[String]) = c.universe.reify(Ipv4Address.fromString(s.splice).get.asMulticast.get)
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Multicast[Ipv4Address]] = apply(c)(args: _*)
  }

  object mipv6
      extends SimpleLiterally[Multicast[Ipv6Address]](
        "IPv6 multicast address",
        s => Ipv6Address.fromString(s).flatMap(_.asMulticast)
      ) {
    def build(c: Context)(s: c.Expr[String]) = c.universe.reify(Ipv6Address.fromString(s.splice).get.asMulticast.get)
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Multicast[Ipv6Address]] = apply(c)(args: _*)
  }

  object ssmip
      extends SimpleLiterally[SourceSpecificMulticast[IpAddress]](
        "source specific IP multicast address",
        s => IpAddress.fromString(s).flatMap(_.asSourceSpecificMulticast)
      ) {
    def build(c: Context)(s: c.Expr[String]) =
      c.universe.reify(IpAddress.fromString(s.splice).get.asSourceSpecificMulticast.get)
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[SourceSpecificMulticast[IpAddress]] = apply(c)(args: _*)
  }

  object ssmipv4
      extends SimpleLiterally[SourceSpecificMulticast[Ipv4Address]](
        "source specific IPv4 multicast address",
        s => Ipv4Address.fromString(s).flatMap(_.asSourceSpecificMulticast)
      ) {
    def build(c: Context)(s: c.Expr[String]) =
      c.universe.reify(Ipv4Address.fromString(s.splice).get.asSourceSpecificMulticast.get)
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[SourceSpecificMulticast[Ipv4Address]] = apply(c)(args: _*)
  }

  object ssmipv6
      extends SimpleLiterally[SourceSpecificMulticast[Ipv6Address]](
        "source specific IPv6 multicast address",
        s => Ipv6Address.fromString(s).flatMap(_.asSourceSpecificMulticast)
      ) {
    def build(c: Context)(s: c.Expr[String]) =
      c.universe.reify(Ipv6Address.fromString(s.splice).get.asSourceSpecificMulticast.get)
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[SourceSpecificMulticast[Ipv6Address]] = apply(c)(args: _*)
  }

  object port
      extends SimpleLiterally[Port](
        "port",
        s => Try(s.toInt).toOption.flatMap(Port.fromInt)
      ) {
    def build(c: Context)(s: c.Expr[String]) = c.universe.reify(Port.fromInt(s.splice.toInt).get)
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Port] = apply(c)(args: _*)
  }

  object hostname
      extends SimpleLiterally[Hostname](
        "hostname",
        Hostname.fromString
      ) {
    def build(c: Context)(s: c.Expr[String]) = c.universe.reify(Hostname.fromString(s.splice).get)
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Hostname] = apply(c)(args: _*)
  }

  object idn
      extends SimpleLiterally[IDN](
        "idn",
        IDN.fromString
      ) {
    def build(c: Context)(s: c.Expr[String]) = c.universe.reify(IDN.fromString(s.splice).get)
    def make(c: Context)(args: c.Expr[Any]*): c.Expr[IDN] = apply(c)(args: _*)
  }
}
