package chatapp

import akka.actor.{Actor, ActorLogging, ActorRef}
import chatapp.Messages._

class ChannelActor extends Actor with ActorLogging {
  val participants = scala.collection.mutable.Map[User, ActorRef]()

  def receive = {
    case UserJoined(user: User, actor: ActorRef) => {
      participants += (user -> actor)
      log.info("received a user joined message")
      broadcast(SystemMessage(s"${user.username} joined the channel"))
      sender() ! AckMessage
    }

    case UserLeft(user: User) => {
      participants -= user
      log.info("received a UserLeft message")
      broadcast(SystemMessage(s"${user.username} left the channel"))
      // no need for this I guess
      sender() ! AckMessage
    }

    case tm: UserTextMessage => {
      log.info(s"${tm.user.username} sent a message")
      broadcast(tm)
      sender() ! AckMessage
    }

    case ActorInitMessage => {
      log.info(s"channel initialized and ready to take events")
      sender() ! AckMessage
    }

    case InvalidMessage => {
      log.error(s"given an invalid message")
      sender() ! AckMessage
    }

    case _ => {
      log.error("unexpected message received. dropping it")
      sender() ! AckMessage
    }
  }

  def broadcast(tm: UserTextMessage) = participants.values.foreach( _ ! tm)
}

object SystemMessage {
  def apply(msg: String): UserTextMessage = UserTextMessage(User("system"), msg)
}
