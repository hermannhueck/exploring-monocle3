package quicklens

import scala.util.chaining._
import util._
import com.softwaremill.quicklens._
import scala.util.Try

object QuickLensGithubReadmeExamples extends App {

  def printSubTitle(subTitle: String): Unit =
    s"${line(10)} $subTitle".green pipe println

  case class Street(name: String)
  case class Address(street: Street)
  case class Person(address: Address, age: Int)

  val person = Person(Address(Street("1 Functional Rd.")), 35) tap println

  "Modify deeply nested fields in case classes:" pipe printSubTitle

  val p2 = person
    .modify(_.address.street.name)
    .using(_.toUpperCase) tap println

  val p3 = person
    .modify(_.address.street.name)
    .setTo("3 OO Ln.")

  // or

  val p4 = modify(person)(_.address.street.name).using(_.toUpperCase)
  val p5 = modify(person)(_.address.street.name).setTo("3 OO Ln.")

  "Chain modifications:" pipe printSubTitle

  person
    .modify(_.address.street.name)
    .using(_.toUpperCase)
    .modify(_.age)
    .using(_ - 1) pipe println

  "Modify conditionally:" pipe printSubTitle

  person
    .modify(_.address.street.name)
    .setToIfDefined(Some("3 00 Ln.")) pipe println

  val shouldChangeAddress = true
  person
    .modify(_.address.street.name)
    .setToIf(shouldChangeAddress)("3 00 Ln.") pipe println

  "Modify several fields in one go:" pipe printSubTitle

  case class Person2(firstName: String, middleName: Option[String], lastName: String)

  val person2 = Person2("john", Some("steve"), "smith") tap println

  person2
    .modifyAll(_.firstName, _.middleName.each, _.lastName)
    .using(_.capitalize) pipe println

  "Traverse options/lists/maps using .each:" pipe printSubTitle

  case class Street3(name: String)
  case class Address3(street: Option[Street3])
  case class Person3(addresses: List[Address3])

  val person3 = Person3(
    List(
      Address3(Some(Street3("1 Functional Rd."))),
      Address3(Some(Street3("2 Imperative Dr.")))
    )
  ) tap println

  person3
    .modify(_.addresses.each.street.each.name)
    .using(_.toUpperCase) pipe println

  "Traverse selected elements using .eachWhere:" pipe printSubTitle

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

  "Modify specific sequence elements using .at:" pipe printSubTitle

  "-- Modify Address at illegal index 2:" pipe println
  Try {
    person3
      .modify(_.addresses.at(2).street.each.name)
      .using(_.toUpperCase)
  } pipe println

  "-- Modify Address at legal index 1:" pipe println
  person3
    .modify(_.addresses.at(1).street.each.name)
    .using(_.toUpperCase) pipe println

  "Modify specific map elements using .at:" pipe printSubTitle

  case class Property(value: String)
  case class Person4(name: String, props: Map[String, Property])

  val person4 = Person4(
    "Joe",
    Map("Role" -> Property("Programmmer"), "Age" -> Property("45"))
  ) tap println

  person4
    .modify(_.props.at("Age").value)
    .setTo("46") pipe println

  Try {
    person4
      .modify(_.props.at("AgeXXX").value)
      .setTo("46")
  } pipe println

  "Modify specific map elements using .index:" pipe printSubTitle

  person4
    .modify(_.props.index("Age").value)
    .setTo("46") pipe println

  person4
    .modify(_.props.index("AgeXXX").value)
    .setTo("46") pipe println

  "Modify specific elements in an option or map with a fallback using .atOrElse:" pipe printSubTitle

  person4
    .modify(_.props.atOrElse("NumReports", Property("0")).value)
    .setTo("5") pipe println

  person3
    .modify(_.addresses.at(1).street.atOrElse(Street3("main street")).name)
    .using(_.toUpperCase) pipe println

  "Modify Either fields using .eachLeft and eachRight:" pipe printSubTitle

  case class AuthContext(token: String)
  case class AuthRequest(url: String)
  case class Resource(auth: Either[AuthContext, AuthRequest])

  val devResource =
    Resource(auth = Left(AuthContext("fake"))) tap println

  val prodResource =
    devResource
      .modify(_.auth.eachLeft.token)
      .setTo("real") tap println

  "Modify fields when they are of a certain subtype:" pipe printSubTitle

  trait Animal
  case class Dog(age: Int)        extends Animal
  case class Cat(ages: List[Int]) extends Animal

  case class Zoo(animals: List[Animal])

  val zoo =
    Zoo(List(Dog(4), Cat(List(3, 12, 13)))) tap println

  val olderZoo = zoo
    .modifyAll(
      _.animals.each.when[Dog].age,
      _.animals.each.when[Cat].ages.at(0)
    )
    .using(_ + 1) tap println

  "Re-usable modifications (lenses):" pipe printSubTitle

  val modifyStreetName = modify(_: Person)(_.address.street.name)

  modifyStreetName(person).using(_.toUpperCase) pipe println
  modifyStreetName(person).using(_.toLowerCase) pipe println

  val upperCaseStreetName = modify(_: Person)(_.address.street.name).using(_.toUpperCase)

  upperCaseStreetName(person) pipe println

  "Re-usable modifications (lenses): Alternate syntax:" pipe printSubTitle

  val modifyStreetName2 = modifyLens[Person](_.address.street.name)

  modifyStreetName2.using(_.toUpperCase)(person) pipe println
  modifyStreetName2.using(_.toLowerCase)(person) pipe println

  val upperCaseStreetName2 = modifyLens[Person](_.address.street.name).using(_.toUpperCase)

  upperCaseStreetName2(person) pipe println

  "Composing lenses:" pipe printSubTitle

  val modifyAddress3    = modify(_: Person)(_.address)
  val modifyStreetName3 = modify(_: Address)(_.street.name)

  (modifyAddress3 andThenModify modifyStreetName3)(person)
    .using(_.toUpperCase) pipe println

  "or, with alternate syntax:" pipe printSubTitle

  val modifyAddress4    = modifyLens[Person](_.address)
  val modifyStreetName4 = modifyLens[Address](_.street.name)

  (modifyAddress4 andThenModify modifyStreetName4)
    .using(_.toUpperCase)(person) pipe println

  "Modify nested sealed hierarchies:" pipe printSubTitle

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
  ) tap println

  val juniorPets =
    pets
      .modify(_.each.name)
      .using(_ + ", Jr.") tap println
}
