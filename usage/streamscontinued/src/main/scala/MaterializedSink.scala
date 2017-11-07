package simplestreams

import akka.stream._
import akka.stream.scaladsl._

import akka.actor.ActorSystem
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object MaterializedSink extends App {
  implicit val system = ActorSystem("akka-streams")
  implicit val materializer = ActorMaterializer()

  val source = Source(1 to 20)
  val count = Flow[Int].map(_ => 1)
  val sink = Sink.fold[Int, Int](0)(_ + _)

  // val grph = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
  //   import GraphDSL.Implicits._

  //   source ~> Flow[Int].map(_ => 1) ~> sink
  //   ClosedShape
  // })
  val grph = source.via(count).toMat(sink)(Keep.right)

  grph
    .run()
    .foreach {
      c => println(c)
    }
}
