package org.intocps.maestrov2.runtime

import org.apache.commons.lang.NotImplementedException
import org.intocps.maestrov2.data.{Connection, FMUWithMD, Instance}
import org.intocps.maestrov2.program.MultiModelConfiguration
import org.intocps.maestrov2.program.commands._
import org.intocps.maestrov2.program.controlCommands.ControlCommand

object Runtime {

  def executeFMICommand(command: FMICommand, state: State): Either[Exception, State] = {
    command match {
      case cmd: InstantiateCMD                        => FMICommandsRuntime.instantiate(cmd, state)
      case SetupExperimentCMD(fmu, instances)         => Left(new NotImplementedException)
      case SetIniCMD(fmu, instances, scalarVariables) => Left(new NotImplementedException)
      case EnterInitializationModeCMD(fmu, instances) => Left(new NotImplementedException)
      case ExitInitializationModeCMD(fmu, instances)  => Left(new NotImplementedException)
      case SetCMD(fmu, instance, scalarVariables)     => Left(new NotImplementedException)
      case GetCMD(fmu, instance, scalarVariables)     => Left(new NotImplementedException)
      case DoStepCMD(fmu, instance)                   => Left(new NotImplementedException)

    }

  }

  def executeMaestroV2Command(command: MaestroV2Command): Either[Exception, State] = {
    Left(new NotImplementedException)
  }

  def executeControlCommand(command: ControlCommand): Either[Exception, State] = {
    Left(new NotImplementedException())
  }

  def execute(program: MaestroV2Seq,
              multiModelConfiguration: MultiModelConfiguration,
              instances: Map[FMUWithMD, Set[Instance]],
              connections: Set[Connection]): Either[Exception, State] = {

    val result: Either[Exception, State] =
      program.commands.foldLeft(Right(State(multiModelConfiguration)): Either[Exception, State])((state, cmd) =>
        state match {
          case Left(value) => Left(value)
          case Right(value) =>
            cmd match {
              case command: MaestroV2Command => executeMaestroV2Command(command)
              case command: FMICommand       => executeFMICommand(command, value)
              case command: ControlCommand   => executeControlCommand(command)
            }
        }
      )
    result
  }

}
