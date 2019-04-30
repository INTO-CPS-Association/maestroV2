package org.intocps.maestrov2.program.commands

object CommandPrettyPrinter {

  def PrintCommands(cmd: Command): String =
  {
    cmd match {
      case x: MaestroV2Command => PrintMaestroV2Commands(x)
      case y: FMICommand => PrintFMICmd(y)
    }
  }

  def PrintMaestroV2Commands(cmd: MaestroV2Command) : String =
  {
    cmd match {
      case MaestroV2Set(commands) => "Set[\n\t%s]".format(commands.map(x => PrintCommands(x)))
      case MaestroV2Seq(commands) => "Seq[\n\t%s]".format(commands.map(x => PrintCommands(x)))
    }
  }

  def PrintFMICmd(cmd: FMICommand) : String =
  {
    cmd match {
      case InstantiateCMD(fmu, instances: Set[String]) => "instantiate(%s-(%s))".format(fmu, instances.mkString(","))
      case SetupExperimentCMD(fmu, instances) => "SetupExperiment(%s-(%s))".format(fmu, instances.mkString(","))
      case SetIniCMD(fmu, instances, scalarVariables) =>  "SetIni(%s-(%s)-(%s)".format(fmu, instances.mkString(","), scalarVariables.toString())
      case EnterInitializationModeCMD(fmu, instances) => "EnterInitialisationMode(%s-(%s))".format(fmu, instances.mkString(","))
      case ExitInitializationModeCMD(fmu, instances) => "ExitInitialisationMode(%s-(%s))".format(fmu, instances.mkString(","))
      case SetCMD(fmu, instance, scalarVariables) => "Set(%s-(%s)-(%s)".format(fmu, instance, scalarVariables.toString())
      case GetCMD(fmu, instance, scalarVariables) => "Get(%s-(%s)-(%s)".format(fmu, instance, scalarVariables.toString())
      case DoStepCMD(fmu, instance) => "DoStep(%s-(%s))".format(fmu, instance)
    }
  }

  def PrintFMUInstance()

}
