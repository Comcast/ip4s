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

import scalajs._
import scala.scalajs.js.annotation._
import scala.util.Try

private[ip4s] trait IDNCompanionPlatform {

  private[ip4s] def toAscii(value: String): Option[String] =
    Try(Punycode.toASCII(value)).toOption

  private[ip4s] def toUnicode(value: String): String =
    Try(Punycode.toUnicode(value)).toOption.getOrElse(value)
}

@js.native
@JSImport("punycode", JSImport.Default)
private[ip4s] object Punycode extends js.Any {
  def toASCII(value: String): String = js.native
  def toUnicode(value: String): String = js.native
}
