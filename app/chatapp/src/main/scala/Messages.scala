package chatapp

import akka.actor.ActorRef

object Messages {
  sealed trait ChatAppMessage // any kind of message

  sealed trait ChatEvent extends ChatAppMessage
  sealed trait ChatMessage extends ChatAppMessage

  case object AckMessage extends ChatEvent
  case object ActorInitMessage extends ChatEvent

  case class UserJoined(user: User, actor: ActorRef) extends ChatMessage
  case class UserLeft(user: User) extends ChatMessage
  case class UserTextMessage(user: User, content: String) extends ChatMessage
  case object InvalidMessage extends ChatMessage
}
