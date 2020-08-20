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

class HostnameTest extends BaseTestSuite {
  test("roundtrip through string") {
    forAll { (h: Hostname) => assertEquals(Hostname(h.toString), Some(h)) }
  }

  test("allow access to labels") {
    forAll { (h: Hostname) => assertEquals(Hostname(h.labels.toList.mkString(".")), Some(h)) }
  }

  test("require overall length be less than 254 chars") {
    forAll { (h: Hostname) =>
      val hstr = h.toString
      val h2 = hstr + "." + hstr
      val expected = if (h2.length > 253) None else Some(Hostname(h2).get)
      assertEquals(Hostname(h2), expected)
    }
  }

  test("require labels be less than 64 chars") {
    forAll { (h: Hostname) =>
      val hstr = h.toString
      val suffix = new String(Array.fill(63)(hstr.last))
      val tooLong = hstr + suffix
      assertEquals(Hostname(tooLong), None)
    }
  }

  test("disallow labels that end in a dash") {
    forAll { (h: Hostname) =>
      val hstr = h.toString
      val disallowed = hstr + "-"
      assertEquals(Hostname(disallowed), None)
    }
  }

  test("disallow labels that start with a dash") {
    forAll { (h: Hostname) =>
      val hstr = h.toString
      val disallowed = "-" + hstr
      assertEquals(Hostname(disallowed), None)
    }
  }
}
