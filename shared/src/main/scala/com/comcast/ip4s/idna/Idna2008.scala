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

package com.comcast.ip4s.idna

private[idna] object Idna2008 {

  private[this] val dot = "[\u002e\u3002\uff0e\uff61]".r.pattern

  def toAscii(domain: String): String = {
    dot
      .split(domain, -1)
      .map { label =>
        if (label.forall(_ < 128)) label
        else "xn--" + Punycode.encode(label, null)
      }
      .mkString(".")
  }

  def toUnicode(domain: String): String = {
    dot
      .split(domain, -1)
      .map { label =>
        if (label.startsWith("xn--")) Punycode.decode(label.subSequence(4, label.length), null)
        else label
      }
      .mkString(".")
  }

}
