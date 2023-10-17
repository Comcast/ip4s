package com.comcast.ip4s

import org.scalacheck.Prop.forAll
import Arbitraries._

class CidrStrictTest extends BaseTestSuite {
  property("prefix and address are identical") {
    forAll { (cidr: Cidr.Strict[IpAddress]) => assertEquals(cidr.address, cidr.prefix) }
  }
}
