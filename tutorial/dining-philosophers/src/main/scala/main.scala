import akka.actor.{ ActorSystem, ActorRef, Actor, FSM, Props }
import scala.concurrent.duration._

// events that can be sent to a philosopher
case object Eat
case object Think

sealed trait State
case object Thinking extends State
case object Eating extends State

sealed trait Data
case object Uninitialized extends Data
final case class Initialized(numMeals: Int) extends Data

// very very simple case
class Philosopher extends FSM[State, Data] {
  startWith(Thinking, Uninitialized)

  when (Thinking) {
    case Event(Eat, Uninitialized) =>
      stay using Initialized(0)

    case Event(Eat, ds @ Initialized(nm)) =>
      goto(Eating) using ds.copy(numMeals = nm + 1)

    case Event(e, s) =>
      log.warning(s"received unhandled request $e in state $stateName/$s")
      stay
  }

  onTransition {
    case Thinking -> Eating =>
      stateData match {
        case Initialized(numMeals: Int) => log.info(s"eaten $numMeals so far. Going for one more")
        case _ => // nothing to do. log.error(s"invalid state: $stateName")
      }
    
    case Eating -> Thinking =>
      stateData match {
        case Initialized(numMeals: Int) => log.info(s"completed $numMeals so far. Thanks...")
        case _ => // nothing to do.
      }
  }

  when (Eating) {
    case Event(Think, ds @ Initialized(nm)) =>
      goto(Thinking) using ds

    case Event(e, s) =>
      log.warning(s"received unhandled request $e in state $stateName/$s")
      stay
  }

  initialize()
}

object DiningPhilosophers extends App {
  val system = ActorSystem("dining-philosophers")

  val philosopher = system.actorOf(Props(classOf[Philosopher]), "philosopher-1")

  philosopher ! Think
  philosopher ! Eat
  philosopher ! Eat
  philosopher ! Think
  philosopher ! Think
}
