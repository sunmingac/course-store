package course.repository

import repository._
import java.util.UUID
import cats.effect._
import scala.collection.mutable.Map
import model.Course
import cats.implicits._

class MySuite extends munit.FunSuite {
  val id1        = UUID.fromString("3afa3b47-adfc-4d9a-bdb2-b9647a301242")
  val id2        = UUID.fromString("8fbd4439-f3b9-48b1-ae7f-15db7ce6c7d3")
  val id3        = UUID.fromString("56a7c107-1908-488e-acdc-c0c01aa9d350")
  val c1         = Course(id1, "Intro to Compilers")
  val c2         = Course(id2, "Data Structures")
  var envTest    = Map(id1 -> c1, id2 -> c2)
  val courseRepo = CourseRepoInMem[IO](envTest)

  test("get Course") {
    val obtained = courseRepo.getCourse(id1).unsafeRunSync()
    val expected = c1.some
    assertEquals(obtained, expected)
  }

  test("get Course find none") {
    val obtained = courseRepo.getCourse(UUID.randomUUID()).unsafeRunSync()
    val expected = None
    assertEquals(obtained, expected)
  }

  test("get Courses") {
    val obtained = courseRepo.getCourses(List(id1, id2, id3)).unsafeRunSync()
    val expected = List(c1)
    assertNotEquals(obtained, expected)
  }

  test("Create a TDD Course") {
    val obtained = courseRepo.createCourse("Intro to TDD").unsafeRunSync().name
    val expected = "Intro to TDD"
    assertEquals(obtained, expected)
  }
}
