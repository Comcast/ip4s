addSbtPlugin("org.typelevel" % "sbt-typelevel" % "0.7.3")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.17.0")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.5")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.21.1")
addSbtPlugin("com.github.tkawachi" % "sbt-doctest" % "0.10.0")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.6.1")

libraryDependencySchemes += "com.lihaoyi" %% "geny" % VersionScheme.Always
