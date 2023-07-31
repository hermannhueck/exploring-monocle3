// Optics beyond Lenses with Monocle
//
// Composition of Lenses – the classic example
//
// see:
// https://scalac.io/blog/scala-optics-lenses-with-monocle/
// https://github.com/note/monocle-example/blob/master/src/test/scala/com/example/ClassicLensExample.scala
//

import monocle.Lens
import munit.Assertions._

// Business entities
case class Person(fullName: String, address: Address)
case class Address(city: String, street: Street)
case class Street(name: String, number: Int)

// Lens definitions
object PersonLenses {
  val addressLens =
    Lens[Person, Address](person => person.address)(newAddress => person => person.copy(address = newAddress))
  val streetLens  =
    Lens[Address, Street](address => address.street)(newStreet => address => address.copy(street = newStreet))
  val nameLens    =
    Lens[Street, String](street => street.name)(newName => street => street.copy(name = newName))
}

import PersonLenses._

// Let's capitalize street name without Lenses to have some reference point
def upperCaseWithCopy(person: Person): Person =
  person.copy(address =
    person
      .address
      .copy(
        street = person
          .address
          .street
          .copy(
            name = person.address.street.name.toUpperCase
          )
      )
  )

// Same thing as above but with Lenses
def upperCaseWithLens(person: Person): Person = {
  val streetNameLens: Lens[Person, String] =
    addressLens andThen streetLens andThen nameLens
  streetNameLens.modify(_.toUpperCase)(person)
}

val bob = Person("Bob Dylan", Address("New York", Street("some", 67)))

val obtained = upperCaseWithLens(bob)
val expected = upperCaseWithCopy(bob)
obtained == expected
// assertEquals(obtained, expected)
