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

/**
  * Witness that the wrapped address of type `A` is a multicast address.
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
  def apply[A <: IpAddress](address: A): Option[Multicast[A]] =
    if (address.isMulticast) Some(DefaultMulticast(address)) else None

  implicit def ordering[A <: IpAddress]: Ordering[Multicast[A]] = Ordering.by(_.address)
}

/**
  * Witnesses that the wrapped address of type `A` is a source specific multicast address.
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
  def apply[A <: IpAddress](address: A): Option[SourceSpecificMulticast[A]] =
    if (address.isSourceSpecificMulticast) Some(DefaultSourceSpecificMulticast(address)) else None

  implicit def ordering[A <: IpAddress]: Ordering[SourceSpecificMulticast[A]] =
    new Ordering[SourceSpecificMulticast[A]] {
      override def compare(x: SourceSpecificMulticast[A], y: SourceSpecificMulticast[A]): Int =
        Ordering[A].compare(x.address, y.address)
    }
}
