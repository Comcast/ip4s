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

import cats.effect.{Blocker, ContextShift, Sync}
import cats.syntax.all._

import java.net.{InetAddress, UnknownHostException}

/** Capability for an effect `F[_]` which can do DNS lookups.
  *
  * An instance is available for any effect which has a `Sync` instance.
  */
trait Dns[F[_]] {

  /** Resolves the supplied hostname to an ip address using the platform DNS resolver.
    *
    * If the hostname cannot be resolved, the effect fails with a `java.net.UnknownHostException`.
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

  /** Gets an IP address representing the loopback interface. */
  def loopback: F[IpAddress]
}

object Dns {
  def apply[F[_]](implicit F: Dns[F]): F.type = F

  implicit def forSync[F[_]](implicit F: Sync[F], cs: ContextShift[F], blocker: Blocker): Dns[F] = new Dns[F] {
    def resolve(hostname: Hostname): F[IpAddress] =
      blocker.delay {
        val addr = InetAddress.getByName(hostname.toString)
        IpAddress.fromBytes(addr.getAddress).get
      }

    def resolveOption(hostname: Hostname): F[Option[IpAddress]] =
      resolve(hostname).map(Some(_): Option[IpAddress]).recover { case _: UnknownHostException => None }

    def resolveAll(hostname: Hostname): F[List[IpAddress]] =
      blocker.delay {
        try {
          val addrs = InetAddress.getAllByName(hostname.toString)
          addrs.toList.flatMap(addr => IpAddress.fromBytes(addr.getAddress))
        } catch {
          case _: UnknownHostException => Nil
        }
      }

    def loopback: F[IpAddress] =
      blocker.delay(IpAddress.fromInetAddress(InetAddress.getByName(null)))
  }
}
