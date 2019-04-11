package org.intocps.maestrov2.data

sealed trait IODependencyResult
case class IODependencyCyclic(cycle: String) extends IODependencyResult
case class IODependencyAcyclic(totalOrder : List[ConnectionScalarVariable]) extends IODependencyResult
