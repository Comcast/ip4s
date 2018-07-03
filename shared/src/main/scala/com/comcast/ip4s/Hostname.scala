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

import scala.util.hashing.MurmurHash3

/**
  * RFC1123 compliant hostname.
  *
  * A hostname contains one or more labels, where each label consists of letters A-Z, a-z, digits 0-9, or a dash.
  * A label may not start or end in a dash and may not exceed 63 characters in length. Labels are separated by
  * periods and the overall hostname must not exceed 253 characters in length.
  */
final class Hostname private (private val head: Hostname.Label,
                              private val tail: List[Hostname.Label],
                              override val toString: String)
    extends Ordered[Hostname] {

  /** Gets the labels of this hostname. */
  def labels: (Hostname.Label, List[Hostname.Label]) = (head, tail)

  /** Gets the labels of this hostname as a list, guaranteed to have at least one element. */
  def labelsList: List[Hostname.Label] = head :: tail

  def compare(that: Hostname): Int = toString.compare(that.toString)
  override def hashCode: Int = MurmurHash3.stringHash(toString, "Hostname".hashCode)
  override def equals(other: Any): Boolean = other match {
    case that: Hostname => toString == that.toString
    case _              => false
  }
}

object Hostname {

  /**
    * Label component of a hostname.
    *
    * A label consists of letters A-Z, a-z, digits 0-9, or a dash. A label may not start or end in a
    * dash and may not exceed 63 characters in length.
    */
  final class Label private[Hostname] (val value: String) extends Product with Serializable with Ordered[Label] {
    def compare(that: Label): Int = value.compare(that.value)
    override def toString: String = value
    override def hashCode: Int = MurmurHash3.productHash(this, productPrefix.hashCode)
    override def equals(other: Any): Boolean = other match {
      case that: Label => value == that.value
      case _           => false
    }
    override def canEqual(other: Any): Boolean = other.isInstanceOf[Label]
    override def productArity: Int = 1
    override def productElement(n: Int): Any =
      if (n == 0) value else throw new IndexOutOfBoundsException
  }

  private val Pattern =
    """[a-zA-Z0-9](?:[a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?)*""".r

  /** Constructs a `Hostname` from a string. */
  def apply(value: String): Option[Hostname] = value.size match {
    case 0            => None
    case i if i > 253 => None
    case _ =>
      value match {
        case Pattern(_*) =>
          val labels = value
            .split('.')
            .iterator
            .map(new Label(_))
            .toList
          Some(new Hostname(labels.head, labels.tail, value))
        case _ => None
      }
  }
}
