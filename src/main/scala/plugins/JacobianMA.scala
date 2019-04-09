package plugins

import org.intocps.maestrov2.scala.modeldescription.{Connection, InstanceFMUWithMD}

object JacobianMA {
  /* A jacobian master algorithm roughly does the following
  Set inputs for all FMUs
  DoStep for all FMUs
  Get outputs for all FMUs.
  These are to be encoded as
  For all FMUs . seq(setCMD(sv1,...,svN), doStep(fmu1,...,fmuN), getCMD(sv1,...,svN((


   */
  def computeJacobianIteration(instances: Set[InstanceFMUWithMD], extConnections: Set[Connection]) = {

  }

}
