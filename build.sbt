import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtcrossproject.CrossPlugin.autoImport.CrossType
import com.typesafe.tools.mima.core._

ThisBuild / baseVersion := "3.0"

ThisBuild / organization := "com.comcast"
ThisBuild / organizationName := "Comcast Cable Communications Management, LLC"

ThisBuild / homepage := Some(url("https://github.com/comcast/ip4s"))
ThisBuild / startYear := Some(2018)

ThisBuild / publishGithubUser := "mpilquist"
ThisBuild / publishFullName := "Michael Pilquist"

ThisBuild / developers ++= List(
  Developer("matthughes", "Matt Hughes", "@matthughes", url("https://github.com/matthughes")),
  Developer("nequissimus", "Tim Steinbach", "@nequissimus", url("https://github.com/nequissimus"))
)

ThisBuild / crossScalaVersions := List("2.12.15", "2.13.6", "3.0.2")

ThisBuild / spiewakCiReleaseSnapshots := true

ThisBuild / spiewakMainBranches := List("main")

ThisBuild / homepage := Some(url("https://github.com/comcast/ip4s"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/comcast/ip4s"),
    "git@github.com:comcast/ip4s.git"
  )
)

ThisBuild / testFrameworks += new TestFramework("munit.Framework")

ThisBuild / strictSemVer := false

ThisBuild / doctestTestFramework := DoctestTestFramework.ScalaCheck

ThisBuild / scalafmtOnCompile := true

ThisBuild / initialCommands := "import com.comcast.ip4s._"

ThisBuild / fatalWarningsInCI := false

ThisBuild / mimaBinaryIssueFilters ++= Seq(
  ProblemFilters.exclude[DirectMissingMethodProblem]("com.comcast.ip4s.Ipv6Address.toInetAddress")
)

lazy val root = project
  .in(file("."))
  .enablePlugins(NoPublishPlugin, SonatypeCiReleasePlugin)
  .aggregate(coreJVM, coreJS, testKitJVM, testKitJS)

lazy val testKit = crossProject(JVMPlatform, JSPlatform)
  .in(file("./test-kit"))
  .settings(commonSettings)
  .settings(
    name := "ip4s-test-kit"
  )
  .settings(mimaPreviousArtifacts := Set.empty)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % "1.15.4",
      "org.scalameta" %%% "munit-scalacheck" % "0.7.29" % Test
    )
  )
  .jvmSettings(
    libraryDependencies += "com.google.guava" % "guava" % "31.0-jre" % "test",
    OsgiKeys.exportPackage := Seq("com.comcast.ip4s.*;version=${Bundle-Version}"),
    OsgiKeys.importPackage := {
      val Some((major, minor)) = CrossVersion.partialVersion(scalaVersion.value)
      Seq(s"""scala.*;version="[$major.$minor,$major.${minor + 1})"""", "*")
    },
    OsgiKeys.privatePackage := Seq("com.comcast.ip4s.*"),
    OsgiKeys.additionalHeaders := Map("-removeheaders" -> "Include-Resource,Private-Package"),
    osgiSettings
  )
  .dependsOn(core % "compile->compile")

lazy val testKitJVM = testKit.jvm.enablePlugins(SbtOsgi)
lazy val testKitJS = testKit.js
  .disablePlugins(DoctestPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "ip4s-core",
    libraryDependencies ++= {
      if (isDotty.value) Nil else List("org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided")
    },
    scalacOptions := scalacOptions.value.filterNot(_ == "-source:3.0-migration"),
    Compile / scalafmt / unmanagedSources := (Compile / scalafmt / unmanagedSources).value.filterNot(
      _.toString.endsWith("Literals.scala")
    ),
    Test / scalafmt / unmanagedSources := (Test / scalafmt / unmanagedSources).value.filterNot(
      _.toString.endsWith("Literals.scala")
    ),
    Compile / unmanagedSourceDirectories ++= {
      val major = if (isDotty.value) "-3" else "-2"
      List(CrossType.Pure, CrossType.Full).flatMap(
        _.sharedSrcDir(baseDirectory.value, "main").toList.map(f => file(f.getPath + major))
      )
    }
  )
  .settings(
    libraryDependencies += "org.typelevel" %%% "literally" % "1.0.2"
  )
  .jvmSettings(
    libraryDependencies += "org.typelevel" %%% "cats-effect" % "3.2.9"
  )
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.6.1",
      "org.scalacheck" %%% "scalacheck" % "1.15.4" % Test
    )
  )

lazy val coreJVM = core.jvm
  .enablePlugins(SbtOsgi)
  .settings(
    OsgiKeys.exportPackage := Seq("com.comcast.ip4s.*;version=${Bundle-Version}"),
    OsgiKeys.importPackage := {
      val Some((major, minor)) = CrossVersion.partialVersion(scalaVersion.value)
      Seq(s"""scala.*;version="[$major.$minor,$major.${minor + 1})"""", "*")
    },
    OsgiKeys.privatePackage := Seq("com.comcast.ip4s.*"),
    OsgiKeys.additionalHeaders := Map("-removeheaders" -> "Include-Resource,Private-Package"),
    osgiSettings
  )

lazy val coreJS = core.js
  .disablePlugins(DoctestPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    Compile / npmDependencies += "punycode" -> "2.1.1"
  )

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(MdocPlugin)
  .dependsOn(coreJVM)
  .settings(
    mdocIn := baseDirectory.value / "src",
    mdocOut := baseDirectory.value / "../docs",
    crossScalaVersions := (ThisBuild / crossScalaVersions).value.filter(_.startsWith("2.")),
    githubWorkflowArtifactUpload := false
  )

lazy val commonSettings = Seq(
  Compile / unmanagedResources ++= {
    val base = baseDirectory.value / ".."
    (base / "NOTICE") +: (base / "LICENSE") +: (base / "CONTRIBUTING") +: ((base / "licenses") * "LICENSE_*").get
  }
)
