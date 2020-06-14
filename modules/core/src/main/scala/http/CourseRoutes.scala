package http
import cats._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import shop.algebras.Items
import shop.domain.brand._
import shop.http.json._
import shop.http.params._
import natchez.Tags.http

final class CourseRoutes[F[_]: Defer: Monad] extends Http4sDsl[F] {

  private[route] val prefixPath = "course"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root =>
      Ok("Hello Http4s")
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
