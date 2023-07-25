// see:
// https://www.optics.dev/Monocle/docs/optics/iso

// Iso

// intrinsic operations:
// - get: S => A
// - reverseGet (aka apply): A => S

case class Person(name: String, age: Int)

import monocle.Iso

val personToTuple = Iso[Person, (String, Int)](p => (p.name, p.age)) { case (name, age) => Person(name, age) }

personToTuple.get(Person("Zoe", 25))

personToTuple.reverseGet(("Zoe", 25))
personToTuple.apply(("Zoe", 25))
personToTuple(("Zoe", 25))
personToTuple("Zoe" -> 25)

def listToVector[A] = Iso[List[A], Vector[A]](_.toVector)(_.toList)

listToVector.get(List(1, 2, 3))

def vectorToList[A] = listToVector[A].reverse

vectorToList.get(Vector(1, 2, 3))

val stringToList = Iso[String, List[Char]](_.toList)(_.mkString(""))

stringToList.modify(_.tail)("Hello")

// Iso Generation

case class MyString(s: String)
case class Foo()
case object Bar

import monocle.macros.GenIso

GenIso[MyString, String]
// res6: Iso[MyString, String] = monocle.PIso$$anon$3@6f2d1c9a
GenIso[MyString, String].get(MyString("Hello"))

GenIso.unit[Foo]
// res7: Iso[Foo, Unit] = monocle.PIso$$anon$3@a6d795f
val u1: Unit = GenIso.unit[Foo].get(Foo())

GenIso.unit[Bar.type]
// res8: Iso[Bar.type, Unit] = monocle.PIso$$anon$3@6f2d1c9a
val u2: Unit = GenIso.unit[Bar.type].get(Bar)

GenIso.fields[Person]
// res9: Iso[Person, (String, Int)] = monocle.PIso$$anon$3@6f2d1c9a
GenIso.fields[Person].get(Person("John", 42))
