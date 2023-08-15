import sbt._

object Dependencies {

  import Versions._

  lazy val monocleCore   = "dev.optics"                 %% "monocle-core"   % monocleVersion
  lazy val monocleMacro  = "dev.optics"                 %% "monocle-macro"  % monocleVersion
  lazy val alleycatsCore = "org.typelevel"              %% "alleycats-core" % alleycatsVersion
  lazy val circeCore     = "io.circe"                   %% "circe-core"     % circeVersion
  lazy val circeParser   = "io.circe"                   %% "circe-parser"   % circeVersion
  lazy val circeOptics   = ("io.circe"                  %% "circe-optics"   % circeVersion).cross(CrossVersion.for3Use2_13)
  lazy val quicklens     = "com.softwaremill.quicklens" %% "quicklens"      % quicklensVersion
  lazy val munit         = "org.scalameta"              %% "munit"          % munitVersion
  lazy val slf4jApi      = "org.slf4j"                   % "slf4j-api"      % slf4jVersion
  lazy val slf4jSimple   = "org.slf4j"                   % "slf4j-simple"   % slf4jVersion

  // https://github.com/typelevel/kind-projector
  lazy val kindProjectorPlugin    = compilerPlugin(
    compilerPlugin("org.typelevel" % "kind-projector" % kindProjectorVersion cross CrossVersion.full)
  )
  // https://github.com/oleg-py/better-monadic-for
  lazy val betterMonadicForPlugin = compilerPlugin(
    compilerPlugin("com.olegpy" %% "better-monadic-for" % betterMonadicForVersion)
  )

  lazy val compilerDependencies = Seq(
    monocleCore,
    monocleMacro,
    alleycatsCore,
    circeCore,
    circeParser,
    circeOptics,
    quicklens,
    munit,
    slf4jApi,
    slf4jSimple
  )

  lazy val testDependencies = Seq.empty

  def pluginDependencies(scalaVersion: String): Seq[ModuleID] = {
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 13)) => Seq(kindProjectorPlugin, betterMonadicForPlugin)
      case _             => Seq.empty
    }
  }

  def allDependencies(scalaVersion: String) =
    compilerDependencies ++ testDependencies ++ pluginDependencies(scalaVersion)
}
