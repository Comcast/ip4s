![Published Artifact](https://img.shields.io/nexus/r/https/oss.sonatype.org/com.comcast/ip4s_2.12.svg) [![Build Status](https://travis-ci.org/Comcast/ip4s.svg?branch=master)](https://travis-ci.org/Comcast/ip4s)

ip4s: IP Addresses for Scala & Scala.js
=======================================

This project defines immutable, safe data structures for describing IP addresses, multicast joins, socket addresses and similar IP & network related data types.

There are two defining characteristics of this project that make it different from other similar projects:
- all data types are immutable and every function/method is referentially transparent (e.g., no accidental DNS lookups by calling `InetAddress.getByName(...)`)
- published for both Scala and Scala.js

See the [guide](docs/guide.md) for more details.

## Getting Binaries

This library is published on Maven Central under group id `com.comcast` and artifact id `ip4s_${scalaBinaryVersion}`. Add the following to your SBT build:

```scala
libraryDependencies += "com.comcast" %% "ip4s" % "version"
```

## Copyright and License

This project is made available under the [Apache License, Version 2.0](LICENSE). Copyright information can be found in [NOTICE](NOTICE).

