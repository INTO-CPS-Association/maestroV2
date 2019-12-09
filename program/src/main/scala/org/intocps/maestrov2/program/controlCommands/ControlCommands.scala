package org.intocps.maestrov2.program.controlCommands

import org.intocps.maestrov2.program.commands.{Command, MaestroV2Command}
import org.intocps.maestrov2.program.controlCommands.Keyword.Keyword

sealed trait ControlCommand extends Command
case class ExecuteTill(cmd: MaestroV2Command, condition: Condition) extends ControlCommand

sealed trait Condition
case class KeywordCondition(keyword: Keyword) extends Condition


