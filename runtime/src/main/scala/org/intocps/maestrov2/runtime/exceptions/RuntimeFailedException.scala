package org.intocps.maestrov2.runtime.exceptions

final case class RuntimeFailedException(private val message: String = "", private val cause : Throwable = None.orNull) extends Exception(message, cause)
