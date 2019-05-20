import org.intocps.maestrov2.data.{ConnectionScalarVariable, IODependencyAcyclic, IODependencyCyclic, IODependencyResult}
import org.intocps.maestrov2.dependencycalculatorplugin.IODependencyCalculator
import org.intocps.maestrov2.program.commands.CommandPrettyPrinter
import org.intocps.maestrov2.program.exceptions.AlgebraicLoopException
import org.intocps.maestrov2.program.plugins.GaussSeidelMA
import org.scalatest.FlatSpec

class GaussSeidelMATests  extends FlatSpec {
  "GaussSeidelMA" should "compute the GaussSiedel Master Algorithm" in {
    val ioDepRes: IODependencyResult = IODependencyCalculator.calculateIODependencies(TestData.Scenario1.allConnections)
    val order: Seq[ConnectionScalarVariable] = ioDepRes match {
      case IODependencyCyclic(cycle) => throw  AlgebraicLoopException("Algebraic loop detected: " + cycle)
      case IODependencyAcyclic(totalOrder) => totalOrder
    }
    val actual = GaussSeidelMA.computeGaussSeidelIteration(Set(TestData.controlInstance, TestData.tankInstance), TestData.Scenario2.externalConnections, order)
    System.out.println(CommandPrettyPrinter.PrintCommands(actual, 0))
    assert(actual.commands.length == 2)
    }
}