package org.intocps.maestrov2.scala.exceptions

final case class AlgebraicLoopException(private val message: String = "", private val cause : Throwable = None.orNull) extends Exception(message, cause)
