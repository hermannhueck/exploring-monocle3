// see:
// https://www.optics.dev/Monocle/docs/examples/university_example.html

// University Example

case class Lecturer(firstName: String, lastName: String, salary: Int)
case class Department(budget: Int, lecturers: List[Lecturer])
case class University(name: String, departments: Map[String, Department])

val uni = University(
  "oxford",
  Map(
    "Computer Science" -> Department(
      45,
      List(
        Lecturer("john", "doe", 10),
        Lecturer("robert", "johnson", 16)
      )
    ),
    "History"          -> Department(
      30,
      List(
        Lecturer("arnold", "stones", 20)
      )
    )
  )
)

// How to remove or add elements in a Map

import monocle.Focus // require monocle-macro module in Scala 2

val departments = Focus[University](_.departments)

departments.at("History").replace(None)(uni)

val physics = Department(
  36,
  List(
    Lecturer("daniel", "jones", 12),
    Lecturer("roger", "smith", 14)
  )
)

departments.at("Physics").replace(Some(physics))(uni)

// How to update a field in a nested case class

val lecturers = Focus[Department](_.lecturers)
val salary    = Focus[Lecturer](_.salary)

import monocle.Traversal

val allLecturers: Traversal[University, Lecturer] = departments.each.andThen(lecturers).each

allLecturers.andThen(salary).modify(_ + 2)(uni)

// How to create your own Traversal

val firstName = Focus[Lecturer](_.firstName)
val lastName  = Focus[Lecturer](_.lastName)

val upperCasedFirstName =
  allLecturers
    .andThen(firstName)
    .index(0)
    .modify(_.toUpper)(uni)

val upperCasedFirstAndLastName =
  allLecturers
    .andThen(lastName)
    .index(0)
    .modify(_.toUpper)(upperCasedFirstName)

val firstAndLastNames = Traversal.apply2[Lecturer, String](_.firstName, _.lastName) { case (fn, ln, l) =>
  l.copy(firstName = fn, lastName = ln)
}

val upperCasedFirstAndLastName2 =
  allLecturers.andThen(firstAndLastNames).index(0).modify(_.toUpper)(uni)
