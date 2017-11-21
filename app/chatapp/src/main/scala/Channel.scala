package chatapp

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.stream.{FlowShape, OverflowStrategy}
import akka.stream.scaladsl._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import scala.io.StdIn

import akka.http.scaladsl.model.ws.{ Message, TextMessage }

class Channel(channelId: Int, actorSystem: ActorSystem) {
  import chatapp.Messages._

  val channelActor = actorSystem.actorOf(Props[ChannelActor], s"channel-actor-$channelId")

  def flowShape(user: User) = GraphDSL
    .create(Source.actorRef[ChatMessage](bufferSize = 5, OverflowStrategy.fail)) {
      implicit builder =>
        implicit chatSource =>

        import GraphDSL.Implicits._

        val messageFromOutside = builder.add(Flow[Message].map {
          case TextMessage.Strict(msg) => UserTextMessage(user, msg)
          case _ => InvalidMessage
        })

        val messageToOutside = builder.add(Flow[ChatMessage].map {
          case tm: UserTextMessage => TextMessage(s"${tm.user.username} :: ${tm.content}")
          case ul: UserLeft => TextMessage(s"${ul.user.username} just left the channel")
          case uj: UserJoined => TextMessage(s"${uj.user.username} just joined the channel")
          case _ => TextMessage(s"do not know what I just received")
        })

        val merge = builder.add(Concat[ChatMessage](2))

        val channelActorSink = Sink.actorRefWithAck(channelActor, ActorInitMessage, AckMessage, UserLeft(user))

        val actorAsSource = builder.materializedValue.map { actor => UserJoined(user, actor) }

        // merge is a concat shape. messages from (0)
        // will get a higher preference than messages from (1)
        actorAsSource ~> merge.in(0)
        messageFromOutside ~> merge.in(1)
        
        merge ~> channelActorSink
        chatSource ~> messageToOutside

        FlowShape(messageFromOutside.in, messageToOutside.out)
    }

  def chatFlow(user: User): Flow[Message, Message, Any] = Flow.fromGraph(flowShape(user))
}

object Channel {
  def apply(channelId: Int)(implicit actorSystem: ActorSystem) = new Channel(channelId, actorSystem)
}
