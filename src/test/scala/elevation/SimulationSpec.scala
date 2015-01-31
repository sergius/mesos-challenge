package elevation

import org.scalatest.{Matchers, WordSpecLike, FunSuite}

class SimulationSpec extends WordSpecLike with Matchers {

  "Simulation" should {

    "run one Event correctly" in {

      var executed = false

      val action = {executed = true}
      val duration = 1

      object sim extends Simulation
      import sim._

      addToTimeline(duration)(action)
      sim.run()

      executed shouldBe true
    }
  }

}
