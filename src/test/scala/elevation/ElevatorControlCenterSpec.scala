package elevation

import org.scalatest.{Matchers, WordSpecLike}

class ElevatorControlCenterSpec extends WordSpecLike with Matchers {

  "When Elevator Control Center receives a pickup request, it" should {

    "with 1 elevator: assign it to the only elevator available" in {
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

    "with multiple elevators: assign to the elevator with least floors to traverse to reach initFloor" in {
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

    "with multiple elevators and pickup being out of range of elevators " +
      "movements: assign to the one that approaches most" in {

      object el1 extends ElevatorControl(1) with Elevator with Simulation
      object el2 extends ElevatorControl(2) with Elevator with Simulation
      object el3 extends ElevatorControl(3) with Elevator with Simulation

      val ecc = new ElevatorControlCenter(List(el1, el2, el3))

      val tf = 4 //top floor for elevators' movements
      ecc.update(1, tf -3, List((tf - tf, tf - 1), (tf - 3, tf - 3), (tf - 2, tf - tf)))
      ecc.update(2, tf - tf, List((tf - 1, tf - 3), (tf - 1, tf - tf), (tf - 3, tf -1)))
      ecc.update(3, tf - 2, List((tf - 1, tf - tf), (tf - 2, tf), (tf, tf - tf))) // the one that reaches tf

      el3.movements should have size 2

      el3.movements(0).stops should contain inOrder(tf, tf - 1, tf - tf)
      el3.movements(1).stops should contain inOrder(tf - 2, tf)

      println(s"*** Movements before pickup: ${el3.allStops}")
      ecc.pickup(tf + 2, tf + 6)

      el3.movements should have size 2

      println(s"*** Movements after pickup: ${el3.allStops}")
      el3.movements(0).stops should contain inOrder(tf, tf - 1, tf - tf)
      el3.movements(1).stops should contain inOrder(tf - 2, tf, tf + 2, tf + 6)

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
