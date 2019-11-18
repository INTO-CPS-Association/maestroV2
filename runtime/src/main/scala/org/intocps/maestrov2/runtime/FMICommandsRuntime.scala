package org.intocps.maestrov2.runtime

import org.apache.commons.lang.NotImplementedException
import org.intocps.maestrov2.program.commands.{FMICommand, InstantiateCMD}

object FMICommandsRuntime {
  def instantiate(command: InstantiateCMD, state: State) = {
    command match {
      case InstantiateCMD(fmu, instance) => {
        for { fmuPath <- state.multiModelConfiguration.fmus.get(fmu) } fmuPath
        org.intocps.fmi.jnifmuapi.Factory.create(state.multiModelConfiguration.fmus.get(fmu))
        Left(new NotImplementedException)
      }
    }

  }

}
