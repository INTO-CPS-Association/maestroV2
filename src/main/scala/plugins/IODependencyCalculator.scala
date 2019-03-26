package plugins

import org.intocps.maestrov2.plugins.iodependencycalculator.{CycleCheckResult, IODependencyCalculator, IOResult}
import org.intocps.maestrov2.scala.modeldescription.Connection

// Example of a plugin calculate the IO dependencies
object IODependencyCalculator {

  def CalculateIODependencies(connections: Set[Connection]): IODependencyResult = {

    val result: IODependencyResult = org.intocps.maestrov2.plugins.iodependencycalculator.IODependencyCalculator.calculateIODependencies(connections)
    result

  }

}