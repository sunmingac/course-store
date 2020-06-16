package course.http
import cats._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class CourseRoutes[F[_]]() {

  val a = IO[F]

  def routes = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok("Hello Http4s")
  }

  // val routes = httpRoutes

}
