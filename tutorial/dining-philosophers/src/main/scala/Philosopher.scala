package DiningPhilosophers

import akka.actor.{ActorRef, FSM}

import scala.concurrent.duration._

sealed trait PhilosopherMessage
case object Think extends PhilosopherMessage

sealed trait PhilosopherState
case object Waiting extends PhilosopherState
case object Thinking extends PhilosopherState
case object Hungry extends PhilosopherState
case object WaitingForOtherChopstick extends PhilosopherState
case object ChopstickDenied extends PhilosopherState
case object Eating extends PhilosopherState

final case class PhilosopherData(left: Option[ActorRef], right: Option[ActorRef])

class Philosopher(name: String, leftChopstick: ActorRef, rightChopstick: ActorRef) extends FSM[PhilosopherState, PhilosopherData] {
    startWith(Waiting, PhilosopherData(None, None))

    when (Waiting) {
        case Event(Think, PhilosopherData(None, None)) =>
            goto(Thinking) forMax (100.milliseconds)
    }

    when (Thinking) {
        case Event(StateTimeout, _) =>
            leftChopstick ! PickUp
            rightChopstick ! PickUp
            goto(Hungry)

        case Event(e, s) =>
            log.warning(s"$name received unhandled request $e in state $stateName/$s")
            stay
    }

    when (Hungry) {
        case Event(Taken(`leftChopstick`), _) =>
            goto(WaitingForOtherChopstick) using PhilosopherData(Some(leftChopstick), None)

        case Event(Taken(`rightChopstick`), _) =>
            goto(WaitingForOtherChopstick) using PhilosopherData(None, Some(rightChopstick))

        case Event(Busy(_), _) =>
            goto(ChopstickDenied)

        case Event(e, s) =>
            log.warning(s"$name received unhandled request $e in state $stateName/$s")
            stay
    }

    when (WaitingForOtherChopstick) {
        case Event(Taken(`leftChopstick`), PhilosopherData(None, Some(rightChopstick))) =>
            println(s"$name has acquired ${leftChopstick.path.name} and ${rightChopstick.path.name} and begun to eat")
            goto(Eating) using PhilosopherData(Some(leftChopstick), Some(rightChopstick)) forMax (2.seconds)

        case Event(Taken(`rightChopstick`), PhilosopherData(Some(leftChopstick), None)) =>
            println(s"$name has acquired ${leftChopstick.path.name} and ${rightChopstick.path.name} and begun to eat")
            goto(Eating) using PhilosopherData(Some(leftChopstick), Some(rightChopstick)) forMax (2.seconds)

        case Event(Busy(_), PhilosopherData(leftO, rightO)) =>
            leftO.foreach(_ ! PutDown)
            rightO.foreach(_ ! PutDown)
            goto(Thinking) using PhilosopherData(None, None) forMax (50.milliseconds)

        case Event(e, s) =>
            log.warning(s"$name received unhandled request $e in state $stateName/$s")
            stay
    }

    when (ChopstickDenied) {
        case Event(Taken(chopstick), _) =>
            chopstick ! PutDown
            goto(Thinking) using PhilosopherData(None, None) forMax (50.milliseconds)
        case Event(Busy(_), _) =>
            goto(Thinking) using PhilosopherData(None, None) forMax (50.milliseconds)
    }

    when (Eating) {
        case Event(StateTimeout, PhilosopherData(Some(left), Some(right))) =>
            left ! PutDown
            right ! PutDown
            goto(Thinking) using PhilosopherData(None, None) forMax (4.seconds)
        case Event(e, s) =>
            log.warning(s"$name received unhandled request $e in state $stateName/$s")
            stay
    }

    initialize()
}
