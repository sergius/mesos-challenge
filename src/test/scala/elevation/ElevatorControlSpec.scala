package elevation

import org.scalatest.{Matchers, WordSpecLike}

class ElevatorControlSpec extends WordSpecLike with Matchers {

  "When the elevator control receives a pickup request, it" should {

    "for a single request when initFloor == elevator.currentFloor: " +
      "produce a sequence of 1 movement with 2 stops (initFloor counts 1)" in {
      val initFloor = 0
      val destFloor = 5
      object ec extends ElevatorControl(1) with Elevator with Simulation

      ec.currentFloor shouldEqual initFloor

      ec.pickup(initFloor, destFloor)

      ec.movements should have size 1

      val stops = ec.movements(0).stops
      stops should have size 2

      stops(0) shouldEqual initFloor
      stops(1) shouldEqual destFloor
    }

    "for a single request when initFloor != currentFloor: " +
      "produce a sequence of 1 movement with 2 stops (initFloor doesn't count here)" +
      "when the direction from stops `currentFloor` to 1 is the SAME as from 1 to 2" in {
      val initFloor = 3
      val destFloor = 6
      object ec extends ElevatorControl(1) with Elevator with Simulation
      ec.currentFloor = initFloor - 2

      ec.currentFloor should not equal initFloor

      ec.pickup(initFloor, destFloor)

      ec.movements should have size 1

      val stops = ec.movements(0).stops
      stops should have size 2

      stops(0) shouldEqual initFloor
      stops(1) shouldEqual destFloor
    }

    "for a single request when initFloor != currentFloor: " +
      "produce a sequence of 1 movement with 2 stops (initFloor doesn't count here)" +
      "when the direction from stops `currentFloor` to 1 is DIFFERENT as from 1 to 2" in {
      val initFloor = 3
      val destFloor = 6
      object ec extends ElevatorControl(1) with Elevator with Simulation
      ec.currentFloor = initFloor + 2

      ec.currentFloor should not equal initFloor

      ec.pickup(initFloor, destFloor)

      ec.movements should have size 1

      val stops = ec.movements.head.stops
      stops should have size 2
      stops.head shouldEqual initFloor
      stops.last shouldEqual destFloor
    }

    "for 3 requests in the same direction with 1 repeated stop: " +
      "produce a sequence of 1 movement with no repeated stops " +
      "(the number of stops in total = (inits + dests - repeated) " in {
      val pickups = List((3, 6), (4, 10), (6, 8))
      val initFloor = pickups(0)._1

      object ec extends ElevatorControl(1) with Elevator with Simulation
      ec.currentFloor = pickups(0)._1

      ec.currentFloor shouldEqual initFloor

      ec.pickup(pickups(0)._1, pickups(0)._2)
      ec.pickup(pickups(1)._1, pickups(1)._2)
      ec.pickup(pickups(2)._1, pickups(2)._2)

      ec.movements should have size 1

      val stops = ec.movements(0).stops
      stops should have size (pickups.size * 2 - 1) //inits + dests - repeated

      stops should contain inOrder(3, 4, 6, 8, 10)
    }

    "for 2 requests in one direction and 1 in opposite with 1 stop common for both: " +
      "produce a sequence of 2 movements, keeping the common stop separately for each direction" in {
      val pickups = List((3, 6), (6, 1), (4, 10))
      val initFloor = pickups(0)._1

      object ec extends ElevatorControl(1) with Elevator with Simulation
      ec.currentFloor = pickups(0)._1

      ec.currentFloor shouldEqual initFloor

      ec.pickup(pickups(0)._1, pickups(0)._2)
      ec.pickup(pickups(1)._1, pickups(1)._2)
      ec.pickup(pickups(2)._1, pickups(2)._2)

      ec.movements should have size 2

      val stops1 = ec.movements(0).stops
      stops1 should have size 4
      stops1 should contain inOrder(3, 4, 6, 10)

      val stops2 = ec.movements(1).stops
      stops2 should have size 2
      stops2 should contain inOrder(6, 1)
    }
  }


  "When a series of several pickup call are made, ElevatorControl" should {

    "prepare correct agenda for pickups in same direction" in {
      val pickups = List((3, 6), (4, 10), (6, 8))

      object ec extends ElevatorControl(1) with Elevator with Simulation
      import ec._

      val initFloor = ec.currentFloor

      ec.pickup(pickups(0)._1, pickups(0)._2)
      ec.pickup(pickups(1)._1, pickups(1)._2)
      ec.pickup(pickups(2)._1, pickups(2)._2)

      ec.prepareMovements()

      movements should have size 1
      val stops = movements.head.stops
      val expectedSize = stops.size * perStopDuration + (stops.last - initFloor).abs * perFloorDuration
      ec.agenda should have size expectedSize

      ec.run()
    }

    "prepare correct agenda for pickups in both directions" in {
      val pickups = List((3, 6), (6, 1), (4, 10))

      object ec extends ElevatorControl(1) with Elevator with Simulation
      import ec._

      ec.currentFloor = 5
      val initFloor = ec.currentFloor

      ec.pickup(pickups(0)._1, pickups(0)._2)
      ec.pickup(pickups(1)._1, pickups(1)._2)
      ec.pickup(pickups(2)._1, pickups(2)._2)

      ec.prepareMovements()

      movements should have size 2

      val traversedFloors = (initFloor - movements(0).stops.head).abs +
        (movements(0).stops.last - movements(1).stops.head).abs +
        movements.map(m => (m.stops.last - m.stops.head).abs).sum

      val totalStops = movements.map(m => m.stops).flatten.size

      ec.agenda should have size (totalStops * perStopDuration + traversedFloors * perFloorDuration)

      ec.run()
    }
  }
}
