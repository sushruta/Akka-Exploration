package slightly.complicated.example.sourcesinkactor

import akka.stream._
import akka.stream.scaladsl._

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.{ NotUsed, Done }

import scala.concurrent._
import scala.concurrent.duration._

sealed trait ChatMessage
case class UserJoined(name: String, actor: ActorRef) extends ChatMessage
case class TextMessage(name: String, content: String) extends ChatMessage
case class UserLeft(name: String) extends ChatMessage
case class SystemMessage(content: String) extends ChatMessage
case object InvalidMessage extends ChatMessage
case object AckMessage extends ChatMessage
case object ActorInitMessage extends ChatMessage

class ChatRoomActor extends Actor with ActorLogging {
  val participants = scala.collection.mutable.Map[String, ActorRef]()

  def receive = {
    case UserJoined(name: String, actor: ActorRef) => {
      log.info(s"$name just joined the room")
      participants += (name -> actor)
      broadcast(s"_system_:: $name just joined the room")
      sender() ! AckMessage
    }
    case UserLeft(name: String) => {
      log.info(s"received a UserLeft message")
      participants -= name
      broadcast(s"_system_:: $name left the room")
      sender() ! AckMessage
    }
    case TextMessage(name: String, content: String) => {
      log.info(s"received a TextMessage")
      broadcast(s"$name:: $content")
      sender() ! AckMessage
    }
    case InvalidMessage => {
      log.error(s"received an invalid message")
      sender() ! AckMessage
    }
    case ActorInitMessage => {
      log.info(s"actor started")
      sender() ! AckMessage
    }
    case SystemMessage(content: String) => {
      log.info(s"some system message")
      broadcast(s"__system__:: $content")
      sender() ! AckMessage
    }
    case _ => {
      log.error(s"received a completely unexpected message")
      sender() ! AckMessage
    }
  }

  def broadcast(msg: String) = participants.values.foreach(_ ! msg)
}

object ActorAsSink extends App {
  implicit val system = ActorSystem("akka-streams")
  implicit val materializer = ActorMaterializer()
  
  val chatRoomActor = system.actorOf(Props[ChatRoomActor], "chat-room-actor")

  def flowShape(userName: String) = GraphDSL
    .create(Source.actorRef[ChatMessage](bufferSize = 5, OverflowStrategy.fail)) {
      implicit bldr =>
        chatSource =>
          import GraphDSL.Implicits._

          val messageFromOutside = bldr.add(Flow[String].map {
            case msg: String => TextMessage(userName, msg)
            case _ => SystemMessage("something invalid was received")
          })

          val chatActorSink = Sink.actorRef(chatRoomActor, UserLeft(userName)) // WithAck(chatRoomActor, ActorInitMessage, AckMessage, UserLeft(userName))

          val actorAsSource = bldr.materializedValue.map { actor => UserJoined(userName, actor) }

          val merge = bldr.add(Merge[ChatMessage](2))

          messageFromOutside ~> merge.in(0)
          actorAsSource ~> merge.in(1)
          merge.out ~> chatActorSink

          FlowShape(messageFromOutside.in, chatSource.out)
    }

  val chatFlow = Flow.fromGraph(flowShape("sashi"))
  
  val fruits = List[String]("maNGo", "peAr", "Walnut", "kiwi", "orAnGE")

  val source: Source[String, NotUsed] = Source(fruits)
  val sink = Sink.foreach[ChatMessage](println)

  source.via(chatFlow).runWith(sink)
}
