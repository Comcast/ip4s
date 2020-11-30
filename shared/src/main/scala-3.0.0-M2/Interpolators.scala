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

import scala.quoted._
import scala.util.Try

extension (inline ctx: StringContext) inline def ip (inline args: Any*): IpAddress =
  ${Literals.validateIp('ctx, 'args)}

extension (inline ctx: StringContext) inline def ipv4 (inline args: Any*): Ipv4Address =
  ${Literals.validateIpv4('ctx, 'args)}

extension (inline ctx: StringContext) inline def ipv6 (inline args: Any*): Ipv6Address =
  ${Literals.validateIpv6('ctx, 'args)}

extension (inline ctx: StringContext) inline def mip (inline args: Any*): Multicast[IpAddress] =
  ${Literals.validateMip('ctx, 'args)}

extension (inline ctx: StringContext) inline def mipv4 (inline args: Any*): Multicast[Ipv4Address] =
  ${Literals.validateMipv4('ctx, 'args)}

extension (inline ctx: StringContext) inline def mipv6 (inline args: Any*): Multicast[Ipv6Address] =
  ${Literals.validateMipv6('ctx, 'args)}

extension (inline ctx: StringContext) inline def ssmip (inline args: Any*): SourceSpecificMulticast[IpAddress] =
  ${Literals.validateSsmip('ctx, 'args)}

extension (inline ctx: StringContext) inline def ssmipv4 (inline args: Any*): SourceSpecificMulticast[Ipv4Address] =
  ${Literals.validateSsmipv4('ctx, 'args)}

extension (inline ctx: StringContext) inline def ssmipv6 (inline args: Any*): SourceSpecificMulticast[Ipv6Address] =
  ${Literals.validateSsmipv6('ctx, 'args)}

extension (inline ctx: StringContext) inline def port (inline args: Any*): Port =
  ${Literals.validatePort('ctx, 'args)}

extension (inline ctx: StringContext) inline def host (inline args: Any*): Hostname =
  ${Literals.validateHost('ctx, 'args)}

extension (inline ctx: StringContext) inline def idn (inline args: Any*): IDN =
  ${Literals.validateIdn('ctx, 'args)}

object Literals {

  trait Validator[A] {
    def validate(s: String): Option[String]
    def build(s: String)(using Quotes): Expr[A]
  }

  def validateIp(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[IpAddress] =
    validate(ip, strCtxExpr, argsExpr)

  def validateIpv4(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[Ipv4Address] =
    validate(ipv4, strCtxExpr, argsExpr)

  def validateIpv6(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[Ipv6Address] =
    validate(ipv6, strCtxExpr, argsExpr)

  def validateMip(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[Multicast[IpAddress]] =
    validate(mip, strCtxExpr, argsExpr)

  def validateMipv4(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[Multicast[Ipv4Address]] =
    validate(mipv4, strCtxExpr, argsExpr)

  def validateMipv6(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[Multicast[Ipv6Address]] =
    validate(mipv6, strCtxExpr, argsExpr)

  def validateSsmip(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[SourceSpecificMulticast[IpAddress]] =
    validate(ssmip, strCtxExpr, argsExpr)

  def validateSsmipv4(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[SourceSpecificMulticast[Ipv4Address]] =
    validate(ssmipv4, strCtxExpr, argsExpr)

  def validateSsmipv6(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[SourceSpecificMulticast[Ipv6Address]] =
    validate(ssmipv6, strCtxExpr, argsExpr)

  def validatePort(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[Port] =
    validate(port, strCtxExpr, argsExpr)

  def validateHost(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[Hostname] =
    validate(host, strCtxExpr, argsExpr)

  def validateIdn(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[IDN] =
    validate(idn, strCtxExpr, argsExpr)

  def validate[A](validator: Validator[A], strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[A] = {
    strCtxExpr.unlift match {
      case Some(sc) => validate(validator, sc.parts, argsExpr)
      case None =>
        report.error("StringContext args must be statically known")
        ???
    }
  }

  private def validate[A](validator: Validator[A], parts: Seq[String], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[A] = {
    if (parts.size == 1) {
      val literal = parts.head
      validator.validate(literal) match {
        case Some(err) =>
          report.error(err)
          ???
        case None =>
          validator.build(literal)
      }
    } else {
      report.error("interpolation not supported", argsExpr)
      ???
    }
  }

  object ip extends Validator[IpAddress] {
    def validate(s: String): Option[String] =
      IpAddress(s).fold(Some("Invalid IP address"))(_ => None)
    def build(s: String)(using Quotes): Expr[IpAddress] =
      '{_root_.com.comcast.ip4s.IpAddress(${Expr(s)}).get}
  }

  object ipv4 extends Validator[Ipv4Address] {
    def validate(s: String): Option[String] =
      Ipv4Address(s).fold(Some("Invalid IPv4 address"))(_ => None)
    def build(s: String)(using Quotes): Expr[Ipv4Address] =
      '{_root_.com.comcast.ip4s.Ipv4Address(${Expr(s)}).get}
  }

  object ipv6 extends Validator[Ipv6Address] {
    def validate(s: String): Option[String] =
      Ipv6Address(s).fold(Some("Invalid IPv6 address"))(_ => None)
    def build(s: String)(using Quotes): Expr[Ipv6Address] =
      '{_root_.com.comcast.ip4s.Ipv6Address(${Expr(s)}).get}
  }

  object mip extends Validator[Multicast[IpAddress]] {
    def validate(s: String): Option[String] =
      IpAddress(s).flatMap(_.asMulticast).fold(Some("Invalid IP multicast address"))(_ => None)
    def build(s: String)(using Quotes): Expr[Multicast[IpAddress]] =
      '{_root_.com.comcast.ip4s.IpAddress(${Expr(s)}).get.asMulticast.get}
  }

  object mipv4 extends Validator[Multicast[Ipv4Address]] {
    def validate(s: String): Option[String] =
      Ipv4Address(s).flatMap(_.asMulticast).fold(Some("Invalid IPv4 multicast address"))(_ => None)
    def build(s: String)(using Quotes): Expr[Multicast[Ipv4Address]] =
      '{_root_.com.comcast.ip4s.Ipv4Address(${Expr(s)}).get.asMulticast.get}
  }

  object mipv6 extends Validator[Multicast[Ipv6Address]] {
    def validate(s: String): Option[String] =
      Ipv6Address(s).flatMap(_.asMulticast).fold(Some("Invalid IPv6 multicast address"))(_ => None)
    def build(s: String)(using Quotes): Expr[Multicast[Ipv6Address]] =
      '{_root_.com.comcast.ip4s.Ipv6Address(${Expr(s)}).get.asMulticast.get}
  }

  object ssmip extends Validator[SourceSpecificMulticast[IpAddress]] {
    def validate(s: String): Option[String] =
      IpAddress(s).flatMap(_.asSourceSpecificMulticast).fold(Some("Invalid source specific IP multicast address"))(_ => None)
    def build(s: String)(using Quotes): Expr[SourceSpecificMulticast[IpAddress]] =
      '{_root_.com.comcast.ip4s.IpAddress(${Expr(s)}).get.asSourceSpecificMulticast.get}
  }

  object ssmipv4 extends Validator[SourceSpecificMulticast[Ipv4Address]] {
    def validate(s: String): Option[String] =
      Ipv4Address(s).flatMap(_.asSourceSpecificMulticast).fold(Some("Invalid source specific IPv4 multicast address"))(_ => None)
    def build(s: String)(using Quotes): Expr[SourceSpecificMulticast[Ipv4Address]] =
      '{_root_.com.comcast.ip4s.Ipv4Address(${Expr(s)}).get.asSourceSpecificMulticast.get}
  }

  object ssmipv6 extends Validator[SourceSpecificMulticast[Ipv6Address]] {
    def validate(s: String): Option[String] =
      Ipv6Address(s).flatMap(_.asSourceSpecificMulticast).fold(Some("Invalid source specific IPv6 multicast address"))(_ => None)
    def build(s: String)(using Quotes): Expr[SourceSpecificMulticast[Ipv6Address]] =
      '{_root_.com.comcast.ip4s.Ipv6Address(${Expr(s)}).get.asSourceSpecificMulticast.get}
  }

  object port extends Validator[Port] {
    def validate(s: String): Option[String] =
      Try(s.toInt).toOption.flatMap(Port(_)).fold(Some("Invalid port"))(_ => None)
    def build(s: String)(using Quotes): Expr[Port] =
      '{_root_.com.comcast.ip4s.Port(${Expr(s.toInt)}).get}
  }

  object host extends Validator[Hostname] {
    def validate(s: String): Option[String] =
      Hostname(s).fold(Some("Invalid hostname"))(_ => None)
    def build(s: String)(using Quotes): Expr[Hostname] =
      '{_root_.com.comcast.ip4s.Hostname(${Expr(s)}).get}
  }

  object idn extends Validator[IDN] {
    def validate(s: String): Option[String] =
      IDN(s).fold(Some("Invalid IDN"))(_ => None)
    def build(s: String)(using Quotes): Expr[IDN] =
      '{_root_.com.comcast.ip4s.IDN(${Expr(s)}).get}
  }

}
