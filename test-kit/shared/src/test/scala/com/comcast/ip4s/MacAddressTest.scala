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

import org.scalacheck.{Arbitrary, Gen, Prop}
import Prop.forAll
import Arbitraries._

class MacAddressTest extends BaseTestSuite {
  test("roundtrip through string") {
    forAll(Gen.listOfN(6, Arbitrary.arbitrary[Byte])) { bytesList =>
      if (bytesList.size == 6) {
        val bytes = bytesList.toArray
        val addr = MacAddress.fromBytes(bytes).get
        assertEquals(MacAddress.fromString(addr.toString), Some(addr))
      }
    }
  }

  test("support ordering") {
    forAll { (left: MacAddress, right: MacAddress) =>
      val longCompare = left.toLong.compare(right.toLong)
      val result = Ordering[MacAddress].compare(left, right)
      assertEquals(result, longCompare)
    }
  }
}
