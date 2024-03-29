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

import org.scalacheck.Prop.forAll
import Arbitraries._

class MulticastTest extends BaseTestSuite {
  test("support equality") {
    forAll { (mip: Multicast[IpAddress]) =>
      assertEquals(mip.address.asMulticast, Some(mip))
      mip.address.asSourceSpecificMulticastLenient.foreach(x => assertEquals(mip, x))
      mip.address.asSourceSpecificMulticastLenient.foreach(x => assert(x == mip))
    }
  }

  test("support SSM outside source specific range") {
    assertEquals(ip"239.10.10.10".asSourceSpecificMulticast, None)
    assertEquals(
      ip"239.10.10.10".asSourceSpecificMulticastLenient,
      Some(SourceSpecificMulticast.unsafeCreate(ip"239.10.10.10"))
    )
  }
}
