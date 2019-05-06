import java.io.File

import org.intocps.maestrov2.data.{FMUWithMD, InstanceFMUWithMD}
import org.intocps.maestrov2.program.Connections
import org.intocps.orchestration.coe.modeldefinition.ModelDescription

object TestData {
  val mdTank = new ModelDescription(new File("src/test/resources/single-watertank/modelDescription-singlewatertank-20sim.xml"))

  val tankFmu = FMUWithMD("tank",mdTank, Connections.calculateInternalConnections(mdTank))
  val tankInstance = InstanceFMUWithMD("t", tankFmu)

  val mdCrtl = new ModelDescription(new File("src/test/resources/single-watertank/modelDescription-watertankcontroller-c.xml"))
  val controlFmu = FMUWithMD("control", mdCrtl, Connections.calculateInternalConnections(mdCrtl))
  val controlInstance = InstanceFMUWithMD("c", controlFmu)

}
