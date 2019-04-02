package org.intocps.maestrov2.scala

import java.io.File
import java.util

import org.intocps.fmi.IFmu
import org.intocps.fmi.jnifmuapi.{Factory, FmuModelDescription}
import org.intocps.maestrov2.scala.commands._
import org.intocps.maestrov2.scala.configuration.{ConfigurationHandler, Conversions}
import org.intocps.maestrov2.scala.configuration.datatypes.{MultiModelConfiguration, ParameterValue}
import org.intocps.maestrov2.scala.exceptions.AlgebraicLoopException
import org.intocps.maestrov2.scala.modeldescription._
import org.intocps.orchestration.coe.modeldefinition.ModelDescription
import org.intocps.orchestration.coe.modeldefinition.ModelDescription.{Initial, Type, Types, Variability}
import plugins.{IODependencyAcyclic, IODependencyCalculator, IODependencyCyclic, IODependencyResult}

import scala.collection.{immutable, mutable}
import scala.collection.JavaConverters._


object HelloWorld {
  def test(args: Array[String]): Unit = {
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

  // The FMUs has been unpacked
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
    val FMUsWithMDs: immutable.Iterable[FMUWithMD] = fmuEntities.map { case (k, f) => FMUWithMD(k, new ModelDescription(f.getModelDescription)) };

    // Enrich instances with their respective model descriptions.
    val enrichedInstances: Option[Set[InstanceFMUWithMD]] =
      FunctorFunctions.sequence(instances.map(
        x => {
          val fmuWithMd: Option[FMUWithMD] = FMUsWithMDs.find(y => y.key == x.fmu)
          fmuWithMd match {
            case Some(fmu) => Some(InstanceFMUWithMD(x.name, fmu))
            case None => None
          }
        }))

    enrichedInstances.map(x => contAfterEnrichedInstances(externalConnections, x, mmc.parameters))
  }




  // All instances has been found and enriched with their respective FMU
  def contAfterEnrichedInstances(extConnections: Set[Connection], instances: Set[InstanceFMUWithMD], parameters: Map[String, ParameterValue]): Unit = {
    val connections: Set[Connection] = Connections.calculateConnections(extConnections, instances)

    // At this stage, connections contain all connections, both internal and external.
    // It should now be possible to perform a topological sort
    // TODO: Perform topological sorting using the IODependencyCalculator plugin.
    val ioDepRes: IODependencyResult = IODependencyCalculator.CalculateIODependencies(connections)
    val order: immutable.Seq[ConnectionScalarVariable] = ioDepRes match {
      case IODependencyCyclic(cycle) => throw new AlgebraicLoopException("Algebraic loop detected: " + cycle)
      case IODependencyAcyclic(totalOrder) => totalOrder
    }

    val groupByFMU: Set[(FMUWithMD, Set[String])] = instances.groupBy(x => x.fmu).map { case (f, sI) => (f, sI.map(x => x.name)) }.toSet
    // groupByFMUNamed is a set with tuples. Each tuple is (fmuName, Set of Instances of the fmu by Name)
    val groupedByFMUNamed: Set[(String, Set[String])] = groupByFMU.map { case (f, sI) => (f.key, sI) }

    val instantiateCommands: MaestroV2Command = calcInstantiate(groupedByFMUNamed)
    val setupExperimentCommands : MaestroV2Command= calcSetupExperiment(groupedByFMUNamed)
    val setIniCommands : MaestroV2Command = calcSetINI(groupByFMU)
    val enterInitCommands : MaestroV2Command = calcEnterInitializationMode(groupedByFMUNamed)





    //If topological sorting suceeded then we expect the co-simulation can be carried out.

    // TODO: Instantiate the FMUs using FMI2Instantiate (Should return an instantiate command with all relevant data)
    // TODO: Invoke FMI2SetupExperiment for the FMU instances (Should return a setup experiment command with all relevant data)
    // TODO: Set scalar variables with: <ScalarVariable initial = "exact" or "approx"> (p. 22)
    // TODO: Invoke FMI2EnterInitializationMode for the FMU Instances

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

  def calcInstantiate(fmusToInstances: Set[(String, Set[String])]): MaestroV2Command = {
    // TODO: Can an FMU handle concurrent instantiation?
    val b: Set[Command] = fmusToInstances.map { case (a, b) => InstantiateCMD(a, b) };
    MaestroV2Set(b);
  }

  def calcSetupExperiment(fmusToInstances: Set[(String, Set[String])]): MaestroV2Command = {
    MaestroV2Set(fmusToInstances.map { case (a, b) => SetupExperimentCMD(a, b) })
  }

  /*
   INI is one of: Real, Integer, Boolean, String for a variable with
   variability != constant
   initial == "exact" || "approx"
    */
  def calcSetINI(groupByFMU: Set[(FMUWithMD, Set[String])]): MaestroV2Command = {
    val x: Set[Command] = groupByFMU.map { case (fmu, setInstances) =>
      val svs: List[Long] = fmu.modelDescription.getScalarVariables.asScala.toList.filter(s =>
        s.variability != Variability.Constant
          && (s.initial == Initial.Exact || s.initial == Initial.Approx)
          && (s.`type`.`type` != Types.Enumeration)
      ).map(x => x.valueReference)
      SetIniCMD(fmu.key, setInstances, svs)
    }

    MaestroV2Set(x);
  }

  def calcEnterInitializationMode(groupedByFMUNamed: Set[(String, Set[String])]): MaestroV2Command = {
    MaestroV2Set(groupedByFMUNamed.map { case (a, b) => EnterInitializationModeCMD(a, b) })
  }
}