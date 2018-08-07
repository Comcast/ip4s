[![Published Artifact](https://img.shields.io/maven-central/v/com.comcast/ip4s_2.12.svg)](http://search.maven.org/#search%7Cga%7C1%7Cip4s) [![Build Status](https://travis-ci.org/Comcast/ip4s.svg?branch=master)](https://travis-ci.org/Comcast/ip4s)

ip4s: IP Addresses for Scala & Scala.js
=======================================

This project defines immutable, safe data structures for describing IP addresses, multicast joins, socket addresses and similar IP & network related data types.

There are two defining characteristics of this project that make it different from other similar projects:
- all data types are immutable and every function/method is referentially transparent (e.g., no accidental DNS lookups by calling `InetAddress.getByName(...)`)
- published for both Scala and Scala.js

See the [guide](docs/guide-core.md) and [ScalaDoc](https://oss.sonatype.org/service/local/repositories/releases/archive/com/comcast/ip4s_2.12/1.0.2/ip4s_2.12-1.0.2-javadoc.jar/!/com/comcast/ip4s/index.html) for more details.

## Getting Binaries

This library is published on Maven Central under group id `com.comcast` and artifact id `ip4s_${scalaBinaryVersion}`. Add the following to your SBT build:

```scala
libraryDependencies += "com.comcast" %% "ip4s" % "version"
```

## Interop

Modules for interopability with [cats](https://typelevel.org/cats/) or [Scalaz](http://scalaz.org/) are available.
A series of typeclass instances as well as helper functionality is available in these modules.

Supplemental guides are available for [ip4s-cats](docs/guide-cats.md) and [ip4s-scalaz](docs/guide-scalaz.md).

```scala
libraryDependencies += "com.comcast" %% "ip4s-cats" % "version"

libraryDependencies += "com.comcast" %% "ip4s-scalaz" % "version"
```

## Copyright and License

This project is made available under the [Apache License, Version 2.0](LICENSE). Copyright information can be found in [NOTICE](NOTICE).
