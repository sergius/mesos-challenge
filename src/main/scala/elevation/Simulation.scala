package elevation

/**
 * Helps recording and executing a discrete event simulation
 */
abstract class Simulation {

  private var now = 0

  private var timeline = List.empty[Event]

  type Action = () => Unit

  case class Event(action: Action, duration: Int, delay: Int = 0)


  def run(): Unit = {
    timeline +:= Event(() => println("*** Simulation started ***"), 1)
    runLoop()
  }

  def addToTimeline(duration: Int, delay: Int = 0)(action: => Unit): Unit = {
    timeline :+= Event(() => action, duration, delay)
  }

  private def runLoop(): Unit = timeline match {
    case event :: rest =>
      now += 1
      timeline = rest
      event.action()
      runLoop()
    case _ =>
  }
}
