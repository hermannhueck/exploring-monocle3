// Optics beyond Lenses with Monocle
//
// Optional
//
// see:
// https://scalac.io/blog/scala-optics-lenses-with-monocle/
// https://github.com/note/monocle-example/blob/master/src/test/scala/com/example/OptionalExample.scala
//

import monocle._
import scala.util.Try

sealed trait Error
case class ErrorA(message: String, details: DetailedErrorA) extends Error
case object ErrorB                                          extends Error

case class DetailedErrorA(detailedMessage: String)

object ErrorOptics {

  // That's straightforward approach, not recommended but shows the essence of Optional:
  val detailedErrorA = Optional[Error, String] {
    case err: ErrorA => Some(err.details.detailedMessage)
    case _           => None
  } { newDetailedMsg => from =>
    from match {
      case err: ErrorA => err.copy(details = err.details.copy(newDetailedMsg))
      case _           => from
    }
  }

  // Better approach is to get `Optional[OperationError, String]` by composing Prism and Lens:
  val errorA = Prism.partial[Error, ErrorA] { case err: ErrorA =>
    err
  }(identity)

  val detailedError =
    Lens[ErrorA, DetailedErrorA](_.details)(newDetails => from => from.copy(details = newDetails))

  val detailedErrorMsg =
    Lens[DetailedErrorA, String](_.detailedMessage)(newMsg => from => from.copy(detailedMessage = newMsg))

  val composedDetailedErrorA: Optional[Error, String] =
    errorA.andThen(detailedError.andThen(detailedErrorMsg))
}

import ErrorOptics._

def testExamples(operationToTest: Example => Boolean)(examples: Seq[Example]): Seq[Boolean] = {
  examples.map { example =>
    operationToTest(example)
  }
}

case class Example(input: Error, expectedOutput: Option[Error])

val examples = List(
  Example(ErrorA("msg", DetailedErrorA("detailedMessage")), Some(ErrorA("msg", DetailedErrorA("DETAILEDMESSAGE")))),
  Example(ErrorB, None)
)

val modify =
  detailedErrorA
    .modifyOption(_.toUpperCase)

val results = testExamples { example =>
  (modify(example.input) == example.expectedOutput)
}(examples)
