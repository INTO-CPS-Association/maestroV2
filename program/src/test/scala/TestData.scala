import java.io.File


import org.intocps.maestrov2.data._
import org.intocps.maestrov2.program.Connections
import org.intocps.maestrov2.program.plugins.InitialisationCommandsComputer
import org.intocps.orchestration.coe.modeldefinition.ModelDescription

object TestData {

  /*
          |------------|                |----------------|
        level       valve -->  valvecontrol      level---|
          |            |                |                |
        minLevel       |                |                |
          |            |                |                |
          |  control c |                |      tank t    |
          |------------|                |----------------|

*/

  val mdTank = new ModelDescription(new File("src/test/resources/single-watertank/modelDescription-singlewatertank-20sim.xml"))

  val tankFmu = FMUWithMD("tank",mdTank, Connections.calculateInternalConnections(mdTank))
  val tankInstanceName = "t"
  val tankInstance = InstanceFMUWithMD(tankInstanceName, tankFmu)

  val tankInput = ConnectionScalarVariable("valvecontrol", Instance("t", "tank"))
  val tankOutput = ConnectionScalarVariable("level", Instance("t", "tank"))

  val mdCrtl = new ModelDescription(new File("src/test/resources/single-watertank/modelDescription-watertankcontroller-c.xml"))
  val controlFmu = FMUWithMD("control", mdCrtl, Connections.calculateInternalConnections(mdCrtl))
  val controlInstanceName = "c"
  val controlInstance = InstanceFMUWithMD(controlInstanceName, controlFmu)

  val controlInput = ConnectionScalarVariable("level", Instance("c", "control"))
  val controlInput2 = ConnectionScalarVariable("minlevel", Instance("c", "control"))

  val controlOutput = ConnectionScalarVariable("valve", Instance("c", "control"))

  val mapFmusToInstances: Map[FMUWithMD, Set[String]] = Map(tankFmu -> Set(tankInstanceName), controlFmu -> Set(controlInstanceName))

  object Scenario1{
    /*
          |----------------------NOT USED--------------------------|
          |   |------------|                |----------------|     |
          |->level  ----->valve -->  valvecontrol  ------> level---|
              |         |  |                |                |
            minLevel ---^  |                |                |
              |            |                |                |
              |  control c |                |      tank t    |
              |------------|                |----------------|

   */

    // level -> valve
    val controlInternalConnection = Connection(controlInput, Set(controlOutput), ConnectionType.Internal)
    // minlevel -> valve
    val controlInternalConnection2 = Connection(controlInput2, Set(controlOutput), ConnectionType.Internal)

    // valveControl -> Level
    val tankInternalConnection = Connection(tankInput, Set(tankOutput), ConnectionType.Internal)

    // control.valve -> tank.valvecontrol
    val ext1 = Connection(controlOutput, Set(tankInput), ConnectionType.External)
    // tank.level -> control.level
    val ext2 = Connection(tankOutput, Set(controlInput), ConnectionType.External)

    val externalConnections = Set(ext1)

    val allConnections = Set(controlInternalConnection, tankInternalConnection, controlInternalConnection2).union(externalConnections)

    val dependentVariables = InitialisationCommandsComputer.calcDependentVariables(allConnections)
  }

  object Scenario2{
    /*
         |--------------------------------------------------------|
         |   |------------|                |----------------|     |
         |->level        valve ---->valvecontrol  ------> level---|
             |            |                |                |
           minLevel       |                |                |
             |            |                |                |
             |  control c |                |      tank t    |
             |------------|                |----------------|

  */

    // valveControl -> Level
    val tankInternalConnection = Connection(tankInput, Set(tankOutput), ConnectionType.Internal)

    // c.valve -> t.valvecontrol
    // t.level -> c.level
    val externalConnections = Set(Connection(controlOutput, Set(tankInput), ConnectionType.External), Connection(tankOutput, Set(controlInput), ConnectionType.External))
  }

}
