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

/** Supertype for types that specify address information for opening a socket.
  *
  * There are two built-in subtypes:
  *  - [[SocketAddress]] - which specifies address and port info for IPv4/IPv6 sockets
  *  - [[UnixSocketAddress]] - which specifies the path to a unix domain socket
  *
  * This trait is left open for extension, allowing other address types to be defined.
  * When using this trait, pattern match on the supported subtypes.
  */
abstract class GenSocketAddress private[ip4s] ()
