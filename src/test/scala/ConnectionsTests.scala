import java.io.File

import org.intocps.maestrov2.scala.Connections
import org.intocps.maestrov2.scala.configuration.Conversions
import org.intocps.maestrov2.scala.modeldescription._
import org.intocps.orchestration.coe.modeldefinition.ModelDescription
import org.scalatest.FlatSpec

class ConnectionsTests extends FlatSpec{
  val tankFmu = FMUWithMD("tank",
    new ModelDescription(new File("src/test/resources/single-watertank/modelDescription-singlewatertank-20sim.xml")))
  val tankInstance = new InstanceFMUWithMD("t", tankFmu);

  val controlFmu = FMUWithMD("control",
    new ModelDescription(new File("src/test/resources/single-watertank/modelDescription-watertankcontroller-c.xml")))
  val controlInstance = new InstanceFMUWithMD("c", controlFmu);

  "CalculateInternalConnections" should "calculate all internal connections of all instances" in {
    val actual = Connections.calculateInternalConnections(Set(tankInstance, controlInstance));
    val expected = Set(Connection(
      ConnectionScalarVariable("valvecontrol",Instance("t","tank")),
      Set(ConnectionScalarVariable("level",Instance("t","tank"))),ConnectionType.Internal))

    assert(actual==expected)

  }
}
