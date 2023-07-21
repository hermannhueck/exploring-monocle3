// see:
// https://www.optics.dev/Monocle/docs/focus#update-a-field-of-a-case-class-scala-2--3

// Update a field of a case class (Scala 2 & 3)

case class User(name: String, address: Address)
case class Address(streetNumber: Int, streetName: String)

val anna = User("Anna", Address(12, "high street"))

import monocle.syntax.all._

anna
  .focus(_.name)
  .replace("Bob")

anna
  .focus(_.address.streetNumber)
  .modify(_ + 1)
