package DiningPhilosophers

import akka.actor.{ActorRef, FSM}

sealed trait ChopstickMessage

// messages sent to the chopstick actor
case object PickUp extends ChopstickMessage
case object PutDown extends ChopstickMessage

// messages sent by the chopstick actor
final case class Taken(chopstick: ActorRef) extends ChopstickMessage
final case class Busy(chopstick: ActorRef) extends ChopstickMessage

sealed trait ChopstickState
case object ChopstickAvailable extends ChopstickState
case object ChopstickTaken extends ChopstickState

final case class ChopstickData(takenBy: Option[ActorRef])

class Chopstick(chopstickName: String) extends FSM[ChopstickState, ChopstickData] {
    startWith(ChopstickAvailable, ChopstickData(None))

    when(ChopstickAvailable) {
        case Event(PickUp, ChopstickData(None)) =>
            goto(ChopstickTaken) using ChopstickData(Some(sender())) replying Taken(self)

        case Event(e, s) =>
            log.warning(s"$chopstickName received unhandled request $e in state $stateName/$s")
            stay
    }

    when(ChopstickTaken) {
        case Event(PutDown, ChopstickData(Some(philosopher))) =>
            philosopher == sender() match {
                case true => goto(ChopstickAvailable) using ChopstickData(None)
                case false => stay replying Busy(self)
            }

        case Event(PickUp, ChopstickData(Some(_))) =>
            stay replying Busy(self)

        case Event(e, s) =>
            log.warning(s"$chopstickName received unhandled request $e in state $stateName/$s")
            stay
    }

    initialize()
}
