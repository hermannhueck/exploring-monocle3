// see:
// https://www.optics.dev/Monocle/docs/focus#update-an-optional-field-of-a-case-class-scala-3-only

// Update an optional field of a case class (Scala 3 only)

case class User(name: String, address: Option[Address])
case class Address(streetNumber: Int, streetName: String)

val anna = User("Anna", Some(Address(12, "high street")))
val bob  = User("bob", None)

import monocle.syntax.all._

anna
  .focus(_.address.some.streetNumber)
  .modify(_ + 1)

bob
  .focus(_.address.some.streetNumber)
  .modify(_ + 1)
