// Optics beyond Lenses with Monocle
//
// Iso
//
// see:
// https://scalac.io/blog/scala-optics-lenses-with-monocle/
// https://github.com/note/monocle-example/blob/master/src/test/scala/com/example/IsoExample.scala
//

import monocle._
import scala.util.Try

// business entities
case class Meter(whole: Int, fraction: Int)
case class Centimeter(whole: Int)

// Optics
val centimeterToMeterIso = Iso[Centimeter, Meter] { cm =>
  Meter(cm.whole / 100, cm.whole % 100)
} { m =>
  Centimeter(m.whole * 100 + m.fraction)
}

val intCentimeter                             = Iso[Int, Centimeter](Centimeter.apply)(_.whole)
val wholeMeterLens                            = Lens[Meter, Int](_.whole)(newWhole => prevMeter => prevMeter.copy(whole = newWhole))
val stringToIntPrism                          = Prism[String, Int](str => Try(str.toInt).toOption)(_.toString)
val stringToWholeMeter: Optional[String, Int] =
  stringToIntPrism
    .andThen(intCentimeter)
    .andThen(centimeterToMeterIso)
    .andThen(wholeMeterLens)

// centimeterToMeterIso should work
centimeterToMeterIso.modify(m => m.copy(m.whole + 3))(Centimeter(155)) == Centimeter(455)
centimeterToMeterIso.modify(meter => meter.copy(meter.whole + 3))(Centimeter(155)).toString

// centimeterToMeterIso be more readable with composed Optics
stringToWholeMeter.modify(_ + 3)("155") == "455"
