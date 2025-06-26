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

/** Description of a network interface.
  *
  * Note this is an immutable description, representing a snapshot of the state of the network interface at the time the operating system was queried.
  *
  * To get an instance of `NetworkInterface`, use `NetworkInterfaces[F].getAll`.
  */
sealed trait NetworkInterface {

  /** Unique name for the interface. */
  def name: String

  /** Descriptive name of the interface, suitable for use on user interfaces. */
  def displayName: String

  /** MAC address of the interface, if available. */
  def macAddress: Option[MacAddress]

  /** IP addresses associated with the interface. */
  def addresses: List[Cidr[IpAddress]]

  /** True if the interface is a loopback interface. */
  def isLoopback: Boolean

  /** True if the interface is up/active. */
  def isUp: Boolean
}

object NetworkInterface extends NetworkInterfaceCompanionPlatform {

  def apply(
      name: String,
      displayName: String,
      macAddress: Option[MacAddress],
      addresses: List[Cidr[IpAddress]],
      isLoopback: Boolean,
      isUp: Boolean
  ): NetworkInterface =
    DefaultNetworkInterface(name, displayName, macAddress, addresses, isLoopback, isUp)

  private case class DefaultNetworkInterface(
      name: String,
      displayName: String,
      macAddress: Option[MacAddress],
      addresses: List[Cidr[IpAddress]],
      isLoopback: Boolean,
      isUp: Boolean
  ) extends NetworkInterface {
    override def toString: String =
      s"NetworkInterface($name, $displayName, $macAddress, $addresses, $isLoopback, $isUp)"
  }
}
