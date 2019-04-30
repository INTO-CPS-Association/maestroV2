package org.intocps.maestrov2.program.commands

object CommandPrettyPrinter {

  def PrintCommands(cmd: Command, indentCount: Int): String = {
    cmd match {
      case x: MaestroV2Command => PrintMaestroV2Commands(x, indentCount)
        case y: FMICommand => PrintFMICmd(y)
    }
  }

  def PrintMaestroV2Commands(cmd: MaestroV2Command, indentCount: Int): String = {

    def calcIndents(acc: String, rem: Int) : String = {
      if (rem == 0)
        acc
      else
        calcIndents(acc ++ "\t", rem-1)
    }

    val indents = calcIndents("", indentCount+1)

    cmd match {
      case MaestroV2Set(commands) => "Set[%s]".format {
        val x = commands.map(x => "\n" ++ indents ++ PrintCommands(x, indentCount+1))
        x.mkString(",")
      }
      case MaestroV2Seq(commands) => "Seq[%s]".format {
        val x = commands.map(x => "\n" ++ indents ++ PrintCommands(x, indentCount+1))
        x.mkString(",")
      }
    }
  }

  def PrintFMICmd(cmd: FMICommand): String = {
    cmd match {
      case InstantiateCMD(fmu, instances: Set[String]) => "instantiateCMD(%s-(%s))".format(fmu, instances.mkString(","))
      case SetupExperimentCMD(fmu, instances) => "SetupExperimentCMD(%s-(%s))".format(fmu, instances.mkString(","))
      case SetIniCMD(fmu, instances, scalarVariables) => "SetIniCMD(%s-(%s)-(%s))".format(fmu, instances.mkString(","), scalarVariables.mkString(","))
      case EnterInitializationModeCMD(fmu, instances) => "EnterInitialisationModeCMD(%s-(%s))".format(fmu, instances.mkString(","))
      case ExitInitializationModeCMD(fmu, instances) => "ExitInitialisationModeCMD(%s-(%s))".format(fmu, instances.mkString(","))
      case SetCMD(fmu, instance, scalarVariables) => "SetCMD(%s-(%s)-(%s))".format(fmu, instance, scalarVariables.mkString(","))
      case GetCMD(fmu, instance, scalarVariables) => "GetCMD(%s-(%s)-(%s))".format(fmu, instance, scalarVariables.mkString(","))
      case DoStepCMD(fmu, instance) => "DoStepCMD(%s-(%s))".format(fmu, instance)
    }
  }


}
