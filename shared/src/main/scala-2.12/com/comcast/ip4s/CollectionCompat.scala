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

import scala.collection.JavaConverters._

private[ip4s] object Ip4sCollectionCompat {
  implicit class JIterableOps[A](private val self: java.lang.Iterable[A]) extends AnyVal {
    def asScala: Iterable[A] = iterableAsScalaIterable(self)
  }
  implicit class ListOps[A](private val self: List[A]) extends AnyVal {
    def asJava: java.util.List[A] = seqAsJavaList(self)
  }
  implicit class EnumerationOps[A](private val self: java.util.Enumeration[A]) extends AnyVal {
    def asScala: Iterator[A] = enumerationAsScalaIterator(self)
  }
}
