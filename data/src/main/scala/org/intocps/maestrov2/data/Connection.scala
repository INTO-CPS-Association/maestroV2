package org.intocps.maestrov2.data

import org.intocps.maestrov2.data.ConnectionType.ConnectionType

// A connection is a from output to inputs.
case class Connection(from: ConnectionScalarVariable, to: Set[ConnectionScalarVariable], typeOf: ConnectionType)
