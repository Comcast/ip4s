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
import cats.syntax.all._
import munit.CatsEffectSuite

class DnsTest extends CatsEffectSuite {

  test("resolve/reverseAll round-trip") {
    for {
      hostname <- Hostname.fromString("comcast.com").liftTo[IO](new NoSuchElementException)
      address <- Dns[IO].resolve(hostname)
      hostnames <- Dns[IO].reverseAll(address)
      _ <- IO(assert(hostnames.nonEmpty))
      reversedAddress <- hostnames.traverse(Dns[IO].resolve)
    } yield assert(reversedAddress.forall(_ == address))
  }

  test("resolveAll/reverse round-trip") {
    for {
      hostname <- Hostname.fromString("comcast.com").liftTo[IO](new NoSuchElementException)
      addresses <- Dns[IO].resolveAll(hostname)
      _ <- IO(assert(addresses.nonEmpty))
      hostnames <- addresses.traverse(Dns[IO].reverse)
      reversedAddresses <- hostnames.traverse(Dns[IO].resolve)
    } yield assertEquals(Set(addresses), Set(reversedAddresses))
  }

  test("loopback") {
    val loopbacks = Set(ip"127.0.0.1", ip"::1")
    Dns[IO].loopback.flatMap { loopback =>
      IO(assert(loopbacks.contains(clue(loopback))))
    }
  }

  test("resolve unknown host") {
    (Dns[IO].resolve(host"not.example.com") >>
      IO.raiseError(new AssertionError("Did not raise `UnknownHostException`"))).recover {
      case ex: UnknownHostException =>
        assert(
          ex.getMessage == "not.example.com: Name or service not known" || ex.getMessage == "not.example.com: nodename nor servname provided, or not known"
        )
    }
  }

  test("reverse unknown ip") {
    Dns[IO].reverse(ip"240.0.0.0").interceptMessage[UnknownHostException]("240.0.0.0")
  }

}
