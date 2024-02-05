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

class Ipv4AddressTest extends BaseTestSuite {
  test("parsing from string - does not parse the empty string") {
    assertEquals(Ipv4Address.fromString(""), None)
  }

  test("parsing from string - does not parse white space string") {
    assertEquals(Ipv4Address.fromString(" "), None)
  }

  test("roundtrip through string") {
    forAll(Gen.listOfN(4, Arbitrary.arbitrary[Byte])) { bytesList =>
      if (bytesList.size == 4) {
        val bytes = bytesList.toArray
        val addr = Ipv4Address.fromBytes(bytes).get
        assertEquals(Ipv4Address.fromString(addr.toString), Some(addr))
      }
    }
  }

  test("roundtrip through long") {
    forAll(Gen.listOfN(4, Arbitrary.arbitrary[Byte])) { bytesList =>
      if (bytesList.size == 4) {
        val bytes = bytesList.toArray
        val addr = Ipv4Address.fromBytes(bytes).get
        assertEquals(Ipv4Address.fromLong(addr.toLong), addr)
      }
    }
  }

  test("support ordering") {
    forAll { (left: Ipv4Address, right: Ipv4Address) =>
      val longCompare = left.toLong.compare(right.toLong)
      val result = Ordering[Ipv4Address].compare(left, right)
      assertEquals(result, longCompare)
    }
  }

  test("support computing next IP") {
    assertEquals(Ipv4Address.fromString("255.255.255.255").map(_.next), Ipv4Address.fromString("0.0.0.0"))
    forAll { (ip: Ipv4Address) => assertEquals(ip.next, Ipv4Address.fromLong(ip.toLong + 1)) }
  }

  test("support computing previous IP") {
    assertEquals(Ipv4Address.fromString("0.0.0.0").map(_.previous), Ipv4Address.fromString("255.255.255.255"))
    forAll { (ip: Ipv4Address) => assertEquals(ip.previous, Ipv4Address.fromLong(ip.toLong - 1)) }
  }

  test("isPrivate") {
    assert(!ipv4"10.0.0.0".previous.isPrivate)
    assert(ipv4"10.0.0.0".isPrivate)
    assert(ipv4"10.255.255.255".isPrivate)
    assert(!ipv4"10.255.255.255".next.isPrivate)

    assert(!ipv4"172.16.0.0".previous.isPrivate)
    assert(ipv4"172.16.0.0".isPrivate)
    assert(ipv4"172.31.255.255".isPrivate)
    assert(!ipv4"172.31.255.255".next.isPrivate)

    assert(!ipv4"192.168.0.0".previous.isPrivate)
    assert(ipv4"192.168.0.0".isPrivate)
    assert(ipv4"192.168.255.255".isPrivate)
    assert(!ipv4"192.168.255.255".next.isPrivate)
  }
}
