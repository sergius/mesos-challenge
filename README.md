# Mesos-challenge

## Second approach: development from scratch
(centered in the algorithm, not simulation)

*(package `elevation`)*


### Elevator control

The purpose of an elevator control actually consists in producing a sequence of stops.
The elevator only has 2 states: *stopped* and *in movement*. The elevator has no notion of any calls or business logic. It only understands when the stops are to be made and that it has to move from stop to stop, till the last indicated one. Therefore, our task is actually to produce the correct sequence of stops.

### Scheduling

The most interesting part of the task is scheduling. The following algorithm has been chosen.

A pickup request has two parameters: initial floor (`initFloor`) and destination floor (`destFloor`).

*Note: Though we are mostly used to elevators which have the buttons panel inside, hence the elevator control never knows which will be the destination floor before the elevator arrives to the initial floor, we will assume that the situation is different: the buttons panel is where we are used to have (just one) "call" button. Thus, when calling the elevator, the person is actually supposed to press the destination floor button (you are on floor 10 going to floor 3: you press 3, the elevator knows it comes from 10). Working on this problem reminded me that I have already actually seen an elevator like this in one governmental building in Madrid. Everything has a cost and with technological progress the priorities change. Perhaps this change is due to the fact that years ago the energy was cheap and the metal was expensive, so we had one button per elevator per plant. Now the energy and waiting time is the most expensive and the metal is cheap and optimized, so we can have multi-button panels on every floor and save on energy and time.*

#### One elevator

Every single elevator serves the requests, according to the following rules:

 1. The first to come is served first. FCFS
 2. As the elevator starts moving (either up or down) it will keep serving all the requests made in the same direction of movement. Obviously, with the condition that the requests must be received before the elevator has passed the `initFloor`.
 3. The requests in the opposite direction aren't lost. They are being stored for next movement.
 4. When all the requests in one direction are served or the limit of movement is reached, the elevator starts moving in the opposite direction, serving all the requests stored before and those which are being made in time during the movement.
 5. The same routine keeps repeating while pickup requests are received.

#### Multiple elevators

When a pickup request arrives to a centre which controls several elevators, the pickup is assigned to the elevator with the shortest path to `initFloor`. It's being computed the following way:

 1. Is being checked the condition that `initFloor` is situated between elevator's current floor and its next stop, taking the direction of movement into consideration. It means that the case when the described condition is `true` but the elevator moves in the direction opposite to the pickup, it counts as `false`
 2. If the condition in p. 1 is `true`, the number of floors (difference between elevator's current floor and the `initFloor`) is returned.
 3. If the condition is `false`, the same is checked for the next step: checking if `initFloor` is situated between the next stop and the further one.
 4. While the condition is `false` the number of floors is being accumulated.
 5. The same routine is repeated till the condition is `true` or the elevator has no more stops planned.
 6. Exceptions to the main rule:
    6.1. If there is a stopped elevator on exactly the same floor as `initFloor`, this elevator is assigned
    6.2. If the `initFloor` is situated out of the range of the movements of all elevators, the one whose stops come closest to `initFloor` will be assigned


 For example, we have an elevator with already `N` planned stops when it receives a pickup request `pickup(x, y)` (`x` is `initFloor`, `y` is `destFloor`)

 1. Is being checked if the `currentFloor < x < nextStop1` (for a movement up, or ascending)
 2. Suppose it's not. Then we record the number `diff = (nextStop1 - currentFloor).abs` *(note that for descending direction the `diff` could result negative, therefore we count the absolute value of the difference)*
 3. We check further: `nextStop1 < x < nextStop2`. `False` again? Then `diff += (nextStop2 - nextStop1).abs`
 4. Say the next time we check and discover that `nextStop2 < x < nextStop3` is `true`. Then we sum `diff += (x - nextStop2).abs` and `diff` will be the path which the elevator has to run before it could serve this pickup request.

 The elevator with the shortest path will be assigned the request.

 An example for the exception rule 6.2: if we have `M` elevators and all of them have planned stops that don't exceed floor `x`, if a request with `initFloor = x +  3` comes in then the elevator whose stops reach floor `x` will be assigned this request. In case there are several elevators fulfilling this condition, the first one found will be returned. (This last detail might be optimised further.)

 *Note: In real dynamic conditions, this implementation might have a race condition problem. I would like to mention that an implementation for real world would execute these computations in parallel or, even better, the parallelism could be provided by an implementation based on an actors system, e.g. Akka.*



### Simulation (DES)

The proposed simulation is going to be a [Discrete event simulation](http://en.wikipedia.org/wiki/Discrete_event_simulation), which means that it has to *"... model the operation of a system as a discrete sequence of events in time. Each event occurs at a particular instant in time and marks a change of state in the system. Between consecutive events, no change in the system is assumed to occur; thus the simulation can directly jump in time from one event to the next."*

Thus, a simulation is going to be fully programmed before being run. Events may be programmed to occur with certain delay or at a particular moment in time, but no dynamic behaviour is being considered.

For convenience and as a way to watch the simulation's execution, traces (`println()`) are added to actions. This can be easily re-factored to other kinds of reporting.

#### Programming a simulation

*Note: the time is measured in conventional time units, which are just integer values and represent instants in the timeline of the simulation. The values of `duration` and `delay` are of this type. More than one event can be programmed for the same instant. `Delays` are available, but aren't used in the proposed application.*

To program a simulation, actions should be added to simulation's agenda calling function `Simulation.addToAgenda(action, delay)`, `delay` is optional with default value 0. `Action`s have duration and when added to agenda, by default are assigned to different consecutive instants, taking as many instants as the duration time units. An `Action` with delay is programmed from the instance when the call to `addToAgenda()` is made, traversing forward in agenda as many instants as `delay` time units, followed by the instants corresponding to `Action`'s `duration`. If moving forward no instants are found yet, `Empty` ones will be created.

__Examples from tests__

```    //

    "should add 5 items to agenda for 2 events: 1st with (delay: 3) & (duration: 2), " +
      "2nd with (delay: 0) & (duration: 2)" in {
      var executed = false
      val duration = 2
      val delay = duration + 1

      object sim extends Simulation
      import sim._

      val action1 = Action("SUT Action 4 - 1",
      {executed = true; println(s"executing: duration $duration, delay 0")}, duration)

      val action2 = Action("SUT Action 4 - 2",
      {executed = true; println(s"executing: duration $duration, delay $delay")}, duration)

      addToAgenda(action2, delay)
      addToAgenda(action1)

      agenda should have size 5

      val eventsInEmptyInstant: List[sim.Event] = agenda(duration + (delay - duration) - 1).events
      eventsInEmptyInstant should have size 1
      eventsInEmptyInstant should contain (Empty)

      sim.run()

      executed shouldBe true
    }

    //Will produce the following trace:
    *** Time: 0 | Simulation | starting...
    *** Time: 1 | SUT Action 4 - 1 | executing: duration 2, delay 0
    *** Time: 2 | SUT Action 4 - 1 | executing: duration 2, delay 0
    *** Time: 3 | Empty | tic-tac
    *** Time: 4 | SUT Action 4 - 2 | executing: duration 2, delay 3
    *** Time: 5 | SUT Action 4 - 2 | executing: duration 2, delay 3


    "should add 7 items to agenda for 2 events: 1st with (delay: 0) & (duration: 2), " +
      "2nd with (delay: 3) & (duration: 2)" in {
      var executed = false
      val duration = 2
      val delay = duration + 1

      object sim extends Simulation
      import sim._

      val action1 = Action("SUT Action 5 - 1",
      {executed = true; println(s"executing: duration $duration, delay 0")}, duration)

      val action2 = Action("SUT Action 5 - 2",
      {executed = true; println(s"executing: duration $duration, delay $delay")}, duration)

      addToAgenda(action1)
      addToAgenda(action2, delay)

      agenda should have size 7

      duration until duration + delay foreach {t =>
        agenda(t).events should have size 1
        agenda(t).events should contain (Empty)
      }

      sim.run()

      executed shouldBe true
    }

    //Will produce the following trace:
    *** Time: 0 | Simulation | starting...
    *** Time: 1 | SUT Action 5 - 1 | executing: duration 2, delay 0
    *** Time: 2 | SUT Action 5 - 1 | executing: duration 2, delay 0
    *** Time: 3 | Empty | tic-tac
    *** Time: 4 | Empty | tic-tac
    *** Time: 5 | Empty | tic-tac
    *** Time: 6 | SUT Action 5 - 2 | executing: duration 2, delay 3
    *** Time: 7 | SUT Action 5 - 2 | executing: duration 2, delay 3
```



---

## First intent
*(package `~~simulation~~`)*

The most difficult part of the challenge wasn't the algorithm itself but the task to combine it with a simulation implementation.

## The algorithm

The algorithm is not complex and is pretty known: when the movement starts, it keeps all the way in the same direction, serving requests that fit direction and timing. When there are no more requests or the movement limit is reached, direction changes and all the requests in the opposite direction are being served.

In other words, with reference to our elevator, the following scenario might serve as example:

  a. The elevator is on 4th floor

  b. A pickup request is received, from floors 2 to 7

  c. The elevator starts moving down to floor 2

  d. If on the way down more pickup requests are received, those that can be served on the way down are being served. Others are stored for later passes, e.g. a request from floors 3 to 1 will be served (if it is received before reaching floor 3) but the pickup from 3 to 10 is kept for next pass up.

  e. When reaching floor 2 (or 1, if any request was made on the way to this floor), the elevator starts moving up to floor 7

  f. As in the case of moving down, if any pickup request is received on the way up (and arrives before the elevator passes by the pickup floor) it is being served.

  g. The elevator finishes serving all the requests of going up (which it could serve from it current position in the moment of receiving the request) and/or reaches the highest floor in the building.

  h. If there are requests pending, the elevator starts moving down, serving all the requests in this direction.


## The intent

My first thought was to implement something simple for one elevator, kind of FIFO one, to just have it working. Then, if time left, I would optimize the first version according to the algorithm described above. After that I would take the implementation to multiple elevators.


## First approach

As I have never happened to implement any simulation, therefore I went for a guide to the the one that I remembered, suggested by M. Odersky in the [Reactive Programming course](https://class.coursera.org/reactive-001/lecture/41). Probably that was my biggest mistake. It is a good example of a simulation, but the task is pretty different. The main difference is the nature of the problem. In the course the simulation is treating a problem when a change in one element of a system is affecting all the rest of the elements simultaneously, though with specific delays (electronic circuit). In elevator scheduling problem the effect is sequential in time and "physical" world: step by step and floor by floor.

This is the detail which I didn't quite realise from the start and lost a good part of time trying to adjust the example to what's necessary in the problem. Again, that's just lack of experience to implement simulations.

## The code

I tried to keep the code as good as possible. Nevertheless, in the rush it's not a priority, thus the code is definitely improvable in many senses.

**How to test whats's done**

Had no time for proper tests, though tried to do something. Nevertheless, in the test directory there is a file `ElevatorSpec` where I started "testing": actually running the simulation and watching its traces.
