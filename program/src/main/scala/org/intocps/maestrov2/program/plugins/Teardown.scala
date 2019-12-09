package org.intocps.maestrov2.program.plugins

import org.intocps.maestrov2.data._
import org.intocps.maestrov2.program.commands._
import org.intocps.orchestration.coe.modeldefinition.ModelDescription
import org.intocps.orchestration.coe.modeldefinition.ModelDescription.Causality

import scala.collection.JavaConverters._

object Teardown {
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
  def computeTeardown(isInstanceCommandsView: Map[FMUWithMD, Set[String]]): MaestroV2Set = {
    val teardown: Set[Command] = isInstanceCommandsView.map{case (fmu, instances) =>
      val ret: Set[MaestroV2Seq] = instances.map(instance => MaestroV2Seq(Seq(TerminateCMD(fmu.key, instance), FreeInstanceCMD(fmu.key, instance))))
    ret
    }.flatten.toSet
    MaestroV2Set(teardown)


 }

 }

