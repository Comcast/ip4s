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

import org.scalacheck.{Arbitrary, Gen}

import Arbitraries._

class Ipv4AddressTest extends BaseTestSuite {
  "Ipv4Address" should {
    "support parsing from string form".which {
      "does not parse the empty string" in {
        Ipv4Address.apply("") shouldBe None
      }
      "does not parse white space string" in {
        Ipv4Address.apply(" ") shouldBe None
      }
    }

    "roundtrip through string" in {
      forAll(Gen.listOfN(4, Arbitrary.arbitrary[Byte])) { bytesList =>
        if (bytesList.size == 4) {
          val bytes = bytesList.toArray
          val addr = Ipv4Address.fromBytes(bytes).get
          Ipv4Address(addr.toString) shouldBe Some(addr)
        }
      }
    }

    "roundtrip through long" in {
      forAll(Gen.listOfN(4, Arbitrary.arbitrary[Byte])) { bytesList =>
        if (bytesList.size == 4) {
          val bytes = bytesList.toArray
          val addr = Ipv4Address.fromBytes(bytes).get
          Ipv4Address.fromLong(addr.toLong) shouldBe addr
        }
      }
    }

    "support ordering" in {
      forAll { (left: Ipv4Address, right: Ipv4Address) =>
        val longCompare = left.toLong.compare(right.toLong)
        val result = Ordering[Ipv4Address].compare(left, right)
        result shouldBe longCompare
      }
    }

    "support computing next IP" in {
      Ipv4Address("255.255.255.255").map(_.next) shouldBe Ipv4Address("0.0.0.0")
      forAll { (ip: Ipv4Address) =>
        ip.next shouldBe Ipv4Address.fromLong(ip.toLong + 1)
      }
    }

    "support computing previous IP" in {
      Ipv4Address("0.0.0.0").map(_.previous) shouldBe Ipv4Address("255.255.255.255")
      forAll { (ip: Ipv4Address) =>
        ip.previous shouldBe Ipv4Address.fromLong(ip.toLong - 1)
      }
    }
  }
}
