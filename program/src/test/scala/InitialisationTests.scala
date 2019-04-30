import java.io.File

import org.intocps.maestrov2.data._
import org.intocps.maestrov2.program.commands.{Command, CommandPrettyPrinter, MaestroV2Command}
import org.intocps.maestrov2.program.plugins.InitialisationCommandsComputer
import org.intocps.orchestration.coe.modeldefinition.ModelDescription
import org.scalatest.FlatSpec

import scala.collection.JavaConverters._

class InitialisationTests extends FlatSpec {

  val tankFmu = FMUWithMD("tank",
    new ModelDescription(new File("src/test/resources/single-watertank/modelDescription-singlewatertank-20sim.xml")))
  val tankInstance = new InstanceFMUWithMD("t", tankFmu);

  val controlFmu = FMUWithMD("control",
    new ModelDescription(new File("src/test/resources/single-watertank/modelDescription-watertankcontroller-c.xml")))
  val controlInstance = new InstanceFMUWithMD("c", controlFmu);

  val interCon1Input = ConnectionScalarVariable("level", Instance("c", "control"))
  val interCon1Input1 = ConnectionScalarVariable("minlevel", Instance("c", "control"))

  val interCon1Output = ConnectionScalarVariable("valve", Instance("c", "control"))
  val internalCon1 = Connection(interCon1Input, Set(interCon1Output), ConnectionType.Internal)
  val internalCon3 = Connection(interCon1Input1, Set(interCon1Output), ConnectionType.Internal)

  val interCon2Input = ConnectionScalarVariable("valvecontrol", Instance("t", "tank"))
  val interCon2Output = ConnectionScalarVariable("level", Instance("t", "tank"))
  val interCon2 = Connection(interCon2Input, Set(interCon2Output), ConnectionType.Internal)

  val ext1 = Connection(interCon1Output, Set(interCon2Input), ConnectionType.External)
  val ext2 = Connection(interCon2Output, Set(interCon1Input), ConnectionType.External)

  val allConnections = Set(internalCon1, interCon2, internalCon3, ext1);

  val dependentVariables = InitialisationCommandsComputer.calcDependentVariables(allConnections)
  "test 1" should "verify three dependents" in {
    assert(dependentVariables.size == 3)
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

