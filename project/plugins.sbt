addSbtPlugin("org.typelevel" % "sbt-typelevel" % "0.8.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.19.0")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.4.17")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.21.1")
addSbtPlugin("io.github.sbt-doctest" % "sbt-doctest" % "0.11.2")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.7.2")

libraryDependencySchemes += "com.lihaoyi" %% "geny" % VersionScheme.Always
