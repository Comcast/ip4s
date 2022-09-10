[![Published Artifact](https://img.shields.io/maven-central/v/com.comcast/ip4s-core_3.svg)](http://search.maven.org/#search%7Cga%7C1%7Cip4s-core_3)
[![javadoc](https://javadoc.io/badge2/com.comcast/ip4s-core_3/javadoc.svg)](https://javadoc.io/doc/com.comcast/ip4s-core_3)

ip4s: IP Addresses for Scala, Scala.js & Scala Native
=====================================================

This project defines immutable, safe data structures for describing IP addresses, multicast joins, socket addresses and similar IP & network related data types.

There are two defining characteristics of this project that make it different from other similar projects:
- all data types are immutable and every function/method is referentially transparent (e.g., no accidental DNS lookups by calling `InetAddress.getByName(...)`)
- published for Scala, Scala.js and Scala Native

See the [guide](docs/guide.md) and [ScalaDoc](https://javadoc.io/doc/com.comcast/ip4s-core_3) for more details.

## Getting Binaries

This library is published on Maven Central under group id `com.comcast` and artifact id `ip4s-core_${scalaBinaryVersion}`. Add the following to your SBT build:

```scala
libraryDependencies += "com.comcast" %% "ip4s-core" % "version"
```

## Interop

As of 1.4, ip4s depends on cats and provides type class instances directly in data type companion objects. For Scalaz support, we recommend [shims](https://github.com/djspiewak/shims).

## Copyright and License

This project is made available under the [Apache License, Version 2.0](LICENSE). Copyright information can be found in [NOTICE](NOTICE).

## Code of Conduct

See the [Code of Conduct](CODE_OF_CONDUCT.md).

