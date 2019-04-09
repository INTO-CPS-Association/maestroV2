package org.intocps.maestrov2.scala.exceptions

final case class LookupFailedException(private val message: String = "", private val cause : Throwable = None.orNull) extends Exception(message, cause)
