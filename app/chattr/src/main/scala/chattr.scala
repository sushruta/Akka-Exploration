import akka.actor.{ ActorSystem, ActorRef, Actor, FSM, Props}
import scala.concurrent.duration._

sealed trait State

// ChatManager States
case object ChatOffline extends State
case object ChatOnline extends State

// ChatManager Events
