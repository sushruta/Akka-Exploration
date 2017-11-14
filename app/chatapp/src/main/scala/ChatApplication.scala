package chatapp

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import chatapp.Messages._

import akka.actor.{ ActorRef, Actor, ActorSystem, Props }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Source, Flow }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{ TextMessage, Message }
import akka.http.scaladsl.server.Directives._
import scala.io.StdIn

import akka.stream.OverflowStrategy
import akka.stream._
import akka.stream.scaladsl._

object ChatApplication {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("chat-application")
    implicit val materializer = ActorMaterializer()

    val route = pathPrefix("ws-chat" / IntNumber) {
      channelId => {
        parameter('name) {
          username => handleWebSocketMessages(Channels.findOrCreate(channelId).chatFlow(User(username)))
        }
      }
    }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8888)

    println(s"server online at http://localhost:8888\nPress RETURN to stop the server")
    StdIn.readLine()

    import system.dispatcher
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
