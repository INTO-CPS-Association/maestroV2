package org.intocps.maestrov2.data

import scala.collection.JavaConverters._

// A ConnectionScalarVariable is a ScalarVariable in context of a connection
case class ConnectionScalarVariable(vName: String, vInstance: Instance) {
  def getValueReference(instances: Set[InstanceFMUWithMD]): Option[Long] = {
    instances.find(i => i.fmu.key == vInstance.fmu).flatMap(i => i.fmu.modelDescription.getScalarVariables.asScala.toList.find(sv => sv.name == vName).map(sv => sv.getValueReference))
  }
}

// A ConnectionScalarVariable is a ScalarVariable in context of a connection
case class ConnectionScalarVariableIndependent(vName: String)


