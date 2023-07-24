// see:
// https://www.optics.dev/Monocle/docs/optics/prism

// Prism

// intrinsic operations:
// - getOption: S => Option[A]
// - reverseGet (aka apply): A => S

// simplified Json encoding:
sealed trait Json
case object JNull                     extends Json
case class JStr(v: String)            extends Json
case class JNum(v: Double)            extends Json
case class JObj(v: Map[String, Json]) extends Json

import monocle.Prism

val pJStr_1 = Prism[Json, String] {
  case JStr(v) => Some(v)
  case _       => None
}(JStr)

val partialJStr: PartialFunction[Json, String] = { case JStr(v) => v }
val pJStr_2                                    =
  Prism[Json, String](partialJStr.lift)(JStr)

val jStr = Prism.partial[Json, String] { case JStr(v) => v }(JStr)

jStr("hello")
jStr.reverseGet("hello")

jStr.getOption(JStr("Hello"))
jStr.getOption(JNum(3.2))

// A Prism can be used in a pattern matching position:

def isLongString(json: Json): Boolean = json match {
  case jStr(v) => v.length > 100
  case _       => false
}

isLongString(JNull)
isLongString(JNum(3.2))
isLongString(JStr("Hello"))
isLongString(JStr("Hello" * 21))

// We can also use replace and modify to update a Json only if it is a JStr:

jStr.replace("Bar")(JStr("Hello"))
jStr.modify(_.reverse)(JStr("Hello"))

// If we supply another type of Json, replace and modify will be a no-op:

jStr.replace("Bar")(JNum(10))
jStr.modify(_.reverse)(JNum(10))

// If we care about the success or failure of the update, we can use replaceOption or modifyOption:

jStr.modifyOption(_.reverse)(JStr("Hello"))
jStr.modifyOption(_.reverse)(JNum(10))

// Prism Composition

import monocle.std.double.doubleToInt // Prism[Double, Int] defined in Monocle

val jNum: Prism[Json, Double] = Prism.partial[Json, Double] { case JNum(v) => v }(JNum)

val jInt: Prism[Json, Int] = jNum.andThen(doubleToInt)

jInt(5)

jInt.getOption(JNum(5.0))
jInt.getOption(JNum(5.2))
jInt.getOption(JStr("Hello"))

// Prism Generation

import monocle.macros.GenPrism

val rawJNum: Prism[Json, JNum] = GenPrism[Json, JNum]

rawJNum.getOption(JNum(4.5))
rawJNum.getOption(JStr("Hello"))

import monocle.macros.GenIso

val jNum2: Prism[Json, Double] =
  GenPrism[Json, JNum]
    .andThen(GenIso[JNum, Double])

val jNull: Prism[Json, Unit] =
  GenPrism[Json, JNull.type]
    .andThen(GenIso.unit[JNull.type])
