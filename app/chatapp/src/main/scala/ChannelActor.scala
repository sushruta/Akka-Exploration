package chatapp

import scala.io.AnsiColor._
import akka.actor.{Actor, ActorLogging, ActorRef}
import chatapp.Messages._

class ChannelActor extends Actor with ActorLogging {
  val participants = scala.collection.mutable.Map[User, ActorRef]()

  def receive = {
    case UserJoined(user: User, actor: ActorRef) => {
      participants += (user -> actor)
      log.info(BOLD + BLUE + "received a user joined message" + RESET)
      broadcast(SystemMessage(s"${user.username} joined the channel"))
      sender() ! AckMessage
    }

    case UserLeft(user: User) => {
      participants -= user
      log.info(BOLD + YELLOW + "received a UserLeft message" + RESET)
      broadcast(SystemMessage(s"${user.username} left the channel"))
      // no need for this I guess
      sender() ! AckMessage
    }

    case tm: UserTextMessage => {
      log.info(BOLD + GREEN + s"${tm.user.username} sent a message" + RESET)
      broadcast(tm)
      sender() ! AckMessage
    }

    case ActorInitMessage => {
      log.info(BOLD + WHITE + s"channel initialized and ready to take events" + RESET)
      sender() ! AckMessage
    }

    case InvalidMessage => {
      log.error(BOLD + RED + s"given an invalid message" + RESET)
      sender() ! AckMessage
    }

    case _ => {
      log.error(BOLD + RED + "unexpected message received. dropping it" + RESET)
      sender() ! AckMessage
    }
  }

  def broadcast(tm: UserTextMessage) = participants.values.foreach( _ ! tm)
}

object SystemMessage {
  def apply(msg: String): UserTextMessage = UserTextMessage(User("system"), msg)
}
