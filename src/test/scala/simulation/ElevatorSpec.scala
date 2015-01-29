package simulation

import org.scalatest.{Matchers, WordSpecLike}

class ElevatorSpec extends WordSpecLike with Matchers {

  "When an elevator is called by pickup, it" must {

    "go through every floor and stop at the destination floor" in {
      // just watching simulation traces

      object sim extends ElevatorControl with Parameters
      import sim._

      val initFloor = 2
      val destFloor =5

      // direction will be used later for better scheduling
      val m = Movement(Direction(initFloor, destFloor))

      move(m, 0, 3)
      pickup(m, initFloor, destFloor) //should go back to 2 and start from there
      pickup(m, maxFloors, 0)

      sim.run()
    }
  }

}
