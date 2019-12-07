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

import Arbitraries._

class HostnameTest extends BaseTestSuite {
  "Hostname" should {
    "roundtrip through string" in {
      forAll { (h: Hostname) =>
        Hostname(h.toString) shouldBe Some(h)
      }
    }

    "allow access to labels" in {
      forAll { (h: Hostname) =>
        Hostname(h.labels.toList.mkString(".")) shouldBe Some(h)
      }
    }

    "require overall length be less than 254 chars" in {
      forAll { (h: Hostname) =>
        val hstr = h.toString
        val h2 = hstr + "." + hstr
        val expected = if (h2.length > 253) None else Some(Hostname(h2).get)
        Hostname(h2) shouldBe expected
      }
    }

    "require labels be less than 64 chars" in {
      forAll { (h: Hostname) =>
        val hstr = h.toString
        val suffix = new String(Array.fill(63)(hstr.last))
        val tooLong = hstr + suffix
        Hostname(tooLong) shouldBe None
      }
    }

    "disallow labels that end in a dash" in {
      forAll { (h: Hostname) =>
        val hstr = h.toString
        val disallowed = hstr + "-"
        Hostname(disallowed) shouldBe None
      }
    }

    "disallow labels that start with a dash" in {
      forAll { (h: Hostname) =>
        val hstr = h.toString
        val disallowed = "-" + hstr
        Hostname(disallowed) shouldBe None
      }
    }
  }
}
