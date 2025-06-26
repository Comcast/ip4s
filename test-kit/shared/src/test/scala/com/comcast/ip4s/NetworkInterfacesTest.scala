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

import cats.effect.IO
import cats.syntax.all.*
import munit.CatsEffectSuite

class NetworkInterfacesTest extends CatsEffectSuite {

  test("getAll") {
    assume(!TestPlatform.isNative) // TODO Remove after upgrading to SN 0.5
    NetworkInterfaces[IO].getAll.map { nis =>
      assert(nis.nonEmpty)
    }
  }

  test("getByName") {
    assume(!TestPlatform.isNative) // TODO Remove after upgrading to SN 0.5
    NetworkInterfaces[IO].getAll.flatMap { nis =>
      nis.values.toList.traverse { ni =>
        NetworkInterfaces[IO].getByName(ni.name).map { nni =>
          assertEquals(nni, Some(ni))
        }
      }
    }
  }

  test("getByAddress") {
    assume(!TestPlatform.isNative) // TODO Remove after upgrading to SN 0.5
    NetworkInterfaces[IO].getAll.flatMap { nis =>
      nis.values.toList.traverse { ni =>
        ni.addresses.traverse { cidr =>
          NetworkInterfaces[IO].getByAddress(cidr.address).map { nni =>
            assertEquals(nni, Some(ni))
          }
        }
      }
    }
  }

  test("getByMacAddress") {
    assume(!TestPlatform.isNative) // TODO Remove after upgrading to SN 0.5
    NetworkInterfaces[IO].getAll.flatMap { nis =>
      nis.values.toList.traverse { ni =>
        ni.macAddress.traverse { mac =>
          NetworkInterfaces[IO].getByMacAddress(mac).map { nni =>
            assert(nni.contains(ni))
          }
        }
      }
    }
  }
}
