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

import org.typelevel.idna4s.core.bootstring.Bootstring
import org.typelevel.idna4s.core.bootstring.BootstringParams.PunycodeParams
import cats.syntax.all._

private[ip4s] trait IDNCompanionPlatform {

  private[this] val DotPattern = "[\u002e\u3002\uff0e\uff61]".r.pattern

  private[ip4s] def toAscii(value: String): Option[String] =
    DotPattern
      .split(value, -1)
      .toList
      .traverse { label =>
        if (label.forall(_ < 128)) Some(label)
        else Bootstring.encodeRaw(PunycodeParams)(label).toOption.map("xn--" + _)
      }
      .map(_.mkString("."))

  private[ip4s] def toUnicode(value: String): String =
    DotPattern
      .split(value, -1)
      .toList
      .traverse { label =>
        if (label.startsWith("xn--")) Bootstring.decodeRaw(PunycodeParams)(label.substring(4, label.length))
        else Right(label)
      }
      .map(_.mkString("."))
      .fold(e => throw new IllegalArgumentException(e), identity[String](_))
}
