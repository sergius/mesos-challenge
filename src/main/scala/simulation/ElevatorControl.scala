package simulation

/**
 * Elevator control for one elevator
 */
abstract class ElevatorControl extends Elevator {

  def maxFloors: Int

  private var destFloor = 0
  private var waiting = List.empty[Movement]
  private var movement: Movement = _


  def status(): (Int, Int, Int) = (1, movement.getFloor, movement.destFloor)

  def update(id: Int, actFloor: Int, destFloor: Int) = status() //TODO For now the same as status


  def pickup(m: Movement, initFloor: Int, destFloor: Int) {
    move(m, m.destFloor, initFloor)
    move(m, initFloor, destFloor)
  }
}
