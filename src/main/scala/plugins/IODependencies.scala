package plugins

import org.intocps.maestrov2.scala.modeldescription.ConnectionScalarVariable

sealed trait IODependencyResult
case class IODependencyCyclic(cycle: String) extends IODependencyResult
case class IODependencyAcyclic(totalOrder : List[ConnectionScalarVariable]) extends IODependencyResult
