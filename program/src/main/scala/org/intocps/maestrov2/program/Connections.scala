package org.intocps.maestrov2.program

import org.intocps.maestrov2.data._
import org.intocps.orchestration.coe.modeldefinition.ModelDescription
import org.intocps.orchestration.coe.modeldefinition.ModelDescription.ScalarVariable

import scala.collection.JavaConverters._
import scala.collection.{immutable, mutable}

object Connections {

  def calculateExternalConnections(connections: Map[String, List[String]]) =
    Conversions.MMCConnectionsToMaestroConnections(connections);

  // TODO: Improve by calculating internal connections on FMU level instead of Instance level
  def calculateInternalConnections(instances: Set[InstanceFMUWithMD]) = {
    val internalConnections: Set[OneToOneConnection] = instances.flatMap { x =>
      val outputs: List[ModelDescription.ScalarVariable] = x.fmu.modelDescription.getOutputs().asScala.toList;
      outputs.flatMap(output => {
        val instance = Instance(x.name, x.fmu.key)
        val output_ = ConnectionScalarVariable(output.name, instance)
        output.outputDependencies.asScala.toList.map {
          case (input: ModelDescription.ScalarVariable, _) => {
            val input_ = ConnectionScalarVariable(input.name, instance)
            OneToOneConnection(input_, output_)
          }
        }.toSet
      }).toSet
    }
    // This updates the previous internalConnections to be from ONE input to potentially MANY outputs
    val internalConnections_ : Set[Connection] = internalConnections.map(x => x.from).map(input => {
      val outputs: Set[ConnectionScalarVariable] = internalConnections.filter(c => c.from == input).map(c => c.to)
      Connection(input, outputs, ConnectionType.Internal)
    })

    internalConnections_
  }

  def calculateInternalConnections(md: ModelDescription): Set[ConnectionIndependent] = {
    val outputs = md.getOutputs.asScala.toSet
    val deps = outputs.flatMap(x => x.outputDependencies.keySet().asScala)

    deps.map { d =>
      val from: ConnectionScalarVariableIndependent = ConnectionScalarVariableIndependent(d.name)
      val to: Set[ConnectionScalarVariableIndependent] = outputs.collect { case o if o.outputDependencies.containsKey(d) => ConnectionScalarVariableIndependent(o.name) }
      ConnectionIndependent(from, to, ConnectionType.Internal)
    }


  }

  def calculateConnections(externalConnections: Set[Connection], instances: Set[InstanceFMUWithMD]): Set[Connection] = {
    externalConnections.union(calculateInternalConnections(instances));
  }

  def FMUInternalConnectionsToConnections(fIs: Map[FMUWithMD, Set[Instance]]): Set[Connection] = {
    def IndependantToDependant(fmu: String, instance: String)(connectionIndependent: ConnectionIndependent): Connection = {
      def singleConv(cSV: ConnectionScalarVariableIndependent): ConnectionScalarVariable = {
        ConnectionScalarVariable(connectionIndependent.from.vName, Instance(instance, fmu))
      }

      Connection(
        singleConv(connectionIndependent.from),
        connectionIndependent.to.map(singleConv(_)),
        connectionIndependent.typeOf)
    }

    val res1: Set[Connection] = fIs.flatMap { case (k, v) =>
      v.flatMap(i => k.connections.map(IndependantToDependant(k.key, i.name)))
    }.toSet

    res1
  }


}
