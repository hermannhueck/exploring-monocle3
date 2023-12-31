// Optics beyond Lenses with Monocle
//
// Other usages of Lens
//
// see:
// https://scalac.io/blog/scala-optics-lenses-with-monocle/
// https://github.com/note/monocle-example/blob/master/src/test/scala/com/example/MaintainingInvariantsExample.scala
//

import monocle.Lens

import scala.util.Try
import cats.Eq
import cats.syntax.eq._

/** Example inspired by Simon Payton Jones talk: https://skillsmatter.com/skillscasts/4556-simon-peyton-jones Example
  * with Duration starts around 42:30
  */
case class Duration(hours: Int, minutes: Int, seconds: Int) {
  private val secondsInHour   = 60 * 60
  private val secondsInMinute = 60

  def asSeconds = hours * secondsInHour + minutes * secondsInMinute + seconds
}

object Duration {
  implicit val eqInstance: Eq[Duration] = (d1: Duration, d2: Duration) => {
    d1.asSeconds == d2.asSeconds
  }
}

/** NOTE: those Lens are not lawful!
  *
  * DurationOpticsSpec proves that
  */
object DurationOptics {

  val hoursL = Lens[Duration, Int](_.hours)(newHours => duration => duration.copy(hours = newHours))

  val minutesL: Lens[Duration, Int] = Lens[Duration, Int](_.minutes) { newMinutes => duration =>
    val minutesValidated = newMinutes % 60
    val newHours         = (newMinutes / 60) + duration.hours
    val durationUpdated  = hoursL.replace(newHours)(duration)
    durationUpdated.copy(minutes = minutesValidated)
  }

  val secondsL = Lens[Duration, Int](_.seconds) { newSeconds => duration =>
    val secondsValidated = newSeconds % 60
    val newMinutes       = (newSeconds / 60) + duration.minutes
    val durationUpdated  = minutesL.replace(newMinutes)(duration)
    durationUpdated.copy(seconds = secondsValidated)
  }

}

import DurationOptics._

// updating hours should maintain invariants
hoursL.modify(_ + 10)(Duration(3, 10, 50)) === Duration(13, 10, 50)

// updating minutes should maintain invariant
minutesL.modify(_ + 10)(Duration(3, 20, 50)) === Duration(3, 30, 50)
minutesL.modify(_ + 10)(Duration(3, 55, 50)) === Duration(4, 5, 50)

minutesL.modify(_ + 100)(Duration(3, 0, 50)) === Duration(4, 40, 50)
minutesL.modify(_ + 239)(Duration(3, 0, 50)) === Duration(6, 59, 50)
minutesL.modify(_ + 240)(Duration(3, 0, 50)) === Duration(7, 0, 50)

minutesL.modify(_ => 0)(Duration(3, 2, 50)) === Duration(3, 0, 50)

// updating seconds should maintain invariant
secondsL.modify(_ + 9)(Duration(3, 20, 50)) === Duration(3, 20, 59)
secondsL.modify(_ + 10)(Duration(3, 20, 50)) === Duration(3, 21, 0)
secondsL.modify(_ + 11)(Duration(3, 20, 50)) === Duration(3, 21, 1)

secondsL.modify(_ + 100)(Duration(3, 0, 0)) === Duration(3, 1, 40)
secondsL.modify(_ + (7 * 60))(Duration(3, 0, 0)) === Duration(3, 7, 0)

secondsL.modify(_ + (121 * 60))(Duration(3, 0, 0)) === Duration(5, 1, 0)
secondsL.modify(_ + (121 * 60) + 17)(Duration(3, 0, 0)) === Duration(5, 1, 17)
