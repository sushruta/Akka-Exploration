import akka.actor.{ActorSystem, Actor, ActorRef, Props, PoisonPill}
import scala.concurrent.duration._

case object Ping
case object Pong

class Pinger extends Actor {
  var countdown = 50

  def receive = {
    case Pong =>
      println(s"${self.path} received pong. Counting down to $countdown")

      countdown > 0 match {
        case true =>
          countdown -= 1
          sender() ! Ping
        case false =>
          sender() ! PoisonPill
          self ! PoisonPill
      }
  }
}

class Ponger(pinger: ActorRef) extends Actor {
  def receive = {
    case Ping =>
      println(s"${self.path} received a ping")
      pinger ! Pong
  }
}

object Main extends App {
  val system = ActorSystem("pingpong")

  val pinger = system.actorOf(Props[Pinger], "pinger")
  val ponger = system.actorOf(Props(classOf[Ponger], pinger), "ponger")

  import system.dispatcher
  system.scheduler.scheduleOnce(2 seconds) {
    ponger ! Ping
  }
}
