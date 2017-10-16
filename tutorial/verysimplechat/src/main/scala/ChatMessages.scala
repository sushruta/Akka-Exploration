package VerySimpleChat

import akka.actor.ActorRef

case class ChatMessage(sender: String, message: String)

object SystemMessage {
  def apply(message: String) = ChatMessage("system", message)
}

sealed trait ChatEvent

case class UserJoined(name: String, userActor: ActorRef) extends ChatEvent
case class UserLeft(name: String) extends ChatEvent
case class IncomingMessage(sender: String, message: String) extends ChatEvent
