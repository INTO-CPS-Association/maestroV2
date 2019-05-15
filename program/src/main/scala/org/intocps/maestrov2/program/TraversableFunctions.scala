package org.intocps.maestrov2.program

object TraversableFunctions {
  def sequence[A](a: Set[Option[A]]): Option[Set[A]] =
    a.foldRight[Option[Set[A]]](Some(Set()))((x: Option[A], y: Option[Set[A]]) =>
      y.flatMap( ya => x.map( xa => ya.+(xa))))


// Unfortunately this does not work due to interactions between type inference and implicit lookup...
  implicit class MSet[A](set: Set[Option[A]]) {
    def sequence: Option[Set[A]] =  set.foldRight[Option[Set[A]]](Some(Set()))((x: Option[A], y: Option[Set[A]]) =>
      y.flatMap( ya => x.map( xa => ya.+(xa))))
  }
}
