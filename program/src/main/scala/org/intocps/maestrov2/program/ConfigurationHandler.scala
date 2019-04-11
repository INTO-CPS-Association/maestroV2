package org.intocps.maestrov2.program

import java.io.File

import argonaut.Parse

object ConfigurationHandler {


  def loadMMCFromFile(file: File): Either[String, MultiModelConfiguration] = {
    val source = scala.io.Source.fromFile(file)
    val lines = try source.mkString finally source.close();
    val mmc: Either[String, MultiModelConfiguration] = Parse.decodeEither[MultiModelConfiguration](lines);
    mmc;
  }

}
