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

import cats.{Applicative, ApplicativeThrow}
import cats.syntax.all._

private[ip4s] trait HostPlatform { self: Host =>

  /** Resolves this host to an ip address using the platform DNS resolver.
    *
    * If the host cannot be resolved, the effect fails with a `java.net.UnknownHostException`.
    */
  def resolve[F[_]: Dns: Applicative]: F[IpAddress] =
    self match {
      case ip: IpAddress      => Applicative[F].pure(ip)
      case hostname: Hostname => Dns[F].resolve(hostname)
      case idn: IDN           => Dns[F].resolve(idn.hostname)
    }

  /** Resolves this host to an ip address using the platform DNS resolver.
    *
    * If the host cannot be resolved, a `None` is returned.
    */
  def resolveOption[F[_]: Dns: ApplicativeThrow]: F[Option[IpAddress]] =
    resolve[F].map(Some(_): Option[IpAddress]).recover { case _: UnknownHostException => None }

  /** Resolves this host to all ip addresses known to the platform DNS resolver.
    *
    * If the host cannot be resolved, an empty list is returned.
    */
  def resolveAll[F[_]: Dns: Applicative]: F[List[IpAddress]] =
    self match {
      case ip: IpAddress      => Applicative[F].pure(List(ip))
      case hostname: Hostname => Dns[F].resolveAll(hostname)
      case idn: IDN           => Dns[F].resolveAll(idn.hostname)
    }
}
