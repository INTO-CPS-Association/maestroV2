package org.intocps.maestrov2.program

import java.io
import java.io.File

import org.intocps.fmi.IFmu
import org.intocps.fmi.jnifmuapi.Factory
import org.intocps.maestrov2.data._
import org.intocps.maestrov2.program.commands._
import org.intocps.maestrov2.program.exceptions.{AlgebraicLoopException, ProgramComputationFailedException}
import org.intocps.maestrov2.program.plugins.{IODependencyCalculator, InitialisationCommandsComputer, JacobianMA}
import org.intocps.orchestration.coe.modeldefinition.ModelDescription

import scala.collection.immutable

object Program {
  def computeProgram(mmc: MultiModelConfiguration): Either[Exception, MaestroV2Command] = {
    // External Connections are connections between outputs of one FMU and inputs of another FMU
    val externalConnections: Set[Connection] = Conversions.MMCConnectionsToMaestroConnections(mmc.connections)

    // Extract instances from connections and associate these with their respective FMU
    val instances: Set[Instance] = computeInstances(externalConnections)

    // Unpack FMUs and calculate internal dependencies
    val fmus: Map[FMUWithMD, Set[Instance]] = mmc.fmus.foldRight(Map.empty[FMUWithMD, Set[Instance]]) { case ((key, path), acc) =>
      val md = new ModelDescription(Factory.create(new File(path)).getModelDescription)
      val fmu = FMUWithMD(key, md, Connections.calculateInternalConnections(md))
      val is = instances.collect { case i if i.fmu == fmu.key => i }
      acc.+(fmu -> is)
    }

    // All connections are both internal and external connections
    val allConnections: Set[Connection] = externalConnections.union(
      Connections.FMUInternalConnectionsToConnections(fmus))

    // TODO: PLUGIN
    // Perform topological sorting
    val topologicalSortedSVs: Either[Exception, List[ConnectionScalarVariable]] = topologicalSort(allConnections)

    // TODO: Use topSortedSVs
    val program: Either[Exception, MaestroV2Command] = topologicalSortedSVs.flatMap(_ => computeCommands(fmus, allConnections))

    program

  }

  def computeCommands(is: Map[FMUWithMD, Set[Instance]], connections: Set[Connection]) = {
    val isInstanceCommandsView: Map[FMUWithMD, Set[String]] = is.map { case (k, is) => (k, is.map(i => i.name)) }

    val isSetView: Set[(FMUWithMD, Set[String])] = isInstanceCommandsView.toSet

    val instantiate: MaestroV2Command = CommandComputer.instanceCommandsMap(isInstanceCommandsView, (a : FMUWithMD, b: Set[String]) => InstantiateCMD(a.key, b))
    val setupExperiment = CommandComputer.instanceCommandsMap(isInstanceCommandsView, (a : FMUWithMD, b: Set[String]) => SetupExperimentCMD(a.key, b))
    val setIniCommands: MaestroV2Command = InstantiatedCommandsComputer.calcSetINI(isSetView)
    val enterInitCommands: MaestroV2Command = CommandComputer.instanceCommandsMap(isInstanceCommandsView, (a : FMUWithMD, b: Set[String]) => EnterInitializationModeCMD(a.key, b))
    val initializationScalarCommand: MaestroV2Command = InitialisationCommandsComputer.calcInitializationScalarCommand(connections, isSetView)
    val exitInitCommands: MaestroV2Command = CommandComputer.instanceCommandsMap(isInstanceCommandsView, (a : FMUWithMD, b: Set[String]) => ExitInitializationModeCMD(a.key, b))

    val ma : Option[MaestroV2Seq] = JacobianMA.computeJacobianIteration2(isInstanceCommandsView, connections)

    val program: Option[MaestroV2Seq] = for {
      masterAlgo <- ma
    } yield MaestroV2Seq(List(instantiate, setupExperiment, setIniCommands, enterInitCommands, initializationScalarCommand, exitInitCommands, masterAlgo))

    program match {
      case None => Left(exceptions.ProgramComputationFailedException("One or more program computation steps failed"))
      case Some(value) => Right(value)
    }
  }

  def topologicalSort(connections: Set[Connection]): Either[AlgebraicLoopException, List[ConnectionScalarVariable]] = {
    val ioDepRes: IODependencyResult = IODependencyCalculator.CalculateIODependencies(connections)
    ioDepRes match {
      case IODependencyCyclic(cycle) => Left(new AlgebraicLoopException("Algebraic loop detected: " + cycle))
      case IODependencyAcyclic(totalOrder) => Right(totalOrder)
    }
  }

  // Extracts instances from connections
  def computeInstances(connections: Set[Connection]): Set[Instance] = {
    connections.flatMap { conn =>
      val fromInstance: Instance = conn.from.vInstance
      val toInstances: Set[Instance] = conn.to.map(_.vInstance)
      toInstances.+(fromInstance)
    }
  }
}
