// Optics beyond Lenses with Monocle
//
// Simple example with sealed trait hierarchy
//
// see:
// https://scalac.io/blog/scala-optics-lenses-with-monocle/
// https://github.com/note/monocle-example/blob/master/src/test/scala/com/example/CoproductPrismExample.scala
//

import monocle.Prism
import munit.Assertions._

/** Prism for Coproduct - in that case modelled with sealed trait hierarchy.
  *
  * Example idea taken from http://julien-truffaut.github.io/Monocle/optics/prism.html
  */

sealed trait Json
case object JNull                     extends Json
case class JStr(v: String)            extends Json
case class JNum(v: Double)            extends Json
case class JObj(v: Map[String, Json]) extends Json

val stringPrism =
  Prism.partial[Json, String] { case JStr(v) => v }(JStr.apply)

stringPrism.getOption(JStr("someString")) == Some("someString")
stringPrism.reverseGet("someString") == JStr("someString")

// prism `getOption` returns None if does not succeed
stringPrism.getOption(JNull) == None

val someJson: Json = JStr("someString")

// first let's try to capitalize JStr with pattern match
val withPatternMatch = someJson match {
  case JStr(s)      => JStr(s.toUpperCase)
  case anythingElse => anythingElse
}

// now the same thing with Prism
val withPrism = stringPrism.modify(_.toUpperCase)(someJson)

withPrism == withPatternMatch
