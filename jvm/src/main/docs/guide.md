ip4s: IP Addresses for Scala & Scala.js
=======================================

This is the guide for IP Addresses for Scala & Scala.js. This library provides the package `com.comcast.ip4s`, which contains all types. It is a small package and it is often useful to import all of its contents via `import com.comcast.ip4s._` -- doing so will enable some syntax.

# IP Addresses

The `IpAddress` type represents either an IPv4 address or an IPv6 address. The primary mechanism to construct an `IpAddress` is `IpAddress.apply`, which converts a string to an `Option[IpAddress]`. You can also construct an `IpAddress` from a byte array of either 4 bytes or 16 bytes.

```scala mdoc:to-string
import com.comcast.ip4s.IpAddress

val home = IpAddress("127.0.0.1")
val home6 = IpAddress("::1")
```

The `toString` method on `IpAddress` renders the IP in dotted-decimal notation if it is a V4 address and condensed string notation if it is a V6 address. The `toBytes` method converts the IP into a 4 or 16 element byte array. There are a few more methods on `IpAddress` that we'll look at later but not many more -- the API is small.

Sometimes it is useful to explicitly require an IPv4 or IPv6 address -- for example, when modelling the configuration of a device that requires an IPv6 address. This can be accomplished by using the `Ipv4Address` or `Ipv6Address` types, both of which are subtypes of `IpAddress`. We can construct these types directly via methods on their companions:

```scala mdoc:to-string
import com.comcast.ip4s.{Ipv4Address, Ipv6Address}

val explicitV4Home = Ipv4Address("127.0.0.1")
val explicitV6Home = Ipv6Address("::1")
```

Because `Ipv4Address` and `Ipv6Address` are subtypes of `IpAddress`, we can pattern match on an `IpAddress` or use the `fold` method:

```scala mdoc:to-string
import com.comcast.ip4s.{Ipv4Address, Ipv6Address}

val homeIsV4 = home.get match {
  case _: Ipv4Address => true
  case _: Ipv6Address => false
}

val home6IsV4 = home6.get.fold(_ => true, _ => false)
```

## IP Literals

In the previous examples, all of the IP addresses were wrapped in an `Option`. When a string is statically (i.e. at compile time) known to be a valid IP address, we can avoid the `Option` entirely by using IP address string interpolators.

```scala mdoc:reset:to-string
import com.comcast.ip4s._

val home = ip"127.0.0.1"
val home4 = ipv4"127.0.0.1"
val home6 = ipv6"::1"
```

The `ip` interpolator returns an `IpAddress`, the `ipv4` interpolator returns an `Ipv4Address`, and the `ipv6` interpolator returns an `Ipv6Address`. If the string is not a valid IP of the requested type, the expression will fail to compile.

```scala mdoc:fail:to-string
val bad = ipv4"::1"
```

## IPv6 String Formats

IPv6 addresses have a number of special string formats. The default format (what's returned by `toString`) adheres to [RFC5952](https://tools.ietf.org/html/rfc5952) -- e.g., maximal use of `::` to condense string length. If instead, you want a string that does not use `::` and expresses each hextet as 4 characters, call `.toUncondensedString`. Note that the `toString` method never outputs a mixed string consisting of both V6 hextets and a dotted decimal V4 address. For example, the address consisting of 12 0 bytes followed by 127, 0, 0, 1 is rendered as `::7f00:1` instead of `::127.0.0.1`. `Ipv6Address.apply` and `IpAddress.apply` can parse all of these formats.

```scala mdoc:reset:to-string
import com.comcast.ip4s._

val home = ipv6"::7f00:1"
val homeLong = home.toUncondensedString
val homeMixed = home.toMixedString

val parsedHomeLong = Ipv6Address(homeLong)
val parsedHomeMixed = Ipv6Address(homeMixed)
```

## Ordering

IP addresses have a defined ordering, making them sortable. Note when comparing an IPv4 address to an IPv6 address, the V4 address is converted to a V6 address by left padding with 0 bytes (aka, a "compatible" V4-in-V6 address).

```scala mdoc:nest:to-string
val ips = List(ipv4"10.1.1.1", ipv4"10.1.2.0", ipv4"10.1.0.0", ipv6"::1", ipv6"ff3b::")
val sorted = ips.sorted
```

## JVM Integration

When compiling for the JVM, the various IP address classes have a `toInetAddress` method which returns a `java.net.InetAddress`, allowing easy integration with libraries that use `InetAddress`.

```scala mdoc:to-string
val homeIA = ip"127.0.0.1".toInetAddress
val home4IA = ipv4"127.0.0.1".toInetAddress
val home6IA = ipv6"::1".toInetAddress
```

# Multicast

Both IPv4 and IPv6 have reserved address ranges for multicast and smaller reserved ranges for source specific multicast. The address types in this library are aware of these ranges:

```scala mdoc:nest:to-string
val ips = List(ip"127.0.0.1", ip"224.10.10.10", ip"232.11.11.11", ip"::1", ip"ff00::10", ip"ff3b::11")
val multicastIps = ips.filter(_.isMulticast)
val ssmIps = ips.filter(_.isSourceSpecificMulticast)
```

## Multicast Witnesses

Often, especially when modelling configuration of systems, you need a type that indicates an address is a valid multicast or source specific multicast address. That's provided by the `Multicast` and `SourceSpecificMulticast` types. These roughly look like:

```scala mdoc:to-string
sealed trait Multicast[A <: IpAddress] { def address: A }
sealed trait SourceSpecificMulticast[A <: IpAddress] extends Multicast[A]
```

These wrappers serve as type level witnesses that the wrapped address is a valid multicast / source specific multicast address. These types are parameterized by the type of IP address -- either `IpAddress`, `Ipv4Address`, or `Ipv6Address`, to allow for accurate domain modeling at the type level. For example, if an application only supported IPv6 source specific multicast, we can use `SourceSpecificMulticast[Ipv6Address]`.

To construct instances of `Multicast[A]` and `SourceSpecificMulticast[A]`, we can use the `asMulticast` and `asSourceSpecificMulticast` methods on `IpAddress`:

```scala mdoc:nest:to-string
val multicastIps = ips.flatMap(_.asMulticast)
val ssmIps = ips.flatMap(_.asSourceSpecificMulticast)
```

## Multicast Literals

There are string interpolators for constructing multicast and source specific multicast address from literal strings, similar to the `ip`, `ipv4`, and `ipv6` interpolators. The multicast interpolators are:

|Interpolator|Description|Result Type|Example|
|------------|-----------|-----------|-------|
|`mip`|Multicast IP|`Multicast[IpAddress]`|`mip"224.10.10.10"`|
|`mipv4`|V4 Multicast IP|`Multicast[Ipv4Address]`|`mipv4"224.10.10.10"`|
|`mipv6`|V6 Multicast IP|`Multicast[Ipv6Address]`|`mipv6"ff3b::10"`|
|`ssmip`|Source Specific Multicast IP|`SourceSpecificMulticast[IpAddress]`|`ssmip"224.10.10.10"`|
|`ssmipv4`|V4 Source Specific Multicast IP|`SourceSpecificMulticast[Ipv4Address]`|`ssmipv4"224.10.10.10"`|
|`ssmipv6`|V6 Source Specific Multicast IP|`SourceSpecificMulticast[Ipv6Address]`|`ssmipv6"ff3b::10"`|

## Multicast Joins

The `MulticastJoin` type models a request to join a multicast group. There are two types of joins -- any source joins and source specific joins. An any source join is specified by supplying a multicast IP address whereas a source specific join is specified by supplying a source IP address and a source specific multicast address. In both cases, the multicast IP address is referred to as the group.

This is modeled roughly as:

```scala mdoc:to-string
sealed trait MulticastJoin[A <: IpAddress]
case class AnySourceMulticastJoin[A <: IpAddress](group: Multicast[A]) extends MulticastJoin[A]
case class SourceSpecificMulticastJoin[A <: IpAddress](source: A, group: SourceSpecificMulticast[A]) extends MulticastJoin[A]
```

`MulticastJoin` and its subtypes are parameterized by the address type in order to allow domain modelling that requires a V4, V6, or either. The `AnySourceMulticastJoin` and `SourceSpecificMulticastJoin` types are exposed, instead of being kept as an implementation detail (or data constructor), for a similar reason -- to allow modelling where a type or function wants a very specific type like `SourceSpecificMulticastJoin[Ipv6Address]` while supporting other scnenarios that want something much more general like a `MulticastJoin[IpAddress]`.

To construct a `MulticastJoin`, we can use the `asm` and `ssm` methods in the `MulticastJoin` companion.

```scala mdoc:to-string
val j1 = MulticastJoin.ssm(ipv4"10.11.12.13", ssmipv4"232.1.2.3")
val j2 = MulticastJoin.ssm(ipv4"10.11.12.13", ipv4"232.1.2.3".asSourceSpecificMulticast.get)
val j3 = MulticastJoin.asm(mipv6"ff3b::10")
```

# CIDR

CIDR (classless inter-domain routing) addresses are a compact representation of an IP address and a routing prefix. They are expressed as an IP followed by `/prefixLength` where `prefixLength` represents the number of bits of the start of the address that define the routing prefix. For example, the CIDR `10.123.45.67/8` represents the IP `10.123.45.67` and the routing prefix of `255.0.0.0`.

The `Cidr` type represents CIDR addresses. It's parameterized by the type of IP address you are working with -- either `IpAddress`, `Ipv4Address`, or `Ipv6Address`.

```scala mdoc:nest:to-string
val x = Cidr(ip"10.123.45.67", 8)
val y = Cidr(ipv4"10.123.45.67", 8)
val z = Cidr(ipv6"ff3b::10", 16)
```

A shortand for constructing a `Cidr` is available as a method on `IpAddress`:

```scala mdoc:nest:to-string
val x = ip"10.123.45.67" / 8
val y = ipv4"10.123.45.67" / 8
val z = ipv6"ff3b::10" / 16
```

The `Cidr` object provides mechanisms for parsing CIDR strings as well:

```scala mdoc:nest:to-string
val parsedX = Cidr.fromString("10.123.45.67/8")
val parsedY = Cidr.fromString4("10.123.45.67/8")
val parsedZ = Cidr.fromString6("ff3b::10/16")
```

Given a `Cidr[A]`, we can ask various things about the routing prefix:

```scala mdoc:nest:to-string
val prefixX = x.prefix
val prefixZ = z.prefix

val maskX = x.mask
val maskZ = z.mask

val doesXContainHome = x.contains(home)
val doesXContainSuccessor = x.contains(x.address.next)
val lastAddressInX = x.last
```

# Socket Addresses

A socket address is an IP address and a TCP/UDP port number. This is roughly modeled as:

```scala
case class SocketAddress[+A <: IpAddress](ip: A, port: Port)
```

Like we saw with `CIDR` and `MulticastJoin`, `SocketAddress` is polymorphic in address type, allowing expression of contraints like a socket address with an IPv6 IP. `SocketAddress` can be converted to and from a string representation, where V6 addresses are surrounded by square brackets.

```scala mdoc:nest:to-string
val s = SocketAddress(ipv4"127.0.0.1", port"5555")
val s1 = SocketAddress.fromString(s.toString)
val t = SocketAddress(ipv6"::1", port"5555")
val t1 = SocketAddress.fromString6(t.toString)
```

On the JVM, a `SocketAddress` can be converted to a `java.net.InetSocketAddress` via the `toInetSocketAddress` method.

```scala mdoc:nest:to-string
val u = t.toInetSocketAddress
```

## Multicast Socket Addresses

Similarly, a multicast socket address is a multicast join and a UDP port number. It is defined polymorphically in both the IP address type and the join type (general join, any source join, or source specific join). For example, compare the types of `s` and `t`:

```scala mdoc:reset:to-string
import com.comcast.ip4s._

val s = MulticastSocketAddress(SourceSpecificMulticastJoin(ipv4"10.10.10.10", ssmipv4"232.10.11.12"), port"5555")
val t = MulticastSocketAddress(MulticastJoin.ssm(ip"10.10.10.10", ssmip"232.10.11.12"), port"5555")
```

# Hostnames

The `Hostname` type models an RFC1123 compliant hostname -- limited to 253 total characters, labels separated by periods, and each label consisting of ASCII letters and digits and dashes, not beginning or ending in a dash, and not exceeding 63 characters.

```scala mdoc:nest:to-string
val home = Hostname("localhost")
val ls = home.map(_.labels)
val comcast = host"comcast.com"
val cs = comcast.labels
```

## Hostname Resolution

On the JVM, hostnames can be resolved to IP addresses via `resolve` and `resolveAll`:

```scala mdoc:reset:to-string
import com.comcast.ip4s._
import cats.effect.IO

val home = host"localhost"
val homeIp = home.resolve[IO]
homeIp.unsafeRunSync

val homeIps = home.resolveAll[IO]
homeIps.unsafeRunSync
```

# Internationalized Domain Names

RFC1123 hostnames are limited to ASCII characters. The `IDN` type provides a way to represent Unicode hostnames.

```scala mdoc:to-string
val unicodeComcast = idn"comcast\u3002com"
unicodeComcast.hostname

val emojiRegistrar = idn"i❤.ws"
emojiRegistrar.hostname
```
