package VerySimpleChat

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl._
import akka.stream._

class ChatRoom(roomId: Int, actorSystem: ActorSystem) {
    val chatRoomActor = actorSystem.actorOf(Props(classOf[ChatRoomActor], roomId), s"ChatRoomActor-$roomId")

    def sendMessage(message: ChatMessage): Unit = chatRoomActor ! message

    def websocketFlow(user: String): Flow[Message, Message, Any] =
      Flow.fromGraph(GraphDSL.create(Source.actorRef[ChatMessage](bufferSize = 5, OverflowStrategy.fail)) {
        implicit builder =>
          chatSource =>
            import GraphDSL.Implicits._

            val fromWebsocket = builder.add(Flow[Message].collect {
              case TextMessage.Strict(txt) => IncomingMessage(user, txt)
            })

            val backToWebsocket = builder.add(Flow[ChatMessage].map {
              case ChatMessage(author, message) => TextMessage(s"[$author]: $message")
            })

            val chatActorSink = Sink.actorRef[ChatEvent](chatRoomActor, UserLeft(user))

            val merge = builder.add(Merge[ChatEvent](2))

            val actorAsSource = builder.materializedValue.map { actor => UserJoined(user, actor) }

            fromWebsocket ~> merge.in(0)

            actorAsSource ~> merge.in(1)

            merge ~> chatActorSink

            chatSource ~> backToWebsocket

            FlowShape(fromWebsocket.in, backToWebsocket.out)
    })
}

object ChatRoom {
    def apply(roomId: Int)(implicit actorSystem: ActorSystem) = new ChatRoom(roomId, actorSystem)
}
