import sbtcrossproject.CrossPlugin.autoImport.crossProject

lazy val scalaTestVersion = "3.1.0-RC3"

lazy val root = project
  .in(file("."))
  .aggregate(coreJVM, coreJS, catsJVM, catsJS, scalazJVM, scalazJS, testKitJVM, testKitJS)
  .settings(commonSettings)
  .settings(
    publish := {},
    publishLocal := {},
    publishArtifact := false,
    mimaPreviousArtifacts := Set.empty
  )
  .settings(publishingSettings)

lazy val testKit = crossProject(JVMPlatform, JSPlatform)
  .in(file("./test-kit"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(commonSettings)
  .settings(
    name := "ip4s-test-kit",
    libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.14.2",
    libraryDependencies += "org.scalatestplus" %%% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    libraryDependencies += "org.scalatest" %%% "scalatest" % scalaTestVersion % "test"
  )
  .settings(mimaPreviousArtifacts := Set.empty)
  .settings(publishingSettings)
  .jvmSettings(
    libraryDependencies += "com.google.guava" % "guava" % "28.1-jre" % "test",
    libraryDependencies := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, v)) if v >= 13 =>
          libraryDependencies.value.filterNot(_.toString.contains("tut-core"))
        case _ =>
          libraryDependencies.value
      }
    },
    scalacOptions in Tut := (scalacOptions in Compile).value.filter(opt =>
      !(opt.startsWith("-Ywarn-unused") || opt == "-Xfatal-warnings" || opt == "-Xlint")),
    tutTargetDirectory := baseDirectory.value / "../../docs",
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

lazy val testKitJVM = testKit.jvm.enablePlugins(TutPlugin, SbtOsgi)
lazy val testKitJS = testKit.js.disablePlugins(DoctestPlugin).enablePlugins(ScalaJSBundlerPlugin)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(commonSettings)
  .settings(
    name := "ip4s-core",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
  )
  .jvmSettings(mimaSettings)
  .jsSettings(mimaPreviousArtifacts := Set.empty)
  .jsSettings(
    npmDependencies in Compile += "punycode" -> "2.1.1"
  )
  .settings(publishingSettings)
  .jvmSettings(
    libraryDependencies += "org.scalatest" %%% "scalatest" % scalaTestVersion % "test",
    libraryDependencies := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, v)) if v >= 13 =>
          libraryDependencies.value.filterNot(_.toString.contains("tut-core"))
        case _ =>
          libraryDependencies.value
      }
    },
    scalacOptions in Tut := (scalacOptions in Compile).value.filter(opt =>
      !(opt.startsWith("-Ywarn-unused") || opt == "-Xfatal-warnings" || opt == "-Xlint")),
    tutTargetDirectory := baseDirectory.value / "../docs",
    OsgiKeys.exportPackage := Seq("com.comcast.ip4s.*;version=${Bundle-Version}"),
    OsgiKeys.importPackage := {
      val Some((major, minor)) = CrossVersion.partialVersion(scalaVersion.value)
      Seq(s"""scala.*;version="[$major.$minor,$major.${minor + 1})"""", "*")
    },
    OsgiKeys.privatePackage := Seq("com.comcast.ip4s.*"),
    OsgiKeys.additionalHeaders := Map("-removeheaders" -> "Include-Resource,Private-Package"),
    osgiSettings
  )

lazy val coreJVM = core.jvm.enablePlugins(TutPlugin, SbtOsgi)
lazy val coreJS = core.js.disablePlugins(DoctestPlugin).enablePlugins(ScalaJSBundlerPlugin)

lazy val cats = crossProject(JVMPlatform, JSPlatform)
  .in(file("./cats"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(commonSettings)
  .settings(
    name := "ip4s-cats",
    libraryDependencies += "org.typelevel" %%% "cats-effect" % "2.0.0"
  )
  .jvmSettings(mimaSettings)
  .jsSettings(mimaPreviousArtifacts := Set.empty)
  .settings(publishingSettings)
  .jvmSettings(
    libraryDependencies := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, v)) if v >= 13 =>
          libraryDependencies.value.filterNot(_.toString.contains("tut-core"))
        case _ =>
          libraryDependencies.value
      }
    },
    scalacOptions in Tut := (scalacOptions in Compile).value.filter(opt =>
      !(opt.startsWith("-Ywarn-unused") || opt == "-Xfatal-warnings" || opt == "-Xlint")),
    tutTargetDirectory := baseDirectory.value / "../../docs",
    OsgiKeys.exportPackage := Seq("com.comcast.ip4s.interop.cats.*;version=${Bundle-Version}"),
    OsgiKeys.importPackage := {
      val Some((major, minor)) = CrossVersion.partialVersion(scalaVersion.value)
      Seq(s"""scala.*;version="[$major.$minor,$major.${minor + 1})"""", "*")
    },
    OsgiKeys.privatePackage := Seq("com.comcast.ip4s.interop.cats.*"),
    OsgiKeys.additionalHeaders := Map("-removeheaders" -> "Include-Resource,Private-Package"),
    osgiSettings
  )
  .dependsOn(testKit % "compile->compile;test->test")

lazy val catsJVM = cats.jvm.enablePlugins(TutPlugin, SbtOsgi)
lazy val catsJS = cats.js.disablePlugins(DoctestPlugin).enablePlugins(ScalaJSBundlerPlugin)

lazy val scalaz = crossProject(JVMPlatform, JSPlatform)
  .in(file("./scalaz"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(commonSettings)
  .settings(
    name := "ip4s-scalaz",
    libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.29"
  )
  .jvmSettings(mimaSettings)
  .jsSettings(mimaPreviousArtifacts := Set.empty)
  .settings(publishingSettings)
  .jvmSettings(
    libraryDependencies := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, v)) if v >= 13 =>
          libraryDependencies.value.filterNot(_.toString.contains("tut-core"))
        case _ =>
          libraryDependencies.value
      }
    },
    scalacOptions in Tut := (scalacOptions in Compile).value.filter(opt =>
      !(opt.startsWith("-Ywarn-unused") || opt == "-Xfatal-warnings" || opt == "-Xlint")),
    tutTargetDirectory := baseDirectory.value / "../../docs",
    OsgiKeys.exportPackage := Seq("com.comcast.ip4s.interop.scalaz.*;version=${Bundle-Version}"),
    OsgiKeys.importPackage := {
      val Some((major, minor)) = CrossVersion.partialVersion(scalaVersion.value)
      Seq(s"""scala.*;version="[$major.$minor,$major.${minor + 1})"""", "*")
    },
    OsgiKeys.privatePackage := Seq("com.comcast.ip4s.interop.scalaz.*"),
    OsgiKeys.additionalHeaders := Map("-removeheaders" -> "Include-Resource,Private-Package"),
    osgiSettings
  )
  .dependsOn(testKit % "compile->compile;test->test")

lazy val scalazJVM = scalaz.jvm.enablePlugins(TutPlugin, SbtOsgi)
lazy val scalazJS = scalaz.js.disablePlugins(DoctestPlugin).enablePlugins(ScalaJSBundlerPlugin)

lazy val commonSettings = Seq(
  organization := "com.comcast",
  organizationName := "Comcast Cable Communications Management, LLC",
  organizationHomepage := Some(new URL("https://comcast.github.io")),
  git.remoteRepo := "git@github.com:comcast/ip4s.git",
  scmInfo := Some(ScmInfo(url("https://github.com/comcast/ip4s"), "git@github.com:comcast/ip4s.git")),
  homepage := Some(url("https://github.com/comcast/ip4s")),
  startYear := Some(2018),
  licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
  unmanagedResources in Compile ++= {
    val base = baseDirectory.value
    (base / "NOTICE") +: (base / "LICENSE") +: (base / "CONTRIBUTING") +: ((base / "licenses") * "LICENSE_*").get
  },
  scalaVersion := "2.12.8",
  crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.0"),
  scalacOptions ++= Seq(
    "-language:higherKinds",
    "-deprecation",
    "-encoding",
    "utf-8",
    "-explaintypes",
    "-feature",
    "-unchecked",
    "-Xcheckinit",
    "-Xfatal-warnings",
    "-Xlint",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  ),
  scalacOptions in Test := (scalacOptions in Test).value.filterNot(_ == "-Xfatal-warnings"),
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v >= 12 =>
        Seq(
          "-Ywarn-extra-implicit",
          "-Ywarn-unused:implicits",
          "-Ywarn-unused:imports",
          "-Ywarn-unused:locals",
          // "-Ywarn-unused:params", disabled until https://github.com/tkawachi/sbt-doctest/issues/102
          "-Ywarn-unused:patvars",
          "-Ywarn-unused:privates"
        )
      case _ => Nil
    }
  },
  scalacOptions in (Compile, console) := (scalacOptions in (Compile, console)).value.filter(opt =>
    !(opt.startsWith("-Ywarn-unused") || opt == "-Xfatal-warnings" || opt == "-Xlint")),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,
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
  doctestTestFramework := DoctestTestFramework.ScalaTest,
  initialCommands := "import com.comcast.ip4s._"
)

lazy val publishingSettings = Seq(
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots".at(nexus + "content/repositories/snapshots"))
    else
      Some("releases".at(nexus + "service/local/staging/deploy/maven2"))
  },
  credentials ++= (for {
    username <- Option(System.getenv().get("SONATYPE_USERNAME"))
    password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
  } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq,
  publishMavenStyle := true,
  pomIncludeRepository := (_ => false),
  pomExtra := {
    <developers>
      {for ((username, name) <- contributors) yield
      <developer>
        <id>{username}</id>
        <name>{name}</name>
        <url>http://github.com/{username}</url>
      </developer>
      }
    </developers>
  },
  pomPostProcess := { node =>
    import scala.xml._
    import scala.xml.transform._
    def stripIf(f: Node => Boolean) = new RewriteRule {
      override def transform(n: Node) = if (f(n)) NodeSeq.Empty else n
    }
    val stripTestScope = stripIf { n =>
      n.label == "dependency" && (n \ "scope").text == "test"
    }
    new RuleTransformer(stripTestScope).transform(node)(0)
  },
  releaseCrossBuild := true
)

lazy val mimaSettings = Seq(
  mimaPreviousArtifacts := previousVersion(version.value).map { pv =>
    organization.value % (normalizedName.value + "_" + scalaBinaryVersion.value) % pv
  }.toSet,
  mimaBinaryIssueFilters ++= Nil
)

def previousVersion(currentVersion: String): Option[String] = {
  val Version = """(\d+)\.(\d+)\.(\d+).*""".r
  val Version(x, y, z) = currentVersion
  if (z == "0") None
  else Some(s"$x.$y.${z.toInt - 1}")
}

lazy val contributors = Seq(
  "mpilquist" -> "Michael Pilquist",
  "matthughes" -> "Matt Hughes",
  "nequissimus" -> "Tim Steinbach"
)
