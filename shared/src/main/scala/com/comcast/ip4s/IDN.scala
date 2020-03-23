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

import cats.{Eq, Order, Show}

/**
  * Internationalized domain name, as specified by RFC3490 and RFC5891.
  *
  * This type models internationalized hostnames. An IDN provides unicode labels, a unicode string form,
  * and an ASCII hostname form.
  *
  * A well formed IDN consists of one or more labels separated by dots. Each label may contain unicode characters
  * as long as the converted ASCII form meets the requirements of RFC1123 (e.g., 63 or fewer characters and no
  * leading or trailing dash). A dot is represented as an ASCII period or one of the unicode dots: full stop,
  * ideographic full stop, fullwidth full stop, halfwidth ideographic full stop.
  *
  * The `toString` method returns the IDN in the form in which it was constructed. Sometimes it is useful to
  * normalize the IDN -- converting all dots to an ASCII period and converting all labels to lowercase.
  *
  * Note: equality and comparison of IDNs is case-sensitive. Consider comparing normalized toString values
  * for a more lenient notion of equality.
  */
final class IDN private (val labels: List[IDN.Label], val hostname: Hostname, override val toString: String)
    extends Ordered[IDN] {

  /** Converts this IDN to lower case and replaces dots with ASCII periods. */
  def normalized: IDN = {
    val newLabels = labels.map(l => new IDN.Label(l.toString.toLowerCase))
    new IDN(newLabels, hostname.normalized, newLabels.toList.mkString("."))
  }

  def compare(that: IDN): Int = toString.compare(that.toString)
  override def hashCode: Int = MurmurHash3.stringHash(toString, "IDN".hashCode)
  override def equals(other: Any): Boolean = other match {
    case that: IDN => toString == that.toString
    case _         => false
  }
}

object IDN extends IDNCompanionPlatform {

  /** Label component of an IDN. */
  final class Label private[IDN] (override val toString: String) extends Serializable with Ordered[Label] {
    def compare(that: Label): Int = toString.compare(that.toString)
    override def hashCode: Int = MurmurHash3.stringHash(toString, "Label".hashCode)
    override def equals(other: Any): Boolean = other match {
      case that: Label => toString == that.toString
      case _           => false
    }
  }

  private val DotPattern = raw"[\.\u002e\u3002\uff0e\uff61]"

  /** Constructs a `IDN` from a string. */
  def apply(value: String): Option[IDN] = value.size match {
    case 0 => None
    case _ =>
      val labels = value
        .split(DotPattern)
        .iterator
        .map(new Label(_))
        .toList
      Option(labels).filterNot(_.isEmpty).flatMap { ls =>
        val hostname = toAscii(value).flatMap(Hostname(_))
        hostname.map(h => new IDN(ls, h, value))
      }
  }

  /** Converts the supplied (ASCII) hostname in to an IDN. */
  def fromHostname(hostname: Hostname): IDN = {
    val labels =
      hostname.labels.map(l => new Label(toUnicode(l.toString)))
    new IDN(labels, hostname, labels.toList.mkString("."))
  }

  implicit val eq: Eq[IDN] = Eq.fromUniversalEquals[IDN]
  implicit val order: Order[IDN] = Order.fromComparable[IDN]
  implicit val show: Show[IDN] = Show.fromToString[IDN]
}
