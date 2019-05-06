package org.intocps.maestrov2.program

import org.intocps.maestrov2.data.FMUWithMD
import org.intocps.maestrov2.program.commands.{Command, MaestroV2Command, MaestroV2Set, SetIniCMD}
import org.intocps.orchestration.coe.modeldefinition.ModelDescription
import org.intocps.orchestration.coe.modeldefinition.ModelDescription.{Initial, Types, Variability}

import scala.collection.JavaConverters._

object InstantiatedCommandsComputer {
  /*
     INI is one of: Real, Integer, Boolean, String for a variable with
     variability != constant
     initial == "exact" || "approx"
      */
  def calcSetINI(groupByFMU: Set[(FMUWithMD, Set[String])]): MaestroV2Command = {
    val x: Set[Command] = groupByFMU.map { case (fmu, setInstances) =>
      val allScalars: List[ModelDescription.ScalarVariable] = fmu.modelDescription.getScalarVariables.asScala.toList;
      val filteredScalars: List[ModelDescription.ScalarVariable] = allScalars.filter(s =>
        s.variability != Variability.Constant
          && (s.initial == Initial.Exact || s.initial == Initial.Approx)
          && (s.`type`.`type` != Types.Enumeration)
      )
      val valueRefs: List[Long] = filteredScalars.map { case (x: ModelDescription.ScalarVariable) => x.valueReference }

      SetIniCMD(fmu.key, setInstances, valueRefs)
    }

    MaestroV2Set(x);
  }
}