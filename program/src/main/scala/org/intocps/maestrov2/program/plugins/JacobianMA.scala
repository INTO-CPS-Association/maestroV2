package org.intocps.maestrov2.program.plugins

import java.lang

import org.intocps.maestrov2.data._
import org.intocps.maestrov2.program.TraversableFunctions
import org.intocps.maestrov2.program.TraversableFunctions._
import org.intocps.maestrov2.program.commands._
import org.intocps.maestrov2.program.exceptions.LookupFailedException

import scala.collection.JavaConverters._
import scala.collection.immutable

object JacobianMA {
  /* A jacobian master algorithm roughly does the following
  Set inputs for all FMUs
  DoStep for all FMUs
  Get outputs for all FMUs.
  These are to be encoded as
  For all FMUs . seq(setCMD(sv1,...,svN), doStep(fmu1,...,fmuN), getCMD(sv1,...,svN))
  OR
   */
  def computeJacobianIteration(instances: Set[InstanceFMUWithMD], extConnections: Set[Connection]): MaestroV2Seq = {
    // Compute set commands
    // Get all inputs
    val inputs: Set[ConnectionScalarVariable] = extConnections.flatMap(x => x.to)

    // Group variables by instance
    val inputSvsGroupedByInstance: Map[Instance, Set[ConnectionScalarVariable]] = inputs.groupBy(x => x.vInstance);



    def getValRefsOrThrowException(svs : Set[ConnectionScalarVariable]) :  Set[Long] = {
      svs.map((sv: ConnectionScalarVariable) => sv.getValueReference(instances).getOrElse(throw new LookupFailedException("Failed to find the valuereference for the SV with name: " + sv.vName + " in fmu: " + sv.vInstance.fmu)))
    }

    // calculate the set commands
    val setCmds: Set[Command] = inputSvsGroupedByInstance.map{case (i, svs: Set[ConnectionScalarVariable]) =>
      SetCMD(i.fmu, i.name, getValRefsOrThrowException(svs))}.toSet

    val doStepCmds: Set[Command] = instances.map(i => DoStepCMD(i.fmu.key, i.name))

    val outputsGroupedByInstance = extConnections.map(c => c.from).groupBy(cf => cf.vInstance)
    val getCmds : Set[Command] =  outputsGroupedByInstance.map{case (i, svs) => GetCMD(i.fmu, i.name, getValRefsOrThrowException(svs))}.toSet

    MaestroV2Seq(Seq(MaestroV2Set(setCmds),MaestroV2Set(doStepCmds), MaestroV2Set(getCmds)))
  }

  def computeJacobianIteration2(instances: Map[FMUWithMD, Set[String]], extConnections: Set[Connection]): Option[MaestroV2Seq] = {
    // Compute set commands
    // Get all inputs
    val inputs: Set[ConnectionScalarVariable] = extConnections.flatMap(x => x.to)

    // Group variables by instance
    val inputSvsGroupedByInstance: Map[Instance, Set[ConnectionScalarVariable]] = inputs.groupBy(x => x.vInstance)

    def getValRefs(svs : Set[ConnectionScalarVariable]) :  Option[Set[Long]] = {
      val t: Set[Option[Long]] = svs.flatMap((conSV: ConnectionScalarVariable) =>
        instances.keys.find(_.key == conSV.vInstance.fmu)
          .map(i => i.modelDescription.getScalarVariables.asScala.toList.find(sv => sv.name == conSV.vName)
            .map(sv => sv.getValueReference.toLong)))

      t.sequence
    }

   // calculate the set commands
    val setCmds: Option[Set[Command]] = TraversableFunctions.sequence(inputSvsGroupedByInstance.map{case (i, svs: Set[ConnectionScalarVariable]) =>
      val vrefs = getValRefs(svs)
      vrefs.map(xs => SetCMD(i.fmu, i.name, xs))}.toSet)

    val doStepCmds: Set[Command] = instances.flatMap{case (k,v) => v.map(DoStepCMD(k.key, _))}.toSet

    val outputsGroupedByInstance = extConnections.map(c => c.from).groupBy(cf => cf.vInstance)
    val getCmds: Option[Set[Command]] =  TraversableFunctions.sequence(outputsGroupedByInstance.map{case (i, svs) =>
      val vrefs = getValRefs(svs)
      vrefs.map(xs => GetCMD(i.fmu, i.name, xs))}.toSet)

    for{
      sets <- setCmds
      gets <- getCmds
    } yield MaestroV2Seq(Seq(MaestroV2Set(sets.asInstanceOf[Set[Command]]),MaestroV2Set(doStepCmds), MaestroV2Set(gets.asInstanceOf[Set[Command]])))
  }

}
