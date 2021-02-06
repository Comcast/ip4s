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

import cats.{Order, Show}

/** Witness that the wrapped address of type `A` is a multicast address.
  *
  * An instance of `Multicast` is typically created by either calling `Multicast.apply` or by using
  * the `asMulticast` method on `IpAddress`.
  */
sealed trait Multicast[+A <: IpAddress] extends Product with Serializable {
  def address: A
}

object Multicast {
  private case class DefaultMulticast[+A <: IpAddress](address: A) extends Multicast[A] {
    override def toString: String = address.toString
  }

  /** Constructs a multicast IP address. Returns `None` is the supplied address is not in the valid multicast range. */
  def fromIpAddress[A <: IpAddress](address: A): Option[Multicast[A]] =
    if (address.isSourceSpecificMulticast) Some(SourceSpecificMulticast.unsafeCreate(address))
    else if (address.isMulticast) Some(DefaultMulticast(address))
    else None

  implicit def order[J[x <: IpAddress] <: Multicast[x], A <: IpAddress]: Order[J[A]] =
    Order.fromOrdering(Multicast.ordering[J, A])
  implicit def ordering[J[x <: IpAddress] <: Multicast[x], A <: IpAddress]: Ordering[J[A]] = Ordering.by(_.address)
  implicit def show[J[x <: IpAddress] <: Multicast[x], A <: IpAddress]: Show[J[A]] =
    Show.fromToString[J[A]]
}

/** Witnesses that the wrapped address of type `A` is a source specific multicast address.
  *
  * An instance of `SourceSpecificMulticast` is typically created by either calling `Multicast.apply`
  * or by using the `asSourceSpecificMulticast` method on `IpAddress`.
  */
sealed trait SourceSpecificMulticast[+A <: IpAddress] extends Multicast[A] {
  override def toString: String = address.toString
}

object SourceSpecificMulticast {
  private case class DefaultSourceSpecificMulticast[+A <: IpAddress](address: A) extends SourceSpecificMulticast[A] {
    override def toString: String = address.toString
  }

  /** Constructs a source specific multicast IP address. Returns `None` is the supplied address is not in the valid source specific multicast range. */
  def fromIpAddress[A <: IpAddress](address: A): Option[SourceSpecificMulticast[A]] =
    if (address.isSourceSpecificMulticast) Some(new DefaultSourceSpecificMulticast(address)) else None

  private[ip4s] def unsafeCreate[A <: IpAddress](address: A): SourceSpecificMulticast[A] =
    DefaultSourceSpecificMulticast(address)

  implicit def ordering[A <: IpAddress]: Ordering[SourceSpecificMulticast[A]] =
    Multicast.ordering[SourceSpecificMulticast, A]
}
