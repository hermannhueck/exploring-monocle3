import com.softwaremill.quicklens._
import scala.util.Try

case class Street(name: String)
case class Address(street: Street)
case class Person(address: Address, age: Int)

val person = Person(Address(Street("1 Functional Rd.")), 35)

// ----- Modify deeply nested fields in case classes:

val pLens1 =
  person
    .modify(_.address.street.name)

pLens1.using(_.toUpperCase)
pLens1.setTo("3 OO Ln.")

// or

val pLens2 =
  modify(person)(_.address.street.name)

pLens2.using(_.toUpperCase)
pLens2.setTo("3 OO Ln.")

// ----- Chain modifications:

person
  .modify(_.address.street.name)
  .using(_.toUpperCase)
  .modify(_.age)
  .using(_ - 1)

// ----- Modify conditionally:

person
  .modify(_.address.street.name)
  .setToIfDefined(Some("3 00 Ln."))

val shouldChangeAddress = true
person
  .modify(_.address.street.name)
  .setToIf(shouldChangeAddress)("3 00 Ln.")

// ----- Modify several fields in one go:

case class Person2(firstName: String, middleName: Option[String], lastName: String)

val person2 = Person2("john", Some("steve"), "smith")

person2
  .modifyAll(_.firstName, _.middleName.each, _.lastName)
  .using(_.capitalize)

// ----- Traverse options/lists/maps using .each:

case class Street3(name: String)
case class Address3(street: Option[Street3])
case class Person3(addresses: List[Address3])

val person3 = Person3(
  List(
    Address3(Some(Street3("1 Functional Rd."))),
    Address3(Some(Street3("2 Imperative Dr.")))
  )
)

person3
  .modify(_.addresses.each.street.each.name)
  .using(_.toUpperCase)

// ----- Traverse selected elements using .eachWhere:

def filterAddress: Address3 => Boolean =
  a =>
    a.street match {
      case None         => false
      case Some(street) => street.name.toLowerCase contains "functional"
    }
person3
  .modify(
    _.addresses
      .eachWhere(filterAddress)
      .street
      .eachWhere(_.name.startsWith("1"))
      .name
  )
  .using(_.toUpperCase)

// ----- Modify specific elements in an option/sequence/map using .at:

// Modify Address at illegal index 2:
Try {
// illegal index --> IndexOutOfBoundsException
  person3
    .modify(_.addresses.at(2).street.at.name)
    .using(_.toUpperCase)
}

// Modify Address at legal index 1:
Try {
// legal index --> successful modification
  person3
    .modify(_.addresses.at(1).street.at.name)
    .using(_.toUpperCase)
}

case class Property(value: String)
case class Person4(name: String, props: Map[String, Property])

val person4 = Person4(
  "Joe",
  Map("Role" -> Property("Programmmer"), "Age" -> Property("45"))
)

person4
  .modify(_.props.at("Age").value)
  .setTo("46")

Try {
  person4
    .modify(_.props.at("AgeXXX").value)
    .setTo("46")
}

// ----- Modify specific elements in an option/sequence/map using .at:

// illegal index --> no modification
person3
  .modify(_.addresses.index(2).street.index.name)
  .using(_.toUpperCase)
// legal index --> successful modification
person3
  .modify(_.addresses.index(1).street.index.name)
  .using(_.toUpperCase)

person4
  .modify(_.props.index("Age").value)
  .setTo("46")

person4
  .modify(_.props.index("AgeXXX").value)
  .setTo("46")

// ----- Modify specific elements in an option or map with a fallback using .atOrElse:

person4
  .modify(_.props.atOrElse("NumReports", Property("0")).value)
  .setTo("5")

person3
  .modify(_.addresses.at(1).street.atOrElse(Street3("main street")).name)
  .using(_.toUpperCase)

// ----- Modify Either fields using .eachLeft and eachRight:

case class AuthContext(token: String)
case class AuthRequest(url: String)
case class Resource(auth: Either[AuthContext, AuthRequest])

val devResource =
  Resource(auth = Left(AuthContext("fake")))

val prodResource =
  devResource
    .modify(_.auth.eachLeft.token)
    .setTo("real")

// ----- Modify fields when they are of a certain subtype:

trait Animal
case class Dog(age: Int)        extends Animal
case class Cat(ages: List[Int]) extends Animal

case class Zoo(animals: List[Animal])

val zoo =
  Zoo(List(Dog(4), Cat(List(3, 12, 13))))

val olderZoo = zoo
  .modifyAll(
    _.animals.each.when[Dog].age,
    _.animals.each.when[Cat].ages.at(0)
  )
  .using(_ + 1)

// ----- Re-usable modifications (lenses):

val modifyStreetName =
  modify(_: Person)(_.address.street.name)

modifyStreetName(person)
  .using(_.toUpperCase)
modifyStreetName(person)
  .using(_.toLowerCase)

val upperCaseStreetName =
  modify(_: Person)(_.address.street.name).using(_.toUpperCase)

upperCaseStreetName(person)

// ----- Re-usable modifications (lenses): Alternate syntax:

val modifyStreetName2 =
  modifyLens[Person](_.address.street.name)

modifyStreetName2
  .using(_.toUpperCase)(person)
modifyStreetName2
  .using(_.toLowerCase)(person)

val upperCaseStreetName2 =
  modifyLens[Person](_.address.street.name).using(_.toUpperCase)

upperCaseStreetName2(person)

// ----- Composing lenses:

val modifyAddress3    = modify(_: Person)(_.address)
val modifyStreetName3 = modify(_: Address)(_.street.name)

(modifyAddress3 andThenModify modifyStreetName3)(person)
  .using(_.toUpperCase)

// ----- or, with alternate syntax:

val modifyAddress4    = modifyLens[Person](_.address)
val modifyStreetName4 = modifyLens[Address](_.street.name)

(modifyAddress4 andThenModify modifyStreetName4)
  .using(_.toUpperCase)(person)

// ----- Modify nested sealed hierarchies:

// Note: this feature is experimental and might not work due to compilation order issues.
// See https://issues.scala-lang.org/browse/SI-7046 for more details.

sealed trait Pet { def name: String }
case class Fish2(name: String) extends Pet
sealed trait LeggedPet         extends Pet
case class Cat2(name: String)  extends LeggedPet
case class Dog2(name: String)  extends LeggedPet

val pets = List[Pet](
  Fish2("Finn"),
  Cat2("Catia"),
  Dog2("Douglas")
)

val juniorPets =
  pets
    .modify(_.each.name)
    .using(_ + ", Jr.")
