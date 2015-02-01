package elevation

trait Elevator {
  this: Simulation =>

  val perFloorDuration = 1
  val perStopDuration = 2

  var currentFloor = 0
}