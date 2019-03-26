package org.intocps.maestrov2.scala.configuration.datatypes

import argonaut.DecodeJson

object MultiModelConfiguration
{
  implicit val dec: DecodeJson[MultiModelConfiguration] = DecodeJson(r => for {
    fmus <- (r --\ "fmus").as[Map[String, String]]
    connections <- (r --\ "connections").as[Map[String, List[String]]]
    parameters <- (r --\ "parameters").as[Map[String, ParameterValue]]
  } yield MultiModelConfiguration(fmus, connections, parameters));
}
case class MultiModelConfiguration(fmus: Map[String,String], connections: Map[String, List[String]], parameters: Map[String, ParameterValue]);


