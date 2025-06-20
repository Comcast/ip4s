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

import cats.effect.IO

/** Capability for an effect `F[_]` which can query for local network interfaces.
  *
  * An instance is available for any effect which has an `Async` instance.
  */
sealed trait NetworkInterfaces[F[_]] {

  /** Gets a map of network interfaces by name. */
  def getAll: F[Map[String, NetworkInterface]]
}

object NetworkInterfaces extends NetworkInterfacesCompanionPlatform {
  private[ip4s] trait UnsealedNetworkInterfaces[F[_]] extends NetworkInterfaces[F]

  def apply[F[_]](implicit F: NetworkInterfaces[F]): F.type = F

  implicit def forIO: NetworkInterfaces[IO] = forAsync[IO]
}
