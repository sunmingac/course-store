package repository
import java.util.UUID
import model._

trait CourseRepo[F[_]] {
    def getCourse(id: UUID): F[Course]
    def createCourse(name: String): F[Course]
    def deleteCouse(id: UUID): F[Unit]
    def modifyCourse(id: UUID, course: Course): F[Course]
}

case class CourseRepoInMem()
