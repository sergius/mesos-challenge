package elevation

/**
 * Represents an elevator. An elevator only knows its
 * current floor, the time it should last from one
 * floor to another and the time to spend on stops.
 */
trait Elevator {
  this: Simulation =>

  /**
   * Time spent when moving, from one floor
   * to another (the speed).
   */
  val perFloorDuration = 1

  /**
   * Time spent when making a stop
   */
  val perStopDuration = 1

  var currentFloor = 0
}