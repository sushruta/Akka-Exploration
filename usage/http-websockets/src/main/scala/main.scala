import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Source, Flow }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{ TextMessage, Message }
import akka.http.scaladsl.server.Directives
import scala.io.StdIn

object AkkaWebsocketsExample {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("akka-http-websockets")
    implicit val materializer = ActorMaterializer()

    import Directives._

    val greeterWebSocketService =
      Flow[Message]
        .collect {
          case tm: TextMessage => TextMessage(Source.single("Hello ") ++ tm.textStream)
        }

    val route = path("greeter") {
      get {
        handleWebSocketMessages(greeterWebSocketService)
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
