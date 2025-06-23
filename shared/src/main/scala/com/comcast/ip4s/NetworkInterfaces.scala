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

import cats.effect.{IO, Sync}
import cats.syntax.all.*

/** Capability for an effect `F[_]` which can query for local network interfaces.
  *
  * An instance is available for any effect which has a `Sync` instance.
  */
sealed trait NetworkInterfaces[F[_]] {

  /** Gets a map of network interfaces by name. */
  def getAll: F[Map[String, NetworkInterface]]

  /** Gets the network interface with the specified name. */
  def getByName(name: String): F[Option[NetworkInterface]]

  /** Gets the network interface with the specified address. */
  def getByAddress(address: IpAddress): F[Option[NetworkInterface]]

  /** Gets the network interfaces with the specified MAC address. */
  def getByMacAddress(address: MacAddress): F[List[NetworkInterface]]
}

object NetworkInterfaces extends NetworkInterfacesCompanionPlatform {
  private[ip4s] abstract class SyncNetworkInterfaces[F[_]: Sync] extends NetworkInterfaces[F] {
    def getByName(name: String): F[Option[NetworkInterface]] =
      getAll.map(_.get(name))

    def getByAddress(address: IpAddress): F[Option[NetworkInterface]] =
      getAll.map { all =>
        all.values.collectFirst { case iface if iface.addresses.exists(_.address == address) => iface }
      }

    def getByMacAddress(address: MacAddress): F[List[NetworkInterface]] =
      getAll.map { all =>
        all.values.collect { case iface if iface.macAddress == Some(address) => iface }.toList
      }
  }

  def apply[F[_]](implicit F: NetworkInterfaces[F]): F.type = F

  implicit def forIO: NetworkInterfaces[IO] = forSync[IO]
}
