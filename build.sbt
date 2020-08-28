import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtcrossproject.CrossPlugin.autoImport.CrossType

ThisBuild / baseVersion := "1.4"

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

ThisBuild / crossScalaVersions := List("2.12.11", "2.13.3", "0.26.0-RC1")

ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.Equals(Ref.Branch("main")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)
ThisBuild / githubWorkflowEnv ++= Map(
  "SONATYPE_USERNAME" -> s"$${{ secrets.SONATYPE_USERNAME }}",
  "SONATYPE_PASSWORD" -> s"$${{ secrets.SONATYPE_PASSWORD }}",
  "PGP_SECRET" -> s"$${{ secrets.PGP_SECRET }}"
)
ThisBuild / githubWorkflowTargetTags += "v*"

ThisBuild / githubWorkflowPublishPreamble +=
  WorkflowStep.Run(
    List("echo $PGP_SECRET | base64 -d | gpg --import"),
    name = Some("Import signing key")
  )

ThisBuild / githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("release")))

ThisBuild / homepage := Some(url("https://github.com/comcast/ip4s"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/comcast/ip4s"),
    "git@github.com:comcast/ip4s.git"
  )
)

ThisBuild / testFrameworks += new TestFramework("munit.Framework")

ThisBuild / strictSemVer := false

lazy val root = project
  .in(file("."))
  .aggregate(coreJVM, coreJS, testKitJVM, testKitJS)
  .settings(commonSettings)
  .settings(noPublishSettings)

lazy val testKit = crossProject(JVMPlatform, JSPlatform)
  .in(file("./test-kit"))
  .settings(commonSettings)
  .settings(
    name := "ip4s-test-kit",
    testFrameworks += new TestFramework("munit.Framework")
  )
  .settings(mimaPreviousArtifacts := Set.empty)
  .settings(dottyLibrarySettings)
  .settings(dottyJsSettings(ThisBuild / crossScalaVersions))
  .settings(libraryDependencies += "org.scalameta" %%% "munit-scalacheck" % "0.7.12")
  .jvmSettings(
    libraryDependencies += "com.google.guava" % "guava" % "29.0-jre" % "test",
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
lazy val testKitJS = testKit.js.disablePlugins(DoctestPlugin).enablePlugins(ScalaJSBundlerPlugin)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "ip4s-core",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % "2.1.4",
      "org.scalacheck" %%% "scalacheck" % "1.14.3" % "test"
    ),
    libraryDependencies ++= {
      if (isDotty.value) Nil else List("org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided")
    },
    Compile / unmanagedSourceDirectories ++= {
      val major = if (isDotty.value) "-3" else "-2"
      List(CrossType.Pure, CrossType.Full).flatMap(
        _.sharedSrcDir(baseDirectory.value, "main").toList.map(f => file(f.getPath + major))
      )
    },
    Compile / scalafmt / unmanagedSources := (Compile / scalafmt / unmanagedSources).value.filterNot(_.toString.endsWith("Interpolators.scala")),
    Test / scalafmt / unmanagedSources := (Test / scalafmt / unmanagedSources).value.filterNot(_.toString.endsWith("Interpolators.scala")),
  )
  .settings(scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)))
  .settings(dottyLibrarySettings)
  .settings(dottyJsSettings(ThisBuild / crossScalaVersions))
  .jsSettings(
    npmDependencies in Compile += "punycode" -> "2.1.1"
  )
  .jvmSettings(
    mdocIn := baseDirectory.value / "src/main/docs",
    mdocOut := baseDirectory.value / "../docs",
    OsgiKeys.exportPackage := Seq("com.comcast.ip4s.*;version=${Bundle-Version}"),
    OsgiKeys.importPackage := {
      val Some((major, minor)) = CrossVersion.partialVersion(scalaVersion.value)
      Seq(s"""scala.*;version="[$major.$minor,$major.${minor + 1})"""", "*")
    },
    OsgiKeys.privatePackage := Seq("com.comcast.ip4s.*"),
    OsgiKeys.additionalHeaders := Map("-removeheaders" -> "Include-Resource,Private-Package"),
    osgiSettings
  )

lazy val coreJVM = core.jvm.enablePlugins(MdocPlugin, SbtOsgi)
lazy val coreJS = core.js.disablePlugins(DoctestPlugin).enablePlugins(ScalaJSBundlerPlugin)

lazy val commonSettings = Seq(
  unmanagedResources in Compile ++= {
    val base = baseDirectory.value
    (base / "NOTICE") +: (base / "LICENSE") +: (base / "CONTRIBUTING") +: ((base / "licenses") * "LICENSE_*").get
  },
  scalacOptions := scalacOptions.value.filterNot(_ == "-Xfatal-warnings"),
  scalacOptions in (Compile, doc) ++= {
    val tagOrBranch = {
      if (version.value endsWith "SNAPSHOT") git.gitCurrentBranch.value
      else ("v" + version.value)
    }
    Seq(
      "-implicits",
      "-implicits-show-all",
      "-sourcepath",
      baseDirectory.value.getCanonicalPath,
      "-doc-source-url",
      s"https://github.com/comcast/ip4s/tree/$tagOrBranch/€{FILE_PATH}.scala",
      "-diagrams"
    )
  },
  sourceDirectories in (Compile, scalafmt) += baseDirectory.value / "../shared/src/main/scala",
  scalafmtOnCompile := true,
  doctestTestFramework := DoctestTestFramework.ScalaCheck,
  initialCommands := "import com.comcast.ip4s._"
)
