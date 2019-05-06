import java.io.File

import org.intocps.maestrov2.data._
import org.intocps.maestrov2.program.commands.{Command, CommandPrettyPrinter, MaestroV2Command}
import org.intocps.maestrov2.program.plugins.InitialisationCommandsComputer
import org.intocps.orchestration.coe.modeldefinition.ModelDescription
import org.scalatest.FlatSpec

import scala.collection.JavaConverters._

class InitialisationTests extends FlatSpec {


  /*      |----------------------NOT USED--------------------------|
          |   |------------|                |----------------|     |
          |->level  ----->valve -->  valvecontrol  ------> level---|
              |         |  |                |                |
            minLevel ---^  |                |                |
              |            |                |                |
              |  control c |                |      tank t    |
              |------------|                |----------------|

   */

  val tankFmu = TestData.tankFmu
  val tankInstance = InstanceFMUWithMD("t", tankFmu);

  val controlFmu = TestData.controlFmu
  val controlInstance = InstanceFMUWithMD("c", controlFmu);

  val controlInput = ConnectionScalarVariable("level", Instance("c", "control"))
  val controlInput2 = ConnectionScalarVariable("minlevel", Instance("c", "control"))

  val controlOutput = ConnectionScalarVariable("valve", Instance("c", "control"))
  // level -> valve
  val controlInternalConnection = Connection(controlInput, Set(controlOutput), ConnectionType.Internal)
  // minlevel -> valve
  val controlInternalConnection2 = Connection(controlInput2, Set(controlOutput), ConnectionType.Internal)


  val tankInput = ConnectionScalarVariable("valvecontrol", Instance("t", "tank"))
  val tankOutput = ConnectionScalarVariable("level", Instance("t", "tank"))
  // valveControl -> Level
  val tankInternalConnection = Connection(tankInput, Set(tankOutput), ConnectionType.Internal)

  // control.valve -> tank.valvecontrol
  val ext1 = Connection(controlOutput, Set(tankInput), ConnectionType.External)
  // tank.level -> control.level
  val ext2 = Connection(tankOutput, Set(controlInput), ConnectionType.External)

  val allConnections = Set(controlInternalConnection, tankInternalConnection, controlInternalConnection2, ext1);

  val dependentVariables = InitialisationCommandsComputer.calcDependentVariables(allConnections)

  "calcDependentVariables" should "verify three dependents" in {
    val expected = Set(controlOutput, tankOutput, tankInput);
    assert(dependentVariables.size == 3)
    assert(expected.equals(dependentVariables))
  }



  val orderedVariablesH: Seq[ConnectionScalarVariable] = InitialisationCommandsComputer.calcDependencies(allConnections, dependentVariables.head)
  val orderedVariablesL: Seq[ConnectionScalarVariable] = InitialisationCommandsComputer.calcDependencies(allConnections, dependentVariables.last)
  val orderedVariables: Set[Seq[ConnectionScalarVariable]] = dependentVariables.map(dependentVariable => InitialisationCommandsComputer.calcDependencies(allConnections, dependentVariable))
  "test 2" should "verify calcDependecies function" in {

    assert(orderedVariables.size == 3)
  }
  val z: Set[Seq[EnrichedConnectionScalarVariable]] = orderedVariables.map(
    seqCSV => seqCSV.map(
      CSV => {
        val groupByFMU: Set[(FMUWithMD, Set[String])] = Set((tankFmu, Set(tankInstance.name)), (controlFmu, Set(controlInstance.name)))
        val allFMU: Set[FMUWithMD] = groupByFMU.map(fmu => fmu._1)
        val currentFMU: Set[FMUWithMD] = allFMU.filter(fmu => fmu.key == CSV.vInstance.fmu)
        val currentVariables: Set[ModelDescription.ScalarVariable] = currentFMU.head.modelDescription.getScalarVariables.asScala.toSet
        val currentVariable: Set[ModelDescription.ScalarVariable] = currentVariables.filter(variable => variable.name == CSV.vName)
        EnrichedConnectionScalarVariable(CSV.vName, CSV.vInstance, currentVariable.head.causality, currentVariable.head.valueReference)
      }
    )
  )
  val q: Set[Seq[Command]] = z.map(SEQ => InitialisationCommandsComputer.calcCommandsForRuntime(SEQ))
  "test 3" should "verify calcCommandsForRuntime function" in {

    assert(q.size == 3)
  }


  val w: MaestroV2Command = InitialisationCommandsComputer.calcInitializationScalarCommand(allConnections, Set((tankFmu, Set(tankInstance.name)), (controlFmu, Set(controlInstance.name))))
  "test 3" should "verify calcInitializationScalarCommand function" in {

    println(CommandPrettyPrinter.PrintCommands(w, 0));

    assert(!(Nil equals w))
  }
}

