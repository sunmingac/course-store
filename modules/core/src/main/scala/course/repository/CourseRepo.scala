package course.repository
import course.model._
import cats.Applicative
import cats.implicits._
import cats.effect.Sync
import skunk._
import skunk.implicits._
import skunk.codec.all._
import cats.effect.Resource
import java.util.UUID
import cats.effect.Concurrent
import cats.effect.ContextShift
import natchez.Trace.Implicits.noop

trait CourseRepo[F[_]] {
  def getCourse(id: UUID): F[Option[Course]]
  def getCourses(ids: List[UUID])(implicit F: Applicative[F]): F[List[Course]] =
    ids.traverse(getCourse).map(_.flatten)
  def getCoursesInPage(pageNumber: Int, limitPerPage: Int): F[List[Course]]
  def createCourse(name: String): F[Course]
  def deleteCouse(id: UUID): F[Unit]
  def modifyCourse(id: UUID, course: Course): F[Option[Course]]
}

object CourseRepo {
  def courseRepositoryInPostgres[F[_]: Sync: ContextShift: Concurrent]: CourseRepo[F] = {
    val resource: Resource[F, Session[F]] =
      Session.single (
        host = "localhost",
        port = 5432,
        user = "postgres",
        database = "course-store",
        password = Some("postgres")
      )

    CourseRepoSkunk[F](resource)
  }

  def courseRepositoryInMemory[F[_]: Sync]: CourseRepo[F] =
    CourseRepoInMem[F](scala.collection.mutable.Map[UUID, Course]())
}

final case class CourseRepoInMem[F[_]](env: scala.collection.mutable.Map[UUID, Course])(implicit
    F: Sync[F]
) extends CourseRepo[F] {

  override def getCourse(id: UUID): F[Option[Course]] =
    F.delay {
      env.get(id)
    }

  override def getCoursesInPage(pageNumber: Int, limitPerPage: Int): F[List[Course]] = {
    val startIndex = pageNumber * limitPerPage
    val ids = env.drop(startIndex).take(limitPerPage).keySet.toList
    getCourses(ids)
  }

  override def deleteCouse(id: UUID): F[Unit] =
    F.delay {
      env.remove(id)
      ()
    }

  override def modifyCourse(id: UUID, course: Course): F[Option[Course]] = {
    env -= id
    env += (id -> course)
    getCourse(id)
  }

  override def createCourse(name: String): F[Course] =
    F.delay {
      val id = UUID.randomUUID()
      val course = Course(id, name)
      env += (id -> course)
      course
    }
}

final case class CourseRepoSkunk[F[_]: Sync](resource: Resource[F, Session[F]])
    extends CourseRepo[F] {

  val courseDecoder: Decoder[Course] = (uuid ~ varchar).map {
    case (i, n) => Course(i, n)
  }

  override def getCourse(id: UUID): F[Option[Course]] = {

    val query: Query[UUID, Course] =
      sql"SELECT ID, NAME FROM COURSE WHERE ID = $uuid"
        .query(courseDecoder)

    resource.use(_.prepare(query).use(_.option(id)))
  }

  def getCoursesInPage(pageNumber: Int, limitPerPage: Int): F[List[Course]] = {
    val startIndex = pageNumber * limitPerPage

    val query: Query[Void, Course] =
      sql"SELECT ID, NAME FROM COURSE"
        .query(courseDecoder)

    resource.use(_.execute(query)).map(_.drop(startIndex).take(limitPerPage))
  }

  override def createCourse(name: String): F[Course] = {
    val insert: Command[Course] =
      sql"INSERT INTO COURSE (ID, NAME) VALUES ($uuid, $varchar)".command
        .contramap(c => c.id ~ c.name)

    val course = Course(UUID.randomUUID(), name)

    for {
      _ <- resource.use(_.prepare(insert).use(_.execute(course)))
    } yield course
  }

  override def deleteCouse(id: UUID): F[Unit] = {
    val delete: Command[UUID] =
      sql"DELETE FROM COURSE WHERE ID = $uuid".command
    resource.use(_.prepare(delete).use(_.execute(id)).map(_ => ()))
  }

  override def modifyCourse(id: UUID, course: Course): F[Option[Course]] = {
    val update: Command[Course] =
      sql"UPDATE COURSE SET NAME = $varchar WHERE ID = $uuid".command
        .contramap(c => c.name ~ c.id)
    resource.use(_.prepare(update).use(_.execute(course)))

    getCourse(id)
  }

}
