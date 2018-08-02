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

trait MulticastJoinInstances {
  implicit def MulticastJoinEq[J[x <: IpAddress] <: MulticastJoin[x], A <: IpAddress]: Eq[J[A]] =
    Eq.fromUniversalEquals[J[A]]
  implicit def MulticastJoinOrder[J[x <: IpAddress] <: MulticastJoin[x], A <: IpAddress]: Order[J[A]] =
    Order.fromOrdering(MulticastJoin.ordering[J, A])
  implicit def MulticastJoinShow[J[x <: IpAddress] <: MulticastJoin[x], A <: IpAddress]: Show[J[A]] =
    Show.fromToString[J[A]]
}
