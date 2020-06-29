package course.route

import cats.data._
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
  import CourseEndpoint._

  def dsl[F[_]: Sync](implicit C: ContextShift[F]) =
    new CourseRoute[F] with Http4sDsl[F] {

      def routes = NonEmptyChain(
          routeSwagger,
          routeGetCourse,
          routeGetCourseInPage
        ).reduceLeft(_ <+> _)

      private def routeSwagger = {
        val docs: OpenAPI = allEndpoints.toOpenAPI("Course Store", "1.0")
        new SwaggerHttp4s(docs.toYaml).routes[F]
      }

      private def routeGetCourse =
        courseById.toRoutes(uuid =>
          Sync[F].delay {
            val course = Course(UUID.randomUUID(), s"Data Structures $uuid")
            course.asRight[Unit]
          }
        )

      private def routeGetCourseInPage =
        courseInPage.toRoutes {
          case (page, limit) =>
            Sync[F].delay {
              val c1 = Course(UUID.randomUUID(), s"Data Structures")
              val c2 = Course(UUID.randomUUID(), s"Mathmatics $page $limit")
              List(c1, c2).asRight[Unit]
            }
        }
    }

}

object CourseEndpoint {

  def allEndpoints =
    List(
      courseById,
      courseInPage
    )

  val courseById: Endpoint[UUID, Unit, Course, Nothing] = endpoint.get
    .in(("course" / path[UUID]("uuid")))
    .out(jsonBody[Course])

  val courseInPage: Endpoint[(Int, Int), Unit, List[Course], Nothing] = endpoint.get
    .in(("course"))
    .in(
      query[Int]("page")
        .description("Page number")
        .and(query[Int]("limit").description("Number of records per page"))
    )
    .out(jsonBody[List[Course]])
}
