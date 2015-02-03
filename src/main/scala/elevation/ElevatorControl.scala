package elevation

/**
 * A unit of control of one elevator. Its main purpose is to compute
 * elevators series of movements according to incoming requests and
 * the direction taken
 */
abstract class ElevatorControl(val id: Int) {
  this: Elevator with Simulation =>

  private var allMoves = List.empty[Movement]
  private val StopActionName = "Stopping"
  
  def movements: List[Movement] = allMoves
  def allStops: List[Int] = allMoves.foldLeft(List.empty[List[Int]])((acc, m) => acc :+ m.stops).flatten

  def getCurrentFloor: Int = currentFloor
  def setCurrentFloor(floor: Int): Unit = currentFloor = floor
  def perFloorDuration: Int
  def perStopDuration: Int

  /**
   * Steps through the timeline of simulation agenda.
   * At each step checks if it is a Stop action and
   * if so, removes "next stop": first stop in the first movement.
   * Thus, if at a certain point of stepping through the
   * simulation a new `pickup()` is received, it will be
   * processed according to the current status and the
   * simulation will be updated.
   */
  def stepSimulation(): Unit = {
    
    removeStop(stepAgenda())

    def removeStop(doneActions: List[String]): Unit = {
      doneActions foreach { s =>
        if (s == StopActionName) 
          allMoves.head.dropStop() match {
            case Nil if allMoves.size > 1 =>
              allMoves = allMoves.tail
              allMoves.head.dropStop()
            case Nil => allMoves = List.empty[Movement]
            case _ =>
          }
      }
    }
  }

  def runSimulation(): Unit = run()

  def reset(): Unit = {
    currentFloor = 0
    simReset()
  }
  /**
   * Represents a one direction movement that the elevator takes and
   * doesn't stop till finishing the requests in the same direction or
   * till reaching the limit of possible movement.
   * A new movement is created when a request is not finding an existing
   * movement of same direction, to be served.
   * @param initFloor The initial floor that together with the destination
   *                  floor `destFloor` records the direction which this
   *                  movement will be serving, characterised by the relation
   *                  `initFloor` < `destFloor` or `initFloor` < `destFloor`.
   * @param destFloor Destination floor
   */
  class Movement(val initFloor: Int, val destFloor: Int) {

    private var stopsList = List(initFloor, destFloor)

    def stops = stopsList

    def dropStop(): List[Int] = {
      stopsList = stopsList.tail
      stopsList
    }

    /**
     * Adds to the movement new stop points that represent the origin and
     * destination of a requested move. It is assumed that the direction should
     * be verified before calling this function, nevertheless an additional
     * check is done. The error handling could (should?) be improved.
     * @param init Initial floor, origin of move
     * @param dest Destination floor, destination of move
     * @return
     */
    def add(init: Int, dest: Int) = {
      (init, dest) match {
        case (i, d) if wrongDirection =>
          new Error("Trying to add stops to a movement of wrong direction")
        case (i, d) if i < d => addByDirection(_ < _)
        case (i, d) if i > d => addByDirection(_ > _)
        case _ =>
      }

      def wrongDirection =
        (init < dest) && (initFloor >= destFloor) || (init > dest) && (initFloor <= destFloor)

      def addByDirection(f: (Int, Int) => Boolean) = {

        def insert(el: Int, list: List[Int]): List[Int] = list match {
          case h :: tail =>
            if (f(h, el)) h :: insert(el, tail)
            else if (h == el) list // omit repeated stops
            else el :: list
          case List() => List(el)
        }
        stopsList = insert(dest, insert(init, stopsList))
      }
    }
    
  }

  /**
   * Updates the list of planned movements, according to elevator's position
   * and movement direction.
   * **Note:** After creating the simulation with `pickup()`, `updateAgenda()`
   * *must* be called before executing the simulation either with `step()` or `run()`
   * @param initFloor Initial floor
   * @param destFloor Destination floor
   */
  def pickup(initFloor: Int, destFloor: Int): Unit = {

      addMove(initFloor, destFloor)
  }

  private def addMove(init: Int, dest: Int) = {

    (init, dest) match {
      case (i, d) if i < d =>
        addToOrCreate(allMoves.find(m => m.initFloor < m.destFloor))
      case (i, d) if i > d =>
        addToOrCreate(allMoves.find(m => m.initFloor > m.destFloor))
      case _ => // ignore moves when init == dest
    }

    def addToOrCreate(o: Option[Movement]): Unit = o match {
      case Some(m) => m.add(init, dest)
      case _ => allMoves :+= new Movement(init, dest)
    }
  }

  private def stopAt(floor: Int) = {
    val action = Action(StopActionName, {
      currentFloor = floor
      println(s"El#$id: Stopped at floor $floor")
    }, perStopDuration)
    addToAgenda(action)
  }

  private def floorIndication(floor: Int) = {
    val action = Action("Moving  ", {
      currentFloor = floor
      println(s"El#$id: Moving  by floor $floor")
    }, perFloorDuration)
    addToAgenda(action)
  }

  /**
   * Updates simulation agenda with the recorded
   * `Movement`s. Note that the previous or current
   * simulation (agenda) will be totally cleared.
   */
  def updateAgenda(): Unit = {
    val remember = currentFloor

    simReset()
    
    allMoves foreach (m => {
      m.stops foreach (stop => {
        val floors =
          if (currentFloor < stop) currentFloor until stop
          else currentFloor until stop by -1
        floors foreach {
          f => floorIndication(f)
            currentFloor = f
        }
        stopAt(stop)
        currentFloor = stop
      })
    })

    currentFloor = remember
  }

}
