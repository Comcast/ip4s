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

private[ip4s] trait HostnamePlatform { self: Hostname =>

  /** Resolves this hostname to an ip address using the platform DNS resolver.
    *
    * If the hostname cannot be resolved, the effect fails with a `java.net.UnknownHostException`.
    */
  def resolve[F[_]: Dns]: F[IpAddress] =
    Dns[F].resolve(self)

  /** Resolves this hostname to an ip address using the platform DNS resolver.
    *
    * If the hostname cannot be resolved, a `None` is returned.
    */
  def resolveOption[F[_]: Dns]: F[Option[IpAddress]] =
    Dns[F].resolveOption(self)

  /** Resolves this hostname to all ip addresses known to the platform DNS resolver.
    *
    * If the hostname cannot be resolved, an empty list is returned.
    */
  def resolveAll[F[_]: Dns]: F[List[IpAddress]] =
    Dns[F].resolveAll(self)
}
