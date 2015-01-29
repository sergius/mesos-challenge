package simulation

abstract class Simulation {

  type Action = () => Unit

  case class Event(initFloor: Int, action: Action)

  private var currFloor: Int = 0

  private var agenda: List[Event] = List()

  def currentTime: Int = currFloor

  //TODO This method orders the actions according to scheduling logic
  //For now we leave it like it is and add the actions FIFO
  def accordingToMovement(initFloor: Int)(block: => Unit): Unit = {
    val item = Event(initFloor, () => block)//Event(currFloor + delay, () => block)
    agenda = insert(agenda, item)
  }

  def run(): Unit = {
    accordingToMovement(0) {
      println(s"****** Simulation started; initFloor = $currFloor ******")
    }
    loop()
  }

  private def insert(agenda: List[Event], item: Event): List[Event] = {
    if (item.initFloor == 0) item :: agenda
    else agenda :+ item
  }
/*    agenda match {
    case first :: rest if first.initFloor <= item.initFloor =>
      first :: insert(rest, item)
    case _ =>
      item :: agenda
  }*/

  private def loop(): Unit = agenda match {
    case first :: rest =>
      agenda = rest
      currFloor = first.initFloor
      first.action()
      loop()
    case Nil =>
  }
}
