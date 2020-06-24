package course

import cats.data._
import cats.implicits._
import cats.effect._
import org.http4s.server.blaze._
import org.http4s._
import org.http4s.implicits._
import route._

object Main extends IOApp {

  def run(args: List[String]) = {

    val httpApp: HttpApp[IO] = NonEmptyChain(
      CourseRoute.dsl[IO].route
    ).reduceLeft(_ <+> _).orNotFound

    val server = BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .resource
      .use(_ => IO.never)
    // .as(ExitCode.Success) // put this line back when deploy to cloud

    // remove the for comprehension when deploy to cloud
    for {
      fiber <- server.start
      _ <- IO(println(Console.MAGENTA + "Press ENTER to shutdown server" + Console.RESET))
      _ <- IO(scala.io.StdIn.readLine())
      _ <- fiber.cancel
    } yield ExitCode.Success
  }

}
