import org.scalatest.FlatSpec

class DummyTests extends FlatSpec{

  case class B(name: String)
  case class A(name: String, b: B)

  "test" should "test" in {
    val a = Set(A("a",B("b")),A("a2",B("b")))
    val b = a.groupBy(x => x.b.name)
    assert(b.empty == false);

  }

}
