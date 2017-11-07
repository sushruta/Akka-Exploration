package VerySimpleChat

import akka.actor.ActorRef

case class Image(url: String)
case class File(url: String)
case class Link(url: String)

sealed trait ChatMessage

case class TextMessage(sender: String, message: String) extends ChatMessage
case class ImageMessage(sender: String, image: Image) extends ChatMessage
case class FileMessage(sender: String, file: File) extends ChatMessage
case class SystemMessage(message: String) extends ChatMessage

object SystemMessage {
  def apply(message: String) = ChatMessage("system", message)
}

sealed trait ChatEvent

case class UserJoined(name: String, userActor: ActorRef) extends ChatEvent
case class UserLeft(name: String) extends ChatEvent
case class IncomingMessage(sender: String, message: String) extends ChatEvent
