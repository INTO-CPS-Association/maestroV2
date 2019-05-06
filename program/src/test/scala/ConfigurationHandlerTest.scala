import java.io.File

import org.intocps.maestrov2.program.{ConfigurationHandler, IntegerVal, MultiModelConfiguration}
import org.scalatest._

class ConfigurationHandlerTest extends FlatSpec {
"loadMMCFromFile" should "Load a json file into a case class" in {
  val f = new File("src/test/resources/single-watertank/single-watertank.json");

  val expected = Right(MultiModelConfiguration(
    Map(
      "{control}" -> "watertankcontroller-c.fmu",
      "{tank}" -> "singlewatertank-20sim.fmu"),

    Map(
      "{control}.c.valve" -> List("{tank}.t.valvecontrol"),
      "{tank}.t.level" -> List("{control}.c.level")),

    Map("{control}.c.maxlevel" -> IntegerVal(2),
      "{control}.c.minlevel" -> IntegerVal(1))))
  val actual = ConfigurationHandler.loadMMCFromFile(f);
  assert(expected == actual)
}

}
