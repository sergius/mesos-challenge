package simulation

abstract class Elevator extends Simulation {

  def timePerFloorDelay: Int
  def timePerStopDelay: Int

  class Direction {

    private var floor: Int = 0
    private var dest: Int = 0
    private var actions: List[Action] = List()

    def getFloor = floor
    def destFloor = dest

    def setFloor(f: Int) =
      if (f != floor) {
        floor = f
        actions foreach(_())
      }

    def addAction(a: Action, destFloor: Int): Unit = {
      dest = destFloor
      actions = a :: actions
      a()
    }
  }

  def floorIndicator(d: Direction, floor: Int): Unit = {
    def indicatorAction(): Unit = {
      afterDelay(timePerFloorDelay) {
        d.setFloor(floor)
        println(s"*** Passing floor ${d.getFloor}")
      }
    }
    d addAction(indicatorAction, d.destFloor)
  }
  
  def stop(floor: Int, d: Direction): Unit = {
      def stopAction(): Unit = {
        afterDelay(timePerStopDelay) {
          println(s"*** Stopped at floor ${d.getFloor}")
        }
      }
    d addAction(stopAction, d.destFloor)
  }
  
  def move(flInit: Int, flDest: Int, d: Direction): Unit = {
    def moveAction(): Unit = {
      val range =
      if (flInit < flDest) flInit until flDest
      else flInit until flDest by -1

      range foreach(f => floorIndicator(d, f))
      stop(flDest, d)
    }
    d addAction(moveAction, flDest)
  }
}
