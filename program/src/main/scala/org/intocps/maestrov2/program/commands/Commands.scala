package org.intocps.maestrov2.program.commands



trait Command

trait MaestroV2Command extends Command
case class MaestroV2Set(commands: Set[Command]) extends MaestroV2Command
case class MaestroV2Seq(commands: Seq[Command]) extends MaestroV2Command

sealed trait FMICommand extends Command
case class InstantiateCMD(fmu: String, instances: Set[String]) extends FMICommand
case class SetupExperimentCMD(fmu: String, instances: Set[String] ) extends FMICommand
case class SetIniCMD(fmu: String, instances: Set[String], scalarVariables: List[Long]) extends FMICommand
case class EnterInitializationModeCMD(fmu: String, instances: Set[String]) extends FMICommand
case class ExitInitializationModeCMD(fmu: String, instances: Set[String]) extends FMICommand
case class SetCMD(fmu: String, instance: String, scalarVariables: Set[Long]) extends FMICommand
case class GetCMD(fmu: String, instance: String, scalarVariables: Set[Long]) extends FMICommand
case class DoStepCMD(fmu: String, instance: String) extends FMICommand
case class TerminateCMD(fmu: String, instance: String) extends FMICommand
case class FreeInstanceCMD(fmu: String, instance: String) extends FMICommand