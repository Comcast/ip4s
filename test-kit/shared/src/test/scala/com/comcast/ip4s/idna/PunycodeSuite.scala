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

// https://github.com/mathiasbynens/punycode.js/blob/6cd1ddd078176a5b2afdf09c60633217e2009e53/tests/tests.js

/* Copyright Mathias Bynens <https://mathiasbynens.be/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.comcast.ip4s
package idna

class PunycodeSuite extends BaseTestSuite {

  case class TestCase(description: Option[String], decoded: String, encoded: String)
  object TestCase {
    def apply(description: String, decoded: String, encoded: String): TestCase =
      apply(Some(description), decoded, encoded)

    def apply(decoded: String, encoded: String): TestCase =
      apply(None, decoded, encoded)
  }

  val testData = List(
    TestCase(
      "a single basic code point",
      "Bach",
      "Bach-"
    ),
    TestCase(
      "a single non-ASCII character",
      "\u00FC",
      "tda"
    ),
    TestCase(
      "multiple non-ASCII characters",
      "\u00FC\u00EB\u00E4\u00F6\u2665",
      "4can8av2009b"
    ),
    TestCase(
      "mix of ASCII and non-ASCII characters",
      "b\u00FCcher",
      "bcher-kva"
    ),
    TestCase(
      "long string with both ASCII and non-ASCII characters",
      "Willst du die Bl\u00FCthe des fr\u00FChen, die Fr\u00FCchte des sp\u00E4teren Jahres",
      "Willst du die Blthe des frhen, die Frchte des spteren Jahres-x9e96lkal"
    ),
    // https://tools.ietf.org/html/rfc3492#section-7.1
    TestCase(
      "Arabic (Egyptian)",
      "\u0644\u064A\u0647\u0645\u0627\u0628\u062A\u0643\u0644\u0645\u0648\u0634\u0639\u0631\u0628\u064A\u061F",
      "egbpdaj6bu4bxfgehfvwxn"
    ),
    TestCase(
      "Chinese (simplified)",
      "\u4ED6\u4EEC\u4E3A\u4EC0\u4E48\u4E0D\u8BF4\u4E2d\u6587",
      "ihqwcrb4cv8a8dqg056pqjye"
    ),
    TestCase(
      "Chinese (traditional)",
      "\u4ED6\u5011\u7232\u4EC0\u9EBD\u4E0D\u8AAA\u4E2D\u6587",
      "ihqwctvzc91f659drss3x8bo0yb"
    ),
    TestCase(
      "Czech",
      "Pro\u010Dprost\u011Bnemluv\u00ED\u010Desky",
      "Proprostnemluvesky-uyb24dma41a"
    ),
    TestCase(
      "Hebrew",
      "\u05DC\u05DE\u05D4\u05D4\u05DD\u05E4\u05E9\u05D5\u05D8\u05DC\u05D0\u05DE\u05D3\u05D1\u05E8\u05D9\u05DD\u05E2\u05D1\u05E8\u05D9\u05EA",
      "4dbcagdahymbxekheh6e0a7fei0b"
    ),
    TestCase(
      "Hindi (Devanagari)",
      "\u092F\u0939\u0932\u094B\u0917\u0939\u093F\u0928\u094D\u0926\u0940\u0915\u094D\u092F\u094B\u0902\u0928\u0939\u0940\u0902\u092C\u094B\u0932\u0938\u0915\u0924\u0947\u0939\u0948\u0902",
      "i1baa7eci9glrd9b2ae1bj0hfcgg6iyaf8o0a1dig0cd"
    ),
    TestCase(
      "Japanese (kanji and hiragana)",
      "\u306A\u305C\u307F\u3093\u306A\u65E5\u672C\u8A9E\u3092\u8A71\u3057\u3066\u304F\u308C\u306A\u3044\u306E\u304B",
      "n8jok5ay5dzabd5bym9f0cm5685rrjetr6pdxa"
    ),
    TestCase(
      "Korean (Hangul syllables)",
      "\uC138\uACC4\uC758\uBAA8\uB4E0\uC0AC\uB78C\uB4E4\uC774\uD55C\uAD6D\uC5B4\uB97C\uC774\uD574\uD55C\uB2E4\uBA74\uC5BC\uB9C8\uB098\uC88B\uC744\uAE4C",
      "989aomsvi5e83db1d2a355cv1e0vak1dwrv93d5xbh15a0dt30a5jpsd879ccm6fea98c"
    ),
    /** As there's no way to do it in JavaScript, Punycode.js doesn't support
      * mixed-case annotation (which is entirely optional as per the RFC).
      * So, while the RFC sample string encodes to:
      * `b1abfaaepdrnnbgefbaDotcwatmq2g4l`
      * Without mixed-case annotation it has to encode to:
      * `b1abfaaepdrnnbgefbadotcwatmq2g4l`
      * https://github.com/bestiejs/punycode.js/issues/3
      */
    TestCase(
      "Russian (Cyrillic)",
      "\u043F\u043E\u0447\u0435\u043C\u0443\u0436\u0435\u043E\u043D\u0438\u043D\u0435\u0433\u043E\u0432\u043E\u0440\u044F\u0442\u043F\u043E\u0440\u0443\u0441\u0441\u043A\u0438",
      "b1abfaaepdrnnbgefbadotcwatmq2g4l"
    ),
    TestCase(
      "Spanish",
      "Porqu\u00E9nopuedensimplementehablarenEspa\u00F1ol",
      "PorqunopuedensimplementehablarenEspaol-fmd56a"
    ),
    TestCase(
      "Vietnamese",
      "T\u1EA1isaoh\u1ECDkh\u00F4ngth\u1EC3ch\u1EC9n\u00F3iti\u1EBFngVi\u1EC7t",
      "TisaohkhngthchnitingVit-kjcr8268qyxafd2f1b9g"
    ),
    TestCase(
      "3\u5E74B\u7D44\u91D1\u516B\u5148\u751F",
      "3B-ww4c5e180e575a65lsy2b"
    ),
    TestCase(
      "\u5B89\u5BA4\u5948\u7F8E\u6075-with-SUPER-MONKEYS",
      "-with-SUPER-MONKEYS-pc58ag80a8qai00g7n9n"
    ),
    TestCase(
      "Hello-Another-Way-\u305D\u308C\u305E\u308C\u306E\u5834\u6240",
      "Hello-Another-Way--fc4qua05auwb3674vfr0b"
    ),
    TestCase(
      "\u3072\u3068\u3064\u5C4B\u6839\u306E\u4E0B2",
      "2-u9tlzr9756bt3uc0v"
    ),
    TestCase(
      "Maji\u3067Koi\u3059\u308B5\u79D2\u524D",
      "MajiKoi5-783gue6qz075azm5e"
    ),
    TestCase(
      "\u30D1\u30D5\u30A3\u30FCde\u30EB\u30F3\u30D0",
      "de-jg4avhby1noc0d"
    ),
    TestCase(
      "\u305D\u306E\u30B9\u30D4\u30FC\u30C9\u3067",
      "d9juau41awczczp"
    ),
    /** This example is an ASCII string that breaks the existing rules for host
      * name labels. (It's not a realistic example for IDNA, because IDNA never
      * encodes pure ASCII labels.)
      */
    TestCase(
      "ASCII string that breaks the existing rules for host-name labels",
      "-> $1.00 <-",
      "-> $1.00 <--"
    )
  )

  group("decode") {
    testData.foreach { case TestCase(description, decoded, encoded) =>
      test(description.getOrElse(encoded)) {
        assertEquals(Punycode.decode(encoded, null).toString, decoded)
      }
    }

    test("uppercase Z") {
      assertEquals(Punycode.decode("ZZZ", null).toString, "\u7BA5")
    }
  }

  group("encode") {
    testData.foreach { case TestCase(description, decoded, encoded) =>
      test(description.getOrElse(decoded)) {
        assertEquals(Punycode.encode(decoded, null).toString, encoded)
      }
    }
  }

}
