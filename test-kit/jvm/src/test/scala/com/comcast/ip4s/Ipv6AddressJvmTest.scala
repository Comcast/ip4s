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

import com.google.common.net.InetAddresses
import java.net.{InetAddress, Inet6Address}
import org.scalacheck.{Arbitrary, Gen, Test}

import org.scalacheck.Prop.forAll
import Arbitraries._

class Ipv6AddressJvmTest extends BaseTestSuite {
  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters.withMinSuccessfulTests(10000)

  test("to string - roundtrips through strings") {
    forAll(Gen.listOfN(16, Arbitrary.arbitrary[Byte])) { bytesList =>
      if (bytesList.size == 16) {
        val bytes = bytesList.toArray
        val str =
          InetAddresses.toAddrString(InetAddress.getByAddress(bytes))
        assertEquals(Ipv6Address(str).map(_.toString), Some(str))
      }
    }
  }

  test("to string - follows RFC5952") {
    forAll(Gen.listOfN(16, Arbitrary.arbitrary[Byte])) { bytesList =>
      if (bytesList.size == 16) {
        val bytes = bytesList.toArray
        val expected =
          InetAddresses.toAddrString(InetAddress.getByAddress(bytes))
        assertEquals(Ipv6Address.fromBytes(bytes).map(_.toString), Some(expected))
      }
    }
  }

  test("support conversion to Inet6Address") {
    forAll { (ip: Ipv6Address) => assert(ip.toInetAddress.isInstanceOf[Inet6Address]) }
  }
}
