package org.intocps.maestrov2.program.plugins

import org.intocps.maestrov2.data.{Connection, IODependencyResult}

// Example of a plugin calculate the IO dependencies
object IODependencyCalculator {

  def CalculateIODependencies(connections: Set[Connection]): IODependencyResult = {
    val result: IODependencyResult = org.intocps.maestrov2.dependencycalculatorplugin.IODependencyCalculator.calculateIODependencies(connections)
    result
  }
}