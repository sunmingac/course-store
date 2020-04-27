package com.kidane.coursestore

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) =
    CourseStoreServer.stream[IO].compile.drain.as(ExitCode.Success)
}