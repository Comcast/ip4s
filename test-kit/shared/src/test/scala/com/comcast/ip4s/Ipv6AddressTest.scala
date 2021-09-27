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

import org.scalacheck.Prop.forAll
import Arbitraries._

class Ipv6AddressTest extends BaseTestSuite {
  test("parsing from string - does not parse the empty string") {
    assertEquals(Ipv6Address.fromString(""), None)
  }

  test("parsing from string - does not parse white space string") {
    assertEquals(Ipv6Address.fromString(" "), None)
  }

  test("parsing from string - does not parse a single :") {
    assertEquals(Ipv6Address.fromString(":"), None)
    assertEquals(Ipv6Address.fromString(" : "), None)
  }

  test("parsing from string - does parse ::") {
    assertEquals(Ipv6Address.fromString("::").isDefined, true)
    assertEquals(Ipv6Address.fromString(" :: ").isDefined, true)
  }

  test("parsing from string - supports mixed strings") {
    forAll { (v4: Ipv4Address) =>
      assertEquals(Ipv6Address.fromString("::" + v4), Some(v4.toCompatV6))
      assertEquals(Ipv6Address.fromString("::ffff:" + v4), Some(v4.toMappedV6))
    }
  }

  test("parsing from string - does not misinterpret hosts") {
    assertEquals(Ipv6Address.fromString("db"), None)
  }

  test("support converting to uncondensed string form") {
    forAll(Gen.listOfN(16, Arbitrary.arbitrary[Byte])) { bytesList =>
      if (bytesList.size == 16) {
        val bytes = bytesList.toArray
        val addr = Ipv6Address.fromBytes(bytes).get
        assert(addr.toUncondensedString.size == 4 * 8 + 7)
      }
    }
  }

  test("roundtrip through uncondensed strings") {
    forAll(Gen.listOfN(16, Arbitrary.arbitrary[Byte])) { bytesList =>
      if (bytesList.size == 16) {
        val bytes = bytesList.toArray
        val addr = Ipv6Address.fromBytes(bytes).get
        assertEquals(Ipv6Address.fromString(addr.toUncondensedString), Some(addr))
      }
    }
  }

  test("support converting to mixed string form") {
    forAll { (v4: Ipv4Address) =>
      assertEquals(v4.toCompatV6.toMixedString, "::" + v4)
      assertEquals(v4.toMappedV6.toMixedString, "::ffff:" + v4)
    }
  }

  test("roundtrip through mixed strings") {
    forAll(Gen.listOfN(16, Arbitrary.arbitrary[Byte])) { bytesList =>
      if (bytesList.size == 16) {
        val bytes = bytesList.toArray
        val addr = Ipv6Address.fromBytes(bytes).get
        assertEquals(Ipv6Address.fromString(addr.toMixedString), Some(addr))
      }
    }
  }

  test("roundtrip through BigInt") {
    forAll(Gen.listOfN(16, Arbitrary.arbitrary[Byte])) { bytesList =>
      if (bytesList.size == 16) {
        val bytes = bytesList.toArray
        val addr = Ipv6Address.fromBytes(bytes).get
        assertEquals(Ipv6Address.fromBigInt(addr.toBigInt), addr)
      }
    }
  }

  test("support ordering") {
    forAll { (left: Ipv6Address, right: Ipv6Address) =>
      val bigIntCompare = left.toBigInt.compare(right.toBigInt)
      val result = Ordering[Ipv6Address].compare(left, right)
      assertEquals(result, bigIntCompare)
    }
  }

  test("support computing next IP") {
    assertEquals(
      Ipv6Address.fromString("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff").map(_.next),
      Ipv6Address.fromString("::")
    )
    forAll { (ip: Ipv6Address) => assertEquals(ip.next, Ipv6Address.fromBigInt(ip.toBigInt + 1)) }
  }

  test("support computing previous IP") {
    assertEquals(
      Ipv6Address.fromString("::").map(_.previous),
      Ipv6Address.fromString("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")
    )
    forAll { (ip: Ipv6Address) => assertEquals(ip.previous, Ipv6Address.fromBigInt(ip.toBigInt - 1)) }
  }

  test("converting V4 mapped address") {
    val addr = ip"::ffff:f:f"
    assertEquals[Any, Any](addr.getClass, classOf[Ipv6Address])
    assertEquals(addr.version, IpVersion.V6)
    assertEquals(addr.toString, "::ffff:f:f")
    assertEquals[Any, Any](addr.collapseMappedV4.getClass, classOf[Ipv4Address])
    assertEquals[Any, Any](addr.asIpv6, Some(addr))
    assertEquals[Any, Any](addr.asIpv4, Some(ip"0.15.0.15"))
  }
}
