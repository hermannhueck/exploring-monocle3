// see:
// https://www.optics.dev/Monocle/docs/optics/lens

// Lens

// intrinsic operations:
// - get: S => A
// - replace (aka set): A => S => S

case class Address(streetNumber: Int, streetName: String)

import monocle.Lens

val lStreetNumber_ = Lens[Address, Int](_.streetNumber)(n => a => a.copy(streetNumber = n))

// This case is really straightforward so we automated the generation of Lenses from case classes using a macro:

import monocle.macros.GenLens

val lStreetNumber = GenLens[Address](_.streetNumber)

val address = Address(10, "High Street")

lStreetNumber_.get(address)
lStreetNumber.get(address)

lStreetNumber_.replace(5)(address)
lStreetNumber.replace(5)(address)

lStreetNumber_.modify(_ + 1)(address)
lStreetNumber.modify(_ + 1)(address)

// modify is equivalent to using get and replace:
val n = lStreetNumber.get(address)
lStreetNumber.replace(n + 1)(address)

// We can push the idea even further, with modifyF we can update the target of a Lens in a context, cf cats.Functor:

def neighbors(n: Int): List[Int] =
  if (n > 0) List(n - 1, n + 1) else List(n + 1)

// import cats.implicits._ // to get all Functor instances
// it works without this import - why?

lStreetNumber.modifyF(neighbors)(address)
lStreetNumber.modifyF(neighbors)(Address(135, "High Street"))

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits._ // to get global ExecutionContext

def updateNumber(n: Int): Future[Int] =
  Future.successful(n + 1)

lStreetNumber.modifyF(updateNumber)(address)

case class Person(name: String, age: Int, address: Address)
val john = Person("John", 20, Address(10, "High Street"))

val lAddress = GenLens[Person](_.address)

lAddress.andThen(lStreetNumber).get(john)
lAddress.andThen(lStreetNumber).replace(2)(john)

// Other Ways of Lens Composition

val composedLens =
  GenLens[Person](_.name).replace("Mike") compose GenLens[Person](_.age).modify(_ + 1)

composedLens(john)

// Same but with the simplified macro based syntax:

import monocle.macros.syntax.lens._

john.lens(_.name).replace("Mike").lens(_.age).modify(_ + 1)

// Sometimes you need an easy way to update Product type inside Sum type -
// for that case you can compose Prism with Lens by using some:

import monocle.macros.GenLens

case class B(c: Int)
case class A(b: Option[B])

val c = GenLens[B](_.c)
val b = GenLens[A](_.b)

b.some
  .andThen(c)
  .getOption(A(Some(B(1))))

// Lens Generation

import monocle.macros.GenLens

val age = GenLens[Person](_.age)

GenLens[Person](_.address.streetName).replace("Iffley Road")(john)

// annotate all fields with @Lenses

import monocle.macros.Lenses

@Lenses
case class Point(x: Int, y: Int)

val p = Point(5, 3)

Point.x.get(p)
// res13: Int = 5
Point.y.replace(0)(p)

// You can also add a prefix to @Lenses in order to prefix the generated Lenses:

@Lenses("_")
case class PrefixedPoint(x: Int, y: Int)

val p2 = PrefixedPoint(5, 3)

PrefixedPoint._x.get(p2)
PrefixedPoint._y.replace(0)(p2)

// Note: before using @Lenses remember to activate macro annotations.
