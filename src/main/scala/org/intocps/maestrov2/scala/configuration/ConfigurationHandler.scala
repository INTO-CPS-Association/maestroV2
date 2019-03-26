package org.intocps.maestrov2.scala.configuration

import java.io.File

import argonaut._
import Argonaut._
import org.intocps.maestrov2.scala.configuration.datatypes._

import scala.io.BufferedSource

object ConfigurationHandler {


  def loadMMCFromFile(file: File): Either[String, MultiModelConfiguration] = {
    val source = scala.io.Source.fromFile(file)
    val lines = try source.mkString finally source.close();
    val mmc: Either[String, MultiModelConfiguration] = Parse.decodeEither[MultiModelConfiguration](lines);
    mmc;
  }

}
