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

import scala.reflect.macros.blackbox.Context
import scala.util.Try

/** Macros that support literal string interpolators. */
object LiteralSyntaxMacros {
  def ipInterpolator(c: Context)(args: c.Expr[Any]*): c.Expr[IpAddress] =
    singlePartInterpolator(c)(args,
                              "IP address",
                              IpAddress(_).isDefined,
                              s => c.universe.reify(IpAddress(s.splice).get))

  def ipv4Interpolator(c: Context)(args: c.Expr[Any]*): c.Expr[Ipv4Address] =
    singlePartInterpolator(c)(args,
                              "IPv4 address",
                              Ipv4Address(_).isDefined,
                              s => c.universe.reify(Ipv4Address(s.splice).get))

  def ipv6Interpolator(c: Context)(args: c.Expr[Any]*): c.Expr[Ipv6Address] =
    singlePartInterpolator(c)(args,
                              "IPv6 address",
                              Ipv6Address(_).isDefined,
                              s => c.universe.reify(Ipv6Address(s.splice).get))

  def mipInterpolator(c: Context)(args: c.Expr[Any]*): c.Expr[Multicast[IpAddress]] =
    singlePartInterpolator(c)(
      args,
      "IP multicast address",
      s => IpAddress(s).flatMap(_.asMulticast).isDefined,
      s => c.universe.reify(IpAddress(s.splice).get.asMulticast.get)
    )
  def mipv4Interpolator(c: Context)(args: c.Expr[Any]*): c.Expr[Multicast[Ipv4Address]] =
    singlePartInterpolator(c)(
      args,
      "IPv4 multicast address",
      s => Ipv4Address(s).flatMap(_.asMulticast).isDefined,
      s => c.universe.reify(Ipv4Address(s.splice).get.asMulticast.get)
    )
  def mipv6Interpolator(c: Context)(args: c.Expr[Any]*): c.Expr[Multicast[Ipv6Address]] =
    singlePartInterpolator(c)(
      args,
      "IPv6 multicast address",
      s => Ipv6Address(s).flatMap(_.asMulticast).isDefined,
      s => c.universe.reify(Ipv6Address(s.splice).get.asMulticast.get)
    )

  def ssmipInterpolator(c: Context)(args: c.Expr[Any]*): c.Expr[SourceSpecificMulticast[IpAddress]] =
    singlePartInterpolator(c)(
      args,
      "source specific IP multicast address",
      s => IpAddress(s).flatMap(_.asSourceSpecificMulticast).isDefined,
      s => c.universe.reify(IpAddress(s.splice).get.asSourceSpecificMulticast.get)
    )
  def ssmipv4Interpolator(c: Context)(args: c.Expr[Any]*): c.Expr[SourceSpecificMulticast[Ipv4Address]] =
    singlePartInterpolator(c)(
      args,
      "source specific IPv4 multicast address",
      s => Ipv4Address(s).flatMap(_.asSourceSpecificMulticast).isDefined,
      s => c.universe.reify(Ipv4Address(s.splice).get.asSourceSpecificMulticast.get)
    )
  def ssmipv6Interpolator(c: Context)(args: c.Expr[Any]*): c.Expr[SourceSpecificMulticast[Ipv6Address]] =
    singlePartInterpolator(c)(
      args,
      "source specific IPv6 multicast address",
      s => Ipv6Address(s).flatMap(_.asSourceSpecificMulticast).isDefined,
      s => c.universe.reify(Ipv6Address(s.splice).get.asSourceSpecificMulticast.get)
    )

  def portInterpolator(c: Context)(args: c.Expr[Any]*): c.Expr[Port] =
    singlePartInterpolator(c)(args,
                              "port",
                              s => Try(s.toInt).toOption.flatMap(Port(_)).isDefined,
                              s => c.universe.reify(Port(s.splice.toInt).get))

  def hostnameInterpolator(c: Context)(args: c.Expr[Any]*): c.Expr[Hostname] =
    singlePartInterpolator(c)(args,
                              "hostname",
                              s => Hostname(s).isDefined,
                              s => c.universe.reify(Hostname(s.splice).get))

  def idnInterpolator(c: Context)(args: c.Expr[Any]*): c.Expr[IDN] =
    singlePartInterpolator(c)(args, "IDN", s => IDN(s).isDefined, s => c.universe.reify(IDN(s.splice).get))

  private def singlePartInterpolator[A](c: Context)(args: Seq[c.Expr[Any]],
                                                    typeName: String,
                                                    validate: String => Boolean,
                                                    construct: c.Expr[String] => c.Expr[A]): c.Expr[A] = {
    import c.universe._
    identity(args)
    c.prefix.tree match {
      case Apply(_, List(Apply(_, (lcp @ Literal(Constant(p: String))) :: Nil))) =>
        val valid = validate(p)
        if (valid) construct(c.Expr(lcp))
        else c.abort(c.enclosingPosition, s"invalid $typeName")
    }
  }
}
