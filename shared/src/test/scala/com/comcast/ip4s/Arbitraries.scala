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

object Arbitraries {

  val ipv4Generator: Gen[Ipv4Address] = for {
    bytes <- Gen.listOfN(4, Arbitrary.arbitrary[Byte])
  } yield Ipv4Address.fromBytes(bytes.toArray).get

  implicit val ipv4Arbitrary: Arbitrary[Ipv4Address] = Arbitrary(ipv4Generator)

  val ipv6Generator: Gen[Ipv6Address] = for {
    bytes <- Gen.listOfN(16, Arbitrary.arbitrary[Byte])
  } yield Ipv6Address.fromBytes(bytes.toArray).get

  implicit val ipv6Arbitrary: Arbitrary[Ipv6Address] = Arbitrary(ipv6Generator)

  val ipGenerator: Gen[IpAddress] = Gen.oneOf(ipv4Generator, ipv6Generator)

  implicit val ipArbitrary: Arbitrary[IpAddress] = Arbitrary(ipGenerator)

  def cidrGenerator[A <: IpAddress](genIp: Gen[A]): Gen[Cidr[A]] =
    for {
      ip <- genIp
      bitLength = ip.fold(_ => 32, _ => 128)
      prefix <- Gen.chooseNum(0, bitLength)
    } yield ip / prefix

  implicit def cidrArbitrary[A <: IpAddress](implicit arbIp: Arbitrary[A]): Arbitrary[Cidr[A]] =
    Arbitrary(cidrGenerator(arbIp.arbitrary))

  val portGenerator: Gen[Port] = Gen.chooseNum(0, 65535).map(Port(_).get)

  implicit val portArbitrary: Arbitrary[Port] = Arbitrary(portGenerator)

  def socketAddressGenerator[A <: IpAddress](genIp: Gen[A], genPort: Gen[Port]): Gen[SocketAddress[A]] =
    for { 
      ip <- genIp
      port <- genPort
    } yield SocketAddress(ip, port)

  implicit def socketAddressArbitrary[A <: IpAddress](implicit arbIp: Arbitrary[A], arbPort: Arbitrary[Port]): Arbitrary[SocketAddress[A]] =
    Arbitrary(socketAddressGenerator(arbIp.arbitrary, arbPort.arbitrary))

  val multicastGenerator4: Gen[Multicast[Ipv4Address]] = for {
    ip <- ipv4Generator
  } yield Ipv4Address.fromLong(ip.toLong & ~(15 << 28) | (14 << 28)).asMulticast.get

  implicit val multicastArbitrary4: Arbitrary[Multicast[Ipv4Address]] = Arbitrary(multicastGenerator4)

  val multicastGenerator6: Gen[Multicast[Ipv6Address]] = for {
    ip <- ipv6Generator
  } yield Ipv6Address.fromBigInt(ip.toBigInt & ~(BigInt(255) << 120) | (BigInt(255) << 120)).asMulticast.get

  implicit val multicastArbitrary6: Arbitrary[Multicast[Ipv6Address]] = Arbitrary(multicastGenerator6)

  val multicastGenerator: Gen[Multicast[IpAddress]] = Gen.oneOf(multicastGenerator4, multicastGenerator6)
    
  implicit val multicastArbitrary: Arbitrary[Multicast[IpAddress]] = Arbitrary(multicastGenerator)

  def multicastJoinGenerator[A <: IpAddress](genSource: Gen[A], genGroup: Gen[Multicast[A]]): Gen[MulticastJoin[A]] =
    genGroup.flatMap { group =>
      group.address.asSourceSpecificMulticast match {
        case Some(grp) => genSource.filter(_.getClass == grp.getClass).flatMap(src => MulticastJoin.ssm(src, grp))
        case None => MulticastJoin.asm(group)
      }
    }

  implicit def multicastJoinArbitrary[A <: IpAddress](implicit arbSource: Arbitrary[A], arbGroup: Arbitrary[Multicast[A]]): Arbitrary[MulticastJoin[A]] =
    Arbitrary(multicastJoinGenerator(arbSource.arbitrary, arbGroup.arbitrary))

  def multicastSocketAddressGenerator[A <: IpAddress](genJoin: Gen[MulticastJoin[A]], genPort: Gen[Port]): Gen[MulticastSocketAddress[MulticastJoin, A]] =
    for { 
      join <- genJoin
      port <- genPort
    } yield MulticastSocketAddress(join, port)

  implicit def multicastSocketAddressArbitrary[A <: IpAddress](implicit arbJoin: Arbitrary[MulticastJoin[A]], arbPort: Arbitrary[Port]): Arbitrary[MulticastSocketAddress[MulticastJoin, A]] =
    Arbitrary(multicastSocketAddressGenerator(arbJoin.arbitrary, arbPort.arbitrary))
}