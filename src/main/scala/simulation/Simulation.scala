package simulation

abstract class Simulation {

  type Action = () => Unit

  case class Event(time: Int, action: Action)

  private var now: Int = 0

  private var agenda: List[Event] = List()

  def currentTime: Int = now

  def afterDelay(delay: Int)(block: => Unit): Unit = {
    val item = Event(now + delay, () => block)
    agenda = insert(agenda, item)
  }

  def run(): Unit = {
    afterDelay(0) {
      println(s"****** Simulation started; time = $now ******")
    }
    loop()
  }

  private def insert(agenda: List[Event], item: Event): List[Event] =
    agenda match {
    case first :: rest if first.time <= item.time =>
      first :: insert(rest, item)
    case _ =>
      item :: agenda
  }

  private def loop(): Unit = agenda match {
    case first :: rest =>
      agenda = rest
      now = first.time
      first.action()
      loop()
    case Nil =>
  }
}
