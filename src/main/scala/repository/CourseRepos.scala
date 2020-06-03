package repository
import java.util.UUID
import model._
import cats.effect._
import cats.effect.implicits._
import java.{util => ju}
import scala.collection.mutable.Map

trait CourseRepo[F[_]] {
    def getCourse(id: UUID): F[Option[Course]]
    def createCourse(name: String): F[Course]
    def deleteCouse(id: UUID): F[Unit]
    def modifyCourse(id: UUID, course: Course): F[Option[Course]]
}

case class CourseRepoInMem[F[_]](env: Map[UUID, Course])(implicit F: Sync[F]) extends CourseRepo[F]{

    override def getCourse(id: ju.UUID): F[Option[Course]] = F.delay {
        env.get(id)
    }

    override def deleteCouse(id: ju.UUID): F[Unit] = F.delay {
        env.-(id)
    }

    override def modifyCourse(id: ju.UUID, course: Course): F[Option[Course]] =  {
        env -= id
        env += Map(id, course)
        getCourse(id)
    }

    override def createCourse(name: String): F[Course] = F.delay {
        val id = UUID.randomUUID()
        val course = Course(id, name)
        env += Map(id -> course)
        course
    }

}
