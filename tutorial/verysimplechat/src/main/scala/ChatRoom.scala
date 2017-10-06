package VerySimpleChat

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Merge, Sink, Source}

class ChatRoom(roomId: Int, actorSystem: ActorSystem) {
    val chatRoomActor = actorSystem.actorOf(Props(classOf[ChatRoomActor], roomId), s"ChatRoomActor-$roomId")

    def sendMessage(message: ChatMessage): Unit = chatRoomActor ! message

    def websocketFlow(user: String) = Flow[Message].collect {
        case tm: TextMessage => TextMessage(Source.single("Hello ") ++ tm.textStream)
    }
}

object ChatRoom {
    def apply(roomId: Int)(implicit actorSystem: ActorSystem) = new ChatRoom(roomId, actorSystem)
}