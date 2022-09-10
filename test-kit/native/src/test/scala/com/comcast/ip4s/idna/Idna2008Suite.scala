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

// https://github.com/libuv/libuv/blob/97dcdb1926f6aca43171e1614338bcef067abd59/test/test-idna.c

/* Copyright The libuv project and contributors. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.comcast.ip4s
package idna

import scala.util.Try

class Idna2008Suite extends BaseTestSuite {

  case class TestCase(decoded: String, encoded: Option[String])
  object TestCase {
    def apply(decoded: String, encoded: String): TestCase =
      apply(decoded, Some(encoded))
  }

  val testData = List(
    // No conversion
    TestCase("", ""),
    TestCase(".", "."),
    TestCase(".com", ".com"),
    TestCase("example", "example"),
    TestCase("example-", "example-"),
    TestCase("straße.de", "xn--strae-oqa.de"),
    // Test cases adapted from punycode.js. Most are from RFC 3492.
    TestCase("foo.bar", "foo.bar"),
    TestCase("mañana.com", "xn--maana-pta.com"),
    TestCase("example.com.", "example.com."),
    TestCase("bücher.com", "xn--bcher-kva.com"),
    TestCase("café.com", "xn--caf-dma.com"),
    TestCase("café.café.com", "xn--caf-dma.xn--caf-dma.com"),
    TestCase("☃-⌘.com", "xn----dqo34k.com"),
    TestCase("퐀☃-⌘.com", "xn----dqo34kn65z.com"),
    TestCase("💩.la", "xn--ls8h.la"),
    TestCase("mañana.com", "xn--maana-pta.com"),
    TestCase("mañana。com", "xn--maana-pta.com"),
    TestCase("mañana．com", "xn--maana-pta.com"),
    TestCase("mañana｡com", "xn--maana-pta.com"),
    TestCase("ü", "xn--tda"),
    TestCase(".ü", ".xn--tda"),
    TestCase("ü.ü", "xn--tda.xn--tda"),
    TestCase("ü.ü.", "xn--tda.xn--tda."),
    TestCase("üëäö♥", "xn--4can8av2009b"),
    TestCase(
      "Willst du die Blüthe des frühen, die Früchte des späteren Jahres",
      "xn--Willst du die Blthe des frhen, die Frchte des spteren Jahres-x9e96lkal"
    ),
    TestCase("ليهمابتكلموشعربي؟", "xn--egbpdaj6bu4bxfgehfvwxn"),
    TestCase("他们为什么不说中文", "xn--ihqwcrb4cv8a8dqg056pqjye"),
    TestCase("他們爲什麽不說中文", "xn--ihqwctvzc91f659drss3x8bo0yb"),
    TestCase("Pročprostěnemluvíčesky", "xn--Proprostnemluvesky-uyb24dma41a"),
    TestCase("למההםפשוטלאמדבריםעברית", "xn--4dbcagdahymbxekheh6e0a7fei0b"),
    TestCase("यहलोगहिन्दीक्योंनहींबोलसकतेहैं", "xn--i1baa7eci9glrd9b2ae1bj0hfcgg6iyaf8o0a1dig0cd"),
    TestCase("なぜみんな日本語を話してくれないのか", "xn--n8jok5ay5dzabd5bym9f0cm5685rrjetr6pdxa"),
    TestCase("세계의모든사람들이한국어를이해한다면얼마나좋을까", "xn--989aomsvi5e83db1d2a355cv1e0vak1dwrv93d5xbh15a0dt30a5jpsd879ccm6fea98c"),
    TestCase("почемужеонинеговорятпорусски", "xn--b1abfaaepdrnnbgefbadotcwatmq2g4l"),
    TestCase("PorquénopuedensimplementehablarenEspañol", "xn--PorqunopuedensimplementehablarenEspaol-fmd56a"),
    TestCase("TạisaohọkhôngthểchỉnóitiếngViệt", "xn--TisaohkhngthchnitingVit-kjcr8268qyxafd2f1b9g"),
    TestCase("3年B組金八先生", "xn--3B-ww4c5e180e575a65lsy2b"),
    TestCase("安室奈美恵-with-SUPER-MONKEYS", "xn---with-SUPER-MONKEYS-pc58ag80a8qai00g7n9n"),
    TestCase("Hello-Another-Way-それぞれの場所", "xn--Hello-Another-Way--fc4qua05auwb3674vfr0b"),
    TestCase("ひとつ屋根の下2", "xn--2-u9tlzr9756bt3uc0v"),
    TestCase("MajiでKoiする5秒前", "xn--MajiKoi5-783gue6qz075azm5e"),
    TestCase("パフィーdeルンバ", "xn--de-jg4avhby1noc0d"),
    TestCase("そのスピードで", "xn--d9juau41awczczp"),
    TestCase("-> $1.00 <-", "-> $1.00 <-"),
    // Test cases from https://unicode.org/reports/tr46/
    TestCase("faß.de", "xn--fa-hia.de"),
    TestCase("βόλος.com", "xn--nxasmm1c.com"),
    TestCase("ශ්‍රී.com", "xn--10cl1a0b660p.com"),
    TestCase("نامه‌ای.com", "xn--mgba3gch31f060k.com")
  )

  group("toAscii") {
    testData.foreach { case TestCase(decoded, encoded) =>
      test(decoded) {
        assertEquals(Try(Idna2008.toAscii(decoded)).toOption, encoded)
      }
    }
  }

  group("toUnicode") {
    testData.foreach {
      case TestCase(decoded, Some(encoded)) =>
        test(encoded) {
          assertEquals(Idna2008.toUnicode(encoded), decoded.replaceAll("[\u002e\u3002\uff0e\uff61]", "."))
        }
      case _ => ()
    }
  }

}
