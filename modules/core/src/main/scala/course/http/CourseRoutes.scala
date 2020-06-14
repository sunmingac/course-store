package course.http
import cats._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class CourseRoutes[F[_]: Defer: Monad] extends Http4sDsl[F] {


  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root =>
      Ok("Hello Http4s")
  }

  val routes: HttpRoutes[F] = Router(
    "course" -> httpRoutes
  )

}
