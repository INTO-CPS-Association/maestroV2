package org.intocps.maestrov2.program

import argonaut.DecodeJson

object ParameterValue {
  implicit val dec: DecodeJson[ParameterValue] = DecodeJson(c =>
    c.as[Int].map[ParameterValue](IntegerVal(_))
      ||| c.as[Boolean].map[ParameterValue](BooleanVal(_))
      ||| c.as[String].map[ParameterValue](StringVal(_))
      ||| c.as[Double].map[ParameterValue](RealVal(_)))
}

sealed trait ParameterValue
case class BooleanVal(v: Boolean) extends ParameterValue
case class RealVal(v: Double) extends ParameterValue
case class StringVal(v: String) extends ParameterValue
case class IntegerVal(v: Int) extends ParameterValue