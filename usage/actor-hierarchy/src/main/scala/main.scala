import akka.actor.{ ActorRef, Actor, ActorSystem, Props }


class ChildActor extends Actor {
  def receive = {
    case "printsomething" => {
      println(s">> in the child actor :: $self")
    }
  }
}

class TopLevelActor extends Actor {
  def receive = {
    case "createmore" => {
      println(s">> in the toplevel actor")
      val childActor = context.actorOf(Props[ChildActor], "childactor")
      childActor ! "printsomething"
    }
  }
}

object SimpleActorHierarchy extends App {
  val system = ActorSystem("actorhierachy")

  val toplevelActor = system.actorOf(Props[TopLevelActor], "toplevel-actor")
  println(s">> $toplevelActor")
  toplevelActor ! "createmore"
}
