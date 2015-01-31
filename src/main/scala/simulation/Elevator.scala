package simulation

@deprecated
abstract class Elevator extends Simulation {

  def timePerFloorDelay: Int
  def timePerStopDelay: Int

  object Direction {
    def apply(init: Int, dest: Int) =
      if (init < dest) Up
      else Down
  }

  sealed trait Direction
  object Up extends Direction
  object Down extends Direction

  case class Movement(dir: Direction) {

    private var floor: Int = 0
    private var dest: Int = 0
    private var actions: List[Action] = List()

    def getFloor = floor
    def destFloor = dest
    def isStopped = actions.isEmpty

    def setFloor(f: Int) =
      if (f != floor) {
        floor = f
      }

    def addAction(a: Action, destFloor: Int): Unit = {
      dest = destFloor
      actions = a :: actions
      a()
    }
  }

  def floorIndicator(m: Movement, floor: Int): Unit = {
    def indicatorAction(): Unit = {
      accordingToMovement(timePerFloorDelay) {
        m.setFloor(floor)
        println(s"*** Passing floor ${m.getFloor}")
      }
    }
    m addAction(indicatorAction, m.destFloor)
  }
  
  def stop(m: Movement, floor: Int): Unit = {
      def stopAction(): Unit = {
        accordingToMovement(timePerStopDelay) {
          println(s"*** Stopped at floor ${m.getFloor}")
        }
      }
    m addAction(stopAction, m.destFloor)
  }
  
  def move(m: Movement, flInit: Int, flDest: Int): Unit = {
    def moveAction(): Unit = {
      val range =
      if (flInit < flDest) flInit until flDest + 1
      else flInit until flDest - 1 by -1

      range foreach(f => floorIndicator(m, f))
      stop(m, flDest)
    }
    m addAction(moveAction, flDest)
  }
}
