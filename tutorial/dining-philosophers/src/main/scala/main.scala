import akka.actor.{ ActorSystem, ActorRef, Actor, FSM, Props }
import scala.concurrent.duration._

/**
 *
 * Chopstick Stuff
 *
 */
sealed trait ChopstickMessage
case object PickUp extends ChopstickMessage
case object PutDown extends ChopstickMessage

sealed trait ChopstickState
case object ChopstickAvailable extends ChopstickState
case object ChopstickTaken extends ChopstickState

final case class ChopstickData(takenBy: Option[ActorRef])

class Chopstick(chopstickId: Int) extends FSM[ChopstickState, ChopstickData] {
  startWith(ChopstickAvailable, ChopstickData(None))

  when(ChopstickAvailable) {
    case Event(Pickup, cd @ ChopstickData(None)) =>
      goto(ChopstickTaken) using ChopstickData(Some(sender()))

    case Event(e, s) =>
      log.warning(s"Chopstick:$chopstickId received unhandled request $e in state $stateName/$s")
      stay
  }

  onTransition {
    case ChopstickAvailable -> ChopstickTaken =>
      stateData match {
        case ChopstickData(Some(Philosopher(philosopherName: String, _, _))) =>
          log.info(s"$philosopher picked up $chopstickId")
        case _ => // nothing to do
      }
        
    case ChopstickTaken -> ChopstickAvailable =>
      stateData match {
        case ChopstickData(None) =>
          log.info(s"$chopstickId put down")
        case _ => // nothing to do
      }
  }

  when(ChopstickTaken) {
    case Event(PutDown, cd @ ChopstickData(Some(philosopher))) =>
      goto(ChopstickAvailable) using ChopstickData(None)

    case Event(e, s) =>
      log.warning(s"Chopstick:$chopstickId received unhandled request $e in state $stateName/$s")
      stay
  }
}

/**
 *
 * Philosopher Stuff
 *
 */

sealed trait PhilosopherMessage
case object Eat extends PhilosopherMessage
case object Think extends PhilosopherMessage
case object PickLeft extends PhilosopherMessage
case object PickRight extends PhilosopherMessage

sealed trait PhilosopherState
case object Thinking extends PhilosopherState
case object Hungry extends PhilosopherState
case object LeftWaiting extends PhilosopherState
case object RightWaiting extends PhilosopherState
case object Eating extends PhilosopherState

final case class PhilospherData(left: Option[ActorRef], right: Option[ActorRef])

class Philosopher(name: String, leftId: ActorRef, rightId: ActorRef) extends FSM[PhilosopherState, PhilosopherData] {
  startWith(Thinking, PhilosopherData(None, None))

  when (Thinking, stateTimeout = 5 seconds) {
    case Event(Hungry | StateTimeout, pd @ PhilosopherData(None, None)) =>
      goto(RightWaiting) using ds.copy(left = Some(leftId))

    case Event(e, s) =>
      log.warning(s"$name received unhandled request $e in state $stateName/$s")
      stay
  }

  onTransition {
    case Thinking -> Eating =>
      stateData match {
        case Initialized(numMeals: Int) => log.info(s"$name has eaten $numMeals so far. Going for a meal")
        case _ => // nothing to do. log.error(s"invalid state: $stateName")
      }
    
    case Eating -> Thinking =>
      stateData match {
        case Initialized(numMeals: Int) => log.info(s"$name completed $numMeals so far. nom nom...")
        case _ => // nothing to do.
      }
  }

  when (Eating, stateTimeout = 10 seconds) {
    case Event(Think | StateTimeout, ps @ PhilosopherData(Some(l), Some(r))) =>
      goto(Thinking) using ds

    case Event(e, s) =>
      log.warning(s"$name received unhandled request $e in state $stateName/$s")
      stay
  }

  initialize()
}

object DiningPhilosophers extends App {
  val system = ActorSystem("dining-philosophers")

  val philosopherNames = List[String]("Chanakya", "Lao-Tze", "Socrates", "Nachiketa", "Sun-Tzu")
  val size = philosopherNames.length

  // val philosopher = system.actorOf(Props(classOf[Philosopher]), "philosopher-1")
  val philosophers = for {
    (name, i) <- List[String]("Chanakya", "Lao-Tze", "Socrates", "Nachiketa", "Sun-Tzu").zipWithIndex
  } yield system.actorOf(Props(classOf[Philosopher], name, i % size, (i + 1) % size), name)
}
