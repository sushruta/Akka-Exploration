package chatapp

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.stream.{FlowShape, OverflowStrategy}
import akka.stream.scaladsl._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import scala.io.StdIn

import akka.http.scaladsl.model.ws.{ Message, TextMessage }

/**
 * This Channel is a harness for testing graph
 * properties and should be used just for that.
 * Please avoid using this for actual chat
 * application by sending strings from the source LOL
 */

class NonHttpChannel(channelId: Int, actorSystem: ActorSystem) {
  import chatapp.Messages._

  val channelActor = actorSystem.actorOf(Props[ChannelActor], s"channel-actor-$channelId")

  def flowShape(user: User) = GraphDSL
    .create(Source.actorRef[ChatMessage](bufferSize = 5, OverflowStrategy.fail)) {
      implicit builder =>
        implicit chatSource =>

        import GraphDSL.Implicits._

        val messageFromOutside = builder.add(Flow[String].map {
          case msg: String => UserTextMessage(user, msg)
          case _ => InvalidMessage
        })

        val messageToOutside = builder.add(Flow[ChatMessage].map {
          case tm: UserTextMessage => s"${tm.user.username} :: ${tm.content}"
          case ul: UserLeft => s"${ul.user.username} just left the channel"
          case uj: UserJoined => s"${uj.user.username} just joined the channel"
          case _ => s"do not know what I just received"
        })

        // notice the use of Concat instead of Merge.
        // this is being done to enforce an ordering
        // among the two inlets
        val merge = builder.add(Concat[ChatMessage](2))

        val channelActorSink = Sink.actorRefWithAck(channelActor, ActorInitMessage, AckMessage, UserLeft(user))

        val actorAsSource = builder.materializedValue.map { actor => UserJoined(user, actor) }

        actorAsSource ~> merge.in(0)
        messageFromOutside ~> merge.in(1)
        merge ~> channelActorSink
        chatSource ~> messageToOutside

        FlowShape(messageFromOutside.in, messageToOutside.out)
    }

  def chatFlow(user: User) = Flow.fromGraph(flowShape(user))
}

object NonHttpChannel {
  def apply(channelId: Int)(implicit actorSystem: ActorSystem) = new NonHttpChannel(channelId, actorSystem)
}
