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
package interop.cats

import _root_.cats.effect.IO

class HostnameJvmTest extends BaseTestSuite {
  "Hostname" should {
    "support ip resolution" in {
      val localhost = Hostname("localhost").get
      val ip = HostnameResolver.resolve[IO](localhost).unsafeRunSync
      ip shouldBe 'defined
      val allIps = HostnameResolver.resolveAll[IO](localhost).unsafeRunSync
      allIps.get.toList should contain(ip.get)
    }
  }
}
