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

class Ipv6AddressTest extends BaseTestSuite {

  "Ipv6Address" should {
    "support parsing from string form".which {
      "does not parse the empty string" in {
        Ipv6Address("") shouldBe None
      }
      "does not parse white space string" in {
        Ipv6Address(" ") shouldBe None
      }
      "does not parse a single :" in {
        Ipv6Address(":") shouldBe None
        Ipv6Address(" : ") shouldBe None
      }
      "does parse ::" in {
        Ipv6Address("::").isDefined shouldBe true
        Ipv6Address(" :: ").isDefined shouldBe true
      }
      "supports mixed strings" in {
        forAll { (v4: Ipv4Address) =>
          Ipv6Address("::" + v4) shouldBe Some(v4.toCompatV6)
          Ipv6Address("::ffff:" + v4) shouldBe Some(v4.toMappedV6)
        }
      }
    }

    "support converting to uncondensed string form" in {
      forAll(Gen.listOfN(16, Arbitrary.arbitrary[Byte])) { bytesList =>
        if (bytesList.size == 16) {
          val bytes = bytesList.toArray
          val addr = Ipv6Address.fromBytes(bytes).get
          addr.toUncondensedString should have size (4 * 8 + 7)
        }
      }
    }

    "roundtrip through uncondensed strings" in {
      forAll(Gen.listOfN(16, Arbitrary.arbitrary[Byte])) { bytesList =>
        if (bytesList.size == 16) {
          val bytes = bytesList.toArray
          val addr = Ipv6Address.fromBytes(bytes).get
          Ipv6Address(addr.toUncondensedString) shouldBe Some(addr)
        }
      }
    }

    "support converting to mixed string form" in {
      forAll { (v4: Ipv4Address) =>
        v4.toCompatV6.toMixedString shouldBe ("::" + v4)
        v4.toMappedV6.toMixedString shouldBe ("::ffff:" + v4)
      }
    }

    "roundtrip through mixed strings" in {
      forAll(Gen.listOfN(16, Arbitrary.arbitrary[Byte])) { bytesList =>
        if (bytesList.size == 16) {
          val bytes = bytesList.toArray
          val addr = Ipv6Address.fromBytes(bytes).get
          Ipv6Address(addr.toMixedString) shouldBe Some(addr)
        }
      }
    }

    "roundtrip through BigInt" in {
      forAll(Gen.listOfN(16, Arbitrary.arbitrary[Byte])) { bytesList =>
        if (bytesList.size == 16) {
          val bytes = bytesList.toArray
          val addr = Ipv6Address.fromBytes(bytes).get
          Ipv6Address.fromBigInt(addr.toBigInt) shouldBe addr
        }
      }
    }

    "support ordering" in {
      forAll { (left: Ipv6Address, right: Ipv6Address) =>
        val bigIntCompare = left.toBigInt.compare(right.toBigInt)
        val result = Ordering[Ipv6Address].compare(left, right)
        result shouldBe bigIntCompare
      }
    }

    "support computing next IP" in {
      Ipv6Address("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff").map(_.next) shouldBe Ipv6Address("::")
      forAll { (ip: Ipv6Address) =>
        ip.next shouldBe Ipv6Address.fromBigInt(ip.toBigInt + 1)
      }
    }

    "support computing previous IP" in {
      Ipv6Address("::").map(_.previous) shouldBe Ipv6Address("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")
      forAll { (ip: Ipv6Address) =>
        ip.previous shouldBe Ipv6Address.fromBigInt(ip.toBigInt - 1)
      }
    }
  }
}
