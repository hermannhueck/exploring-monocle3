// Optics beyond Lenses with Monocle
//
// Prism as a safe downcasting
// Prism composition
//
// see:
// https://scalac.io/blog/scala-optics-lenses-with-monocle/
// https://github.com/note/monocle-example/blob/master/src/test/scala/com/example/NaivePrismExample.scala
//

import monocle.Prism

import scala.util.Try
import cats.Eq
import cats.syntax.eq._

case class Percent private (value: Int) {
  require(value >= 0)
  require(value <= 100)
}

object Percent {
  def fromInt(input: Int): Option[Percent] =
    if (input >= 0 && input <= 100) {
      Some(Percent(input))
    } else {
      None
    }

  implicit val eqInstance: Eq[Percent] = (p1: Percent, p2: Percent) => p1 == p2
}

object NaivePrisms {
  // this is not a lawful Prism but in naive version we don't care about it
  // for lawful Prism take a look at DowncastingPrisms
  val stringToIntPrism  = Prism[String, Int](str => Try(str.toInt).toOption)(_.toString)
  val intToPercentPrism = Prism[Int, Percent](i => Percent.fromInt(i))(_.value)
}

object DowncastingPrisms {
  val regex = "(-?[1-9][0-9]*)|0".r

  val stringToIntPrism  = Prism[String, Int] { str =>
    if (regex.pattern.matcher(str).matches) {
      Try(str.toInt).toOption
    } else {
      None
    }
  }(_.toString)
  val intToPercentPrism = Prism[Int, Percent](i => Percent.fromInt(i))(_.value)
}

import NaivePrisms._

// stringToIntPrism work for inputs parsable to Double
stringToIntPrism.getOption("22") === Some(22)
stringToIntPrism.replace(40)("22") === "40"
stringToIntPrism.modify(_ + 1)("22") === "23"

// stringToIntPrism should return None when `getOption` called on unparsable input
stringToIntPrism.getOption("someString") === None

// stringToIntPrism should return unmodified input when `set` called on unparsable input
stringToIntPrism.replace(40)("someString") === "someString"

// stringToIntPrism should return unmodified input when `modify` called on unparsable input
stringToIntPrism.modify(_ + 1)("someString") === "someString"

// stringToIntPrism should return Option informing about success of `setOption`
stringToIntPrism.replaceOption(40)("22") === Some("40")
stringToIntPrism.replaceOption(40)("someString") === None

// stringToIntPrism should return Option informing about success of `modifyOption`
stringToIntPrism.modifyOption(_ + 1)("22") === Some("23")
stringToIntPrism.modifyOption(_ + 1)("someString") === None

// prism composition should work as expected
val stringToPercent =
  stringToIntPrism andThen intToPercentPrism

stringToPercent.getOption("someString") === None
stringToPercent.getOption("188") === None
stringToPercent.getOption("88.0") === None
stringToPercent.getOption("88") === Percent.fromInt(80)
