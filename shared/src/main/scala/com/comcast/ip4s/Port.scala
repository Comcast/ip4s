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

import scala.util.Try
import scala.util.hashing.MurmurHash3

import cats.{Order, Show}

/** TCP or UDP port number. */
final class Port private (val value: Int) extends Product with Serializable with Ordered[Port] {
  def copy(value: Int): Option[Port] = Port(value)
  def compare(that: Port): Int = value.compare(that.value)
  override def toString: String = value.toString
  override def hashCode: Int = MurmurHash3.productHash(this, productPrefix.hashCode)
  override def equals(other: Any): Boolean = other match {
    case that: Port => value == that.value
    case _          => false
  }
  override def canEqual(other: Any): Boolean = other.isInstanceOf[Port]
  override def productArity: Int = 1
  override def productElement(n: Int): Any =
    if (n == 0) value else throw new IndexOutOfBoundsException
}

object Port {
  val MinValue: Int = 0
  val MaxValue: Int = 65535

  def apply(value: Int): Option[Port] =
    if (value >= MinValue && value <= MaxValue) Some(new Port(value)) else None

  def fromString(value: String): Option[Port] =
    Try(value.toInt).toOption.flatMap(apply)

  def unapply(p: Port): Option[Int] = Some(p.value)

  implicit val order: Order[Port] = Order.fromComparable[Port]
  implicit val show: Show[Port] = Show.fromToString[Port]
}
