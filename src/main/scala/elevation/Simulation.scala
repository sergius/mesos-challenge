package elevation

import scala.util.{Success, Try}

/**
 * Helps recording and executing a discrete event simulation
 */
trait Simulation {

  private var timeline = List.empty[Instant]
  private var timelineCursor = 0 //indicates the current instant: to be created or modified
  private var now = 0

  def currentTime = now
  def agenda = timeline
  def simReset(): Unit = {
    timeline = List.empty[Instant]
    timelineCursor = 0
    now = 0
  }

  object Action {
    def apply(name: String, block: => Unit, duration: Int) = new Action(name, block, duration)
  }

  class Action(val name: String, block: => Unit, val duration: Int) {
    def effect = () => block
  }

  object Event {
    def apply(name: String, effect: () => Unit) = new Event(name, effect)
  }
  class Event(val name: String, val effect: () => Unit)
  case object Empty extends Event("Empty", () => println("tic-tac"))

  object Instant {
    def apply(e: Event) = new Instant(e)
    def empty = {
      new Instant(Empty)
    }
  }

  class Instant(e: Event) {
    private var eventList:List[Event] = List(e)
    
    def addEvent(ev: Event) = eventList.head match {
      case Empty =>
        eventList = List(ev)
      case _ =>
        eventList :+= ev
    }

    def events = eventList
  }

  def run(): Unit = {
    if (timeline.isEmpty) println("*** Can't run, agenda empty")
    else {
      prepareForRun()
      runLoop()
    }
  }

  protected def stepAgenda(): List[String] = {
    if (timeline.isEmpty) {
      println("*** Can't run, agenda empty")
      List.empty[String]
    } else {
      stepTimeline().map(e => e.name)
    }
  }

  private def prepareForRun(): Unit = {
    val startAction = Action("Simulation", println("starting..."), 1)
    val finishAction = Action("Simulation", println("finished!"), 1)
    timeline +:= Instant(Event(startAction.name, startAction.effect))
    timeline :+= Instant(Event(startAction.name, finishAction.effect))
  }

  /**
   * Inserts `Action`s to `Instant`s of `Simulation` agenda. The `Actions`s are
   * added to agenda in order of calling (this function), creating a new `Instant`
   * in each call.
   * If an `Action` has duration `x > 0`, `x` `Instant`s will be created for each
   * unit of duration, thus repeating the same `Action` during `x` `Instant`s.
   * If an `Action` has a delay `y > 0`, the `Action` will be placed to the `Instant`
   * (now + `y` units of time). If no `Instant`s have been created yet that far,
   * the agenda will be filled with empty ones up to where the `Action` should start.
   * @param delay Amount of time units that have to pass from now till the `Instant`
   *              when the Action should be executed. Default delay is 0.
   * @param action The `Action` to be added to agenda
   * @return
   */
  def addToAgenda(action: Action, delay: Int = 0): Unit = {
    
    if (delay > 0) addWithDelay(delay)
    else addForDuration()

    def addWithDelay(d: Int) = {
      val currCursor = timelineCursor
      val advCursor = currCursor + d

      Try(timeline(advCursor)) match {
        case Success(instant) =>
          timelineCursor = advCursor
          addForDuration()
        case _ =>
          fillWithEmptyFor(delay)
          addForDuration()
      }
      timelineCursor = currCursor

      def fillWithEmptyFor(i: Int) = {
        0 until i foreach {
          t => { timeline :+= Instant.empty
            timelineCursor += 1 }
        }
      }
    }

    def addForDuration(): Unit = {
      val newEvent: Event = Event(action.name, action.effect)
      val duration = action.duration
      0 until duration foreach { t => {
          Try(timeline(timelineCursor)) match {
            case Success(instant) =>
              instant.addEvent(newEvent)
            case _ =>
              timeline :+= Instant(newEvent)
          }
          timelineCursor += 1
        }
      }
    }
  }

  private def runLoop(): Unit = timeline match {
    case instant :: rest =>
      timeline = rest
      instant.events foreach {
        e => printEvent(e)
          e.effect()
      }
      now += 1
      runLoop()
    case _ =>
  }

  private def stepTimeline(): List[Event] = timeline match {
    case instant :: rest =>
      timeline = rest
      instant.events foreach {
        e => printEvent(e)
          e.effect()
      }
      now += 1
      instant.events
    case _ => List.empty[Event]
  }

  private def printEvent(e: Event) =
    print(s"*** Time: $now | ${e.name} | ")
}
