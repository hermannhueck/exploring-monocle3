// see:
// https://www.optics.dev/Monocle/docs/optics/traversal

// Traversal

// A Traversal is the generalisation of an Optional to several targets.
// In other words, a Traversal allows to focus from a type S into 0 to n values of type A.

// The most common example of a Traversal would be to focus into all elements inside of a container (e.g. List, Vector, Option).
// To do this we will use the relation between the typeclass cats.Traverse and Traversal:

import monocle.Traversal
// import cats.implicits._ // to get all cats instances including Traverse[List]
// it works without this import - why?

val xs = List(1, 2, 3, 4, 5)

val eachL = Traversal.fromTraverse[List, Int]

eachL.replace(0)(xs)
eachL.modify(_ + 1)(xs)

// A Traversal is also a Fold, so we have access to a few interesting methods to query our data:

eachL.getAll(xs)
eachL.headOption(xs)
eachL.find(_ > 3)(xs)
eachL.all(_ % 2 == 0)(xs)

case class Point(id: String, x: Int, y: Int)

val points = Traversal.apply2[Point, Int](_.x, _.y)((x, y, p) => p.copy(x = x, y = y))

points.replace(5)(Point("bottom-left", 0, 0))

import monocle.Traversal
import cats.Applicative
import cats.implicits._
import alleycats.std.map._ // to get Traverse instance for Map (SortedMap does not require this import)

def filterKey[K, V](predicate: K => Boolean): Traversal[Map[K, V], V] =
  new Traversal[Map[K, V], V] {
    def modifyA[F[_]: Applicative](f: V => F[V])(s: Map[K, V]): F[Map[K, V]] =
      s.map { case (k, v) =>
        k -> (if (predicate(k)) f(v) else v.pure[F])
      }.sequence
  }

val m = Map(1 -> "one", 2 -> "two", 3 -> "three", 4 -> "Four")

val filterEven = filterKey[Int, String](_ % 2 == 0)

filterEven.modify(_.toUpperCase)(m)
