package elevation

class ElevatorControlCenter(val elevators: Seq[ElevatorControl]) {

  /**
   * Current status of all elevators
   * @return A `Seq` of 3-tuples, each representing the status of one elevator:
   *         (elevator id, current floor, a list of all programmed stops)
   */
  def status(): Seq[(Int, Int, List[Int])] = {
    elevators.map(el => (el.id, el.getCurrentFloor, el.movements.map(m => m.stops).flatten))
  }

  /**
   * Resets the state of elevator with `id` passed as parameter.
   * **Note:** Remember to call `prepareMovements()` before running
   * or stepping the simulation.
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
      case _ => println(s"*** Elevator Id not found: $id")
    }
  }

  /**
   * Pickup call. Will be served by the closest elevator which is stopped
   * or is moving in the same direction as the pickup. If all elevators
   * are moving in the opposite direction, the one with least pending stops
   * will be chosen to assign the request.
   * @param initFloor Initial floor
   * @param destFloor Destination floor
   */
  def pickup(initFloor: Int, destFloor: Int): Unit = {
    //TODO Choose the elevator to serve the pickup and assign it
    elevators.head.pickup(initFloor, destFloor)
  }

  /**
   * This function steps through the simulations of each elevator.
   * A simulation should be prepared, calling `pickup` the desired
   * amount of times.
   * **Note:** For simulation to run, `prepareMovements()` should
   * be called first on those elevators whose behavior will be watched.
   */
  def step(): Unit = elevators foreach (el => el.simStep())

  /**
   * The same as `step()` but executing the whole simulation at once.
   */
  def run(): Unit = elevators foreach (el => el.simRun())

}
