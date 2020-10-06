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

ThisBuild / crossScalaVersions := List("2.12.11", "2.13.3", "0.27.0-RC1")


ThisBuild / versionIntroduced := Map(
  "0.27.0-RC1" -> "1.4.1"
)

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

ThisBuild / doctestTestFramework := DoctestTestFramework.ScalaCheck

lazy val root = project
  .in(file("."))
  .aggregate(coreJVM, coreJS, testKitJVM, testKitJS)
  .settings(commonSettings)
  .settings(noPublishSettings)

lazy val testKit = crossProject(JVMPlatform, JSPlatform)
  .in(file("./test-kit"))
  .settings(commonSettings)
  .settings(
    name := "ip4s-test-kit"
  )
  .settings(mimaPreviousArtifacts := Set.empty)
  .settings(dottyLibrarySettings)
  .settings(dottyJsSettings(ThisBuild / crossScalaVersions))
  .settings(libraryDependencies += "org.scalameta" %%% "munit-scalacheck" % "0.7.13")
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
lazy val testKitJS = testKit.js.disablePlugins(DoctestPlugin).enablePlugins(ScalaJSBundlerPlugin).settings(
  crossScalaVersions := crossScalaVersions.value.filterNot(_.startsWith("0."))
)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "ip4s-core",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % "2.2.0",
      "org.scalacheck" %%% "scalacheck" % "1.14.3" % "test"
    ),
    libraryDependencies ++= {
      if (isDotty.value) Nil else List("org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided")
    },
    Compile / scalafmt / unmanagedSources := (Compile / scalafmt / unmanagedSources).value.filterNot(_.toString.endsWith("Interpolators.scala")),
    Test / scalafmt / unmanagedSources := (Test / scalafmt / unmanagedSources).value.filterNot(_.toString.endsWith("Interpolators.scala")),
  )
  .settings(dottyLibrarySettings)
  .settings(dottyJsSettings(ThisBuild / crossScalaVersions))
  .jsSettings(
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    npmDependencies in Compile += "punycode" -> "2.1.1",
    crossScalaVersions := crossScalaVersions.value.filterNot(_.startsWith("0."))
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

lazy val coreJVM = core.jvm.enablePlugins(MdocPlugin, SbtOsgi).settings(
  pomPostProcess := { (node) =>
    import scala.xml._
    import scala.xml.transform._
    def stripIf(f: Node => Boolean) = new RewriteRule {
      override def transform(n: Node) =
        if (f(n)) NodeSeq.Empty else n
    }
    val stripTestScope = stripIf(n => n.label == "dependency" && (n \ "artifactId").text.startsWith("mdoc"))
    new RuleTransformer(stripTestScope).transform(node)(0)
  }
)
lazy val coreJS = core.js.disablePlugins(DoctestPlugin).enablePlugins(ScalaJSBundlerPlugin)

lazy val commonSettings = Seq(
  unmanagedResources in Compile ++= {
    val base = baseDirectory.value
    (base / "NOTICE") +: (base / "LICENSE") +: (base / "CONTRIBUTING") +: ((base / "licenses") * "LICENSE_*").get
  },
  scalacOptions := scalacOptions.value.filterNot(_ == "-Xfatal-warnings"),
  scalafmtOnCompile := true,
  initialCommands := "import com.comcast.ip4s._"
)
