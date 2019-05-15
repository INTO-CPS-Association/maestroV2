import org.intocps.maestrov2.program.commands.CommandPrettyPrinter
import org.intocps.maestrov2.program.plugins.JacobianMA
import org.scalatest.FlatSpec


class JacobianMATests extends FlatSpec {

  "JacobianMA" should "compute the Jacobian Master Algorithm" in {
    val actual = JacobianMA.computeJacobianIteration2(TestData.mapFmusToInstances, TestData.Scenario2.externalConnections)
    assert(actual.isDefined)
    System.out.println(CommandPrettyPrinter.PrintCommands(actual.get, 0))
    assert(actual.get.commands.length == 3)
  }

}
