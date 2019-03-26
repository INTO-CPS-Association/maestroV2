package org.intocps.maestrov2.scala

import java.io.File
import java.util

import org.intocps.fmi.IFmu
import org.intocps.fmi.jnifmuapi.{Factory, FmuModelDescription}
import org.intocps.maestrov2.scala.configuration.{ConfigurationHandler, Conversions}
import org.intocps.maestrov2.scala.configuration.datatypes.MultiModelConfiguration
import org.intocps.maestrov2.scala.modeldescription._
import org.intocps.orchestration.coe.modeldefinition.ModelDescription

import scala.collection.immutable
import scala.collection.JavaConverters._



object HelloWorld {
  def test(args: Array[String]): Unit = {
    if(args.length == 1)
      {
        val mmc: Either[String, MultiModelConfiguration] = ConfigurationHandler.loadMMCFromFile(new File(args(0)));
        mmc.map(x => contAfterMMCLoaded(x));



      }
    else
      throw new IllegalArgumentException("Lacking path argument");
    println("Hello world from Scala!")
  }

  // The MM has been parsed
  def contAfterMMCLoaded(mmc : MultiModelConfiguration) =
  {
    // JNI FMU API takes care of unpacking
    val fmuEntities: Option[Map[String, IFmu]] = createFMUEntities(mmc.fmus);
  }

  // The FMUs has been unpacked
  def contAfterFMUEntsCreated(mmc : MultiModelConfiguration, fmuEntities : Map[String, IFmu]) =
  {
    // External connections are connections between instances of FMUs.
    // These originate from the multi model.
    val externalConnections: Set[Connection] = Conversions.MMCConnectionsToMaestroConnections(mmc.connections);

    val instances: Set[Instance]= externalConnections.flatMap{ x =>
      val toInstance: Set[Instance] = x.to.map(y => y.vInstance)
      val ret: Set[Instance] = toInstance.+(x.from.vInstance);
      ret
    }
    // Associate FMUs with their model descriptions
    val FMUsWithMDs: immutable.Iterable[FMUWithMD] = fmuEntities.map{case (k, f) => FMUWithMD(k, new ModelDescription(f.getModelDescription))};

    // Enrich instance FMUs with FMUWithMD
    val enrichedInstances: Option[Set[InstanceFMUWithMD]] = FunctorFunctions.sequence(instances.map(
      x => {
        val fmuWithMd: Option[FMUWithMD] = FMUsWithMDs.find(y => y.key == x.fmu)
        fmuWithMd match {
        case Some(fmu) => Some(InstanceFMUWithMD(x.name, fmu))
        case None => None
        }}))

    enrichedInstances.map(x => contAfterEnrichedInstances(externalConnections, x));
  }

  // All instances has been found and enriched with their respective FMU
  def contAfterEnrichedInstances(extConnections: Set[Connection], instances: Set[InstanceFMUWithMD]): Unit =
  {
    val connections: Set[Connection] = Connections.calculateConnections(extConnections, instances);
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