addSbtPlugin("org.typelevel" % "sbt-typelevel" % "0.8.4")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.20.1")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.9")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.21.1")
addSbtPlugin("io.github.sbt-doctest" % "sbt-doctest" % "0.12.3")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.8.1")

libraryDependencySchemes += "com.lihaoyi" %% "geny" % VersionScheme.Always
