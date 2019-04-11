package org.intocps.maestrov2.program.plugins

import org.intocps.maestrov2.data.{Connection, ConnectionScalarVariable, EnrichedConnectionScalarVariable, FMUWithMD}
import org.intocps.maestrov2.program.commands._
import org.intocps.orchestration.coe.modeldefinition.ModelDescription
import org.intocps.orchestration.coe.modeldefinition.ModelDescription.{Causality, Initial}

import scala.collection.JavaConverters._

object InitialisationCommandComputer {
  /*
    This function returns the commands performed in the initialization state
    TODO  currently we rely on the assumption that there are no algebraic loops
     */
  def calcInitializationScalarCommand(connections: Set[Connection], groupByFMU: Set[(FMUWithMD, Set[String])]): MaestroV2Command = {
    /* first se set all the independent ScalarVariables:
      SV with Causality Output and Initial Exact and
      SV with Causality Parameter and Initial not Calculated
     */
    val x: Set[Command] = groupByFMU.flatMap(fmu => {
      val allScalars: List[ModelDescription.ScalarVariable] = fmu._1.modelDescription.getScalarVariables.asScala.toList
      val independentScalars: List[ModelDescription.ScalarVariable] = allScalars.filter(s =>
        (s.causality == Causality.Output && s.initial == Initial.Exact) ||
          (s.causality == Causality.Parameter && s.initial != Initial.Calculated))
      val valueRefs: List[Long] = independentScalars.map(x => x.valueReference)

      fmu._2.map(instance => SetCMD(fmu._1.key, instance, valueRefs.toSet))
    })


    // we compute the sequence of all relatives of each dependent variable
    // we create a set with the sequence of all relatives of all the dependent variables
    val setOfSeqOfOrderedVariables: Set[Seq[ConnectionScalarVariable]] = {
      val dependentScalars: Set[ConnectionScalarVariable] = calcDependentVariables(connections)
      val orderedVariables: Set[Seq[ConnectionScalarVariable]] = dependentScalars.map(dependentVariable => calcDependencies(connections, dependentVariable))
      orderedVariables
    }

    // we need to convert the sequences of variables into sequences of enrichedvariables
    val setOfSeqOfEnrichedOrderedVariables: Set[Seq[EnrichedConnectionScalarVariable]] = convertConnectionScalarVariable2EnrichedConnectionScalarVariable(setOfSeqOfOrderedVariables, groupByFMU)
    // we flatten the Set with distinct so that we keep the order but avoid replications
    val flattenedSeqOfEnrichedOrderedVariables: Seq[EnrichedConnectionScalarVariable] = setOfSeqOfEnrichedOrderedVariables.toSeq.flatten.distinct
    // we change the variable with the FMIcommand( Set or Get based on the causality)
    val seqOfCommands: Seq[Command] = {
      calcCommandsForRuntime(flattenedSeqOfEnrichedOrderedVariables)
    }


    /* the return of the function is the Seq of (x,seqOfCommands)
    * where x is the set of commands on independent variables
    and seqOfCommand is the seq of commands for all the dependent variables
    */

    MaestroV2Seq(Seq(MaestroV2Set(x), MaestroV2Seq(seqOfCommands)))


  }

  /*
     Returns all the destinations
    */
  def calcDependentVariables(connections: Set[Connection]): Set[ConnectionScalarVariable] = {
    connections.flatMap(c => c.to)
  }

  /*
      This functions returns the Seq of variables on which variableUnderAnalysis is dependent on
      The return is ordered starting from the furthest variables, there are no repetitions, and includes the variableUnderAnalysis

  */
  def calcDependencies(connections: Set[Connection], variableUnderAnalysis: ConnectionScalarVariable): Seq[ConnectionScalarVariable] = {
    //filteredVariables contains all the connection with variableunderanalysis in the destination
    val filteredVariables: Set[Connection] = connections.filter(connection => connection.to.contains(variableUnderAnalysis))
    //parentVariables contains all the source variables of the variableunderanalysis( direct fathers)
    val parentVariables: Seq[ConnectionScalarVariable] = filteredVariables.map(connection => connection.from).toSeq
    //if parentVariables is empty, CASE BASE of recursion and return a Seq composed only of the current variable under analysis
    if (parentVariables.isEmpty) {
      val x: Seq[ConnectionScalarVariable] = Seq(variableUnderAnalysis)
      x
    }
    // else we recursively call calcDependencies on all the fathers
    else {
      val x: Seq[ConnectionScalarVariable] =
        parentVariables.flatMap(parent =>
          // in order to avoid replication we add the variableUnderAnalysis only when we analyze the last parent
          if (parent == parentVariables.last) {
            calcDependencies(connections, parent) :+ variableUnderAnalysis
          }
          else calcDependencies(connections, parent))
      x
    }
  }

  /*
  this function converts a Set of Seq of ConnectionScalarVariable into a Set of Seq of EnrichedScalarVariables,
  which also has info about the valueRef and Causality
  the ConnectionScalarVariable are already ordered so we don't change the order
   */
  def convertConnectionScalarVariable2EnrichedConnectionScalarVariable(ordered: Set[Seq[ConnectionScalarVariable]], groupByFMU: Set[(FMUWithMD, Set[String])]): Set[Seq[EnrichedConnectionScalarVariable]] = {
    val allFMU: Set[FMUWithMD] = groupByFMU.map(fmu => fmu._1)
    ordered.map(
      seqCSV => seqCSV.map(
        CSV => {
          // for each variable we find its own FMU
          val currentFMU: Set[FMUWithMD] = allFMU.filter(fmu => fmu.key == CSV.vInstance.fmu)
          // currentFMU is a Set of a single element, so we can only use the head to find all the varibles of the FMU
          val currentVariables: Set[ModelDescription.ScalarVariable] = currentFMU.head.modelDescription.getScalarVariables.asScala.toSet
          //then we can filter the list to retrieve the variable under analysis
          val currentVariable: Set[ModelDescription.ScalarVariable] = currentVariables.filter(variable => variable.name == CSV.vName)
          // Again, currentVariable is a Set made of only 1 element so we can use its head, that contains the extra info needed to create the enrichedVariable
          EnrichedConnectionScalarVariable(CSV.vName, CSV.vInstance, currentVariable.head.causality, currentVariable.head.valueReference)
        }
      )
    )
  }

  /*
   This function returns the list of commands related to the Seq of parents of 1 single dependetVariable
   */
  def calcCommandsForRuntime(orderedVariables: Seq[EnrichedConnectionScalarVariable]): Seq[Command] = {
    val x: Seq[Command] = orderedVariables.map(variable => {
      // since SetCMD and GetCMD require a List of valueref but we have only 1 valueref we need to create a list of 1 element
      val listOfReference: List[Long] = List[Long](variable.valueRef)
      // if the variable is an Input or a Parameter its a SetCMD otherwise its a GetCMD
      if (variable.causality == Causality.Input || variable.causality == Causality.Parameter)
        SetCMD(variable.vInstance.fmu, variable.vInstance.name, listOfReference.toSet)
      else GetCMD(variable.vInstance.fmu, variable.vInstance.name, listOfReference.toSet)
    })
    x
  }
}