import java.io.File

import org.intocps.maestrov2.plugins.iodependencycalculator.GraphBuilder
import org.intocps.maestrov2.scala.Connections
import org.intocps.maestrov2.scala.modeldescription._
import org.intocps.orchestration.coe.modeldefinition.ModelDescription
import org.scalatest.FlatSpec
import plugins.{IODependencyAcyclic, IODependencyCalculator, IODependencyCyclic}

class IODependencyCalculatorTest extends FlatSpec {

  val interCon1Input = ConnectionScalarVariable("C1InV", Instance("C1I", "C1F"))
  val interCon1Output = ConnectionScalarVariable("C1OutV", Instance("C1I", "C1F"))
  val internalCon1 = Connection(interCon1Input, Set(interCon1Output), ConnectionType.Internal)

  val interCon2Input = ConnectionScalarVariable("C2InV", Instance("C2I", "C2F"))
  val interCon2Output = ConnectionScalarVariable("C2OutV", Instance("C2I", "C2F"))
  val interCon2 = Connection(interCon2Input, Set(interCon2Output), ConnectionType.Internal)

  val ext1 = Connection(interCon1Output, Set(interCon2Input), ConnectionType.External)
  val ext2 = Connection(interCon2Output, Set(interCon1Input), ConnectionType.External)

  val allConnections = Set(internalCon1, interCon2, ext1, ext2)

  "CalculateIODependencies" should "report an algebraic loop" in {
    val tests: Iterator[Set[Connection]] = allConnections.toList.combinations(4).map(x => x.toSet)

    tests.foreach((f: Set[Connection]) => {
      val actual = IODependencyCalculator.CalculateIODependencies(f)
      actual match {
        case IODependencyCyclic(_) => assert(true)
        case IODependencyAcyclic(_) => assert(false)
      }
    })
  }

  "CalculateIODependenciesAcyclic" should "report NO algebraic loops" in {

    val tests: Iterator[Set[Connection]] = allConnections.toList.combinations(3).map(x => x.toSet)

    tests.foreach((f: Set[Connection]) => {
      val actual = IODependencyCalculator.CalculateIODependencies(f)
      actual match {
        case IODependencyCyclic(_) => assert(false)
        case IODependencyAcyclic(x) => assert(true)
      }
    })
  }

  "CalculateIODependencies" should "Calculate an order" in {
    val test = Set(internalCon1, interCon2, ext2);
    val actual = IODependencyCalculator.CalculateIODependencies(test)
    val expected = new IODependencyAcyclic(List(interCon2Input, interCon2Output, interCon1Input, interCon1Output))
    assert(actual == expected)
  }


}
