import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.scaladsl._
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

import akka.util.ByteString
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._

import scala.io.StdIn
import scala.util.Random

import scala.concurrent.Future

object WebServer {
  final case class Item(name: String, id: Long)
  final case class Order(items: List[Item])

  implicit val itemFormat = jsonFormat2(Item)
  implicit val orderFormat = jsonFormat1(Order)

  def fetchItem(id: Long): Option[Item] = Some(Item(s"item-$id", id))
  // def saveOrder(order: Order): Future[Done] = Future { }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("my-http-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val numbers = Source.fromIterator(() => Iterator.continually(Random.nextInt()).filter(_ > 0))

    val route: Route =
      get {
        pathPrefix("item" / LongNumber) { id =>
          val maybeItem = fetchItem(id)

          maybeItem match {
            case Some(item) => complete(item)
            case None => complete(StatusCodes.NotFound)
          }
        } ~
        pathPrefix("stream" / LongNumber) {id =>
          complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, numbers.map(n => ByteString(s"${n%id}\n"))))
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8888)

    println(s"server online at http://localhost:8888/\nPress RETURN to exit")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate)
  }
}
