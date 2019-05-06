import org.intocps.maestrov2.data._
import org.scalatest.FlatSpec

class ConnectionsTests extends FlatSpec{
  "CalculateInternalConnections" should "calculate internal connections of a model description file" in {

    val tankFrom = ConnectionScalarVariableIndependent("valvecontrol")
    val tankTo : Set[ConnectionScalarVariableIndependent]=  Set(ConnectionScalarVariableIndependent("level"))
    val expectedTankConns : Set[ConnectionIndependent] = Set(ConnectionIndependent(tankFrom, tankTo, ConnectionType.Internal))

    assert(TestData.tankFmu.connections == expectedTankConns)
  }
}
