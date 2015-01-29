package simulation

abstract class ElevatorControl extends Elevator with Parameters {

  def maxFloors: Int

  private var destFloor = 0
  private var direction: Direction = _

  private val directionUp: Direction = _
  private val directionDown: Direction = _


  def status(): (Int, Int, Int) =
  //TODO For now we assume only one elevator
    (1, direction.getFloor, direction.destFloor)

  def update(id: Int, actFloor: Int, destFloor: Int) = status() //TODO For now the same as status

  def pickup(initFloor: Int, destFloor: Int): Unit = {

  }
}
