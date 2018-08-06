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
package interop.cats

import _root_.cats.{Eq, Order, Show}

trait CidrInstances {
  implicit def CidrEq[A <: IpAddress]: Eq[Cidr[A]] = Eq.fromUniversalEquals[Cidr[A]]
  implicit def CidrOrder[A <: IpAddress]: Order[Cidr[A]] = Order.fromOrdering(Cidr.ordering[A])
  implicit def CidrShow[A <: IpAddress]: Show[Cidr[A]] = Show.fromToString[Cidr[A]]
}
