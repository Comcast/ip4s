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

import java.net.{InetAddress, UnknownHostException}

import cats.data.NonEmptyList
import cats.effect.Sync

private[ip4s] trait HostnamePlatform { self: Hostname =>

  /**
    * Resolves this hostname to an ip address using the platform DNS resolver.
    *
    * If the hostname cannot be resolved, a `None` is returned.
    *
    * Resolution happens synchronously when the returned task is evaluated
    * so consider shifting the returned task to a blocking execution context.
    */
  def resolve[F[_]: Sync]: F[Option[IpAddress]] =
    Sync[F].delay {
      try {
        val addr = InetAddress.getByName(self.toString)
        IpAddress.fromBytes(addr.getAddress)
      } catch {
        case _: UnknownHostException => None
      }
    }

  /**
    * Resolves this hostname to all ip addresses known to the platform DNS resolver.
    *
    * If the hostname cannot be resolved, a `None` is returned.
    *
    * Resolution happens synchronously when the returned task is evaluated
    * so consider shifting the returned task to a blocking execution context.
    */
  def resolveAll[F[_]: Sync]: F[Option[NonEmptyList[IpAddress]]] =
    Sync[F].delay {
      try {
        val addrs = InetAddress.getAllByName(self.toString)
        NonEmptyList.fromList(addrs.toList.flatMap(addr => IpAddress.fromBytes(addr.getAddress)))
      } catch {
        case _: UnknownHostException => None
      }
    }
}
