package org.intocps.maestrov2.program

object FunctorFunctions {
  def sequence[A](a: Set[Option[A]]): Option[Set[A]] =
    a.foldRight[Option[Set[A]]](Some(Set()))((x: Option[A], y: Option[Set[A]]) =>
      y.flatMap( ya => x.map( xa => ya.+(xa))))

}
