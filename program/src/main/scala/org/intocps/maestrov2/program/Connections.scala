package org.intocps.maestrov2.program

import org.intocps.maestrov2.data._
import org.intocps.orchestration.coe.modeldefinition.ModelDescription

import scala.collection.JavaConverters._

object Connections {

  def calculateExternalConnections(connections: Map[String, List[String]]) =
    Conversions.MMCConnectionsToMaestroConnections(connections);

  // TODO: Improve by calculating internal connections on FMU level instead of Instance level
  def calculateInternalConnections(instances: Set[InstanceFMUWithMD]) =
  {
    val internalConnections: Set[OneToOneConnection] = instances.flatMap{ x =>
      val outputs: List[ModelDescription.ScalarVariable]  = x.fmu.modelDescription.getOutputs().asScala.toList;
      outputs.flatMap(output => {
        val instance = Instance(x.name, x.fmu.key)
        val output_ = ConnectionScalarVariable(output.name, instance)
        output.outputDependencies.asScala.toList.map{
          case (input: ModelDescription.ScalarVariable,_) => {
            val input_ = ConnectionScalarVariable(input.name, instance)
            OneToOneConnection(input_, output_)
          }}.toSet
      }).toSet
    }
    // This updates the previous internalConnections to be from ONE input to potentially MANY outputs
    val internalConnections_ : Set[Connection] = internalConnections.map(x => x.from).map(input => {
      val outputs: Set[ConnectionScalarVariable] = internalConnections.filter(c => c.from == input).map(c => c.to)
      Connection(input, outputs, ConnectionType.Internal)
    })

    internalConnections_
  }

  def calculateConnections(externalConnections: Set[Connection], instances: Set[InstanceFMUWithMD]): Set[Connection] =
  {
    externalConnections.union(calculateInternalConnections(instances));
  }


}
