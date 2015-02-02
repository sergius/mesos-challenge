package elevation

import org.scalatest.{Matchers, WordSpecLike}

class ElevatorControlCenterSpec extends WordSpecLike with Matchers {

  "A pickup request" should {

    "be assigned to the only elevator available" in {
      object el1 extends ElevatorControl(1) with Elevator with Simulation

      val ecc = new ElevatorControlCenter(List(el1))

      val initFloor = 3

      ecc.update(1, initFloor, List((5, 0), (3, 0), (1, 7)))

      el1.movements should have size 2

      el1.movements(0).stops should contain inOrder(5, 3, 0)
      el1.movements(1).stops should contain inOrder(1, 7)

      val stepsToPassStop1 =
        (initFloor - el1.movements(0).stops.head).abs * el1.perFloorDuration  + el1.perStopDuration
      0 until stepsToPassStop1 foreach(n => ecc.step())

      ecc.pickup(2, 0)

      el1.movements(0).stops should contain inOrder(3, 2, 0)

    }

    "be assigned to the elevator with least floors to traverse in order to reach pickup initFloor" in {
      object el1 extends ElevatorControl(1) with Elevator with Simulation
      object el2 extends ElevatorControl(2) with Elevator with Simulation
      object el3 extends ElevatorControl(3) with Elevator with Simulation

      val ecc = new ElevatorControlCenter(List(el1, el2, el3))

      val initFloor = 8
      ecc.update(1, initFloor, List((7, 5), (8, 5), (8, 10)))
      ecc.update(2, 0, List((3, 8), (3, 0), (1, 7)))
      ecc.update(3, 0, List((10, 0), (3, 0), (1, 7)))

      el1.movements should have size 2

      el1.movements(0).stops should contain inOrder(8, 7, 5)
      el1.movements(1).stops should contain inOrder(8, 10)

      val stepsToPassStop1 =
        (initFloor - el1.movements(0).stops.head).abs * el1.perFloorDuration  + el1.perStopDuration
      0 until stepsToPassStop1 foreach(n => ecc.step())

      ecc.pickup(6, 10)

      el1.movements should have size 2

      el1.movements(0).stops should contain inOrder(7, 5)
      el1.movements(1).stops should contain inOrder(6, 8, 10)

    }

  }

  "A space to test ElevatorControlCenter" can {

    "be used following these examples " in {

      object el1 extends ElevatorControl(1) with Elevator with Simulation
      object el2 extends ElevatorControl(2) with Elevator with Simulation
      object el3 extends ElevatorControl(3) with Elevator with Simulation

      val ecc = new ElevatorControlCenter(List(el1, el2, el3))

      // printing status
      ecc.status().map(s => s"Elevator: ${s._1}, Floor: ${s._2}, Stops: ${s._3}").foreach(s => println(s"*** $s"))

      // calling on ElevatorControlCenter
      el1.pickup(3, 8)
      el1.pickup(11, 1)
      el1.pickup(7, 10)
      el1.updateAgenda() // is necessary to call updateAgenda(), to generate simulation!

      // calling on ElevatorControlCenter
      ecc.update(2, 0, List((2, 1), (11, 2), (9, 4))) // updateAgenda() is not needed

      // making steps
      ecc.step()
      ecc.step()
      ecc.step()
    }
  }
}
