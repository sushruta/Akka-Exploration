package VerySimpleChat

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

object VerySimpleChat {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("very-simple-chat")
    implicit val materializer = ActorMaterializer()

    val route = pathPrefix("ws-chat" / IntNumber) {
      chatId => {
        parameter('name) {
          userName => handleWebSocketMessages(ChatRooms.findOrCreate(chatId).websocketFlow(userName))
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
