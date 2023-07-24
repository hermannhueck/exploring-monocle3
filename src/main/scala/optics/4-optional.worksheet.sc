// see:
// https://www.optics.dev/Monocle/docs/optics/optional

// Optional

// intrinsic operations:
// - getOption: S => Option[A]
// - replace (aka set): A => S => S

import monocle.Optional

val head = Optional[List[Int], Int] {
  case Nil     => None
  case x :: xs => Some(x)
} { a =>
  {
    case Nil     => Nil
    case x :: xs => a :: xs
  }
}

val xs = List(1, 2, 3)
val ys = List.empty[Int]

head.nonEmpty(xs)
head.nonEmpty(ys)

head.getOrModify(xs)
head.getOrModify(ys)

head.getOption(xs)
head.getOption(ys)

head.replace(5)(xs)
head.replace(5)(ys)

head.modify(_ + 1)(xs)
head.modify(_ + 1)(ys)

head.modifyOption(_ + 1)(xs)
head.modifyOption(_ + 1)(ys)
