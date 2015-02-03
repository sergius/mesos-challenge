package elevation

import scala.io.StdIn

object el1 extends ElevatorControl(1) with Elevator with Simulation
object el2 extends ElevatorControl(2) with Elevator with Simulation
object el3 extends ElevatorControl(3) with Elevator with Simulation

object ElevatorControlCenterApp extends ElevatorControlCenter(List(el1, el2, el3)) with App with Terminal {

  override val commandParser: CommandParser.Parser[Command] =
    CommandParser.exit | CommandParser.status | CommandParser.pickup |
    CommandParser.update | CommandParser.step

  println(s"*** Starting Elevator Control Center for El-${el1.id}, El-${el2.id}, El-${el3.id}")
  printHelp()
  commandLoop()

  def printHelp(): Unit = {
    println("*** Please use the following formats for commands:\n" +
      "'exit' - to quit the program\n" +
      "'status' - to call status()\n" +
      "'pickup 2 4' - to call pickup(2, 4)\n" +
      "'update 2 0 3,6 1,4 10,3' - to call update(2, 0, List((3, 6), (1, 4), (10, 3)))\n" +
      "'step' - to call step()")
  }

  def printStatus(): Unit = {
    status().foreach(s => println(s"*** El-${s._1}; current floor: ${s._2}; planned stops: ${s._3}"))
  }


  private def commandLoop(): Unit = {
    import Command._

    Command(StdIn.readLine()) match {
      case Quit =>
        println("*** Exiting Elevator Control Center ...")
        sys.exit()
      case Status =>
        println("*** === Status ===")
        printStatus()
        commandLoop()
      case Step =>
        println("*** === Stepping ===")
        step()
        commandLoop()
      case Pickup(initFloor, destFloor) =>
        println(s"*** === Pickup ($initFloor, $destFloor) ===")
        pickup(initFloor, destFloor)
        printStatus()
        commandLoop()
      case Update(id, currentFloor, pickups) =>
        println(s"*** === Update ($id,$currentFloor,$pickups ===")
        update(id, currentFloor, pickups)
        printStatus()
        commandLoop()
      case Unknown(s, message) =>
        println(s"*** Ooops... Something went wrong: $message")
        printHelp()
        commandLoop()
    }
  }

}
