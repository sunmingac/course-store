import Dependencies._

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "dev.kan"
ThisBuild / organizationName := "kan"

lazy val root = (project in file("."))
  .settings(
    name := "shopping-cart"
  )
  .aggregate(core, tests)

lazy val tests = (project in file("modules/tests"))
  .configs(IntegrationTest)
  .settings(
    name := "shopping-cart-test-suite",
    scalacOptions += "-Ymacro-annotations",
    scalafmtOnCompile := true,
    Defaults.itSettings,
    resolvers += Resolver.sonatypeRepo("snapshots"),
    testFrameworks += new TestFramework("munit.Framework"),
    libraryDependencies ++= Seq(
          compilerPlugin(Libraries.kindProjector cross CrossVersion.full),
          compilerPlugin(Libraries.betterMonadicFor),
          Libraries.scalaCheck,
          Libraries.scalaTest,
          Libraries.scalaTestPlus,
          Libraries.munitTest
        )
  )
  .dependsOn(core)

lazy val core = (project in file("modules/core"))
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    name := "shopping-cart-core",
    packageName in Docker := "shopping-cart",
    scalacOptions ++= Seq(
      "-Ymacro-annotations",
      "-encoding", "UTF-8",
      "-deprecation",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen"
      ),
    scalafmtOnCompile := true,
    resolvers += Resolver.sonatypeRepo("snapshots"),
    Defaults.itSettings,
    dockerBaseImage := "openjdk:8u201-jre-alpine3.9",
    dockerExposedPorts ++= Seq(8080),
    makeBatScripts := Seq(),
    dockerUpdateLatest := true,
    libraryDependencies ++= Seq(
          compilerPlugin(Libraries.kindProjector cross CrossVersion.full),
          compilerPlugin(Libraries.betterMonadicFor),
          Libraries.cats,
          Libraries.catsEffect,
          Libraries.catsMeowMtl,
          Libraries.catsRetry,
          Libraries.circeCore,
          Libraries.circeGeneric,
          Libraries.circeParser,
          Libraries.circeRefined,
          Libraries.cirisCore,
          Libraries.cirisEnum,
          Libraries.cirisRefined,
          Libraries.fs2,
          Libraries.http4sDsl,
          Libraries.http4sServer,
          Libraries.http4sClient,
          Libraries.http4sCirce,
          Libraries.http4sJwtAuth,
          Libraries.javaxCrypto,
          Libraries.log4cats,
          Libraries.logback % Runtime,
          Libraries.newtype,
          Libraries.redis4catsEffects,
          Libraries.redis4catsLog4cats,
          Libraries.refinedCore,
          Libraries.refinedCats,
          Libraries.skunkCore,
          Libraries.skunkCirce,
          Libraries.squants,
          Libraries.munitTest
        )
  )

