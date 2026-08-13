addSbtPlugin("org.typelevel" % "sbt-typelevel" % "0.8.6")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.22.0")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.12")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.4.0")
addSbtPlugin("io.github.sbt-doctest" % "sbt-doctest" % "0.12.5")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.9.0")

libraryDependencySchemes += "com.lihaoyi" %% "geny" % VersionScheme.Always
