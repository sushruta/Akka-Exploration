package simplestreams

import akka.stream._
import akka.stream.scaladsl._

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.{ NotUsed, Done }

import scala.concurrent._
import scala.concurrent.duration._

sealed trait ActorMessage
case object InitMessage extends ActorMessage
case object CompleteMessage extends ActorMessage
case object AckMessage extends ActorMessage
case class FruitMessage(name: String) extends ActorMessage

class SinkActor(ref: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case InitMessage => {
      log.info("actor can now take messages")
      sender() ! AckMessage
    }
    case CompleteMessage => {
      log.info("actor dismantled. no more messages")
    }
    case fm: FruitMessage => {
      log.info("received a fruity message")
      sender() ! AckMessage
      ref forward fm
    }
    case _ => {
      log.error("unexpected message received. dropping it")
      sender() ! AckMessage
    }
  }
}

class FruitActor extends Actor with ActorLogging {
  val fruits = Set("banana", "apricot", "apple", "mango", "orange")

  def receive = {
    case FruitMessage(name: String) => {
      fruits(name) match {
        case true => println(s"$name is a known fruit")
        case false => println(s"$name is unknown. Are you a hipster?")
      }
    }
    case _ => {
      log.error("a completely unexpected message was received. Dropped it.")
    }
  }
}

object ActorAsSink extends App {
  implicit val system = ActorSystem("akka-streams")
  implicit val materializer = ActorMaterializer()

  val fruits = List[String]("maNGo", "peAr", "Walnut", "kiwi", "orAnGE", "kIWi")
  val fruitActor = system.actorOf(Props[FruitActor], "fruit-actor")
  val sinkActor = system.actorOf(Props(classOf[SinkActor], fruitActor), "sink-actor")

  val source: Source[String, NotUsed] = Source(fruits)
  val caseTransformer: Flow[String, FruitMessage, NotUsed] = Flow[String].map{ f => FruitMessage(f.toLowerCase) }
  val sink = Sink.actorRefWithAck(sinkActor, InitMessage, AckMessage, CompleteMessage)

  source.via(caseTransformer).to(sink).run()
}
