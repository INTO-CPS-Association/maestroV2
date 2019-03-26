package org.intocps.maestrov2.scala.modeldescription

import org.intocps.maestrov2.scala.modeldescription.ConnectionType.ConnectionType

// A connection is a from output to inputs.
case class Connection(from: ConnectionScalarVariable, to: Set[ConnectionScalarVariable], typeOf: ConnectionType)
