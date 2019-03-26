package org.intocps.maestrov2.scala.configuration

import org.intocps.maestrov2.scala.modeldescription.{Connection, ConnectionScalarVariable, ConnectionType, Instance}

object Conversions {



  def configVarToConnectionSV(configVar: String) : ConnectionScalarVariable =
  {
    // Skip initial {
    val mmcConnectionP = configVar.substring(1);
    val keyEnd = mmcConnectionP.indexOf('}');

    // Split at }
    val keySplit = mmcConnectionP.splitAt(keyEnd);
    val fmuKey = keySplit._1;

    // Skip }.
    val exceptFmuKey = keySplit._2.substring(2)
    val instanceSplit = exceptFmuKey.splitAt(exceptFmuKey.indexOf('.'));
    val instance = instanceSplit._1;

    // Skip .
    val vName = instanceSplit._2.substring(1);

    return ConnectionScalarVariable(vName, Instance(instance, fmuKey));

  }

  def configConnectionToConnection(cC: (String, List[String]))  : Connection = {
    val output = configVarToConnectionSV(cC._1);
    val inputs = cC._2.map(x => configVarToConnectionSV(x)).toSet;
    Connection(output, inputs, ConnectionType.External);

  }

  def MMCConnectionsToMaestroConnections(mmcConnections: Map[String, List[String]]) : Set[Connection] =
    {
      mmcConnections.map(x => configConnectionToConnection(x)).toSet

    }

}
