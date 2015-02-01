package elevation

import org.scalatest.{Matchers, WordSpecLike}

class ElevatorControlCenterTest extends WordSpecLike with Matchers {

  "A space to test ElevatorControlCenter" can {

    "be prepared following the example " in {

      object el1 extends ElevatorControl(1) with Elevator with Simulation
      object el2 extends ElevatorControl(2) with Elevator with Simulation
      object el3 extends ElevatorControl(3) with Elevator with Simulation

      val ecc = new ElevatorControlCenter(List(el1, el2, el3))

      println(s"*** Initial status: ")
      ecc.status().map(s => s"Elevator: ${s._1}, Floor: ${s._2}, Stops: ${s._3}").foreach(s => println(s"*** $s"))

      el1.pickup(3, 8)
      el1.pickup(11, 1)
      el1.pickup(7, 10)

      el2.pickup(2, 1)
      el2.pickup(11, 2)
      el2.pickup(9, 4)

      el3.pickup(5, 12)
      el3.pickup(1, 6)
      el3.pickup(9, 3)

      println(s"*** After pickups status: ")
      ecc.status().map(s => s"Elevator: ${s._1}, Floor: ${s._2}, Stops: ${s._3}").foreach(s => println(s"*** $s"))

      el1.prepareMovements()
      el2.prepareMovements()
      el3.prepareMovements()

      ecc.step()
      ecc.step()
      ecc.step()
    }
  }
}
