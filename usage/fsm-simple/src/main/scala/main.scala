import akka.actor.{ Actor, ActorSystem, ActorRef, FSM, Props }
import scala.concurrent.duration._
import scala.collection._

final case class SetTarget(ref: ActorRef)
final case class Queue(obj: Any)
case object Flush

final case class Batch(obj: immutable.Seq[Any])

sealed trait State
case object Idle extends State
case object Active extends State

sealed trait Data
case object Uninitialized extends Data
final case class Todo(target: ActorRef, queue: immutable.Seq[Any]) extends Data

class Buncher extends FSM[State, Data] {
  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(SetTarget(ref), Uninitialized) =>
      stay using Todo(ref, Vector.empty)
  }

  onTransition {
    case Active -> Idle =>
      stateData match {
        case Todo(ref, queue) => ref ! Batch(queue)
        case _ => // nothing to do
      }
  }

  when(Active) {
    case Event(Flush, t: Todo) =>
      goto(Idle) using t.copy(queue = Vector.empty)
  }

  whenUnhandled {
    // common code for both states
    case Event(Queue(obj), t @ Todo(_, v)) =>
      goto(Active) using t.copy(queue = v :+ obj)

    case Event(e, s) =>
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay
  }

  initialize()
}

class BatchProcessor extends Actor {
  var numObjectsReceived = 0
  var numBatchesReceived = 0

  def receive = {
    case Batch(obj: immutable.Seq[Any]) => {
      numObjectsReceived += obj.length
      numBatchesReceived += 1

      println(s"received $obj")
      println(s"num of objects so far :: $numObjectsReceived and number of batches so far :: $numBatchesReceived")
    }
  }
}

object FsmSimple extends App {
  val system = ActorSystem("batchersystem")

  val buncher = system.actorOf(Props(classOf[Buncher]))
  val batchHandler = system.actorOf(Props(classOf[BatchProcessor]))

  buncher ! SetTarget(batchHandler)
  buncher ! Queue(42)
  buncher ! Queue(43)
  buncher ! Queue(44)
  buncher ! Flush
  buncher ! Queue(45)
  buncher ! Queue(46)
  buncher ! Flush
  buncher ! Queue(47)
  buncher ! Flush

}
