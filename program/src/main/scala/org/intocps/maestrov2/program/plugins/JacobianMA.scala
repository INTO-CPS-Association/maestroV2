package org.intocps.maestrov2.program.plugins

import org.intocps.maestrov2.data.{Connection, ConnectionScalarVariable, Instance, InstanceFMUWithMD}
import org.intocps.maestrov2.program.commands._
import org.intocps.maestrov2.program.exceptions.LookupFailedException

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

    // calculate the set commands

    def getValRefsOrThrowException(svs : Set[ConnectionScalarVariable]) :  Set[Long] = {
      svs.map((sv: ConnectionScalarVariable) => sv.getValueReference(instances).getOrElse(throw new LookupFailedException("Failed to find the valuereference for the SV with name: " + sv.vName + " in fmu: " + sv.vInstance.fmu)))
    }

    val setCmds: Set[Command] = inputSvsGroupedByInstance.map{case (i, svs: Set[ConnectionScalarVariable]) =>
      SetCMD(i.fmu, i.name, getValRefsOrThrowException(svs))}.toSet

    val doStepCmds: Set[Command] = instances.map(i => DoStepCMD(i.fmu.key, i.name))


    val outputsGroupedByInstance = extConnections.map(c => c.from).groupBy(cf => cf.vInstance)
    val getCmds : Set[Command] =  outputsGroupedByInstance.map{case (i, svs) => GetCMD(i.fmu, i.name, getValRefsOrThrowException(svs))}.toSet

    MaestroV2Seq(Seq(MaestroV2Set(setCmds),MaestroV2Set(doStepCmds), MaestroV2Set(getCmds)))



  }

}
