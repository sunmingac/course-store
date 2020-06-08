scalaVersion := "2.13.1"

val circeVersion = "0.12.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "org.typelevel" %% "cats-core" % "2.1.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "2.1.3"

libraryDependencies += "org.tpolecat" %% "skunk-core" % "0.0.7"

libraryDependencies += "org.scalameta" %% "munit" % "0.7.8" % Test
// Use %%% for non-JVM projects.
testFrameworks += new TestFramework("munit.Framework")
