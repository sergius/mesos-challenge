package elevation

import org.scalatest.{Matchers, WordSpecLike}

class SimulationSpec extends WordSpecLike with Matchers {

  "Simulation" should {

    "add 1 item to agenda for 1 Event with duration of 1 time unit" in {
      var executed = false
      val duration = 1

      object sim extends Simulation
      import sim._

      val action = Action("SUT Action 1", {executed = true; println("executing: 1 Event, 1 time unit")}, duration)

      addToAgenda(action)
      agenda should have size 1
      
      sim.run()

      executed shouldBe true
    }

    "should add 2 items to agenda for 1 Event with duration of 2 time units" in {
      var executed = false
      val duration = 2

      object sim extends Simulation
      import sim._

      val action = Action("SUT Action 2", {executed = true; println("executing: 1 Event, 2 time units")}, duration)

      addToAgenda(action)
      agenda should have size 2

      sim.run()

      executed shouldBe true
    }

    "should add 4 items to agenda for 1 Event with duration of 2 time units and delay of 2 time units " in {
      var executed = false
      val duration = 2
      val delay = 2

      object sim extends Simulation
      import sim._

      val action = Action("SUT Action 3",
      {executed = true; println("executing: 1 Event with delay 2, 4 time units")}, duration)

      addToAgenda(action, delay)
      agenda should have size 4

      sim.run()

      executed shouldBe true
    }

    "should add 5 items to agenda for 2 events: 1st with (delay: 3) & (duration: 2), " +
      "2nd with (delay: 0) & (duration: 2)" in {
      var executed = false
      val duration = 2
      val delay = duration + 1

      object sim extends Simulation
      import sim._

      val action1 = Action("SUT Action 4 - 1",
      {executed = true; println(s"executing: duration $duration, delay 0")}, duration)

      val action2 = Action("SUT Action 4 - 2",
      {executed = true; println(s"executing: duration $duration, delay $delay")}, duration)

      addToAgenda(action2, delay)
      addToAgenda(action1)

      agenda should have size 5

      val eventsInEmptyInstant: List[sim.Event] = agenda(duration + (delay - duration) - 1).events
      eventsInEmptyInstant should have size 1
      eventsInEmptyInstant should contain (Empty)

      sim.run()

      executed shouldBe true
    }

    "should add 7 items to agenda for 2 events: 1st with (delay: 0) & (duration: 2), " +
      "2nd with (delay: 3) & (duration: 2)" in {
      var executed = false
      val duration = 2
      val delay = duration + 1

      object sim extends Simulation
      import sim._

      val action1 = Action("SUT Action 5 - 1",
      {executed = true; println(s"executing: duration $duration, delay 0")}, duration)

      val action2 = Action("SUT Action 5 - 2",
      {executed = true; println(s"executing: duration $duration, delay $delay")}, duration)

      addToAgenda(action1)
      addToAgenda(action2, delay)

      agenda should have size 7

      duration until duration + delay foreach {t =>
        agenda(t).events should have size 1
        agenda(t).events should contain (Empty)
      }

      sim.run()

      executed shouldBe true
    }
  }

}
