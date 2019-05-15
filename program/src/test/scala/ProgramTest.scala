import java.io.File

import org.intocps.maestrov2.data.Instance
import org.intocps.maestrov2.program.commands.CommandPrettyPrinter
import org.intocps.maestrov2.program.{ConfigurationHandler, Program}
import org.scalatest.FlatSpec

class ProgramTest extends FlatSpec {

  "computeProgram" should "Compute a program" in {

    val is = Map(TestData.tankFmu -> Set(Instance("t", "tank")), TestData.controlFmu -> Set(Instance("c", "control")))
    val connections = TestData.Scenario1.allConnections;

    val prog = Program.computeCommands(is, connections)
    assert(prog.isRight)
    prog.map(x => println(CommandPrettyPrinter.PrintCommands(x,0)))
  }
}
