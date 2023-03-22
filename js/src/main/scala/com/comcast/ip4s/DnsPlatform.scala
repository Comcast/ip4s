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

import cats.effect.kernel.Async
import cats.syntax.all._

import scala.scalajs.js
import scala.scalajs.js.|
import scala.scalajs.js.annotation.JSImport

private[ip4s] trait DnsCompanionPlatform {
  def forAsync[F[_]](implicit F: Async[F]): Dns[F] = new UnsealedDns[F] {
    def resolve(hostname: Hostname): F[IpAddress] =
      F.fromPromise(F.delay(dnsPromises.lookup(hostname.toString, LookupOptions(all = false))))
        .flatMap { address =>
          IpAddress
            .fromString(address.asInstanceOf[LookupResult].address)
            .liftTo[F](new RuntimeException("Node.js returned invalid IP address"))
        }
        .adaptError {
          case ex @ js.JavaScriptException(error: js.Error) if error.message.contains("ENOTFOUND") =>
            new JavaScriptUnknownHostException(s"$hostname: Name or service not known", ex)
        }

    def resolveOption(hostname: Hostname): F[Option[IpAddress]] =
      resolve(hostname).map(_.some).recover { case _: UnknownHostException => None }

    def resolveAll(hostname: Hostname): F[List[IpAddress]] =
      F.fromPromise(F.delay(dnsPromises.lookup(hostname.toString, LookupOptions(all = true))))
        .flatMap { addresses =>
          addresses
            .asInstanceOf[js.Array[LookupResult]]
            .toList
            .traverse { address =>
              IpAddress
                .fromString(address.address)
                .liftTo[F](new RuntimeException("Node.js returned invalid IP address"))
            }
        }
        .recover {
          case js.JavaScriptException(error: js.Error) if error.message.contains("ENOTFOUND") =>
            Nil
        }

    def reverse(address: IpAddress): F[Hostname] =
      reverseAllOrError(address).flatMap(_.headOption.liftTo(new UnknownHostException(address.toString)))

    def reverseOption(address: IpAddress): F[Option[Hostname]] = reverseAll(address).map(_.headOption)

    def reverseAll(address: IpAddress): F[List[Hostname]] =
      reverseAllOrError(address).recover { case _: UnknownHostException => Nil }

    private def reverseAllOrError(address: IpAddress): F[List[Hostname]] =
      F.fromPromise(F.delay(dnsPromises.reverse(address.toString)))
        .flatMap { hostnames =>
          hostnames.toList.traverse { hostname =>
            Hostname
              .fromString(hostname)
              .liftTo[F](new RuntimeException("Node.js returned invalid hostname"))
          }
        }
        .adaptError {
          case ex @ js.JavaScriptException(error: js.Error) if error.message.contains("ENOTFOUND") =>
            new JavaScriptUnknownHostException(address.toString, ex)
        }

    def loopback: F[IpAddress] = resolve(Hostname.fromString("localhost").get)
  }
}

@js.native
@JSImport("dns", "promises")
private[ip4s] object dnsPromises extends js.Any {

  def lookup(hostname: String, options: LookupOptions): js.Promise[LookupResult | js.Array[LookupResult]] = js.native

  def reverse(ip: String): js.Promise[js.Array[String]] = js.native
}

private[ip4s] sealed trait LookupOptions extends js.Object
object LookupOptions {
  def apply(all: Boolean): LookupOptions = js.Dynamic.literal(all = all).asInstanceOf[LookupOptions]
}

@js.native
private[ip4s] sealed trait LookupResult extends js.Object {
  def address: String = js.native
  def family: Int = js.native
}
