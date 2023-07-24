// see:
// https://www.optics.dev/Monocle/docs/focus#update-a-single-element-inside-a-list-scala-3-only

// Update a single element inside a list (Scala 3 only)

import java.time.YearMonth

case class User(name: String, debitCards: List[DebitCard])
case class DebitCard(cardNumber: String, expirationDate: YearMonth, securityCode: Int)

val anna = User(
  "Anna",
  List(
    DebitCard("4568 5794 3109 3087", YearMonth.of(2022, 4), 361),
    DebitCard("5566 2337 3022 2470", YearMonth.of(2024, 8), 990)
  )
)

val bob = User("Bob", List())

import monocle.syntax.all._

anna
  .focus(_.debitCards.index(0).expirationDate)
  .replace(YearMonth.of(2026, 2))

bob
  .focus(_.debitCards.index(1).as[DebitCard].expirationDate)
  .replace(YearMonth.of(2026, 2))

// replace had no effect on bob because he doesn't have a debit card.
// index only targets the object at the specified key. If there is no value at this key, then replace and modify are no-operation.
// index also works on other "indexable" datastructures such as Vector or Map.
