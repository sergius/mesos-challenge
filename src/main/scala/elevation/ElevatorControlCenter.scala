package elevation

import scala.annotation.tailrec

class ElevatorControlCenter(val elevators: List[ElevatorControl]) {

  /**
   * Current status of all elevators
   * @return A `Seq` of 3-tuples, each representing the status of one elevator:
   *         (elevator id, current floor, a list of all programmed stops)
   */
  def status(): List[(Int, Int, List[Int])] = {
    elevators.map(el => (el.id, el.getCurrentFloor, el.movements.map(m => m.stops).flatten))
  }

  /**
   * Resets the state of elevator with `id` passed as parameter,
   * and creates a new simulation with the passed parameters.
   * All the previous simulation parameters will be lost.
   * @param id Elevator's id
   * @param currFloor Current floor
   * @param pickups A list of tuples (initial and destination floors)
   *                that represent pickup calls.
   */
  def update(id: Int, currFloor: Int, pickups: List[(Int, Int)]): Unit = {
    elevators.find(el => el.id == id) match {
      case Some(e) =>
        e.reset()
        e.setCurrentFloor(currFloor)
        pickups foreach(pck => e.pickup(pck._1, pck._2))
        e.updateAgenda()
      case _ => println(s"*** Elevator Id not found: $id")
    }
  }

  /**
   * Pickup call. If there is only one elevator available, the
   * request is automatically assigned to it. Otherwise, will
   * be selected the elevator with the shortest path
   * (less floors to traverse, in order to have the
   * `initFloor` covered).
   *
   * @param initFloor Initial floor
   * @param destFloor Destination floor
   */
  def pickup(initFloor: Int, destFloor: Int): Unit = {
    if (elevators.size == 1) {
      val el: ElevatorControl = elevators.head
      el.pickup(initFloor, destFloor)
      el.updateAgenda()
    } else {
      val el: ElevatorControl =
        shortestWait(elevators.map(el =>
          findPath(el.movements.
            foldLeft(List.empty[List[Int]])((acc, m) => acc :+ m.stops).flatten, (el, 0, false, el.getCurrentFloor))))
      el.pickup(initFloor, destFloor)
      el.updateAgenda()
    }

    def shortestWait(results: List[(ElevatorControl, Int, Boolean, Int)]): ElevatorControl =
      results.filter { case (_, _, b, _) => b}.minBy { case (el, floors, _, _) => floors}._1


    @tailrec
    def findPath(stops: List[Int], result: (ElevatorControl, Int, Boolean, Int)): (ElevatorControl, Int, Boolean, Int) =
      result match {
        case (_, _, b, _) if b => result
        case _ if stops.isEmpty => result
        case (el, f, ok, _) => {
          val (floors: Int, found: Boolean, curr: Int) = checkHead(stops, result._4)
          findPath(stops.tail, (el, floors + f, found || ok, curr))
        }
      }

    def checkHead(stops: List[Int], curr: Int) = {
      val stop = stops.head
      if (initFloor < destFloor && (curr until stop contains initFloor))
        (initFloor - curr, true, stop)
      else if (initFloor > destFloor && (stop until curr contains initFloor))
      (curr - initFloor, true, stop)
      else ((curr - stop).abs, false, stop)
    }

  }

  /**
   * This function steps through the simulations of each elevator at once,
   * i. e. the same instant of simulation timeline for all the elevators.
   * Before calling this method, the simulation (for each elevator) should
   * be prepared, calling `update()` with all the pickups as parameter.
   */
  def step(): Unit = elevators foreach (el => el.stepSimulation())

  /**
   * The same as `step()` but executing the whole simulation at once.
   */
  def run(): Unit = elevators foreach ((el: ElevatorControl) => el.runSimulation())

}
