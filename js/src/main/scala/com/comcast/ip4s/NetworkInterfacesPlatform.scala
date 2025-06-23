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

import cats.effect.Async

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

private[ip4s] trait NetworkInterfacesCompanionPlatform {

  def forAsync[F[_]](implicit F: Async[F]): NetworkInterfaces[F] =
    new NetworkInterfaces.AsyncNetworkInterfaces[F] {

      def getAll: F[Map[String, NetworkInterface]] =
        F.blocking {
          val dict = osFacade.networkInterfaces()
          var result = collection.immutable.ListMap.empty[String, NetworkInterface]
          dict.keys.foreach(k => result = result + (k -> fromNetworkInterfaceInfo(k, dict(k))))
          result
        }

      private def fromNetworkInterfaceInfo(
          name: String,
          nia: js.Array[osFacade.NetworkInterfaceInfo]
      ): NetworkInterface =
        NetworkInterface(
          name,
          name,
          MacAddress.fromString(nia.head.mac).filterNot(_.toLong == 0),
          nia.view.map { ni =>
            val cidr = Cidr.fromString(ni.cidr).get
            cidr.address match {
              case v6: Ipv6Address =>
                if (ni.scopeid == 0) cidr
                else Cidr(v6.withScopeId(ni.scopeid.toString), cidr.prefixBits)
              case _ => cidr
            }
          }.toList,
          nia.head.internal,
          true
        )
    }
}

private[ip4s] object osFacade {

  @js.native
  @JSImport("os", "networkInterfaces")
  def networkInterfaces(): js.Dictionary[js.Array[NetworkInterfaceInfo]] = js.native

  @js.native
  trait NetworkInterfaceInfo extends js.Object {
    def mac: String = js.native
    def cidr: String = js.native
    def scopeid: Int = js.native
    def internal: Boolean = js.native
  }

}
