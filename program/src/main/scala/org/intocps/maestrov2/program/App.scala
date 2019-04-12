package org.intocps.maestrov2.program

import java.io.File

import org.intocps.fmi.IFmu
import org.intocps.fmi.jnifmuapi.Factory
import org.intocps.maestrov2.data._
import org.intocps.maestrov2.program.commands._
import org.intocps.maestrov2.program.exceptions.AlgebraicLoopException
import org.intocps.maestrov2.program.plugins.{IODependencyCalculator, InitialisationCommandComputer}
import org.intocps.orchestration.coe.modeldefinition.ModelDescription


/**
 * @author ${user.name}
 */
object App {
  
  def foo(x : Array[String]) = x.foldLeft("")((a,b) => a + b)
  
  def main(args : Array[String]) {
    if (args.length == 1) {
      val mmc: Either[String, MultiModelConfiguration] = ConfigurationHandler.loadMMCFromFile(new File(args(0)));
      mmc.map(x => contAfterMMCLoaded(x));


    }
    else
      throw new IllegalArgumentException("Lacking path argument");
    println("Hello world from Scala!")
  }

  // The multimodel has been parsed
  def contAfterMMCLoaded(mmc: MultiModelConfiguration) = {

    // Use JNI FMU API to unpack the FMU archives.
    val fmuEntities: Option[Map[String, IFmu]] = createFMUEntities(mmc.fmus);

    // Calculate connections between instances of FMUs


    fmuEntities.map(entities => contAfterFMUEntsCreated(mmc, entities));
  }

  // The FMUs have been unpacked
  def contAfterFMUEntsCreated(mmc: MultiModelConfiguration, fmuEntities: Map[String, IFmu]) = {
    // External connections are connections between instances of FMUs.
    // These originate from the multi model.
    val externalConnections: Set[Connection] = Conversions.MMCConnectionsToMaestroConnections(mmc.connections);

    val instances: Set[Instance] = externalConnections.flatMap { x =>
      val toInstance: Set[Instance] = x.to.map(y => y.vInstance)
      val ret: Set[Instance] = toInstance.+(x.from.vInstance);
      ret
    }

    // Associate FMUs with their model descriptions
    val FMUsWithMDs: Iterable[FMUWithMD] = fmuEntities.map { case (k, f) => FMUWithMD(k, new ModelDescription(f.getModelDescription)) };

    // Enrich instances with their respective model descriptions.
    val enrichedInstances: Option[Set[InstanceFMUWithMD]] =
      TraversableFunctions.sequence(instances.map(
        x => {
          val fmuWithMd: Option[FMUWithMD] = FMUsWithMDs.find(y => y.key == x.fmu)
          fmuWithMd match {
            case Some(fmu) => Some(InstanceFMUWithMD(x.name, fmu))
            case None => None
          }
        }))
    val parameters_ = mmc.parameters.map{case (s,v) => (Conversions.configVarToConnectionSV(s),v)};
    enrichedInstances.map(x => contAfterEnrichedInstances(externalConnections, x, parameters_))
  }




  // All instances has been found and enriched with their respective FMU
  def contAfterEnrichedInstances(extConnections: Set[Connection], instances: Set[InstanceFMUWithMD], parameters: Map[ConnectionScalarVariable, ParameterValue]): Unit = {
    val connections: Set[Connection] = Connections.calculateConnections(extConnections, instances)

    // At this stage, connections contain all connections, both internal and external.
    // It should now be possible to perform a topological sort
    // TODO: Perform topological sorting using the IODependencyCalculator plugin.
    val ioDepRes: IODependencyResult = IODependencyCalculator.CalculateIODependencies(connections)
    val order: Seq[ConnectionScalarVariable] = ioDepRes match {
      case IODependencyCyclic(cycle) => throw new AlgebraicLoopException("Algebraic loop detected: " + cycle)
      case IODependencyAcyclic(totalOrder) => totalOrder
    }

    val groupByFMU: Set[(FMUWithMD, Set[String])] = instances.groupBy(x => x.fmu).map { case (f, sI) => (f, sI.map(x => x.name)) }.toSet
    // groupByFMUNamed is a set with tuples. Each tuple is (fmuName, Set of Instances of the fmu by Name)
    val groupedByFMUNamed: Set[(String, Set[String])] = groupByFMU.map { case (f, sI) => (f.key, sI) }

    val instantiateCommands : MaestroV2Command = CommandComputer.instanceCommands(groupedByFMUNamed, (a, b) => InstantiateCMD(a,b));
    val setupExperimentCommands : MaestroV2Command = CommandComputer.instanceCommands(groupedByFMUNamed, (a, b) => SetupExperimentCMD(a,b));
    val setIniCommands : MaestroV2Command = InstantiatedCommandsComputer.calcSetINI(groupByFMU)
    val enterInitCommands : MaestroV2Command = CommandComputer.instanceCommands(groupedByFMUNamed, (a, b) => EnterInitializationModeCMD(a,b));
    val initializationScalarCommand : MaestroV2Command = InitialisationCommandComputer.calcInitializationScalarCommand(connections,groupByFMU)
    val exitInitCommands : MaestroV2Command = CommandComputer.instanceCommands(groupedByFMUNamed, (a,b) => ExitInitializationModeCMD(a,b));

  }

  /*
  Argument is map from fmu key to fmu path
   */
  def createFMUEntities(fmus: Map[String, String]): Option[Map[String, IFmu]] = {
    try {
      Some(fmus.map { case (k, v) => (k, Factory.create(new File(v))) });
    } catch {
      case _ => None
    }
  }

}
