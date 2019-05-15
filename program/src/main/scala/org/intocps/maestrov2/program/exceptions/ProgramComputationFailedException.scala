package org.intocps.maestrov2.program.exceptions

final case class ProgramComputationFailedException(private val message: String = "", private val cause : Throwable = None.orNull) extends Exception(message, cause)
