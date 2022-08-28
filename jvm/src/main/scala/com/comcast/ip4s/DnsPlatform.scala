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

import cats.effect.kernel.Sync
import cats.syntax.all._

import java.net.InetAddress

private[ip4s] trait DnsCompanionPlatform {
  implicit def forSync[F[_]](implicit F: Sync[F]): Dns[F] = new UnsealedDns[F] {
    def resolve(hostname: Hostname): F[IpAddress] =
      F.blocking {
        val addr = InetAddress.getByName(hostname.toString)
        IpAddress.fromBytes(addr.getAddress).get
      }

    def resolveOption(hostname: Hostname): F[Option[IpAddress]] =
      resolve(hostname).map(_.some).recover { case _: UnknownHostException => None }

    def resolveAll(hostname: Hostname): F[List[IpAddress]] =
      F.blocking {
        try {
          val addrs = InetAddress.getAllByName(hostname.toString)
          addrs.toList.flatMap(addr => IpAddress.fromBytes(addr.getAddress))
        } catch {
          case _: UnknownHostException => Nil
        }
      }

    def reverse(address: IpAddress): F[Hostname] = {
      val inetAddress = address.toInetAddress
      F.blocking {
        inetAddress.getCanonicalHostName
      } flatMap { hn =>
        (if (hn == inetAddress.getHostAddress)
           None // getCanonicalHostName returns the IP address as a string on failure
         else
           Hostname.fromString(hn))
          .liftTo[F](new UnknownHostException(address.toString))
      }
    }

    def reverseOption(address: IpAddress): F[Option[Hostname]] =
      reverse(address).map(_.some).recover { case _: UnknownHostException => None }

    def reverseAll(address: IpAddress): F[List[Hostname]] =
      reverseOption(address).map(_.toList)

    def loopback: F[IpAddress] =
      F.blocking(IpAddress.fromInetAddress(InetAddress.getByName(null)))
  }
}
