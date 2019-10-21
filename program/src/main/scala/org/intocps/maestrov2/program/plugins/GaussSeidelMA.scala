package org.intocps.maestrov2.program.plugins

import org.intocps.maestrov2.data.{Connection, ConnectionScalarVariable, EnrichedConnectionScalarVariable, FMUWithMD, Instance, InstanceFMUWithMD}
import org.intocps.maestrov2.program.commands
import org.intocps.maestrov2.program.commands._
import org.intocps.maestrov2.program.exceptions.LookupFailedException
import org.intocps.orchestration.coe.modeldefinition.ModelDescription
import org.intocps.orchestration.coe.modeldefinition.ModelDescription.Causality

import scala.collection.JavaConverters._

object GaussSeidelMA {
  /* A GaussSeidel master algorithm roughly does the following
  Set inputs for first FMU
  DoStep for the first FMU
  Get outputs from the first FMU.
  Apply to next FMU and go on untill the last FMU
  These are to be encoded as
  seq(
    seq(setCMD(Set(inputs_fmu1)), doStep(fmu1), getCMD(Set(outputs_fmu1))
    seq(setCMD(Set(inputs_fmu2)), doStep(fmu2), getCMD(Set(outputs_fmu2))
      ...
    seq(setCMD(Set(inputs_fmuN)), doStep(fmuN), getCMD(Set(outputs_fmuN))
   )
    */
  def computeGaussSeidelIteration(instances: Set[InstanceFMUWithMD], extConnections: Set[Connection], order : Seq[ConnectionScalarVariable]): MaestroV2Seq = {

    val enrichedOrder : Seq[EnrichedConnectionScalarVariable] = order.map(x=>convertConnectionScalarVariable2EnrichedConnectionScalarVariable(instances,x))

    val instanceOrder : Seq[Instance] = order.map(x=>x.vInstance).distinct

    val enrichedOrderGroupedByInstance : Seq[(Instance, Seq[EnrichedConnectionScalarVariable])] = createInstanceVariableMap(instanceOrder,enrichedOrder)

    val GaussSeidelcommands : Seq[MaestroV2Seq] = enrichedOrderGroupedByInstance.toSeq.map(instance=>instanceScalarToInstanceCommand(instance))

    MaestroV2Seq(GaussSeidelcommands)
 }

  /*
  this function combines the ordered Seq of the Instances with the list of variables of the instances
  */
   def createInstanceVariableMap(instances : Seq[Instance], variables : Seq[EnrichedConnectionScalarVariable] ) : Seq[(Instance,Seq[EnrichedConnectionScalarVariable])] = {
    //val states = Map((instances.head, variables.filter(x=>x.vInstance == instances.head)),(instances.last, variables.filter(x=>x.vInstance == instances.last)))
    val instanceVariableMap = instances.map(instance => (instance,variables.filter(x=>x.vInstance == instance)))
    instanceVariableMap
  }

  /*
 this function converts a  ConnectionScalarVariable into an EnrichedScalarVariables,
 which also has info about the valueRef and Causality
  */
  def convertConnectionScalarVariable2EnrichedConnectionScalarVariable(groupByFMU: Set[InstanceFMUWithMD],variable: ConnectionScalarVariable):EnrichedConnectionScalarVariable = {

    val currentFMU: Set[InstanceFMUWithMD] = groupByFMU.filter(fmu => fmu.name == variable.vInstance.name)
    val currentVariable: ModelDescription.ScalarVariable = currentFMU.head.fmu.modelDescription.getScalarVariables.asScala.filter(x => x.name == variable.vName).head
    val enrichedVariable: EnrichedConnectionScalarVariable = EnrichedConnectionScalarVariable(variable.vName, variable.vInstance, currentVariable.causality, currentVariable.valueReference)
    enrichedVariable
  }

  /*
  this function produces the  seq(setCMD(Set(inputs_instanceX)), doStep(instanceX), getCMD(Set(outputs_instanceX))
  for an instance X. The argument is a record (Instance,Seq of enrichedscalarVariables)
  * */
    def instanceScalarToInstanceCommand(instance : (Instance, Seq[EnrichedConnectionScalarVariable])) : MaestroV2Seq = {
    val inputs =instance._2.toSet.filter(scalarVariable => scalarVariable.causality == Causality.Input)
    val setCmds: Set[Command] = inputs.map(scalaVariable =>
      SetCMD(scalaVariable.vInstance.fmu, scalaVariable.vInstance.name, Set(scalaVariable.valueRef))
    )
    val outputs =instance._2.toSet.filter(scalarVariable => scalarVariable.causality == Causality.Output)
      val getCmds: Set[Command] = outputs.map ( scalaVariable =>
        GetCMD(scalaVariable.vInstance.fmu, scalaVariable.vInstance.name, Set(scalaVariable.valueRef))
      )
        MaestroV2Seq(Seq(MaestroV2Set(setCmds),MaestroV2Set(Set(DoStepCMD(instance._1.fmu, instance._1.name))),MaestroV2Set(getCmds)))

  }
 }

