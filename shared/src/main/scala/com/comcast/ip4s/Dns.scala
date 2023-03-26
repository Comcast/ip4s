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

import cats.~>
import cats.Functor
import cats.data.EitherT
import cats.data.Kleisli
import cats.effect.IO

/** Capability for an effect `F[_]` which can do DNS lookups.
  *
  * An instance is available for any effect which has a `Sync` instance on JVM and `Async` on Node.js.
  */
sealed trait Dns[F[_]] { outer =>

  /** Resolves the supplied hostname to an ip address using the platform DNS resolver.
    *
    * If the hostname cannot be resolved, the effect fails with an `UnknownHostException`.
    */
  def resolve(hostname: Hostname): F[IpAddress]

  /** Resolves the supplied hostname to an ip address using the platform DNS resolver.
    *
    * If the hostname cannot be resolved, a `None` is returned.
    */
  def resolveOption(hostname: Hostname): F[Option[IpAddress]]

  /** Resolves the supplied hostname to all ip addresses known to the platform DNS resolver.
    *
    * If the hostname cannot be resolved, an empty list is returned.
    */
  def resolveAll(hostname: Hostname): F[List[IpAddress]]

  /** Reverses the supplied address to a hostname using the platform DNS resolver.
    *
    * If the address cannot be reversed, the effect fails with an `UnknownHostException`.
    */
  def reverse(address: IpAddress): F[Hostname]

  /** Reverses the supplied address to a hostname using the platform DNS resolver.
    *
    * If the address cannot be reversed, a `None` is returned.
    */
  def reverseOption(address: IpAddress): F[Option[Hostname]]

  /** Reverses the supplied address to all hostnames known to the platform DNS resolver.
    *
    * If the address cannot be reversed, an empty list is returned.
    */
  def reverseAll(address: IpAddress): F[List[Hostname]]

  /** Gets an IP address representing the loopback interface. */
  def loopback: F[IpAddress]

  /** Translates effect type from `F` to `G` using the supplied `FunctionK` */
  final def mapK[G[_]](fk: F ~> G): Dns[G] = new Dns[G] {
    def resolve(hostname: Hostname) = fk(outer.resolve(hostname))
    def resolveOption(hostname: Hostname) = fk(outer.resolveOption(hostname))
    def resolveAll(hostname: Hostname) = fk(outer.resolveAll(hostname))
    def reverse(address: IpAddress) = fk(outer.reverse(address))
    def reverseOption(address: IpAddress) = fk(outer.reverseOption(address))
    def reverseAll(address: IpAddress) = fk(outer.reverseAll(address))
    def loopback = fk(outer.loopback)
  }
}

private[ip4s] trait UnsealedDns[F[_]] extends Dns[F]

object Dns extends DnsCompanionPlatform {
  def apply[F[_]](implicit F: Dns[F]): F.type = F

  implicit def forIO: Dns[IO] = forAsync[IO]

  implicit def forEitherT[F[_]: Functor, A](implicit dns: Dns[F]): Dns[EitherT[F, A, *]] =
    dns.mapK(EitherT.liftK)

  implicit def forKleisli[F[_], A](implicit dns: Dns[F]): Dns[Kleisli[F, A, *]] =
    dns.mapK(Kleisli.liftK)

}
