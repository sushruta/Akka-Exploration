package chatapp

import chatapp.Messages._

import akka.actor.{ ActorRef, Actor, ActorSystem, Props }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Source, Flow }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{ TextMessage, Message }
import akka.http.scaladsl.server.Directives._
import scala.io.StdIn

import com.typesafe.config.ConfigFactory

import akka.stream.OverflowStrategy
import akka.stream._
import akka.stream.scaladsl._

object ChatApplication {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("chat-application")
    implicit val materializer = ActorMaterializer()

    val config = ConfigFactory.load().resolve()
    val username = config.getString("chatapp.username")

    val msgs = List[String]("hi", "hello", "new to this channel", "how are all?")
    val source = Source[String](msgs)
    val sink = Sink.foreach[String](println)

    val myChatFlowShape = NonHttpChannels.findOrCreate(420).chatFlow(User(username))
    source.via(myChatFlowShape).runWith(sink)
  }
}
