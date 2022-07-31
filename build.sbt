import com.typesafe.tools.mima.core._

ThisBuild / tlBaseVersion := "3.1"

ThisBuild / organization := "com.comcast"
ThisBuild / organizationName := "Comcast Cable Communications Management, LLC"

ThisBuild / startYear := Some(2018)

ThisBuild / developers ++= List(
  tlGitHubDev("mpilquist", "Michael Pilquist"),
  tlGitHubDev("matthughes", "Matt Hughes"),
  tlGitHubDev("nequissimus", "Tim Steinbach")
)

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("8"))

ThisBuild / crossScalaVersions := List("2.12.16", "2.13.8", "3.1.3")

ThisBuild / tlVersionIntroduced := Map("3" -> "3.0.3")

ThisBuild / doctestTestFramework := DoctestTestFramework.ScalaCheck

ThisBuild / initialCommands := "import com.comcast.ip4s._"

ThisBuild / mimaBinaryIssueFilters ++= Seq(
  ProblemFilters.exclude[DirectMissingMethodProblem]("com.comcast.ip4s.Ipv6Address.toInetAddress"),
  ProblemFilters.exclude[ReversedMissingMethodProblem]("com.comcast.ip4s.Dns.*"), // sealed trait
  // Scala 3 (erroneously?) considered Multicast/SourceSpecificMulticast as sum types
  ProblemFilters.exclude[DirectMissingMethodProblem]("com.comcast.ip4s.Multicast.ordinal"),
  ProblemFilters.exclude[MissingTypesProblem]("com.comcast.ip4s.Multicast$"),
  ProblemFilters.exclude[DirectMissingMethodProblem]("com.comcast.ip4s.SourceSpecificMulticast.ordinal"),
  ProblemFilters.exclude[MissingTypesProblem]("com.comcast.ip4s.SourceSpecificMulticast$")
)

lazy val root = tlCrossRootProject.aggregate(core, testKit)

ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("snapshots")

lazy val testKit = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("./test-kit"))
  .settings(commonSettings)
  .settings(
    name := "ip4s-test-kit"
  )
  .settings(mimaPreviousArtifacts := Set.empty)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % "1.16.0",
      "org.scalameta" %%% "munit-scalacheck" % "1.0.0-M6" % Test,
      "com.armanbilge" %%% "cats-effect" % "3.4-f28b163-SNAPSHOT" % Test,
      "com.armanbilge" %%% "munit-cats-effect" % "2.0-4e051ab-SNAPSHOT" % Test
    )
  )
  .jvmSettings(
    libraryDependencies += "com.google.guava" % "guava" % "31.1-jre" % "test"
  )
  .dependsOn(core % "compile->compile")

lazy val testKitJVM = testKit.jvm
lazy val testKitJS = testKit.js
  .disablePlugins(DoctestPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
lazy val testKitNative = testKit.js
  .disablePlugins(DoctestPlugin)

lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "ip4s-core",
    libraryDependencies ++= {
      if (tlIsScala3.value) Nil
      else List("org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided")
    },
    scalacOptions := scalacOptions.value.filterNot(_ == "-source:3.0-migration")
  )
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "literally" % "1.1-ba0b394-SNAPSHOT",
      "org.typelevel" %%% "cats-core" % "2.8.0",
      "com.armanbilge" %%% "cats-effect-kernel" % "3.4-f28b163-SNAPSHOT"
    )
  )

lazy val coreJVM = core.jvm.settings(
  doctestIgnoreRegex := Some(".*Literals.scala")
)

lazy val coreJS = core.js
  .disablePlugins(DoctestPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    Compile / npmDependencies += "punycode" -> "2.1.1"
  )

lazy val coreNative = core.native
  .disablePlugins(DoctestPlugin)

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(MdocPlugin)
  .dependsOn(coreJVM)
  .settings(
    mdocIn := baseDirectory.value / "src",
    mdocOut := baseDirectory.value / "../docs",
    githubWorkflowArtifactUpload := false,
    libraryDependencies += "org.typelevel" %%% "cats-effect" % "3.3.14"
  )

lazy val commonSettings = Seq(
  Compile / unmanagedResources ++= {
    val base = baseDirectory.value / ".."
    (base / "NOTICE") +: (base / "LICENSE") +: (base / "CONTRIBUTING") +: ((base / "licenses") * "LICENSE_*").get
  }
)
