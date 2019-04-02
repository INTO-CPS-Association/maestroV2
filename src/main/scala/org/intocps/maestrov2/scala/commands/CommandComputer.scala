package org.intocps.maestrov2.scala.commands

import org.intocps.maestrov2.scala.modeldescription.FMUWithMD
import org.intocps.orchestration.coe.modeldefinition.ModelDescription
import org.intocps.orchestration.coe.modeldefinition.ModelDescription.{Initial, Types, Variability}
import scala.collection.JavaConverters._

object CommandComputer {

  /*
  Calculates commands only using fmu and instance names
   */
def instanceCommands(fmusToInstances: Set[(String, Set[String])], fmiF: (String, Set[String]) => FMICommand) :MaestroV2Set = {
  MaestroV2Set(fmusToInstances.map(x => fmiF(x)))
}

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
      val valueRefs: List[Long] = filteredScalars.map{case (x: ModelDescription.ScalarVariable) => x.valueReference}

      SetIniCMD(fmu.key, setInstances, valueRefs)
    }

    MaestroV2Set(x);
  }

}
