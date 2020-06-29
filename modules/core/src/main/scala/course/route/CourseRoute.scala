package course.route

import cats.effect._
import cats.implicits._
import org.http4s.dsl._
import org.http4s._
import course.model._
import java.util.UUID
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s._
import sttp.tapir.server.http4s.Http4sServerOptions._
import sttp.tapir.openapi.OpenAPI
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.swagger.http4s.SwaggerHttp4s

trait CourseRoute[F[_]] {
  def routes: HttpRoutes[F]
}

object CourseRoute {

  val courseById: Endpoint[UUID, Unit, Course, Nothing] = endpoint.get
    .in(("course" / path[UUID]("uuid")))
    // .errorOut(stringBody)
    .out(jsonBody[Course])

  def dsl[F[_]: Sync](implicit C: ContextShift[F]) =
    new CourseRoute[F] with Http4sDsl[F] {

      def routes = routeGetCourse <+> routeGetCourseSwagger

      private def routeGetCourseSwagger = {
        val docs: OpenAPI = courseById.toOpenAPI("Course Store", "1.0")
        new SwaggerHttp4s(docs.toYaml).routes[F]
      }

      private def routeGetCourse =
        courseById.toRoutes(uuid =>
          Sync[F].delay {
            val course = Course(UUID.randomUUID(), s"Data Structures $uuid")
            course.asRight[Unit]
          }
        )
    }

}
