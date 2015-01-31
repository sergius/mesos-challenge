# Mesos-challenge

## Second attack
*(package `elevation`)*

## Development from scratch, centered in the algorithm (not simulation)

### Elevator control

The the purpose of an elevator control actually consists in producing a sequence of stops.
The elevator only has 2 states: *stopped* and *in movement*. The elevator has no notion of any calls or business logic. It only understands when the stops are to be made and that it has to move from stop to stop, till the last indicated one. Therefore, our task is actually to produce the correct sequence of stops.

### The simulation

The proposed simulation is going to be a [Discrete event simulation](http://en.wikipedia.org/wiki/Discrete_event_simulation) (as opposed to [Continuous simulation](http://en.wikipedia.org/wiki/Continuous_simulation)), which means that it has to *"... model the operation of a system as a discrete sequence of events in time. Each event occurs at a particular instant in time and marks a change of state in the system. Between consecutive events, no change in the system is assumed to occur; thus the simulation can directly jump in time from one event to the next."*

Thus, a simulation is going to be fully programmed before being run. Events may be programmed to occur with certain delay or at a particular moment in time, but no dynamic behaviour is being considered.


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
