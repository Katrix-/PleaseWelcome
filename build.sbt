def removeSnapshot(str: String): String = if (str.endsWith("-SNAPSHOT")) str.substring(0, str.length - 9) else str
def katLibDependecy(module: String) = "com.github.Katrix-.KatLib" % s"katlib-$module" % "2.3.1" % Provided

lazy val publishResolver = {
  val artifactPattern = s"""${file("publish").absolutePath}/[revision]/[artifact]-[revision](-[classifier]).[ext]"""
  Resolver.file("publish").artifacts(artifactPattern)
}

lazy val commonSettings = Seq(
  name := s"PleaseWelcome-${removeSnapshot(spongeApiVersion.value)}",
  organization := "net.katsstuff",
  version := "1.1.0",
  scalaVersion := "2.12.2",
  resolvers += "jitpack" at "https://jitpack.io",
  libraryDependencies += katLibDependecy("shared"),
  libraryDependencies += "org.jetbrains" % "annotations" % "15.0" % Provided,
  scalacOptions ++= ScalaOptions.extraOptions,
  scalacOptions in (Compile, console) ~= (_.filterNot(Set("-Ywarn-unused:imports"))),
  crossPaths := false,
  assemblyShadeRules in assembly := Seq(
    ShadeRule.rename("scala.**"     -> "io.github.katrix.katlib.shade.scala.@1").inAll,
    ShadeRule.rename("shapeless.**" -> "io.github.katrix.katlib.shade.shapeless.@1").inAll
  ),
  autoScalaLibrary := false,
  publishTo := Some(publishResolver),
  publishArtifact in makePom := false,
  publishArtifact in (Compile, packageBin) := false,
  publishArtifact in (Compile, packageDoc) := false,
  publishArtifact in (Compile, packageSrc) := false,
  artifact in (Compile, assembly) := {
    val art = (artifact in (Compile, assembly)).value
    art.copy(`classifier` = Some("assembly"))
  },
  artifactName := { (sv, module, artifact) =>
    s"${artifact.name}-${module.revision}.${artifact.extension}"
  },
  assemblyJarName := s"${name.value}-assembly-${version.value}.jar",
  spongePluginInfo := spongePluginInfo.value.copy(
    id = "pleasewelcome",
    name = Some("PleaseWelcome"),
    version = Some(s"${version.value}-${removeSnapshot(spongeApiVersion.value)}"),
    authors = Seq("Katrix"),
    description = Some("A plugin to control what happens when a new player joins the server."),
    dependencies = Set(
      DependencyInfo("spongeapi", Some(removeSnapshot(spongeApiVersion.value))),
      DependencyInfo("katlib", Some(s"2.3.1-${removeSnapshot(spongeApiVersion.value)}"))
    )
  )
) ++ addArtifact(artifact in (Compile, assembly), assembly)

lazy val pleaseWelcomeShared = (project in file("shared"))
  .enablePlugins(SpongePlugin)
  .settings(
    commonSettings,
    name := "PleaseWelcome-Shared",
    publishArtifact := false,
    publish := {},
    publishLocal := {},
    assembleArtifact := false,
    spongeMetaCreate := false,
    //Default version, needs to build correctly against all supported versions
    spongeApiVersion := "4.1.0"
  )

lazy val pleaseWelcomeV410 = (project in file("4.1.0"))
  .enablePlugins(SpongePlugin)
  .dependsOn(pleaseWelcomeShared)
  .settings(commonSettings: _*)
  .settings(spongeApiVersion := "4.1.0", libraryDependencies += katLibDependecy("4-1-0"))

lazy val pleaseWelcomeV500 = (project in file("5.0.0"))
  .enablePlugins(SpongePlugin)
  .dependsOn(pleaseWelcomeShared)
  .settings(commonSettings: _*)
  .settings(spongeApiVersion := "5.0.0", libraryDependencies += katLibDependecy("5-0-0"))

lazy val pleaseWelcomeV600 = (project in file("6.0.0"))
  .enablePlugins(SpongePlugin)
  .dependsOn(pleaseWelcomeShared)
  .settings(commonSettings: _*)
  .settings(spongeApiVersion := "6.0.0", libraryDependencies += katLibDependecy("6-0-0"))

lazy val pleaseWelcomeV700 = (project in file("7.0.0"))
  .enablePlugins(SpongePlugin)
  .dependsOn(pleaseWelcomeShared)
  .settings(commonSettings: _*)
  .settings(spongeApiVersion := "7.0.0-SNAPSHOT", libraryDependencies += katLibDependecy("7-0-0"))

lazy val pleaseWelcomeRoot = (project in file("."))
  .settings(
    publishArtifact := false,
    assembleArtifact := false,
    spongeMetaCreate := false,
    publish := {},
    publishLocal := {}
  )
  .disablePlugins(AssemblyPlugin)
  .aggregate(pleaseWelcomeShared, pleaseWelcomeV410, pleaseWelcomeV500, pleaseWelcomeV600, pleaseWelcomeV700)
