import java.io.File

import org.intocps.maestrov2.scala.configuration.ConfigurationHandler
import org.intocps.maestrov2.scala.configuration.datatypes.{IntegerVal, MultiModelConfiguration}
import org.scalatest._

class ConfigurationHandlerTest extends FlatSpec {
"loadMMCFromFile" should "Load a json file into a case class" in {
  val f = new File("src/test/resources/single-watertank/single-watertank.json");

  val expected = MultiModelConfiguration(
    Map(
      "{control}" -> "watertankcontroller-c.fmu",
      "{tank}" -> "singlewatertank-20sim.fmu"),

    Map(
      "{control}.c.valve" -> List("{tank}.t.valvecontrol"),
      "{tank}.t.level" -> List("{control}.c.level")),

    Map("{control}.c.maxlevel" -> IntegerVal(2),
      "{control}.c.minlevel" -> IntegerVal(1)))
  val actualEither = ConfigurationHandler.loadMMCFromFile(f);
  actualEither match {
    case Left(x) => println(x)
    case Right(actual) => assert(expected==actual);
  }
}

}
