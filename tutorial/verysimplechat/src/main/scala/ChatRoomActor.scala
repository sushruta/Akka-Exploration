package VerySimpleChat

import akka.actor.{ ActorRef, Actor, FSM }
import scala.concurrent.duration._


class ChatRoomActor(roomId: Int) extends Actor {
    var participants: Map[String, ActorRef] = Map.empty[String, ActorRef]

    override def receive: Receive = {
        case UserJoined(name, actorRef) =>
            participants += name -> actorRef
            broadcast(SystemMessage(s"User $name joined the channel"))
            println(s"User $name joined channel[$roomId]")

        case UserLeft(name) =>
            println(s"User $name left channel[$roomId]")
            broadcast(SystemMessage(s"User $name left channel[$roomId]"))
            participants -= name

        case IncomingMessage(sender: String, msg: String) => broadcast(ChatMessage(sender, msg))
    }

    def broadcast(message: ChatMessage): Unit = participants.values.foreach(_ ! message)
}
